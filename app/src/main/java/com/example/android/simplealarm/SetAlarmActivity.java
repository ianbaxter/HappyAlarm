package com.example.android.simplealarm;

/**
 * this class has not been used, can incorporate later.
 */

import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SetAlarmActivity extends AppCompatActivity {

    private static final String TIME_PICKER_FRAGMENT_ID = "timePicker";

    public static Button alarmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_alarm);

        alarmButton = (Button) findViewById(R.id.set_alarm_text_view);
    }

    public void showTimePickerDialog(View view) {
        DialogFragment newFragment = new SetTimeFragment();
        newFragment.show(getSupportFragmentManager(), TIME_PICKER_FRAGMENT_ID);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        String time = alarmButton.getText().toString();

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(Intent.EXTRA_TEXT, time);

        startActivity(intent);
    }
}
