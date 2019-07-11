package com.birdbathapps.HappyAlarmClock.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import android.content.Context;

import timber.log.Timber;

@Database(entities = {AlarmEntry.class}, version = 1, exportSchema = false)

@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    private static final Object LOCK = new Object();
    private static final String DATABASENAME = "alarm_list";
    private static AppDatabase sInstance;

    public static AppDatabase getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                Timber.d("Creating new database instance");
                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                        AppDatabase.class, AppDatabase.DATABASENAME)
                        .build();
            }
        }
        Timber.d("Getting the database instance");
        return sInstance;
    }

    public abstract AlarmDao alarmDao();
}
