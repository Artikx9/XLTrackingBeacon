package com.truetech.xltrackingbeacon.tasks;

import android.app.Notification;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;
import com.truetech.xltrackingbeacon.R;
import com.truetech.xltrackingbeacon.Utils.Util;
import com.truetech.xltrackingbeacon.data.DBHelper;
import com.truetech.xltrackingbeacon.service.TrackerService;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.truetech.xltrackingbeacon.Utils.Constant.*;
import static com.truetech.xltrackingbeacon.Utils.Util.*;
import static com.truetech.xltrackingbeacon.service.TrackerService.timerServer;
import static java.lang.Math.min;


public class TimerTaskToServer extends TimerTask {
    private TrackerService service;
    private DBHelper dbHelper;
    private List<Integer> list;
    private static int response=MINUS_ONE;
    private static byte request=MINUS_ONE;
    private static long timeCreateNotif=DEF_VALUE_NULL;
    public static boolean existNotification=false;


    public TimerTaskToServer(TrackerService service) {
        this.service = service;
        this.dbHelper = DBHelper.getInstance();
    }

    @Override
    public void run() {
        if (isOnline(service)) {
            if (request!=HEX_NULL && response!= DEF_VALUE_NULL) deleteNotification();
            long count=getCountTable(dbHelper,NAME_TABLE_LOC);
            if (count>0 && sendToServer()>0) {
                deleteRowsInBd(list);
            }
            if (count> MAX_LENGTH_RECORDS ){
                if (!service.isRestartFlag()){
                    restartOrNormalStart(true);
                }
            }else if(service.isRestartFlag()){
                restartOrNormalStart(false);
            }
        }else createNotification(service,false,true,R.mipmap.min_icon_transparent,R.mipmap.ic_launch_round,
                R.string.notification_ticker_task_server_internet,R.string.notification_title_task_server_internet,R.string.notification_message_task_server_internet);
    }
    private void restartOrNormalStart(boolean flag){
        service.setRestartFlag(flag);
        int period;
        if (flag){
            period=PERIOD_RESTART_TASK;
        }else {
            period=getIntFromPref(R.string.key_send_data);
            if (period<PERIOD_RESTART_TASK) period=PERIOD_RESTART_TASK;
        }
        TimerTask task=new TimerTaskToServer(service);
        timerServer.cancel();
        timerServer=new Timer();
        timerServer.schedule(task,period*THOUSAND,period*THOUSAND);
    }

