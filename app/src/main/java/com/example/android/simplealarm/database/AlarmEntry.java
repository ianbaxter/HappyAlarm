package com.example.android.simplealarm.database;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "alarm")
public class AlarmEntry {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String time;
    private boolean isAlarmOn;
    private boolean isAlarmRepeating;

    @Ignore
    public AlarmEntry(String time, boolean isAlarmOn, boolean isAlarmRepeating) {
        this.time = time;
        this.isAlarmOn = isAlarmOn;
        this.isAlarmRepeating = isAlarmRepeating;

    }

    public AlarmEntry(int id, String time, boolean isAlarmOn, boolean isAlarmRepeating) {
        this.id = id;
        this.time = time;
        this.isAlarmOn = isAlarmOn;
        this.isAlarmRepeating = isAlarmRepeating;
    }

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public String getTime() { return time; }

    public void setTime(String time) { this.time = time; }

    public boolean isAlarmOn() { return isAlarmOn; }

    public void setAlarmOn(boolean isAlarmOn) { this.isAlarmOn = isAlarmOn; }

    public boolean isAlarmRepeating() { return isAlarmRepeating; }

    public void setAlarmRepeating(boolean isAlarmRepeating) { this.isAlarmRepeating = isAlarmRepeating; }
}
