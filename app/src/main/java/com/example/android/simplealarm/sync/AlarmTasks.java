package com.example.android.simplealarm.sync;

import android.content.Context;

import com.example.android.simplealarm.AlarmReceiver;
import com.example.android.simplealarm.CameraActivity;
import com.example.android.simplealarm.utilities.NotificationUtils;

public class AlarmTasks {

    public static final String ACTION_STOP_AND_DISMISS_ALARM = "stop_and_dismiss_alarm";

    public static void executeTasks(Context context, String action) {
        switch (action) {
            case ACTION_STOP_AND_DISMISS_ALARM:
                stopAndDismissAlarmOnNotification(context);
                break;
        }
    }

    private static void stopAndDismissAlarmOnNotification(Context context) {
        AlarmReceiver.stopAlarm(context);
        NotificationUtils.clearAllNotifications(context);
    }
}