    private int sendToServer(){
        InetAddress serverAddr;
        Socket socket=null;
        DataOutputStream sockOut=null;
        DataInputStream sockIn=null;
        response=MINUS_ONE;
        try {
            serverAddr = InetAddress.getByName(getStringFromPref(R.string.key_server));
            socket = new Socket(serverAddr, getIntFromPref(R.string.key_port));
            socket.setSoTimeout(SOCKET_TIMEOUT);
            sockOut = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            byte[] imei = Util.QR_CODE.getBytes(Charset.forName("UTF-8"));
            sockOut.writeShort(imei.length);
            sockOut.write(imei);
            sockOut.flush();
            request = MINUS_ONE;
            sockIn = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            while (true) {
                try {
                    request = sockIn.readByte();
                } catch (EOFException e) {
                    /**I catch the expiration at the end of the readable bytes*/
                }
                if (request == HEX_ONE){
                    deleteNotification();
                    break;
                }else if (request==HEX_NULL){
                    createNotification(service,false,true,R.mipmap.min_icon_transparent,R.mipmap.ic_launch_round,
                            R.string.notification_ticker_task_server_imei,R.string.notification_title_task_server_imei,R.string.notification_message_task_server_imei);
                    return response;
                }
            }
            byte[] AVLpacket = createAVLPacket();
            sockOut.write(AVLpacket);
            sockOut.flush();
            while (true) {
                try {
                    response = sockIn.readInt();
                } catch (EOFException e) {
                    /**I catch the expiration at the end of the readable bytes*/
                }
                if (response != MINUS_ONE) {
                    System.out.println("!!!!!!!!!!!!Send: \n"+bytesToHex(AVLpacket));
                    deleteNotification();
                    //result = true;
                    break;
                }
            }
        }catch (IOException e) {
            System.out.println("!!!!!!!!!!!!Send: EXCEPTION: " + e.getMessage());
            createNotification(service,false,true,R.mipmap.min_icon_transparent,R.mipmap.ic_launch_round,
                    R.string.notification_ticker_task_server_connect,R.string.notification_title_task_server_connect,R.string.notification_message_task_server_connect);
            response = DEF_VALUE_NULL;
        }catch (Throwable e) {
            Log.e(TAG,"Create socket in TimerTaskToServer, method 'sendToServer'",e);
        }finally {
            closeStream(sockOut,sockIn);
            if (socket!=null && !socket.isClosed())try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return response;
    }

    private byte[] createAVLPacket() {
        byte[] bytes=null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream daos = new DataOutputStream(new BufferedOutputStream(baos));
        try {
            daos.write(PREAMBLE);
            byte[] data=getAVLDataArray();
            System.out.println(bytesToHex(data));
            daos.writeInt(data.length);
            daos.write(data);
            daos.writeInt(getCrc16(data));
            daos.flush();
            bytes=baos.toByteArray();
        } catch (Exception e) {
            Log.e(TAG,"Method 'createAVLPacket' in TimerTaskToServer",e);
        }finally {
            closeStream(baos,daos);
        }
        return bytes;
    }

    private byte[] getAVLDataArray() {
        list=new ArrayList<>();
        byte[] bytes=null;
        SQLiteDatabase db;
        Cursor c=null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream daos = new DataOutputStream(new BufferedOutputStream(baos));
        try {
            db = dbHelper.getWritableDatabase();
            c=db.query(NAME_TABLE_LOC,new String[]{COL_ID,COL_DATA},null,null,null,null,COL_DATE_INSERT,String.valueOf(MAX_LENGTH_RECORDS));
            daos.writeByte(CODEC_ID);
            daos.writeByte(c.getCount());
            daos.write(getAVLData());
            daos.writeByte(c.getCount());
            daos.flush();
            bytes=baos.toByteArray();
        } catch (Exception e) {
            Log.e(TAG,"Create data packet in method 'getAVLDataArray' in TimerTaskToServer",e);
        } finally {
            closeStream(daos,baos);
        }
        return bytes;
    }

    private byte[] getAVLData() {
        list=new ArrayList<>();
        byte[] bytes=null;
        SQLiteDatabase db;
        Cursor c=null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream daos = new DataOutputStream(new BufferedOutputStream(baos));
        try {
            db = dbHelper.getWritableDatabase();
            c=db.query(NAME_TABLE_LOC,new String[]{COL_ID,COL_DATA},null,null,null,null,COL_DATE_INSERT,String.valueOf(MAX_LENGTH_RECORDS));
            if (c.moveToFirst()){
                int id=c.getColumnIndexOrThrow(COL_ID);
                int idColData=c.getColumnIndexOrThrow(COL_DATA);
                do {
                    daos.write(c.getBlob(idColData));
                    list.add(c.getInt(id));
                }while (c.moveToNext());
            }
            daos.flush();
            bytes=baos.toByteArray();
        } catch (Exception e) {
            Log.e(TAG,"Create data packet in method 'gatDataPacket' in TimerTaskToServer",e);
        } finally {
            if (c!=null && !c.isClosed()) c.close();
            closeStream(daos,baos);
        }
        return bytes;
    }



    private void deleteRowsInBd(List<Integer> listIds){
        if (listIds.size()!=response){
            listIds=listIds.subList(0,min(listIds.size(),response));
        }
        String args=TextUtils.join(", ",listIds.toArray());
        SQLiteDatabase db;
        try {
            db = dbHelper.getWritableDatabase();
            db.execSQL(String.format("DELETE FROM "+NAME_TABLE_LOC+" WHERE "+COL_ID+" IN (%s);", args));
        } catch (Exception e) {
            Log.e(TAG,"Delete rows in "+NAME_DB+" in method 'deleteRowsInBd' in TimerTaskToServer",e);
        }
    }
    private void deleteNotification(){
        if (existNotification) {
            cancelNotification(service,R.string.key_notification_task_server);
            existNotification=false;
            setIntFromPref(LIMIT_TRY_CONNECT, DEF_VALUE_NULL);
        }
    }

    private void createNotification(Context context, boolean notSwiped, boolean cancel, int smallIcon, int largeIcon, int idTicker, int idTitle, int idMessage){
        if (!existNotification || compareDay(System.currentTimeMillis(),timeCreateNotif)){
            int limit=getIntFromPref(LIMIT_TRY_CONNECT);
            setIntFromPref(LIMIT_TRY_CONNECT,++limit);
            if (getIntFromPref(LIMIT_TRY_CONNECT)>=LIMIT_NUMBER_TRY_CONNECT){
                Notification notification=getNotification(context,notSwiped,cancel,smallIcon,largeIcon, idTicker,idTitle,idMessage);
                timeCreateNotif=viewNotification(service,notification,R.string.key_notification_task_server);
                existNotification=true;
            }
        }
    }
}
