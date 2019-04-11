package com.example.android.simplealarm.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "alarm")
public class AlarmEntry {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String time;
    private boolean alarmIsOn;

    @Ignore
    public AlarmEntry(String time, boolean alarmIsOn) {
        this.time = time;
        this.alarmIsOn = alarmIsOn;
    }

    public AlarmEntry(int id, String time, boolean alarmIsOn) {
        this.id = id;
        this.time = time;
        this.alarmIsOn = alarmIsOn;
    }

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public String getTime() { return time; }

    public void setTime(String time) { this.time = time; }

    public boolean getAlarmIsOn() { return alarmIsOn; }

    public void setAlarmIsOn(boolean alarmIsOn) { this.alarmIsOn = alarmIsOn; }
}
