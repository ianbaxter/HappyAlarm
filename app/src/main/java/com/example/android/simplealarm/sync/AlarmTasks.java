package com.example.android.simplealarm.sync;

import android.content.Context;
import android.content.Intent;

import com.example.android.simplealarm.utilities.NotificationUtils;

public class AlarmTasks {

    public static final String ACTION_STOP_ALARM = "stop_alarm";
    private static final String ACTION_DISMISS_NOTIFICATION = "dismiss_notification";

    private static final String ALARM_DISMISS_KEY = "dismiss_alarm";

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
        Intent intent = new Intent();
        intent.putExtra(ALARM_DISMISS_KEY, "from BroadcastReceiver");
        intent.setClassName("com.example.android.simplealarm",
                "com.example.android.simplealarm.CameraActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

        NotificationUtils.clearAllNotifications(context);
    }
}
