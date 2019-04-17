package com.example.android.simplealarm.utilities;

import java.util.Calendar;
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
        return c.getTimeInMillis();
    }

    public static String timeUntilAlarmFormatter(String alarmTime) {
        Calendar calendar = Calendar.getInstance();
        long timeNowInMillis = calendar.getTimeInMillis();
        long alarmTimeInMillis = getAlarmTimeInMillis(alarmTime);
        long timeUntilAlarmInMillis = alarmTimeInMillis - timeNowInMillis;

        String hm = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toHours(timeUntilAlarmInMillis),
                TimeUnit.MILLISECONDS.toMinutes(timeUntilAlarmInMillis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeUntilAlarmInMillis)));
        return hm;
    }
}
