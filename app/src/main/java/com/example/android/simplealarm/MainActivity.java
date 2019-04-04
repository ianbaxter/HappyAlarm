package com.example.android.simplealarm;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TIME_PICKER_FRAGMENT_ID = "timePicker";
    private static final String LIFECYCLE_CALLBACKS_ALARM_STATE_KEY = "alarmState";
    private static final String LIFECYCLE_CALLBACKS_ALARM_TIME_KEY = "alarmTime";

    protected static Button mAlarmTime;
    protected static Button mAlarmClock;
    protected static Button mAlarmOff;

    protected static boolean alarmIsOn = false;
    protected static Toast mToast;
    private MyAlarm mAlarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAlarm = new MyAlarm();

        mAlarmTime = findViewById(R.id.alarmTime);
        mAlarmClock = findViewById(R.id.clock);
        mAlarmClock.setOnClickListener(this);
        mAlarmOff = findViewById(R.id.alarmOff);
        mAlarmOff.setOnClickListener(this);
        MainActivity.mAlarmOff.setVisibility(View.INVISIBLE);

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);

        String defaultAlarmTimeValue = getResources().getString(R.string.alarm_time_key);
        String savedAlarmTimeValue = sharedPref.getString(getString(R.string.alarm_time_key), defaultAlarmTimeValue);
        mAlarmTime.setText(savedAlarmTimeValue);

        boolean savedAlarmStateValue = sharedPref.getBoolean(getString(R.string.alarm_state_key), false);
        alarmIsOn = savedAlarmStateValue;

        /*if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(LIFECYCLE_CALLBACKS_ALARM_STATE_KEY)) {
                alarmIsOn = savedInstanceState.getBoolean(LIFECYCLE_CALLBACKS_ALARM_STATE_KEY);
            }
            if (savedInstanceState.containsKey(LIFECYCLE_CALLBACKS_ALARM_TIME_KEY)) {
                String previousLifecycleAlarmTimeCallbacks = savedInstanceState.getString(LIFECYCLE_CALLBACKS_ALARM_TIME_KEY);
                mAlarmTime.setText(previousLifecycleAlarmTimeCallbacks);
            }
        }*/

        showAlarm(alarmIsOn);
    }

    /*@Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(LIFECYCLE_CALLBACKS_ALARM_STATE_KEY, alarmIsOn);
        String lifecycleAlarmTime = mAlarmTime.getText().toString();
        outState.putString(LIFECYCLE_CALLBACKS_ALARM_TIME_KEY, lifecycleAlarmTime);
    }*/

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.alarm_time_key), mAlarmTime.getText().toString());
        editor.putBoolean(getString(R.string.alarm_state_key), alarmIsOn);
        editor.apply();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.clock:
                if (!alarmIsOn) {
                    mAlarmClock.setBackgroundResource(R.drawable.clock_on);
                    mToast.makeText(this, R.string.alarm_on_message, Toast.LENGTH_LONG).show();
                    alarmIsOn = true;

                    triggerAlarm();

                } else if (alarmIsOn) {
                    mAlarmClock.setBackgroundResource(R.drawable.clock_off);
                    mToast.makeText(this, R.string.alarm_off_message, Toast.LENGTH_LONG).show();
                    alarmIsOn = false;

                    mAlarm.cancel();
                }
                break;

            case R.id.alarmOff :
                if (mAlarm.alarmRinging) {
                    mToast.makeText(this, R.string.alarm_turned_off_message, Toast.LENGTH_LONG).show();
                    mAlarm.stopAlarm();
                }
                break;
        }
    }

    public void showTimePickerDialog(View view) {
        DialogFragment newFragment = new SetTimeFragment();
        newFragment.show(getSupportFragmentManager(), TIME_PICKER_FRAGMENT_ID);

        mToast.makeText(this, R.string.alarm_set_message, Toast.LENGTH_LONG).show();
    }

    private void showAlarm(boolean alarmState) {
        if (!alarmIsOn) {
            mAlarmClock.setBackgroundResource(R.drawable.clock_off);
        } else if (alarmIsOn) {
            mAlarmClock.setBackgroundResource(R.drawable.clock_on);
        }
    }

    private void triggerAlarm() {
        // Convert hours and minutes from time picker button to integer values and calculate time of alarm in milliseconds (from midnight)
        String alarmTime = mAlarmTime.getText().toString();
        String[] hoursAndMinutes = alarmTime.split(":");
        String hoursString = hoursAndMinutes[0];
        String minutesString = hoursAndMinutes[1];
        int hours = Integer.parseInt(hoursString);
        int minutes = Integer.parseInt(minutesString);
        long alarmTimeInMillis = hours * 3600000 + minutes * 60000;

        // Calculate time from midnight in milliseconds
        Calendar c = Calendar.getInstance();
        long currentTimeInMillis = c.getTimeInMillis();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long timePassedSinceMidnightInMillis = currentTimeInMillis - c.getTimeInMillis();

        // Calculate time until alarm in seconds
        long timeUntilAlarmInMillis = alarmTimeInMillis - timePassedSinceMidnightInMillis;
        if (timeUntilAlarmInMillis < 0) {
            timeUntilAlarmInMillis = 24 * 3600000 + timeUntilAlarmInMillis;
        }
        int timeUntilAlarmInSeconds = (int) (timeUntilAlarmInMillis / 1000);

        mAlarm = new MyAlarm(this, null, timeUntilAlarmInSeconds);
    }
}





