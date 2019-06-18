package com.example.android.simplealarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.android.simplealarm.adapters.AlarmAdapter;
import com.example.android.simplealarm.database.AlarmEntry;
import com.example.android.simplealarm.database.AppDatabase;
import com.example.android.simplealarm.utilities.AlarmUtils;
import com.example.android.simplealarm.utilities.NotificationUtils;

import java.io.IOException;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = AlarmReceiver.class.getSimpleName();

    private static final String ALARM_ENTRY_ID_KEY = "alarm_entry_id";
    private static final String ALARM_SNOOZED_NOTIFICATION_KEY = "alarm_snoozed_notification";
    private static final String ALARM_DISMISSED_NOTIFICATION_KEY = "alarm_dismissed_notification";

    protected static MediaPlayer mediaPlayer;
    private static Vibrator vibrator;
    private static CountDownTimer countDownTimer;

    private static boolean isVibrating = false;

    public AlarmReceiver() {}

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (intent.getExtras() != null && intent.hasExtra(ALARM_ENTRY_ID_KEY)) {
            if (intent.hasExtra(ALARM_SNOOZED_NOTIFICATION_KEY)) {
                stopAlarm(context);
                snoozeAlarm(context, intent);
            } else if (intent.hasExtra(ALARM_DISMISSED_NOTIFICATION_KEY)) {

                int alarmEntryId = intent.getIntExtra(ALARM_ENTRY_ID_KEY, 0);
                AlarmInstance.cancelAlarm(context, alarmEntryId);
                NotificationUtils.clearAllNotifications(context);
                checkIfRepeating(context, intent);
            } else {
                triggerAlarm(context, intent);
                checkIfRepeating(context, intent);
            }
        }
    }

    public static void stopAlarm(Context context) {
        if (isVibrating) {
            vibrator.cancel();
            isVibrating = false;
        }
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        NotificationUtils.clearAllNotifications(context);
    }

    private void snoozeAlarm(Context context, Intent intent) {
        AppExecutors.getsInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                int alarmEntryId = intent.getIntExtra(ALARM_ENTRY_ID_KEY, 0);
                AppDatabase mDb = AppDatabase.getInstance(context.getApplicationContext());
                AlarmEntry alarmEntry = mDb.alarmDao().loadAlarmById(alarmEntryId);
                alarmEntry.setAlarmSnoozed(true);
                mDb.alarmDao().updateAlarm(alarmEntry);
                snooze(alarmEntry);
            }

            private void snooze(AlarmEntry alarmEntry) {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                int snoozeTime = Integer.parseInt(sharedPreferences.getString(context.getString(R.string.pref_snooze_time_key), "5"));
                String snoozedAlarmTime = AlarmUtils.snoozeAlarmTime(snoozeTime);
                alarmEntry.setTime(snoozedAlarmTime);
                new AlarmInstance(context, alarmEntry);
                NotificationUtils.snoozeAlarmNotification(context, alarmEntry.getId());
            }
        });
    }

    private void checkIfRepeating(Context context, Intent intent) {
        AppExecutors.getsInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                int alarmEntryId = intent.getIntExtra(ALARM_ENTRY_ID_KEY, 0);
                AppDatabase mDb = AppDatabase.getInstance(context.getApplicationContext());
                AlarmEntry alarmEntry = mDb.alarmDao().loadAlarmById(alarmEntryId);
                alarmEntry.setAlarmSnoozed(false);
                boolean isAlarmRepeating = alarmEntry.isAlarmRepeating();
                if (isAlarmRepeating) {
                    repeatAlarm(alarmEntry);
                } else {
                    setAlarmOffIfCurrentlyOn(alarmEntry);
                }
                mDb.alarmDao().updateAlarm(alarmEntry);
            }

            private void repeatAlarm(AlarmEntry alarmEntry) {
                String currentAlarmTime = alarmEntry.getTime();
                String newAlarmTime = AlarmUtils.newAlarmTime(currentAlarmTime, 24);
                alarmEntry.setTime(newAlarmTime);
                new AlarmInstance(context, alarmEntry);
            }

            private void setAlarmOffIfCurrentlyOn(AlarmEntry alarmEntry) {
                if (alarmEntry.isAlarmOn()) {
                    alarmEntry.setAlarmOn(false);
                }
            }
        });
    }

    private void triggerAlarm(Context context, Intent intent) {
        int alarmEntryId = intent.getIntExtra(ALARM_ENTRY_ID_KEY, 0);
        NotificationUtils.alarmTriggeredNotification(context, alarmEntryId);
        startAlarmAudio(context, alarmEntryId);
    }

    private void startAlarmAudio(final Context context, int alarmEntryId) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        int alarmMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        int alarmVolume = sharedPreferences.getInt(context.getString(R.string.pref_volume_key),alarmMaxVolume/2);
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, alarmVolume,
                AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

        createMediaPlayer(context, audioAttributes, alarmEntryId);
        startAutoSilentTimer(context);
    }

    private void createMediaPlayer(Context context, AudioAttributes audioAttributes, int alarmEntryId) {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(audioAttributes);
        mediaPlayer.setLooping(true);

        AppExecutors.getsInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                AppDatabase mDb = AppDatabase.getInstance(context);
                AlarmEntry alarmEntry = mDb.alarmDao().loadAlarmById(alarmEntryId);
                try {
                    String ringtonePath = alarmEntry.getRingtonePath();
                    mediaPlayer.setDataSource(context,
                            Uri.parse(ringtonePath));
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    vibrate(context);
                } catch (IOException e) {
                    Log.d(TAG, "IOException: " + e);
                }
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if (isVibrating) {
                    vibrator.cancel();
                    isVibrating = false;
                }
                mediaPlayer.release();
                NotificationUtils.clearAllNotifications(context);
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                }
            }
        });
    }

    private void vibrate(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        // Check if vibrate is on in shared preferences
        if (sharedPreferences.getBoolean(context.getString(R.string.pref_vibrate_key),
                context.getResources().getBoolean(R.bool.pref_vibrate_default))) {
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                long[] vibrateTimings = {0, 400, 500, 400, 1200};
                vibrator.vibrate(VibrationEffect.createWaveform(vibrateTimings, 0));
            } else {
                vibrator.vibrate(500);
            }
            isVibrating = true;
        }
    }

    private void startAutoSilentTimer(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int countdownTime = Integer.parseInt(sharedPreferences.getString(context.getString(R.string.pref_silence_time_key), "5"));
        int countdownTimeInMillis = countdownTime * 1000 * 60;

        countDownTimer = new CountDownTimer(countdownTimeInMillis, countdownTimeInMillis) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Do nothing
            }
            @Override
            public void onFinish() {
                if (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying()) {
                        countDownTimer = null;
                        stopAlarm(context);
                    }
                }
            }
        };
        countDownTimer.start();
    }
}
