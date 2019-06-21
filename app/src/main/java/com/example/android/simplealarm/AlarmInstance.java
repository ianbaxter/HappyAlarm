package com.example.android.simplealarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.android.simplealarm.database.AlarmEntry;
import com.example.android.simplealarm.utilities.AlarmUtils;

import java.util.Calendar;
import java.util.Locale;

public class AlarmInstance {

    private static final String ALARM_ENTRY_ID_KEY = "alarm_entry_id";
    private static final String CURRENT_ALARM_TIME_IN_MILLIS_KEY = "current_alarm_time_in_millis";

    public AlarmInstance(Context context, AlarmEntry alarmEntry) {

        if (alarmEntry.isAlarmRepeating()) {
            Calendar alarmCalender = Calendar.getInstance(Locale.UK);
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            long previousAlarmTimeInMillis = sharedPreferences.getLong(CURRENT_ALARM_TIME_IN_MILLIS_KEY, 0);
            long alarmTimeInMillis = getRepeatingAlarmTimeInMillis(alarmEntry, alarmCalender);

            if (alarmTimeInMillis != previousAlarmTimeInMillis) {
                setRepeatingAlarm(context, alarmEntry, alarmTimeInMillis);
            }
        } else {
            setOneTimeAlarm(context, alarmEntry);
        }
    }

    private long getRepeatingAlarmTimeInMillis(AlarmEntry alarmEntry, Calendar alarmCalender) {
        Long timeWhenAlarmSet = alarmCalender.getTimeInMillis();
        setCalenderDayOfWeek(alarmCalender, alarmEntry, timeWhenAlarmSet);

        long alarmTimeInMillis = alarmCalender.getTimeInMillis();
        if (alarmTimeInMillis < timeWhenAlarmSet) {
            // Alarm is set to repeat on previous day of week, correct time to the following week
            int oneWeekInMilliseconds = 1000 * 60 * 60 * 24 * 7;
            alarmTimeInMillis += oneWeekInMilliseconds;
        }
        return alarmTimeInMillis;
    }

