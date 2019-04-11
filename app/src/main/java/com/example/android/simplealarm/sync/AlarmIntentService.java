package com.example.android.simplealarm.sync;

import android.app.IntentService;
import android.content.Intent;

public class AlarmIntentService extends IntentService {

    public AlarmIntentService() {
        super("AlarmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        AlarmTasks.executeTasks(this, action);
    }
}
