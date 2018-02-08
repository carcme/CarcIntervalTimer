package me.carc.intervaltimer.model;

import android.os.Parcel;
import android.os.Parcelable;

import me.carc.intervaltimer.ui.MainActivity;

/**
 * Created by bamptonm on 05/02/2018.
 */

public class WorkoutSchedule implements Parcelable {

    private MainActivity.State state;

    private long prep;
    private long work;
    private long rest;

    private long temp;

    private int rounds;
    private int currentRound;

    private long durationTotal;



    public WorkoutSchedule(MainActivity.State state, long prep, long work, long rest, int rounds, int currentRound) {
        this.state = state;
        this.prep= prep;
        this.work = work;
        this.rest = rest;
        this.rounds = rounds;
        this.currentRound = currentRound;

        this.durationTotal = prep + (work * rounds) + (rest * (rounds - 1));
    }

    public MainActivity.State getState() {
        return state;
    }
    public void setState(MainActivity.State state) {
        this.state = state;
    }

    public long getPrep() {
        return prep;
    }
    public void setPrep(long prep) {
        this.prep = prep;
    }

    public long getWork() {
        return work;
    }
    public void setWork(long work) {
        this.work = work;
    }

    public long getRest() {
        return rest;
    }
    public void setRest(long rest) {
        this.rest = rest;
    }

    public int getRounds() {
        return rounds;
    }
    public void setRounds(int rounds) {
        this.rounds = rounds;
    }

    public int getCurrentRound() {
        return currentRound;
    }
    public void setCurrentRound(int currentRound) {
        this.currentRound = currentRound;
    }

    public long getTemp() {
        return temp;
    }
    public void setTemp(long temp) {
        this.temp = temp;
    }


    public long getDurationTotal() { return durationTotal; }
    public void setDurationTotal(long durationTotal) { this.durationTotal = durationTotal; }



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.state == null ? -1 : this.state.ordinal());
        dest.writeLong(this.prep);
        dest.writeLong(this.work);
        dest.writeLong(this.rest);
        dest.writeLong(this.temp);
        dest.writeInt(this.rounds);
        dest.writeInt(this.currentRound);
    }

    protected WorkoutSchedule(Parcel in) {
        int tmpState = in.readInt();
        this.state = tmpState == -1 ? null : MainActivity.State.values()[tmpState];
        this.prep = in.readLong();
        this.work = in.readLong();
        this.rest = in.readLong();
        this.temp = in.readLong();
        this.rounds = in.readInt();
        this.currentRound = in.readInt();
    }

    public static final Parcelable.Creator<WorkoutSchedule> CREATOR = new Parcelable.Creator<WorkoutSchedule>() {
        public WorkoutSchedule createFromParcel(Parcel source) {
            return new WorkoutSchedule(source);
        }

        public WorkoutSchedule[] newArray(int size) {
            return new WorkoutSchedule[size];
        }
    };
}
