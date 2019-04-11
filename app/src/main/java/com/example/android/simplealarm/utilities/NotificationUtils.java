package com.example.android.simplealarm.utilities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.example.android.simplealarm.MainActivity;
import com.example.android.simplealarm.R;
import com.example.android.simplealarm.sync.AlarmIntentService;
import com.example.android.simplealarm.sync.AlarmTasks;

public class NotificationUtils {

    private static final int ALARM_SOUNDING_PENDING_INTENT_ID = 9000;

    private static final int ALARM_SOUNDING_NOTIFICATION_ID = 9001;

    private static final int ACTION_STOP_ALARM_PENDING_INTENT_ID = 9002;

    private static final String ALARM_SOUNDING_NOTIFICATION_CHANNEL_ID = "alarm_sounding_notification_channel";

    public static void clearAllNotifications(Context context) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    public static void alarmSoundingNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    ALARM_SOUNDING_NOTIFICATION_CHANNEL_ID,
                    context.getString(R.string.alarm_notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, ALARM_SOUNDING_NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.outline_alarm_black_48)
                        .setContentTitle(context.getString(R.string.alarm_sounding_message))
                        .setContentText("Alarm at")
//                        .setContentIntent(contentIntent(context))
                        .addAction(stopAlarmAction(context))
                        .setAutoCancel(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notificationBuilder.setPriority(NotificationManager.IMPORTANCE_HIGH);
        }

        notificationManager.notify(ALARM_SOUNDING_NOTIFICATION_ID, notificationBuilder.build());
    }

    private static PendingIntent contentIntent(Context context) {
        Intent startActivityIntent = new Intent(context, MainActivity.class);

        return PendingIntent.getActivity(
                context,
                ALARM_SOUNDING_PENDING_INTENT_ID,
                startActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static NotificationCompat.Action stopAlarmAction(Context context) {

        Intent stopAlarmIntent = new Intent(context, AlarmIntentService.class);

        stopAlarmIntent.setAction(AlarmTasks.ACTION_STOP_ALARM);

        PendingIntent stopAlarmPendingIntent = PendingIntent.getService(
                context,
                ACTION_STOP_ALARM_PENDING_INTENT_ID,
                stopAlarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Action((R.drawable.outline_alarm_off_black_48),
                "Stop Alarm",
                stopAlarmPendingIntent);
    }
}
