package com.truetech.xltrackingbeacon.listener;

import android.content.Context;
import android.hardware.SensorEventListener;

public interface IAzimuthSensorListener extends SensorEventListener {
    void enable(Context context);
    void disable(Context context);
}
