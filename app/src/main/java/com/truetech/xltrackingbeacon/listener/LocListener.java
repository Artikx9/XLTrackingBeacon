package com.truetech.xltrackingbeacon.listener;

import android.location.Location;
import android.os.Bundle;


public class LocListener implements ILocListener {
    private volatile Location currentLoc=null;

    @Override
    public void onLocationChanged(Location location) {
        currentLoc=location;
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public Location getCurrentLoc() {
        return currentLoc;
    }


}

