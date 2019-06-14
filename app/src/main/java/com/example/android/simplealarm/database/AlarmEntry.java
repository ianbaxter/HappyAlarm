package com.example.android.simplealarm.database;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "alarm")
public class AlarmEntry {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String time;
    private String ringtonePath;
    private boolean isAlarmOn;
    private boolean isAlarmRepeating;
    private boolean isAlarmSnoozed;

    @Ignore
    public AlarmEntry(String time, String ringtonePath, boolean isAlarmOn, boolean isAlarmRepeating, boolean isAlarmSnoozed) {
        this.time = time;
        this.ringtonePath = ringtonePath;
        this.isAlarmOn = isAlarmOn;
        this.isAlarmRepeating = isAlarmRepeating;
        this.isAlarmSnoozed = isAlarmSnoozed;

    }

    public AlarmEntry(int id, String time, String ringtonePath, boolean isAlarmOn, boolean isAlarmRepeating, boolean isAlarmSnoozed) {
        this.id = id;
        this.time = time;
        this.ringtonePath = ringtonePath;
        this.isAlarmOn = isAlarmOn;
        this.isAlarmRepeating = isAlarmRepeating;
        this.isAlarmSnoozed = isAlarmSnoozed;
    }

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public String getTime() { return time; }

    public void setTime(String time) { this.time = time; }

    public  String getRingtonePath() { return ringtonePath; }

    public void setRingtonePath(String ringtonePath) { this.ringtonePath = ringtonePath; }

    public boolean isAlarmOn() { return isAlarmOn; }

    public void setAlarmOn(boolean isAlarmOn) { this.isAlarmOn = isAlarmOn; }

    public boolean isAlarmRepeating() { return isAlarmRepeating; }

    public void setAlarmRepeating(boolean isAlarmRepeating) { this.isAlarmRepeating = isAlarmRepeating; }

    public boolean isAlarmSnoozed() { return isAlarmSnoozed; }

    public void setAlarmSnoozed(boolean isAlarmSnoozed) { this.isAlarmSnoozed = isAlarmSnoozed; }
}
