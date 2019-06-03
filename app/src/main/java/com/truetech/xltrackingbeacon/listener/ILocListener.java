package com.truetech.xltrackingbeacon.listener;

import android.location.Location;
import android.location.LocationListener;


public interface ILocListener extends LocationListener {
    Location getCurrentLoc();
}
