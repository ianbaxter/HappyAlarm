package com.birdbathapps.HappyAlarmClock;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.widget.TimePicker;

import java.time.LocalTime;
import java.util.Calendar;
import java.util.Locale;

public class SetTimeFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    private int alarmEntryId;
    private boolean newAlarm = true;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        int hour;
        int minute;

        if (getArguments() != null && getArguments().containsKey(MainActivity.CLICKED_ALARM_ID_KEY)
                && getArguments().containsKey(MainActivity.CLICKED_ALARM_TIME_KEY)) {
            newAlarm = false;
            alarmEntryId = getArguments().getInt(MainActivity.CLICKED_ALARM_ID_KEY);
            String currentAlarmTime = getArguments().getString(MainActivity.CLICKED_ALARM_TIME_KEY);
            String[] hoursAndMinutes = new String[0];
            if (currentAlarmTime != null) {
                hoursAndMinutes = currentAlarmTime.split(":");
            }

            hour = getAlarmHour(hoursAndMinutes);
            minute = getAlarmMinute(hoursAndMinutes);
        } else {
            hour = getCurrentHour();
            minute = getCurrentMinute();
        }

        return new TimePickerDialog(getActivity(),this, hour, minute, true);
    }

    private int getAlarmHour(String[] hoursAndMinutes) {
        String hoursString = hoursAndMinutes[0];
        return Integer.parseInt(hoursString);
    }

    private int getAlarmMinute(String[] hoursAndMinutes) {
        String minutesString = hoursAndMinutes[1];
        return Integer.parseInt(minutesString);
    }

    private int getCurrentHour() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalTime currentTime = LocalTime.now();
            return currentTime.getHour();
        } else {
            Calendar calendar = Calendar.getInstance();
            return calendar.get(Calendar.HOUR_OF_DAY);
        }
    }

    private int getCurrentMinute() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalTime currentTime = LocalTime.now();
            return currentTime.getMinute();
        } else {
            Calendar calendar = Calendar.getInstance();
            return calendar.get(Calendar.MINUTE);
        }
    }

    @Override
    public void onTimeSet(TimePicker view, int hour, int minute) {
        String time24hrFormatted = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);

        TimeDialogListener listener = (TimeDialogListener) getActivity();
        if (listener != null) {
            if (newAlarm) {
                listener.onFinishNewAlarm(time24hrFormatted);
            } else {
                listener.onFinishUpdateAlarm(time24hrFormatted, alarmEntryId);
            }
        }
    }

    public interface TimeDialogListener {
        void onFinishNewAlarm(String time);
        void onFinishUpdateAlarm(String time, int alarmEntryId);
    }
}
