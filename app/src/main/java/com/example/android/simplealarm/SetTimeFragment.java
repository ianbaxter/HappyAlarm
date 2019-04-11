package com.example.android.simplealarm;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.widget.TimePicker;

import java.util.Calendar;

public class SetTimeFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    // Member variable for the database
    private int clickedItemIndex;
    private boolean newAlarm = true;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        if (getArguments() != null && getArguments().containsKey("clickedItemIndex")) {
            clickedItemIndex = getArguments().getInt("clickedItemIndex");
            newAlarm = false;
        }

        return new TimePickerDialog(getActivity(),
                this, hour, minute, true);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        String time;
        if (minute < 10) {
            time = hourOfDay + ":0" + minute;
        } else {
            time = hourOfDay + ":" + minute;
        }

        TimeDialogListener listener = (TimeDialogListener) getActivity();
        if (listener != null) {
            if (newAlarm) {
                listener.onFinishNewTimeDialog(time);
            } else {
                listener.onFinishUpdateTimeDialog(time, clickedItemIndex);
            }
        }
    }

    public interface TimeDialogListener {
        void onFinishNewTimeDialog(String time);
        void onFinishUpdateTimeDialog(String time, int clickedItemIndex);
    }
}
