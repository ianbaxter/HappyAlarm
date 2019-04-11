package com.example.android.simplealarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.widget.Toast;

import com.example.android.simplealarm.utilities.NotificationUtils;

import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {

    private static MediaPlayer mediaPlayer;

    /**
     * Default Constructor
     */
    public AlarmReceiver() {}

    public AlarmReceiver(Context context, String time, int broadcastId) {
        // Create a pending intent for the alarm
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, broadcastId,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Determine time until alarm
        String[] hoursAndMinutes = time.split(":");
        String hoursString = hoursAndMinutes[0];
        String minutesString = hoursAndMinutes[1];
        int hours = Integer.parseInt(hoursString);
        int minutes = Integer.parseInt(minutesString);

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
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, R.string.alarm_sounding_message, Toast.LENGTH_LONG).show();
        NotificationUtils.alarmSoundingNotification(context);

        mediaPlayer = MediaPlayer.create(context, R.raw.alarm1);
        mediaPlayer.setLooping(false);
        mediaPlayer.start();

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.release();
            }
        });
    }

    public void cancelAlarm(Context context, int broadcastId) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent.getBroadcast(context, broadcastId, intent, PendingIntent.FLAG_UPDATE_CURRENT).cancel();
    }

    public static void stopAlarm() {
        mediaPlayer.stop();
        mediaPlayer.release();
    }
}
