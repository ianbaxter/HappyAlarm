package com.example.android.simplealarm.utilities;

import android.app.Activity;
import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import com.example.android.simplealarm.R;
import com.example.android.simplealarm.database.AlarmEntry;
import com.google.android.material.snackbar.Snackbar;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AlarmUtils {

    public static long convertOneTimeAlarmTimeToMillis(String alarmEntryTime) {
        String[] alarmTime = alarmEntryTime.split(":");
        String alarmTimeHours = alarmTime[0];
        String alarmTimeMinutes = alarmTime[1];

        Calendar calendar = Calendar.getInstance(Locale.UK);
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(alarmTimeHours));
        calendar.set(Calendar.MINUTE, Integer.parseInt(alarmTimeMinutes));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + 1);
        }

        return calendar.getTimeInMillis();
    }

    public static long convertRepeatingAlarmTimeToMillis(AlarmEntry alarmEntry, Calendar calender) {
        String[] alarmTime = alarmEntry.getTime().split(":");
        String alarmTimeHours = alarmTime[0];
        String alarmTimeMinutes = alarmTime[1];

        calender.setFirstDayOfWeek(Calendar.MONDAY);
        calender.set(Calendar.HOUR_OF_DAY, Integer.parseInt(alarmTimeHours));
        calender.set(Calendar.MINUTE, Integer.parseInt(alarmTimeMinutes));
        calender.set(Calendar.SECOND, 0);
        calender.set(Calendar.MILLISECOND, 0);
        return calender.getTimeInMillis();
    }

    public static void showTimeUntilAlarmSnack(Context context, AlarmEntry alarmEntry) {
        RecyclerView recyclerView = ((Activity)context).findViewById(R.id.recycler_view_main);
        String alarmTimeString = alarmEntry.getTime();

        long alarmTimeInMillis = alarmEntry.getAlarmTimeInMillis();
        long currentTimeInMillis = System.currentTimeMillis();
        long timeDifferenceInMillis = alarmTimeInMillis - currentTimeInMillis;
        long twentyFourHoursInMillis = 1000 * 60 * 60 * 24;
        long timeDifferenceInDays = timeDifferenceInMillis / twentyFourHoursInMillis;

        String timeUntilAlarm = getTimeUntilAlarm(alarmTimeString);
        String[] parts = timeUntilAlarm.split(":");
        String hours = parts[0];
        String minutes = parts[1];

        if (timeDifferenceInMillis < twentyFourHoursInMillis) {
            if (hours.equals("00")) {
                if (minutes.substring(0, 1).equals("0")) {
                    minutes = minutes.substring(1, 2);
                }
                if (minutes.equals("0")) {
                    minutes = "1";
                    Snackbar.make(recyclerView, context.getString(R.string.alarm_set_message_less_than_minute, minutes), Snackbar.LENGTH_LONG).show();
                } else if (minutes.equals("1")) {
                    Snackbar.make(recyclerView, context.getString(R.string.alarm_set_message_minute, minutes), Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(recyclerView, context.getString(R.string.alarm_set_message_minutes, minutes), Snackbar.LENGTH_LONG).show();
                }
            } else if (minutes.equals("00")) {
                if (hours.substring(0, 1).equals("0")) {
                    hours = hours.substring(1, 2);
                }

                if (hours.equals("1")) {
                    Snackbar.make(recyclerView, context.getString(R.string.alarm_set_message_hour, hours), Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(recyclerView, context.getString(R.string.alarm_set_message_hours, hours), Snackbar.LENGTH_LONG).show();
                }
            } else {
                if (hours.substring(0, 1).equals("0")) {
                    hours = hours.substring(1, 2);
                }
                if (minutes.substring(0, 1).equals("0")) {
                    minutes = minutes.substring(1, 2);
                }

                if (hours.equals("1") && minutes.equals("1")) {
                    Snackbar.make(recyclerView, context.getString(R.string.alarm_set_message_hour_minute, hours, minutes), Snackbar.LENGTH_LONG).show();
                } else if (hours.equals("1")) {
                    Snackbar.make(recyclerView, context.getString(R.string.alarm_set_message_hour_minutes, hours, minutes), Snackbar.LENGTH_LONG).show();
                } else if (minutes.equals("1")) {
                    Snackbar.make(recyclerView, context.getString(R.string.alarm_set_message_hours_minute, hours, minutes), Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(recyclerView, context.getString(R.string.alarm_set_message_hours_minutes, hours, minutes), Snackbar.LENGTH_LONG).show();
                }
            }
        } else if (timeDifferenceInDays >= 1 && timeDifferenceInDays < 2) {
            Snackbar.make(recyclerView, context.getString(R.string.alarm_set_message_1_day, "1"), Snackbar.LENGTH_LONG).show();
        } else if (timeDifferenceInDays >= 2 && timeDifferenceInDays < 3) {
            Snackbar.make(recyclerView, context.getString(R.string.alarm_set_message_multiple_days, "2"), Snackbar.LENGTH_LONG).show();
        } else if (timeDifferenceInDays >= 3 && timeDifferenceInDays < 4) {
            Snackbar.make(recyclerView, context.getString(R.string.alarm_set_message_multiple_days, "3"), Snackbar.LENGTH_LONG).show();
        } else if (timeDifferenceInDays >= 4 && timeDifferenceInDays < 5) {
            Snackbar.make(recyclerView, context.getString(R.string.alarm_set_message_multiple_days, "4"), Snackbar.LENGTH_LONG).show();
        } else if (timeDifferenceInDays >= 5 && timeDifferenceInDays < 6) {
            Snackbar.make(recyclerView, context.getString(R.string.alarm_set_message_multiple_days, "5"), Snackbar.LENGTH_LONG).show();
        } else if (timeDifferenceInDays >= 6 && timeDifferenceInDays < 7) {
            Snackbar.make(recyclerView, context.getString(R.string.alarm_set_message_multiple_days, "6"), Snackbar.LENGTH_LONG).show();
        }
    }

    private static String getTimeUntilAlarm(String alarmTime) {
        Calendar calendar = Calendar.getInstance(Locale.UK);
        long timeNowInMillis = calendar.getTimeInMillis();
        long alarmTimeInMillis = convertOneTimeAlarmTimeToMillis(alarmTime);
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
            Calendar calendar = Calendar.getInstance(Locale.UK);
            long timeNowInMillis = calendar.getTimeInMillis();
            calendar.add(Calendar.MINUTE, additionalMinutes);

            long newAlarmTimeInMillis = calendar.getTimeInMillis();
            long timeUntilAlarmInMillis = newAlarmTimeInMillis - timeNowInMillis;

            return String.format(Locale.getDefault(), "%02d:%02d", TimeUnit.MILLISECONDS.toHours(timeUntilAlarmInMillis),
                    TimeUnit.MILLISECONDS.toMinutes(timeUntilAlarmInMillis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeUntilAlarmInMillis)));
        }
    }
}
