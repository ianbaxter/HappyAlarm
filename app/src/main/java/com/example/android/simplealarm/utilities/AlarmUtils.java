package com.example.android.simplealarm.utilities;

import android.util.Log;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AlarmUtils {

    public static long getAlarmTimeInMillis(String alarmEntryTime) {
        String[] hoursAndMinutes = alarmEntryTime.split(":");
        String hoursString = hoursAndMinutes[0];
        String minutesString = hoursAndMinutes[1];
        int hour = Integer.parseInt(hoursString);
        int minute = Integer.parseInt(minutesString);

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        if (c.getTimeInMillis() < System.currentTimeMillis()) {
            c.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH) + 1);
        }
        Log.i("test", "alarmTimeInMillis is: " + c.getTimeInMillis());
        return c.getTimeInMillis();
    }

    public static String timeUntilAlarmFormatter(String alarmTime) {
        Calendar calendar = Calendar.getInstance();
        long timeNowInMillis = calendar.getTimeInMillis();
        long alarmTimeInMillis = getAlarmTimeInMillis(alarmTime);
        long timeUntilAlarmInMillis = alarmTimeInMillis - timeNowInMillis;

        return String.format(Locale.getDefault(), "%02d:%02d", TimeUnit.MILLISECONDS.toHours(timeUntilAlarmInMillis),
                TimeUnit.MILLISECONDS.toMinutes(timeUntilAlarmInMillis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeUntilAlarmInMillis)));
    }

    public static String snoozeAlarmTime(int additionalMinutes) {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime snoozedAlarmTime = LocalTime.now().plusMinutes(additionalMinutes);
            return snoozedAlarmTime.format(dtf);

        } else {
            Calendar c = Calendar.getInstance();
            long timeNowInMillis = c.getTimeInMillis();
            c.add(Calendar.MINUTE, additionalMinutes);

            long newAlarmTimeInMillis = c.getTimeInMillis();
            long timeUntilAlarmInMillis = newAlarmTimeInMillis - timeNowInMillis;

            return String.format(Locale.getDefault(), "%02d:%02d", TimeUnit.MILLISECONDS.toHours(timeUntilAlarmInMillis),
                    TimeUnit.MILLISECONDS.toMinutes(timeUntilAlarmInMillis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeUntilAlarmInMillis)));
        }
    }

    public static String newAlarmTime(String alarmTime, int additionalHours) {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime newAlarmTime = LocalTime.parse(alarmTime, dtf).plusHours(additionalHours);
            return newAlarmTime.toString();

        } else {
            String[] hoursAndMinutes = alarmTime.split(":");
            String hoursString = hoursAndMinutes[0];
            int hour = Integer.parseInt(hoursString);

            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, hour + additionalHours);
            c.set(Calendar.SECOND, 0);

            Calendar calendar = Calendar.getInstance();
            long timeNowInMillis = calendar.getTimeInMillis();
            long snoozedAlarmTimeInMillis = c.getTimeInMillis();
            long timeUntilAlarmInMillis = snoozedAlarmTimeInMillis - timeNowInMillis;

            return String.format(Locale.getDefault(), "%02d:%02d", TimeUnit.MILLISECONDS.toHours(timeUntilAlarmInMillis),
                    TimeUnit.MILLISECONDS.toMinutes(timeUntilAlarmInMillis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeUntilAlarmInMillis)));
        }
    }
}


