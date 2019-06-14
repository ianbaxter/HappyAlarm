package com.example.android.simplealarm.utilities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import androidx.core.app.NotificationCompat;

import com.example.android.simplealarm.AlarmReceiver;
import com.example.android.simplealarm.MainActivity;
import com.example.android.simplealarm.R;
import com.example.android.simplealarm.sync.AlarmIntentService;
import com.example.android.simplealarm.sync.AlarmTasks;

public class NotificationUtils {

    private static final int ALARM_SOUNDING_NOTIFICATION_ID = 9001;
    private static final int ACTION_DISMISS_ALARM_PENDING_INTENT_ID = 9002;
    private static final int ACTION_STOP_AND_DISMISS_ALARM_PENDING_INTENT_ID = 9003;
    private static final int ALARM_SNOOZED_NOTIFICATION_ID = 9004;
    private static final int ALARM_STOP_PENDING_INTENT_ID = 9005;

    private static final String ALARM_SNOOZED_NOTIFICATION_KEY = "alarm_snoozed_notification";
    private static final String ALARM_DISMISSED_NOTIFICATION_KEY = "alarm_dismissed_notification";
    private static final String ALARM_SOUNDING_NOTIFICATION_CHANNEL_KEY = "alarm_sounding_notification_channel";
    private static final String ALARM_ENTRY_ID_KEY = "alarm_entry_id";
    private static final String ALARM_DISMISS_KEY = "dismiss_alarm";

    public static void clearAllNotifications(Context context) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    public static void alarmTriggeredNotification(Context context, int alarmEntryId) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    ALARM_SOUNDING_NOTIFICATION_CHANNEL_KEY,
                    context.getString(R.string.alarm_notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, ALARM_SOUNDING_NOTIFICATION_CHANNEL_KEY)
                        .setSmallIcon(R.drawable.outline_alarm_24)
                        .setContentTitle(context.getString(R.string.alarm_sounding_message))
                        .setContentText(context.getString(R.string.notification_alarm_triggered_content_text))
                        .addAction(snoozeAlarmAction(context, alarmEntryId))
//                        .addAction(stopAndDismissAlarmAction(context)) CAMERA IS LEFT OPEN IF THIS IS USED
                        .setFullScreenIntent(stopAlarmOnNotification(context, alarmEntryId), true)
                        .setOngoing(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            notificationBuilder.setPriority(NotificationManager.IMPORTANCE_HIGH);
        }

        notificationManager.notify(ALARM_SOUNDING_NOTIFICATION_ID, notificationBuilder.build());
    }

    private static NotificationCompat.Action snoozeAlarmAction(Context context, int alarmEntryId) {
        Intent snoozeIntent = new Intent(context, AlarmReceiver.class);
        snoozeIntent.putExtra(ALARM_SNOOZED_NOTIFICATION_KEY, ALARM_SNOOZED_NOTIFICATION_ID);
        snoozeIntent.putExtra(ALARM_ENTRY_ID_KEY, alarmEntryId);

        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(),
                ALARM_SNOOZED_NOTIFICATION_ID, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Action((R.drawable.outline_snooze_24),
                context.getString(R.string.notification_action_snooze_title),
                snoozePendingIntent);
    }

    private static NotificationCompat.Action stopAndDismissAlarmAction(Context context) {
        Intent stopAndDismissAlarmIntent = new Intent(context, AlarmIntentService.class);
        stopAndDismissAlarmIntent.setAction(AlarmTasks.ACTION_STOP_AND_DISMISS_ALARM);

        PendingIntent stopAndDismissAlarmPendingIntent = PendingIntent.getService(
                context,
                ACTION_STOP_AND_DISMISS_ALARM_PENDING_INTENT_ID,
                stopAndDismissAlarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Action((R.drawable.outline_alarm_off_24),
                context.getString(R.string.notification_action_dismiss_title),
                stopAndDismissAlarmPendingIntent);
    }

    private static PendingIntent stopAlarmOnNotification(Context context, int alarmEntryId) {
        Intent intent = new Intent();
        intent.putExtra(ALARM_DISMISS_KEY, alarmEntryId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setClassName("com.example.android.simplealarm",
                "com.example.android.simplealarm.CameraActivity");

        return PendingIntent.getActivity(context,
                ALARM_STOP_PENDING_INTENT_ID,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
    }

    public static void snoozeAlarm(Context context, int alarmEntryId) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(ALARM_SNOOZED_NOTIFICATION_KEY, ALARM_SNOOZED_NOTIFICATION_ID);
        intent.putExtra(ALARM_ENTRY_ID_KEY, alarmEntryId);
        context.sendBroadcast(intent);
    }

    public static void snoozeAlarmNotification(Context context, int alarmEntryId) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    ALARM_SOUNDING_NOTIFICATION_CHANNEL_KEY,
                    context.getString(R.string.alarm_notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String snoozeTime = sharedPreferences.getString(context.getString(R.string.pref_snooze_time_key), "5");
        String contentText;
        if (snoozeTime != null && snoozeTime.equals("1")) {
            contentText = context.getString(R.string.notification_alarm_snoozed_content_text_minute, snoozeTime);
        } else {
            contentText = context.getString(R.string.notification_alarm_snoozed_content_text_minutes, snoozeTime);
        }

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, ALARM_SOUNDING_NOTIFICATION_CHANNEL_KEY)
                        .setSmallIcon(R.drawable.outline_snooze_24)
                        .setContentTitle(context.getString(R.string.alarm_snoozed_content_title))
                        .setContentText(contentText)
                        .addAction(dismissAlarmAction(context, alarmEntryId))
                        .setContentIntent(mainActivityIntent(context))
                        .setOngoing(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            notificationBuilder.setPriority(NotificationManager.IMPORTANCE_HIGH);
        }

        notificationManager.notify(ALARM_SNOOZED_NOTIFICATION_ID, notificationBuilder.build());
    }

    private static NotificationCompat.Action dismissAlarmAction(Context context, int alarmEntryId) {
        Intent dismissAlarmIntent = getDismissSnoozeIntent(context, alarmEntryId);

        PendingIntent dismissAlarmPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(),
                ACTION_DISMISS_ALARM_PENDING_INTENT_ID, dismissAlarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Action((R.drawable.outline_alarm_off_24),
                context.getString(R.string.notification_action_dismiss_title),
                dismissAlarmPendingIntent);
    }

    public static Intent getDismissSnoozeIntent(Context context, int alarmEntryId) {
        Intent dismissAlarmIntent = new Intent(context, AlarmReceiver.class);
        dismissAlarmIntent.putExtra(ALARM_DISMISSED_NOTIFICATION_KEY, ACTION_DISMISS_ALARM_PENDING_INTENT_ID);
        dismissAlarmIntent.putExtra(ALARM_ENTRY_ID_KEY, alarmEntryId);
        return dismissAlarmIntent;
    }

    private static PendingIntent mainActivityIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);

        return PendingIntent.getActivity(context,
                ALARM_SNOOZED_NOTIFICATION_ID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
    }
}
