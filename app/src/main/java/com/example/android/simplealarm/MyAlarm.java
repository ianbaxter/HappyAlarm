package com.example.android.simplealarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.Calendar;

public class MyAlarm extends BroadcastReceiver {

    private static final String ALARM_BUNDLE = "alarmBundle";

    private PendingIntent pendingIntent;
    private AlarmManager alarmManager;
    private static MediaPlayer mediaPlayer;

    protected static boolean alarmRinging = false;

    /**
     * Default Constructor
     */
    public  MyAlarm() {}

    public MyAlarm(Context context, Bundle extras, int timeoutInSeconds) {
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, MyAlarm.class);
        intent.putExtra(ALARM_BUNDLE, extras);
        pendingIntent = PendingIntent.getBroadcast(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(System.currentTimeMillis());
        time.add(Calendar.SECOND, timeoutInSeconds);

        alarmManager.set(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), pendingIntent);
    }

    /**
     * Do code when broadcast receiver receives.
     *
     * *ISSUE* - due to use of member variables within onReceive, the alarm is not triggered if the app is destroyed.
     *
     * @param context
     *
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, R.string.alarm_sounding_message, Toast.LENGTH_LONG).show();

        mediaPlayer = MediaPlayer.create(context, R.raw.balance_of_power);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
        alarmRinging = true;

        MainActivity.mAlarmOff.setVisibility(View.VISIBLE);

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mediaPlayer.release();
                MainActivity.mAlarmClock.setBackgroundResource(R.drawable.clock_off);
                MainActivity.mAlarmOff.setVisibility(View.INVISIBLE);
                alarmRinging = false;
                MainActivity.alarmIsOn = false;
            }
        });
    }

    public void stopAlarm() {
        mediaPlayer.stop();
        mediaPlayer.release();
        MainActivity.mAlarmClock.setBackgroundResource(R.drawable.clock_off);
        MainActivity.mAlarmOff.setVisibility(View.INVISIBLE);
        alarmRinging = false;
        MainActivity.alarmIsOn = false;
    }

    public void cancel() {
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}
