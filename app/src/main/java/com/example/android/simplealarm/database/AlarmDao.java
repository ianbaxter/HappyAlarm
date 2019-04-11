package com.example.android.simplealarm.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface AlarmDao {

    @Query("SELECT * FROM alarm")
    LiveData<List<AlarmEntry>> loadAllAlarms();

    @Insert
    void insertAlarm(AlarmEntry alarmEntry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateAlarm(AlarmEntry alarmEntry);

    @Delete
    void deleteAlarm(AlarmEntry alarmEntry);

    @Query("SELECT * FROM alarm WHERE id = :id")
    AlarmEntry loadAlarmById(int id);
}
