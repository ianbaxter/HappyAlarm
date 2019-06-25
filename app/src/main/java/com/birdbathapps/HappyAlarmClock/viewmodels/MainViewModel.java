package com.birdbathapps.HappyAlarmClock.viewmodels;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.annotation.NonNull;

import com.birdbathapps.HappyAlarmClock.database.AlarmEntry;
import com.birdbathapps.HappyAlarmClock.database.AppDatabase;

import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private LiveData<List<AlarmEntry>> alarms;

    public MainViewModel(@NonNull Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(this.getApplication());
        alarms = database.alarmDao().loadAllAlarms();
    }

    public LiveData<List<AlarmEntry>> getAlarms() {
        return alarms;
    }
}
