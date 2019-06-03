package com.truetech.xltrackingbeacon.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import com.truetech.xltrackingbeacon.MainActivity;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import static android.content.Context.LOCATION_SERVICE;
import static com.truetech.xltrackingbeacon.Utils.Constant.*;

public class Util {

    public static String QR_CODE  = null;
    public static boolean SERVICE_ACTIVATE = false;
    public static int COUNT_SATELLITES = 0;
    public static double SPEED = 0.0;

    public static boolean isOnline(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in airplane mode it will be null
        return (netInfo != null && netInfo.isConnected());
    }

    public static boolean isGPSEnable(){
        return ((LocationManager)getContext().getSystemService(LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static void showDialog(final Activity activity, String title, String message, String nameButtonOk, DialogInterface.OnClickListener onClickListener) {
        new android.support.v7.app.AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(nameButtonOk, onClickListener)
                .create()
                .show();
    }

    private static Application getApplicationUsingReflectionFirst() throws Exception {
        return (Application) Class.forName("android.app.ActivityThread")
                .getMethod("currentApplication").invoke(null, (Object[]) null);
    }

    private static Application getApplicationUsingReflectionTwo() throws Exception {
        return (Application) Class.forName("android.app.AppGlobals")
                .getMethod("getInitialApplication").invoke(null, (Object[]) null);
    }

    public static Application getContext() {
        Application app = null;
        try {
            app = getApplicationUsingReflectionFirst();
            if (app == null) app = getApplicationUsingReflectionTwo();
        } catch (Exception e) {
            Log.e(TAG, "Return global context", e);
        }
        return app;
    }

    public static Notification getNotification(Context context, boolean notSwiped, boolean cancel, int smallIcon, int largeIcon, int idTicker, int idTitle, int idMessage) {

        String id = "1"; // default_channel_id
        String title = "channel"; // Default Channel
        Intent intent;
        PendingIntent pendingIntent;
        NotificationCompat.Builder builder;
        NotificationManager notifManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = notifManager.getNotificationChannel(id);
            if (mChannel == null) {
                mChannel = new NotificationChannel(id, title, importance);
                mChannel.enableVibration(true);
                mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                notifManager.createNotificationChannel(mChannel);
            }
            builder = new NotificationCompat.Builder(context, id);
            intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            builder.setContentTitle(context.getText(idTitle))
                    .setOngoing(notSwiped)
                    .setSmallIcon(smallIcon)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), largeIcon))
                    .setContentText(context.getText(idMessage))
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(cancel)
                    .setContentIntent(pendingIntent)
                    .setTicker(context.getText(idTicker))
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                    .setWhen(System.currentTimeMillis());
        }
        else {
            builder = new NotificationCompat.Builder(context, id);
            intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            builder.setContentTitle(context.getText(idTitle))
                    .setOngoing(notSwiped)
                    .setSmallIcon(smallIcon)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), largeIcon))
                    .setContentText(context.getText(idMessage))
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(cancel)
                    .setContentIntent(pendingIntent)
                    .setTicker(context.getText(idTicker))
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                    .setWhen(System.currentTimeMillis());
        }
        Notification notification = builder.build();
        return notification;
    }

    public static long viewNotification(Context context, Notification notification, int id) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(id, notification);
        return System.currentTimeMillis();
    }

    public static void cancelNotification(Context context, int id) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(id);
    }

    public static long getCountTable(SQLiteOpenHelper helpBd, String nameTable) {
        SQLiteDatabase db = helpBd.getReadableDatabase();
        return DatabaseUtils.queryNumEntries(db, nameTable);
    }

    public static int getBatteryPercentage(Context context) {

        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, iFilter);

        int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;

        float batteryPct = level / (float) scale;

        return (int) (batteryPct * 100);
    }

    public static int[] getLacAndCid() {
        int cid = DEF_VALUE_NULL;
        int lac = DEF_VALUE_NULL;
        TelephonyManager tm = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        @SuppressLint("MissingPermission") CellLocation location = tm.getCellLocation();
        if (location != null) {
            if (location instanceof GsmCellLocation) {
                cid = ((GsmCellLocation) location).getCid();
                lac = ((GsmCellLocation) location).getLac();
            } else if (location instanceof CdmaCellLocation) {
                cid = ((CdmaCellLocation) location).getBaseStationId();
                lac = ((CdmaCellLocation) location).getSystemId();
            }
            if (lac == -1) lac = DEF_VALUE_NULL;
            if (cid == -1) cid = DEF_VALUE_NULL;
        }
        return new int[]{lac & 0xffff, cid & 0xffff};
    }

    public static int getOperatorCode() {
        TelephonyManager tm = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        return Integer.parseInt(tm.getSimOperator());
    }

    public static short getAltitudeFromGoogleMaps(Location loc) {
        double longitude = loc.getLongitude();
        double latitude = loc.getLatitude();
        //double result = Double.NaN;
        short result = Short.MIN_VALUE;
        HttpClient httpClient = new DefaultHttpClient();
        HttpContext localContext = new BasicHttpContext();
        String url = "http://maps.googleapis.com/maps/api/elevation/"
                + "xml?locations=" + String.valueOf(latitude)
                + "," + String.valueOf(longitude)
                + "&sensor=true";
        HttpGet httpGet = new HttpGet(url);
        try {
            HttpResponse response = httpClient.execute(httpGet, localContext);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream instream = entity.getContent();
                int r = -1;
                StringBuffer respStr = new StringBuffer();
                while ((r = instream.read()) != -1)
                    respStr.append((char) r);
                String tagOpen = "<elevation>";
                String tagClose = "</elevation>";
                result=parsePage(respStr,tagOpen,tagClose);
                instream.close();
            }
        } catch (ClientProtocolException e) {
        } catch (IOException e) {
        }
        return result;
    }

    private static short parsePage(StringBuffer respStr,String tagOpen,String tagClose){
        if (respStr.indexOf(tagOpen) != -1) {
            int start = respStr.indexOf(tagOpen) + tagOpen.length();
            int end = respStr.indexOf(tagClose);
            String value = respStr.substring(start, end);
            return (short) Double.parseDouble(value); // convert from meters to feet
        }
        return Short.MIN_VALUE;
    }

    public static short getAltitudeFromGisdata(Location loc) {
        double longitude = loc.getLongitude();
        double latitude = loc.getLatitude();
        //double result = Double.NaN;
        short result = Short.MIN_VALUE;
        HttpClient httpClient = new DefaultHttpClient();
        HttpContext localContext = new BasicHttpContext();
        String url = "http://gisdata.usgs.gov/"
                + "xmlwebservices2/elevation_service.asmx/"
                + "getElevation?X_Value=" + String.valueOf(longitude)
                + "&Y_Value=" + String.valueOf(latitude)
                + "&Elevation_Units=METERS&Source_Layer=-1&Elevation_Only=true";
        HttpGet httpGet = new HttpGet(url);
        try {
            HttpResponse response = httpClient.execute(httpGet, localContext);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream instream = entity.getContent();
                int r = -1;
                StringBuffer respStr = new StringBuffer();
                while ((r = instream.read()) != -1)
                    respStr.append((char) r);
                String tagOpen = "<double>";
                String tagClose = "</double>";
                result=parsePage(respStr,tagOpen,tagClose);
                instream.close();
            }
        } catch (ClientProtocolException e) {
        } catch (IOException e) {
        }
        return result;
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static void closeStream(Closeable... args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i] != null) try {
                args[i].close();
            } catch (IOException e) {
                Log.e(TAG, "Close OutputStream", e);
            }
        }
    }

    public static int getCrc16(byte[] buffer) {
        return getCrc16(buffer, 0, buffer.length, 0xA001, 0);
    }

    public synchronized static int getCrc16(byte[] buffer, int offset, int bufLen, int polynom, int preset) {
        preset &= 0xFFFF;
        polynom &= 0xFFFF;
        int crc = preset;
        for (int i = 0; i < bufLen; i++) {
            int data = buffer[i + offset] & 0xFF;
            crc ^= data;
            for (int j = 0; j < 8; j++) {
                if ((crc & 0x0001) != 0) {
                    crc = (crc >> 1) ^ polynom;
                } else {
                    crc = crc >> 1;
                }
            }
        }
        return crc & 0xFFFF;
    }

    public static boolean compareDay(long after, long before) {
        return after - before >= DAY;
    }

    public static int getIntFromPref(int key) {
        return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getContext()).getString(getContext().getString(key), null));
    }

    public static int getIntFromPref(String key) {
        return PreferenceManager.getDefaultSharedPreferences(getContext()).getInt(key, DEF_VALUE_NULL);
    }

    public static void setIntFromPref(int key, int value) {
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt(getContext().getString(key), value).apply();
    }

    public static void setIntFromPref(String key, int value) {
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt(key, value).apply();
    }

    public static void setLongFromPref(String key, long value) {
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putLong(key, value).apply();
    }

    public static void setLongFromPref(int key, long value) {
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putLong(getContext().getString(key), value).apply();
    }

    public static long getLongFromPref(int key) {
        return Long.parseLong(PreferenceManager.getDefaultSharedPreferences(getContext()).getString(getContext().getString(key), null));
    }

    public static long getLongFromPref(String key) {
        return PreferenceManager.getDefaultSharedPreferences(getContext()).getLong(key, DEF_VALUE_NULL);
    }

    public static boolean getBoolFromPref(int key) {
        return PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(getContext().getString(key), DEF_VALUE_BOOL);
    }

    public static String getStringFromPref(String nameKey) {
        return PreferenceManager.getDefaultSharedPreferences(getContext()).getString(nameKey, DEF_VALUE_STRING);
    }
    public static String getStringFromPref(int key) {
        return PreferenceManager.getDefaultSharedPreferences(getContext()).getString(getContext().getString(key), null);
    }
    public static void setStringFromPref(String key, String value) {
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString(key, value).apply();
    }

}




