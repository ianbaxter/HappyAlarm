package com.example.android.simplealarm.sync;

import android.content.Context;

import com.example.android.simplealarm.AlarmReceiver;
import com.example.android.simplealarm.utilities.NotificationUtils;

public class AlarmTasks {

    public static final String ACTION_STOP_ALARM = "stop_alarm";

    private static final String ACTION_DISMISS_NOTIFICATION = "dismiss_notification";

    public static void executeTasks(Context context, String action) {
        switch (action) {
            case ACTION_STOP_ALARM:
                stopAlarmOnNotification(context);
                break;
            case ACTION_DISMISS_NOTIFICATION:
                NotificationUtils.clearAllNotifications(context);
        }
    }

    private static void stopAlarmOnNotification(Context context) {
        AlarmReceiver.stopAlarm();
        NotificationUtils.clearAllNotifications(context);
    }

    private static void snoozeAlarm(Context context) {
        AlarmReceiver.stopAlarm();
        NotificationUtils.clearAllNotifications(context);


    }
}
