package com.truetech.xltrackingbeacon.tasks;


import android.app.Notification;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.util.Log;
import com.truetech.xltrackingbeacon.R;
import com.truetech.xltrackingbeacon.data.DBHelper;
import com.truetech.xltrackingbeacon.listener.ILocListener;
import com.truetech.xltrackingbeacon.listener.SatellitesInfo;
import com.truetech.xltrackingbeacon.service.TrackerService;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.nio.ByteBuffer;
import java.util.TimerTask;

import static com.truetech.xltrackingbeacon.Utils.Constant.*;
import static com.truetech.xltrackingbeacon.Utils.Util.*;
import static com.truetech.xltrackingbeacon.listener.AzimuthSensorListener.azimuth;
import static com.truetech.xltrackingbeacon.listener.MyPhoneStateListener.strength;
import static com.truetech.xltrackingbeacon.listener.SatellitesInfo.*;
import static com.truetech.xltrackingbeacon.Utils.Constant.DEF_VALUE_NULL;
import static com.truetech.xltrackingbeacon.Utils.Constant.LIMIT_ROWS_IN_BD;
import static com.truetech.xltrackingbeacon.Utils.Constant.NAME_TABLE_LOC;

public class TimerTaskDB extends TimerTask {
    private TrackerService service;
    private ILocListener locListener;
    private DBHelper dbHelper;
    private long timeCreateNotif = DEF_VALUE_NULL;
    private boolean existNotification = false;

    private Location lastLocation = null;
    private double calculatedSpeed = 0;

    public TimerTaskDB(TrackerService service) {
        this.service = service;
        this.locListener = service.getLocationListener();
        this.dbHelper = DBHelper.getInstance();
    }

    @Override
    public void run() {
        long count = getCountTable(dbHelper, NAME_TABLE_LOC);
        if (count < LIMIT_ROWS_IN_BD) {
            createInsert();
            if (existNotification) {
                cancelNotification(service, R.string.key_notification_task_bd);
                existNotification = false;
            }
        } else if (!existNotification || compareDay(System.currentTimeMillis(), timeCreateNotif)) {
            Notification notification = getNotification(service, false, true, R.mipmap.min_icon_transparent, R.mipmap.ic_launch_round,
                    R.string.notification_ticker_task_bd, R.string.notification_title_task_bd, R.string.notification_message_task_bd);
            timeCreateNotif = viewNotification(service, notification, R.string.key_notification_task_bd);
            existNotification = true;
        }
    }

    private boolean[] readPref(Location loc) {
        boolean[] prefs = new boolean[10];
        prefs[9] = getBoolFromPref(R.string.key_send_without_gps);
        prefs[8] = getBoolFromPref(R.string.key_battery);

        prefs[5] = getBoolFromPref(R.string.key_loc_area_code);
        prefs[6] = getBoolFromPref(R.string.key_gsm_signal);
        prefs[7] = getBoolFromPref(R.string.key_operator_code);
        if (loc != null) {
            prefs[0] = getBoolFromPref(R.string.key_latitude);
            prefs[1] = getBoolFromPref(R.string.key_altitude);
            prefs[2] = getBoolFromPref(R.string.key_angle);
            prefs[3] = getBoolFromPref(R.string.key_satellites);
            prefs[4] = getBoolFromPref(R.string.key_speed);

        }

        return prefs;
    }


