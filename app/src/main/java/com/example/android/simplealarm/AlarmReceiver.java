package com.example.android.simplealarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.Log;

import com.example.android.simplealarm.database.AlarmEntry;
import com.example.android.simplealarm.database.AppDatabase;
import com.example.android.simplealarm.utilities.AlarmUtils;
import com.example.android.simplealarm.utilities.NotificationUtils;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = AlarmReceiver.class.getSimpleName();

    private static final String ALARM_ENTRY_ID_KEY = "alarm_entry_id";
    private static final String ALARM_ENTRY_REPEATING_KEY = "alarm_entry_repeating";
    private static final String ALARM_DISMISSED_NOTIFICATION_KEY = "alarm_dismissed_notification";

    private static MediaPlayer mediaPlayer;

    /**
     * Default Constructor
     */
    public AlarmReceiver() {}

    public AlarmReceiver(Context context, AlarmEntry alarmEntry) {
        // Create a pending intent for the alarm
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        int alarmEntryId = alarmEntry.getId();
        boolean alarmRepeatIsOn = alarmEntry.getAlarmIsRepeating();
        alarmIntent.putExtra(ALARM_ENTRY_ID_KEY, alarmEntryId);
        alarmIntent.putExtra(ALARM_ENTRY_REPEATING_KEY, alarmRepeatIsOn);
        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(context, alarmEntryId,
                alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Determine time until alarm
        String alarmEntryTime = alarmEntry.getTime();
        Log.i(TAG, "AlarmReceiver Time is: " + (alarmEntryTime));
        long alarmTimeInMillis = AlarmUtils.getAlarmTimeInMillis(alarmEntryTime);

        // Set alarm on alarmManager depending on whether it should repeat or not
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTimeInMillis, alarmPendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTimeInMillis, alarmPendingIntent);
        }
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        // Determine which intent has been received
        if (intent.getExtras() != null && intent.hasExtra(ALARM_DISMISSED_NOTIFICATION_KEY)) {
            stopAlarm();

            AppExecutors.getsInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    // Snooze alarm for a set number of minutes
                    if (intent.hasExtra(ALARM_ENTRY_ID_KEY)) {
                        int alarmEntryId = intent.getIntExtra(ALARM_ENTRY_ID_KEY, 0);
                        AppDatabase mDb = AppDatabase.getInstance(context.getApplicationContext());
                        AlarmEntry alarmEntry = mDb.alarmDao().loadAlarmById(alarmEntryId);
                        String snoozedAlarmTime = AlarmUtils.snoozeAlarmTime(5);
                        alarmEntry.setTime(snoozedAlarmTime);
                        new AlarmReceiver(context, alarmEntry);
                    }
                }
            });
        } else if (intent.getExtras() != null && intent.hasExtra(ALARM_ENTRY_ID_KEY)) {
            int alarmEntryId = intent.getIntExtra(ALARM_ENTRY_ID_KEY, 0);
            NotificationUtils.alarmSoundingNotification(context, alarmEntryId);

            mediaPlayer = MediaPlayer.create(context, R.raw.alarm1);
            mediaPlayer.setLooping(false);
            mediaPlayer.start();

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mediaPlayer.release();
                    NotificationUtils.clearAllNotifications(context);
                }
            });

            // Determine if alarm is meant to repeat and set new alarm with same alarmEntry with newAlarmTime
            boolean alarmRepeatIsOn = false;
            if (intent.hasExtra(ALARM_ENTRY_REPEATING_KEY)) {
                alarmRepeatIsOn = intent.getBooleanExtra(ALARM_ENTRY_REPEATING_KEY, false);
                if (alarmRepeatIsOn) {
                    AppExecutors.getsInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            int alarmEntryId = intent.getIntExtra(ALARM_ENTRY_ID_KEY, 0);
                            AppDatabase mDb = AppDatabase.getInstance(context.getApplicationContext());
                            AlarmEntry alarmEntry = mDb.alarmDao().loadAlarmById(alarmEntryId);
                            String currentAlarmTime = alarmEntry.getTime();
                            String newAlarmTime = AlarmUtils.newAlarmTime(currentAlarmTime, 24);
                            alarmEntry.setTime(newAlarmTime);
                            new AlarmReceiver(context, alarmEntry);
                        }
                    });
                }
            }

            // If alarm is not set to repeat, set alarmIsOn to false to turn off alarm
            if (!alarmRepeatIsOn) {
                AppExecutors.getsInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        int alarmEntryId = intent.getIntExtra(ALARM_ENTRY_ID_KEY, 0);
                        AppDatabase mDb = AppDatabase.getInstance(context.getApplicationContext());
                        AlarmEntry alarmEntry = mDb.alarmDao().loadAlarmById(alarmEntryId);
                        // Check if alarm is already on and turn on if not
                        if (alarmEntry.getAlarmIsOn()) {
                            alarmEntry.setAlarmIsOn(false);
                            mDb.alarmDao().updateAlarm(alarmEntry);
                        }
                    }
                });
            }
        }
    }

    public static void cancelAlarm(Context context, int broadcastId) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent.getBroadcast(context, broadcastId, intent, PendingIntent.FLAG_UPDATE_CURRENT).cancel();
    }

    public static void stopAlarm() {
        mediaPlayer.stop();
        mediaPlayer.release();
    }
}
