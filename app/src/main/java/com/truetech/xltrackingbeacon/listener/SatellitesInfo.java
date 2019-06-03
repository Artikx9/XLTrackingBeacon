package com.truetech.xltrackingbeacon.listener;

import android.annotation.SuppressLint;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.LocationListener;
import android.location.LocationManager;

import com.truetech.xltrackingbeacon.MainActivity;
import com.truetech.xltrackingbeacon.R;
import com.truetech.xltrackingbeacon.service.TrackerService;

import static com.truetech.xltrackingbeacon.Utils.Constant.DEF_VALUE_NULL;
import static com.truetech.xltrackingbeacon.Utils.Util.COUNT_SATELLITES;
import static com.truetech.xltrackingbeacon.Utils.Util.SPEED;
import static com.truetech.xltrackingbeacon.Utils.Util.getBoolFromPref;
import static com.truetech.xltrackingbeacon.Utils.Util.getContext;
import static com.truetech.xltrackingbeacon.Utils.Util.getIntFromPref;
import static com.truetech.xltrackingbeacon.receiver.GpsLocationReceiver.disableLocListener;
import static com.truetech.xltrackingbeacon.receiver.GpsLocationReceiver.enableListenerForGPS;


public class SatellitesInfo implements GpsStatus.Listener {
    private LocationListener locListener = null;
    private LocationManager locManager = null;
    private TrackerService service = null;
    public static volatile int satellitesInFix = 0;
    public static volatile float speed = 0;


    @SuppressLint("MissingPermission")
    public SatellitesInfo(TrackerService service) {
        this.service = service;
        this.locManager = service.getLocManager();
        this.locListener = service.getLocationListener();
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onGpsStatusChanged(int event) {
        satellitesInFix=0;
        if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS){
            for (GpsSatellite sat : locManager.getGpsStatus(null).getSatellites()) {
                if (sat.usedInFix()) {
                    satellitesInFix++;
                }
            }
        }
        COUNT_SATELLITES = satellitesInFix;
        SPEED = speed;
        MainActivity.updateStatusScreen();
      // !!!  refreshAdapter(2,getContext().getString(R.string.listView_sat)+satellitesInFix);
     // !!!   refreshAdapter(3,getContext().getString(R.string.listView_speed) +speed);
        if (satellitesInFix>=4 && !service.getCurrentProvider().equals(LocationManager.GPS_PROVIDER)){
            locManager.removeUpdates(locListener);
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,getIntFromPref(R.string.key_reg_data), DEF_VALUE_NULL,locListener);
            service.getAzimuthListener().disable(getContext());
            service.setCurrentProvider(LocationManager.GPS_PROVIDER);
            disableLocListener();
        }else if (service.getCurrentProvider().equals(LocationManager.GPS_PROVIDER) && satellitesInFix<4 && getBoolFromPref(R.string.key_use_gps_and_wifi)){
            locManager.removeUpdates(locListener);
            //GpsLocationReceiver.disableLocListener();
            enableListenerForGPS();
            service.initProvider(false);
            service.getAzimuthListener().enable(getContext());
        }
    }

}
