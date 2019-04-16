package com.example.android.simplealarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.arch.persistence.room.Database;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Toast;

import com.example.android.simplealarm.adapters.AlarmAdapter;
import com.example.android.simplealarm.database.AlarmEntry;
import com.example.android.simplealarm.database.AppDatabase;
import com.example.android.simplealarm.utilities.NotificationUtils;

import java.util.Calendar;
import java.util.List;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String ALARM_BROADCAST_ID = "alarm_broadcast_id";
    private static final String ALARM_ENTRY_ID = "alarm_entry_id";

    private static MediaPlayer mediaPlayer;

    /**
     * Default Constructor
     */
    public AlarmReceiver() {}

    public AlarmReceiver(Context context, String time, int alarmEntryId) {
        // Create a pending intent for the alarm
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(ALARM_ENTRY_ID, alarmEntryId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, alarmEntryId,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Determine time until alarm
        String[] hoursAndMinutes = time.split(":");
        String hoursString = hoursAndMinutes[0];
        String minutesString = hoursAndMinutes[1];
        int hours = Integer.parseInt(hoursString);
        int minutes = Integer.parseInt(minutesString);
        /*
        // Alternate way of calculating time until alarm
        long alarmTimeInMillis = hours * 3600000 + minutes * 60000;

        // Calculate time from midnight in milliseconds
        Calendar c = Calendar.getInstance();
        long currentTimeInMillis = c.getTimeInMillis();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long timePassedSinceMidnightInMillis = currentTimeInMillis - c.getTimeInMillis();

        // Calculate time until alarm in seconds
        long timeUntilAlarmInMillis = alarmTimeInMillis - timePassedSinceMidnightInMillis;
        if (timeUntilAlarmInMillis < 0) {
            timeUntilAlarmInMillis = 24 * 3600000 + timeUntilAlarmInMillis;
        }
        int timeUntilAlarmInSeconds = (int) (timeUntilAlarmInMillis / 1000);*/

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, minutes);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    /**
     * Do code when broadcast receiver receives.
     *
     * @param context
     *
     * @param intent
     */
    @Override
    public void onReceive(final Context context, Intent intent) {
        Toast.makeText(context, R.string.alarm_sounding_message, Toast.LENGTH_LONG).show();
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
        if (intent != null && intent.hasExtra(ALARM_ENTRY_ID)) {
            final int itemIndex = intent.getIntExtra(ALARM_ENTRY_ID, 0);
            Log.i("test", "id intent was:" + itemIndex);

            final AppDatabase mDb = AppDatabase.getInstance(context.getApplicationContext());

            AppExecutors.getsInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
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

    public static void cancelAlarm(Context context, int broadcastId) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent.getBroadcast(context, broadcastId, intent, PendingIntent.FLAG_UPDATE_CURRENT).cancel();
    }

    public static void stopAlarm() {
        mediaPlayer.stop();
        mediaPlayer.release();
    }
}
