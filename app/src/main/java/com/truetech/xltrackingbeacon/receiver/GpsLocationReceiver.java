package com.truetech.xltrackingbeacon.receiver;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

import com.truetech.xltrackingbeacon.R;
import com.truetech.xltrackingbeacon.listener.EmptyLocationListener;
import com.truetech.xltrackingbeacon.service.TrackerService;
import com.truetech.xltrackingbeacon.listener.ILocListener;

import static android.content.Context.LOCATION_SERVICE;
import static com.truetech.xltrackingbeacon.Utils.Constant.DEF_VALUE_NULL;
import static com.truetech.xltrackingbeacon.Utils.Util.*;


public class GpsLocationReceiver extends BroadcastReceiver {

    private BroadcastReceiver broadcastReceiver;
    private static LocationManager locManager = null;
    private static ILocListener locationListener = null;
    private TrackerService service;

    public GpsLocationReceiver(TrackerService service) {
        this.service = service;
        init(service);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        init(context);
        if (intent.getAction().equalsIgnoreCase("android.location.PROVIDERS_CHANGED")  && locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            enableListenerForGPS();
        } else {
            disableLocListener();
        }
        if (intent.getAction().equalsIgnoreCase("android.net.conn.CONNECTIVITY_CHANGE")
                && !locManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && isOnline(context) && getBoolFromPref(R.string.key_use_gps_and_wifi)) {
            service.initProvider(false);
        }
    }

    public static void disableLocListener() {
        if (locationListener != null) locManager.removeUpdates(locationListener);
    }
    @SuppressLint("MissingPermission")
    public static void enableListenerForGPS() {
        if (locationListener != null && locManager!=null) locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, getIntFromPref(R.string.key_reg_data), DEF_VALUE_NULL, locationListener);
    }

    private void init(Context context) {
        if (locManager == null) locManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        if (locationListener == null) locationListener = new EmptyLocationListener();
    }

}