    private void createInsert() {
        double prec = 10000000D;
        Location loc = locListener.getCurrentLoc();
        boolean[] prefs = readPref(loc);
        if (loc == null && !prefs[9]) return;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream daos = new DataOutputStream(new BufferedOutputStream(baos));
        ContentValues cv = new ContentValues();
        SQLiteDatabase db;
        byte N1 = 0x00;byte N2 = 0x00;byte N4 = 0x00;byte N8 = 0x00;
        if(prefs[5]) N2++; //loc
        if(prefs[5]) N2++; //celId
        if(prefs[6]) N1++;
        if(prefs[7]) N2++;
        if(prefs[8]) N1++;
        N1++; // speed ==0 id239=0
        N2++; // speed id24
        try {
            float speed = getSpeed(loc);
            daos.write(getTimestamp());
            daos.writeByte(PRIORITY);


            /**GPS Element*/
            if (prefs[0]) {     //latitude and Longitude
                daos.writeInt((int)(loc.getLongitude()*prec));
                daos.writeInt((int)(loc.getLatitude()*prec));
            }

            if (prefs[1]) {//altitude
                short altitude = getAltitude(loc);
                daos.writeShort(altitude);
            }
            if (prefs[2]) {//angle
                float angle = getAngle(loc);
                daos.writeShort((int)angle);
            }
            if (prefs[3]) {//satellites
                daos.writeByte(satellitesInFix);
            }
            if (prefs[4]) {//speed
                SatellitesInfo.speed = speed;
                daos.writeShort((int) speed);
            }

            /**IO Element*/
            daos.writeByte(0x00); // EVENT ID IO
            daos.writeByte(N1 + N2 + N4 + N8); //IO elements in record

            daos.writeByte(N1); //  IO elements, which length is 1 Byte
            if (prefs[6]) {//gsm_signal
                daos.writeByte(15);
                daos.writeByte(strength);
            }
            if (prefs[8]) {//battery_energy
                daos.writeByte(98);
                daos.writeByte(getBatteryPercentage(service));//write battery level percentage
            }
            //speed ==0 and > 0
            daos.writeByte(0xEF);
            if ((int) speed == 0) {
                daos.writeByte(0x00);
            } else {
                daos.writeByte(0x01);
            }

            daos.writeByte(N2); //  IO elements, which value length is 2 Bytes

            //speed id 24
            daos.writeByte(0x18);
            daos.writeShort((int)speed);

            if (prefs[5]) {//loc_area_code
                int[] array = getLacAndCid();             // array[1]cell id
                daos.writeByte(0xCD);
                daos.writeShort(array[1]);
                daos.writeByte(0xCE);
                daos.writeShort(array[0]);
            }

            if (prefs[7]) {//operator_code
                daos.writeByte(0xF1);
                daos.writeShort(getOperatorCode());
            }
            daos.writeByte(N4); // IO element, which value length is 4 Bytes

            daos.writeByte(N8); // IO elements, which value length is 8 Bytes

            daos.flush();
            byte[] bytes = baos.toByteArray();
            cv.put(COL_DATA, bytes);
            cv.put(COL_DATE_INSERT, System.currentTimeMillis() / 1000);
            db = dbHelper.getWritableDatabase();
            db.insert(NAME_TABLE_LOC, null, cv);
        } catch (Exception e) {
            Log.e(TAG, "Write byteArrayOutputStream in TimerTaskDB", e);
        } finally {
            closeStream(daos, baos);
        }
    }


    private LocationProvider getProvider(String name) {
        LocationManager manager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        return manager.getProvider(name);
    }

    private short getAltitude(Location loc) {
        short altitude;//altitude
        if (getProvider(loc.getProvider()).supportsAltitude()) {
            altitude = (short) loc.getAltitude();
        } else {
            altitude = getAltitudeFromGoogleMaps(loc);
            if (altitude == Short.MIN_VALUE) altitude = getAltitudeFromGisdata(loc);
        }
        return altitude;
    }

    private float getAngle(Location loc) {
        float angle;//angle
        if (loc.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            angle = loc.getBearing();
        } else {
            angle = azimuth;
        }
        return angle;
    }

    private byte[] getTimestamp() {
        long time = System.currentTimeMillis() / 1000 - DIFF_UNIX_TIME;
        return  ByteBuffer.allocate(8).putFloat(time).array();
    }


    private float getSpeed(Location loc){
        if(lastLocation != null){
        double elapsedTime = (loc.getTime() - lastLocation.getTime()) / 1_000; // Convert milliseconds to seconds
            if ( elapsedTime == 0.0) calculatedSpeed = 0;
            else
        calculatedSpeed = lastLocation.distanceTo(loc) / elapsedTime;
        }
        this.lastLocation = loc;
       return (float) ((loc.hasSpeed() && loc.getSpeed()>0 ? loc.getSpeed() : calculatedSpeed)*3.6);
    }


}


