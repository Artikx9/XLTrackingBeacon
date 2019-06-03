package com.truetech.xltrackingbeacon.tasks;

import android.app.Notification;
import com.truetech.xltrackingbeacon.R;
import com.truetech.xltrackingbeacon.service.TrackerService;

import java.util.TimerTask;

import static com.truetech.xltrackingbeacon.Utils.Util.*;


public class TimerTaskEnableGps extends TimerTask {
    private TrackerService service;

    public TimerTaskEnableGps(TrackerService service) {
        this.service = service;
    }

    @Override
    public void run() {
        cancelNotification(service, R.string.key_notification_enable_gps);
        if (!isGPSEnable() && SERVICE_ACTIVATE){
            Notification notif=getNotification(service,false,true,R.mipmap.min_icon_transparent,R.mipmap.ic_launch_round,
                    R.string.notification_ticker_enable_gps,R.string.notification_title_enable_gps,R.string.notification_message_enable_gps);
            viewNotification(service,notif,R.string.key_notification_enable_gps);
        }
    }
}
