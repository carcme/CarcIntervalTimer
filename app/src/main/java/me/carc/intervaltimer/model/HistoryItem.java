package me.carc.intervaltimer.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.Keep;

/**
 * Keep record of old workouts
 * Created by bamptonm on 01/02/2018.
 */

@Keep
@Entity(tableName = "HistoryItem")
public class HistoryItem {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "keyID")
    private int keyID;

    @ColumnInfo(name = "date")
    private String date;

    @ColumnInfo(name = "elaspedTime")
    private String elaspedTime;

    @ColumnInfo(name = "roundsCompleted")
    private int roundsCompleted;
    @ColumnInfo(name = "roundsTotal")
    private int roundsTotal;

    @ColumnInfo(name = "workTime")
    private String workTime;
    @ColumnInfo(name = "restTime")
    private String restTime;


    public HistoryItem(int keyID, String date, String elaspedTime, int roundsCompleted, int roundsTotal, String workTime, String restTime) {
        this.keyID = keyID;
        this.date = date;
        this.elaspedTime = elaspedTime;
        this.roundsCompleted = roundsCompleted;
        this.roundsTotal = roundsTotal;
        this.workTime = workTime;
        this.restTime = restTime;
    }

    public int getKeyID() { return keyID; }
    public void setKeyID(int keyID) { this.keyID = keyID; }

    public String getDate() { return date; }
    public void setDate(String dateMilli) { this.date = dateMilli; }

    public String getElaspedTime() { return elaspedTime; }
    public void setElaspedTime(String elaspedTime) { this.elaspedTime = elaspedTime; }

    public int getRoundsCompleted() { return roundsCompleted; }
    public void setRoundsCompleted(int roundsCompleted) { this.roundsCompleted = roundsCompleted; }

    public int getRoundsTotal() { return roundsTotal; }
    public void setRoundsTotal(int roundsTotal) { this.roundsTotal = roundsTotal; }

    public String getWorkTime() { return workTime; }
    public void setWorkTime(String workTime) { this.workTime = workTime; }

    public String getRestTime() { return restTime; }
    public void setRestTime(String restTime) { this.restTime = restTime;}
}
