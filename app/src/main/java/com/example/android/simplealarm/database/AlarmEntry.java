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
    private boolean alarmIsRepeating;

    @Ignore
    public AlarmEntry(String time, boolean alarmIsOn, boolean alarmIsRepeating) {
        this.time = time;
        this.alarmIsOn = alarmIsOn;
        this.alarmIsRepeating = alarmIsRepeating;

    }

    public AlarmEntry(int id, String time, boolean alarmIsOn, boolean alarmIsRepeating) {
        this.id = id;
        this.time = time;
        this.alarmIsOn = alarmIsOn;
        this.alarmIsRepeating = alarmIsRepeating;
    }

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public String getTime() { return time; }

    public void setTime(String time) { this.time = time; }

    public boolean getAlarmIsOn() { return alarmIsOn; }

    public void setAlarmIsOn(boolean alarmIsOn) { this.alarmIsOn = alarmIsOn; }

    public boolean getAlarmIsRepeating() { return alarmIsRepeating; }

    public void setAlarmIsRepeating(boolean alarmIsRepeating) { this.alarmIsRepeating = alarmIsRepeating; }
}
