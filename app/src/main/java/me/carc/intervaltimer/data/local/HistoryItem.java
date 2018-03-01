package me.carc.intervaltimer.data.local;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Keep;

import java.util.ArrayList;
import java.util.List;

import me.carc.intervaltimer.data.local.TypeConverters.LatLongTypeConverters;
import me.carc.intervaltimer.model.LatLon;
import me.carc.intervaltimer.utils.MapUtils;

/**
 * Keep record of previous workouts
 * Created by bamptonm on 01/02/2018.
 */

@Keep
@Entity(tableName = "HistoryItem")
public class HistoryItem implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "keyID")
    private int keyID;

    @ColumnInfo(name = "date")
    private String date;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "timeRemaining")
    private String timeRemaining;
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

    @ColumnInfo(name = "locked")
    private boolean locked;

    @TypeConverters(LatLongTypeConverters.class)
    private List<LatLon> locations;

    @ColumnInfo(name = "distance")
    private double distance;
    @ColumnInfo(name = "distanceFmt")
    private String distanceFmt;

    @ColumnInfo(name = "steps")
    private int steps;



    @Ignore
    public HistoryItem() {}


    public HistoryItem(int keyID, String date, String title, String timeRemaining, String elaspedTime,
                       int roundsCompleted, int roundsTotal, String workTime, String restTime, boolean locked,
                       List<LatLon> locations, double distance, String distanceFmt, int steps) {
        this.keyID = keyID;
        this.date = date;
        this.title = title;
        this.timeRemaining = timeRemaining;
        this.elaspedTime = elaspedTime;
        this.roundsCompleted = roundsCompleted;
        this.roundsTotal = roundsTotal;
        this.workTime = workTime;
        this.restTime = restTime;
        this.locked = locked;
        this.locations = locations;
        this.distance = distance;
        this.distanceFmt = distanceFmt;
        this.steps = steps;
    }

    public int getKeyID() { return keyID; }
    public void setKeyID(int keyID) { this.keyID = keyID; }

    public String getDate() { return date; }
    public void setDate(String dateMilli) { this.date = dateMilli; }


    public String getTitle() { return title != null ? title : ""; }
    public void setTitle(String title) { this.title = title; }

    public String getTimeRemaining() { return timeRemaining; }
    public void setTimeRemaining(String timeRemaining) { this.timeRemaining = timeRemaining; }

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

    public boolean isLocked() {
        return locked;
    }
    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public ArrayList<LatLon> getLocationsArray() { return new ArrayList<>(locations); }
    public List<LatLon> getLocations() {
        if(locations == null)
            return new ArrayList<>();
        return locations;
    }
    public void setLocations(List<LatLon> locations) {
        this.locations = locations;
        if(locations != null) {
            setDistance(MapUtils.getDistance(locations));
            setDistanceFmt(MapUtils.getFormattedDistance(getDistance()));
        }
    }

    public double getDistance() {
        return distance;
    }
    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getDistanceFmt() {
        return distanceFmt;
    }
    public void setDistanceFmt(String distanceFmt) {
        this.distanceFmt = distanceFmt;
    }

    public int getSteps() {
        return steps;
    }
    public void setSteps(int steps) {
        this.steps = steps;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.keyID);
        dest.writeString(this.date);
        dest.writeString(this.title);
        dest.writeString(this.timeRemaining);
        dest.writeString(this.elaspedTime);
        dest.writeInt(this.roundsCompleted);
        dest.writeInt(this.roundsTotal);
        dest.writeString(this.workTime);
        dest.writeString(this.restTime);
        dest.writeByte(locked ? (byte) 1 : (byte) 0);
        dest.writeTypedList(locations);
        dest.writeDouble(this.distance);
        dest.writeString(this.distanceFmt);
        dest.writeInt(this.steps);
    }

    protected HistoryItem(Parcel in) {
        this.keyID = in.readInt();
        this.date = in.readString();
        this.title = in.readString();
        this.timeRemaining = in.readString();
        this.elaspedTime = in.readString();
        this.roundsCompleted = in.readInt();
        this.roundsTotal = in.readInt();
        this.workTime = in.readString();
        this.restTime = in.readString();
        this.locked = in.readByte() != 0;
        this.locations = in.createTypedArrayList(LatLon.CREATOR);
        this.distance = in.readDouble();
        this.distanceFmt = in.readString();
        this.steps = in.readInt();
    }

    public static final Parcelable.Creator<HistoryItem> CREATOR = new Parcelable.Creator<HistoryItem>() {
        public HistoryItem createFromParcel(Parcel source) {
            return new HistoryItem(source);
        }

        public HistoryItem[] newArray(int size) {
            return new HistoryItem[size];
        }
    };
}
