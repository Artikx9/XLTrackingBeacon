package com.truetech.xltrackingbeacon.listener;

import android.location.Location;
import android.os.Bundle;


public class EmptyLocationListener implements ILocListener {
    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public Location getCurrentLoc() {
        return null;
    }


}
