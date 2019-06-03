package com.truetech.xltrackingbeacon.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.truetech.xltrackingbeacon.service.TrackerService;

import static com.truetech.xltrackingbeacon.Utils.CheckPermissions.ARRAY_PERMISSIONS;
import static com.truetech.xltrackingbeacon.Utils.CheckPermissions.hasPermissions;

public class BootReceived extends BroadcastReceiver {
    public BootReceived() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
            if (hasPermissions(context,ARRAY_PERMISSIONS)) {
                context.startService(new Intent(context, TrackerService.class));
            }
    }
}


