package com.truetech.xltrackingbeacon.listener;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.util.Log;

import static android.content.Context.SENSOR_SERVICE;
import static com.truetech.xltrackingbeacon.Utils.Constant.TAG;


public class AzimuthSensorListener implements IAzimuthSensorListener {
    private static final float ALPHA_LOW_PASS = 0.25f;
    private static final  int LENGTH =3;
    private float gData[]=new float[LENGTH];
    private float mData[]=new float[LENGTH];
    private float rData[]=new float[LENGTH];
    private float rMat[]=new float[9];
    private float iMat[]=new float[9];
    private float result[]=new float[LENGTH];
    public static volatile int azimuth=0;

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch ( event.sensor.getType() ) {
            case Sensor.TYPE_ROTATION_VECTOR:
                System.arraycopy(event.values,0,rData,0, LENGTH);
                //rData=event.values.clone();
                break;
            case Sensor.TYPE_GRAVITY:
                System.arraycopy(event.values,0,gData,0, LENGTH);
                //gData = event.values.clone();
                break;
            case Sensor.TYPE_ACCELEROMETER:
                //System.arraycopy(event.values,0,gData,0,LENGTH);
                gData = lowPass(event.values.clone(),gData);
                //gData = event.values.clone();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                //System.arraycopy(event.values,0,mData,0,LENGTH);
                mData = lowPass(event.values.clone(),mData);
                //mData = event.values.clone();
                break;

            default: return;
        }
        if (rData!=null){
            SensorManager.getRotationMatrixFromVector(rMat,rData);
        }else if(gData!=null && mData!=null){
            SensorManager.getRotationMatrix(rMat,null,gData,mData);
        }else {
            Log.e(TAG,"Not found sensors");
        }
        SensorManager.remapCoordinateSystem(rMat,SensorManager.AXIS_X,SensorManager.AXIS_Z,iMat);
        SensorManager.getOrientation(iMat,result);
        azimuth=toDegrees(result[0]);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void enable(Context context){
        SensorManager mSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        Sensor sensRotation= mSensorManager.getDefaultSensor( Sensor.TYPE_ROTATION_VECTOR );
        if (sensRotation!=null){
            mSensorManager.registerListener(this,sensRotation,SensorManager.SENSOR_DELAY_NORMAL);
        }else {
            Sensor sensGravity= mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
            Sensor sensMagnetic= mSensorManager.getDefaultSensor( Sensor.TYPE_MAGNETIC_FIELD );
            mSensorManager.registerListener( this, sensMagnetic, SensorManager.SENSOR_DELAY_NORMAL );
            Sensor sensAccelerometer;
            if (sensGravity!=null){
                mSensorManager.registerListener( this, sensGravity, SensorManager.SENSOR_DELAY_NORMAL);
            }else {
                sensAccelerometer= mSensorManager.getDefaultSensor( Sensor.TYPE_ACCELEROMETER );
                mSensorManager.registerListener( this, sensAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
    }

    @Override
    public void disable(Context context){
        azimuth=0;
        SensorManager mSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        mSensorManager.unregisterListener(this);
    }

    //Low-pass filter
    private float[] lowPass( float[] input, float[] output ) {
        if ( output == null ) return input;
        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + ALPHA_LOW_PASS * (input[i] - output[i]);
        }
        return output;
    }

    private int toDegrees(float in){
        return (int) Math.round((Math.toDegrees(in)+360)%360);
    }

}
