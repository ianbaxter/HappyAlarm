package com.example.android.simplealarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.example.android.simplealarm.database.AlarmEntry;
import com.example.android.simplealarm.utilities.AlarmUtils;

public class AlarmInstance {

    private static final String ALARM_ENTRY_ID_KEY = "alarm_entry_id";

    public AlarmInstance(Context context, AlarmEntry alarmEntry) {
        PendingIntent alarmReceiverPendingIntent = getAlarmReceiverPendingIntent(context, alarmEntry);
        long alarmTimeInMillis = getAlarmTimeInMillis(alarmEntry);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(alarmTimeInMillis, null);
        alarmManager.setAlarmClock(alarmClockInfo, alarmReceiverPendingIntent);
    }

    private PendingIntent getAlarmReceiverPendingIntent(Context context, AlarmEntry alarmEntry) {
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        int alarmEntryId = alarmEntry.getId();
        alarmIntent.putExtra(ALARM_ENTRY_ID_KEY, alarmEntryId);
        return PendingIntent.getBroadcast(context, alarmEntryId,
                alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private long getAlarmTimeInMillis(AlarmEntry alarmEntry) {
        String alarmEntryTime = alarmEntry.getTime();
        return AlarmUtils.convertAlarmTimeToMillis(alarmEntryTime);
    }

    public static void cancelAlarm(Context context, int broadcastId) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, broadcastId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}
