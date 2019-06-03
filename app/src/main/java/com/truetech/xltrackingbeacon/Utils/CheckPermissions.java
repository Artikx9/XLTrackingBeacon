package com.truetech.xltrackingbeacon.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;


/**
 * Created by Ajwar on 11.07.2017.
 */
public class CheckPermissions {
    public static final int PERMISSION_ALL = 99;
    public static final String[] ARRAY_PERMISSIONS = new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CAMERA, Manifest.permission.FOREGROUND_SERVICE};


    public static boolean checkAllPermissions(Activity activity, int PERMISSION_ALL, String title, String message, String nameButtonOk, String... permissions) {
        boolean result = false;
        if (!hasPermissions(activity, permissions)) {
            // Should we show an explanation?
            for (int i = 0; i < permissions.length; i++) {
                result = result || ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions[i]);
            }
            if (result) {
                showDialog(activity,PERMISSION_ALL,title,message,nameButtonOk,permissions);
            } else {
                ActivityCompat.requestPermissions(activity, permissions, PERMISSION_ALL);
            }
            return false;
        } else {
            return true;
        }
    }
    private static void showDialog(final Activity activity, final int PERMISSION_ALL, String title, String message, String nameButtonOk, final String... permissions) {
        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(nameButtonOk, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Prompt the user once explanation has been shown
                        ActivityCompat.requestPermissions(activity, permissions, PERMISSION_ALL);
                    }
                })
                .create()
                .show();
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
}
