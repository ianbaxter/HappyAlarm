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

    private static MediaPlayer mediaPlayer;

    /**
     * Default Constructor
     */
    public AlarmReceiver() {}

    public AlarmReceiver(Context context, AlarmEntry alarmEntry) {
        // Create a pending intent for the alarm
        Intent intent = new Intent(context, AlarmReceiver.class);
        int alarmEntryId = alarmEntry.getId();
        Log.i(TAG, "MyAlarm set with id: " + alarmEntryId);
        intent.putExtra(ALARM_ENTRY_ID_KEY, alarmEntryId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, alarmEntryId,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Determine time until alarm
        String alarmEntryTime = alarmEntry.getTime();
        long alarmTimeInMillis = AlarmUtils.getAlarmTimeInMillis(alarmEntryTime);

        // Set alarm on alarmManager
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTimeInMillis, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTimeInMillis, pendingIntent);
        }
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (intent.getExtras() != null && intent.hasExtra("alarm_dismissed_notification")) {
            stopAlarm();
        } else {
            NotificationUtils.alarmSoundingNotification(context);

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

            // Set alarmIsOn to false after corresponding alarm has been triggered
            if (intent != null && intent.hasExtra(ALARM_ENTRY_ID_KEY)) {
                final int itemIndex = intent.getIntExtra(ALARM_ENTRY_ID_KEY, 0);
                Log.i("test", "id intent was:" + itemIndex);

                AppExecutors.getsInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        AppDatabase mDb = AppDatabase.getInstance(context.getApplicationContext());
                        AlarmEntry alarmEntry = mDb.alarmDao().loadAlarmById(itemIndex);
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