    private void setCalenderDayOfWeek(Calendar alarmCalender, AlarmEntry alarmEntry, Long timeWhenAlarmSet) {
        int currentDayOfWeek = getCurrentDayOfWeek(alarmCalender);
        boolean[] daysRepeating = alarmEntry.getDaysRepeating();
        boolean isAlarmTimeLaterToday = true;
        Long alarmTimeInMillisToday = AlarmUtils.convertRepeatingAlarmTimeToMillis(alarmEntry, alarmCalender);
        if (alarmTimeInMillisToday < timeWhenAlarmSet) {
            isAlarmTimeLaterToday = false;
        }

        switch (currentDayOfWeek) {
            case 0:
                if (daysRepeating[0] && isAlarmTimeLaterToday) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                } else if (daysRepeating[1]) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
                } else if (daysRepeating[2]) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
                } else if (daysRepeating[3]) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
                } else if (daysRepeating[4]) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
                } else if (daysRepeating[5]) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
                } else if (daysRepeating[6]) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                } else if (daysRepeating[0]) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                }
                break;
            case 1:
                if (daysRepeating[1] && isAlarmTimeLaterToday) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
                } else if (daysRepeating[2]) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
                } else if (daysRepeating[3]) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
                } else if (daysRepeating[4]) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
                } else if (daysRepeating[5]) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
                } else if (daysRepeating[6]) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                }else if (daysRepeating[0]) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                } else if (daysRepeating[1]) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
                }
                break;
            case 2:
                 if (daysRepeating[2] && isAlarmTimeLaterToday) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
                } else if (daysRepeating[3]) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
                } else if (daysRepeating[4]) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
                } else if (daysRepeating[5]) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
                } else if (daysRepeating[6]) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                }else if (daysRepeating[0]) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                } else if (daysRepeating[1]) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
                } else if (daysRepeating[2]) {
                     alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
                 }
                break;
            case 3:
                 if (daysRepeating[3] && isAlarmTimeLaterToday) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
                } else if (daysRepeating[4]) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
                } else if (daysRepeating[5]) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
                } else if (daysRepeating[6]) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                }else if (daysRepeating[0]) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                } else if (daysRepeating[1]) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
                } else if (daysRepeating[2]) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
                } else if (daysRepeating[3]) {
                     alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
                 }
                break;
            case 4:
                if (daysRepeating[4] && isAlarmTimeLaterToday) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
                } else if (daysRepeating[5]) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
                } else if (daysRepeating[6]) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                }else if (daysRepeating[0]) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                } else if (daysRepeating[1]) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
                } else if (daysRepeating[2]) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
                } else if (daysRepeating[3]) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
                } else if (daysRepeating[4]) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
                }
                break;
            case 5:
                if (daysRepeating[5] && isAlarmTimeLaterToday) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
                } else if (daysRepeating[6]) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                }else if (daysRepeating[0]) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                } else if (daysRepeating[1]) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
                } else if (daysRepeating[2]) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
                } else if (daysRepeating[3]) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
                } else if (daysRepeating[4]) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
                } else if (daysRepeating[5]) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
                }
                break;
            case 6:
                if (daysRepeating[6] && isAlarmTimeLaterToday) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                }else if (daysRepeating[0]) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                } else if (daysRepeating[1]) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
                } else if (daysRepeating[2]) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
                } else if (daysRepeating[3]) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
                } else if (daysRepeating[4]) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
                } else if (daysRepeating[5]) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
                } else if (daysRepeating[6]) {
                    alarmCalender.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                }
                break;
        }
    }

    private int getCurrentDayOfWeek(Calendar alarmCalender) {
        int currentDayOfWeek = alarmCalender.get(Calendar.DAY_OF_WEEK);
        int adjustedCurrentDayOfWeek = 0;
        switch (currentDayOfWeek) {
            case 1:
                adjustedCurrentDayOfWeek = 6;
                break;
            case 2:
                adjustedCurrentDayOfWeek = 0;
                break;
            case 3:
                adjustedCurrentDayOfWeek = 1;
                break;
            case 4:
                adjustedCurrentDayOfWeek = 2;
                break;
            case 5:
                adjustedCurrentDayOfWeek = 3;
                break;
            case 6:
                adjustedCurrentDayOfWeek = 4;
                break;
            case 7:
                adjustedCurrentDayOfWeek = 5;
                break;
        }
        return adjustedCurrentDayOfWeek;
    }

    private void setRepeatingAlarm(Context context, AlarmEntry alarmEntry, long alarmTimeInMillis) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putLong(CURRENT_ALARM_TIME_IN_MILLIS_KEY, alarmTimeInMillis).apply();

        PendingIntent alarmReceiverPendingIntent = getAlarmReceiverPendingIntent(context, alarmEntry);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(alarmTimeInMillis, null);
        alarmManager.setAlarmClock(alarmClockInfo, alarmReceiverPendingIntent);

        createTimeUntilAlarmSnackBar(context, alarmEntry);
    }

    private PendingIntent getAlarmReceiverPendingIntent(Context context, AlarmEntry alarmEntry) {
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        int alarmEntryId = alarmEntry.getId();
        alarmIntent.putExtra(ALARM_ENTRY_ID_KEY, alarmEntryId);
        return PendingIntent.getBroadcast(context, alarmEntryId, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void createTimeUntilAlarmSnackBar(Context context, AlarmEntry alarmEntry) {
        if (context instanceof MainActivity) {
            String alarmTimeString = alarmEntry.getTime();
            AlarmUtils.showTimeUntilAlarmSnack(context, alarmTimeString);
        }
    }

    private void setOneTimeAlarm(Context context, AlarmEntry alarmEntry) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        long previousAlarmTimeInMillis = sharedPreferences.getLong(CURRENT_ALARM_TIME_IN_MILLIS_KEY, 0);
        long alarmTimeInMillis = AlarmUtils.convertOneTimeAlarmTimeToMillis(alarmEntry.getTime());

        if (alarmTimeInMillis != previousAlarmTimeInMillis) {
            sharedPreferences.edit().putLong(CURRENT_ALARM_TIME_IN_MILLIS_KEY, alarmTimeInMillis).apply();

            PendingIntent alarmReceiverPendingIntent = getAlarmReceiverPendingIntent(context, alarmEntry);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(alarmTimeInMillis, null);
            alarmManager.setAlarmClock(alarmClockInfo, alarmReceiverPendingIntent);

            createTimeUntilAlarmSnackBar(context, alarmEntry);
        }
    }

    public static void cancelAlarm(Context context, int broadcastId) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, broadcastId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}
