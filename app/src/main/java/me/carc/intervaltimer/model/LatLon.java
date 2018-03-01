package me.carc.intervaltimer.model;

import android.arch.persistence.room.Ignore;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by bamptonm on 18/02/2018.
 */
public class LatLon implements Parcelable {
    private double mLatitude;
    private double mLongitude;
    private double mAltitude;


    public LatLon(double mLatitude, double mLongitude, double mAltitude) {
        this.mLatitude = mLatitude;
        this.mLongitude = mLongitude;
        this.mAltitude = mAltitude;
    }

    @Ignore
    public LatLon(double mLatitude, double mLongitude) {
        this.mLatitude = mLatitude;
        this.mLongitude = mLongitude;
        this.mAltitude = 0;
    }

    @Ignore
    public LatLon(Location aLocation) {
        this(aLocation.getLatitude(), aLocation.getLongitude(), aLocation.getAltitude());
    }


    public double getLatitude() {
        return mLatitude;
    }
    public void setLatitude(double mLatitude) {
        this.mLatitude = mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }
    public void setLongitude(double mLongitude) {
        this.mLongitude = mLongitude;
    }

    public double getAltitude() {
        return mAltitude;
    }
    public void setAltitude(double mAltitude) {
        this.mAltitude = mAltitude;
    }



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.mLatitude);
        dest.writeDouble(this.mLongitude);
        dest.writeDouble(this.mAltitude);
    }

    protected LatLon(Parcel in) {
        this.mLatitude = in.readDouble();
        this.mLongitude = in.readDouble();
        this.mAltitude = in.readDouble();
    }

    public static final Parcelable.Creator<LatLon> CREATOR = new Parcelable.Creator<LatLon>() {
        public LatLon createFromParcel(Parcel source) {
            return new LatLon(source);
        }

        public LatLon[] newArray(int size) {
            return new LatLon[size];
        }
    };
}
