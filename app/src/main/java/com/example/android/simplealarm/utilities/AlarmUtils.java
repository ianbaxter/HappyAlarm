package com.example.android.simplealarm.utilities;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.simplealarm.R;
import com.google.android.material.snackbar.Snackbar;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AlarmUtils {

    public static long convertAlarmTimeToMillis(String alarmEntryTime) {
        String[] hoursAndMinutes = alarmEntryTime.split(":");
        String hoursString = hoursAndMinutes[0];
        String minutesString = hoursAndMinutes[1];
        int hour = Integer.parseInt(hoursString);
        int minute = Integer.parseInt(minutesString);

        Calendar calendarAtAlarmTime = getCalendarAtAlarmTime(hour, minute);

        return calendarAtAlarmTime.getTimeInMillis();
    }

    @NonNull
    private static Calendar getCalendarAtAlarmTime(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
        }
        return calendar;
    }

    public static String timeUntilAlarmFormatter(String alarmTime) {
        Calendar calendar = Calendar.getInstance();
        long timeNowInMillis = calendar.getTimeInMillis();
        long alarmTimeInMillis = convertAlarmTimeToMillis(alarmTime);
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
            Calendar calendar = Calendar.getInstance();
            long timeNowInMillis = calendar.getTimeInMillis();
            calendar.add(Calendar.MINUTE, additionalMinutes);

            long newAlarmTimeInMillis = calendar.getTimeInMillis();
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

    public static void showTimeUntilAlarmSnack(Context context, String timeUntilAlarm) {
        RecyclerView recyclerView = ((Activity)context).findViewById(R.id.recycler_view_main);
        String[] parts = timeUntilAlarm.split(":");
        String hours = parts[0];
        String minutes = parts[1];
        if (hours.equals("00")) {
            if (minutes.substring(0, 1).equals("0")) {
                minutes = minutes.substring(1, 2);
            }
            if (minutes.equals("0")) {
                minutes = "1";
                Snackbar.make(recyclerView, context.getString(R.string.alarm_set_message_less_than_minute, minutes), Snackbar.LENGTH_LONG).show();
            }
            else if (minutes.equals("1")) {
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
    }
}
