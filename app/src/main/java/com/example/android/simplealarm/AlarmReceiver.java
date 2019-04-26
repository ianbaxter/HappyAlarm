package com.example.android.simplealarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;

import com.example.android.simplealarm.database.AlarmEntry;
import com.example.android.simplealarm.database.AppDatabase;
import com.example.android.simplealarm.utilities.AlarmUtils;
import com.example.android.simplealarm.utilities.NotificationUtils;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String ALARM_ENTRY_ID_KEY = "alarm_entry_id";
    private static final String ALARM_ENTRY_REPEATING_KEY = "alarm_entry_repeating";
    private static final String ALARM_DISMISSED_NOTIFICATION_KEY = "alarm_dismissed_notification";

    private static MediaPlayer mediaPlayer;

    public AlarmReceiver() {}

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (intent.getExtras() != null && intent.hasExtra(ALARM_ENTRY_ID_KEY)) {
            if (intent.hasExtra(ALARM_DISMISSED_NOTIFICATION_KEY)) {
                stopAlarm();

                AppExecutors.getsInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        AlarmEntry alarmEntry = getAlarmEntryFromIntent(intent, context);
                        snoozeAlarm(alarmEntry);
                    }

                    private void snoozeAlarm(AlarmEntry alarmEntry) {
                        String snoozedAlarmTime = AlarmUtils.snoozeAlarmTime(5);
                        alarmEntry.setTime(snoozedAlarmTime);
                        new AlarmInstance(context, alarmEntry);
                    }
                });
            } else if (intent.hasExtra(ALARM_ENTRY_REPEATING_KEY)) {
                triggerAlarm(context, intent);

                boolean isAlarmRepeating = intent.getBooleanExtra(ALARM_ENTRY_REPEATING_KEY, false);
                if (isAlarmRepeating) {
                    AppExecutors.getsInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            AlarmEntry alarmEntry = getAlarmEntryFromIntent(intent, context);
                            repeatAlarm(alarmEntry);
                        }

                        private void repeatAlarm(AlarmEntry alarmEntry) {
                            String currentAlarmTime = alarmEntry.getTime();
                            String newAlarmTime = AlarmUtils.newAlarmTime(currentAlarmTime, 24);
                            alarmEntry.setTime(newAlarmTime);
                            new AlarmInstance(context, alarmEntry);
                        }
                    });
                } else {
                    AppExecutors.getsInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            AlarmEntry alarmEntry = getAlarmEntryFromIntent(intent, context);
                            setAlarmOffIfCurrentlyOn(alarmEntry);
                        }

                        private void setAlarmOffIfCurrentlyOn(AlarmEntry alarmEntry) {
                            if (alarmEntry.isAlarmOn()) {
                                alarmEntry.setAlarmOn(false);
                                AppDatabase mDb = AppDatabase.getInstance(context.getApplicationContext());
                                mDb.alarmDao().updateAlarm(alarmEntry);
                            }
                        }
                    });
                }
            }
        }
    }

    public static void stopAlarm() {
        mediaPlayer.stop();
        mediaPlayer.release();
    }

    private AlarmEntry getAlarmEntryFromIntent(Intent intent, Context context) {
        int alarmEntryId = intent.getIntExtra(ALARM_ENTRY_ID_KEY, 0);
        AppDatabase mDb = AppDatabase.getInstance(context.getApplicationContext());
        return mDb.alarmDao().loadAlarmById(alarmEntryId);
    }

    private void triggerAlarm(Context context, Intent intent) {
        int alarmEntryId = intent.getIntExtra(ALARM_ENTRY_ID_KEY, 0);
        NotificationUtils.alarmTriggeredNotification(context, alarmEntryId);
        startMediaPlayer(context);
    }

    private void startMediaPlayer(final Context context) {
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
    }
}
