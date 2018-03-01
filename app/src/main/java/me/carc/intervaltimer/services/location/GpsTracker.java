package me.carc.intervaltimer.services.location;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.maps.LocationSource;

import me.carc.intervaltimer.data.local.prefs.Preferences;
import me.carc.intervaltimer.services.interfaces.LocationObserver;
import me.carc.intervaltimer.services.interfaces.LocationRunner;

/**
 * Report location at intervals
 * <p>
 * Created by bamptonm on 17/02/2018.
 */

public class GpsTracker extends Service implements LocationListener, LocationRunner, LocationSource{

    private static final String TAG = GpsTracker.class.getName();

    private final Context mContext;
    private Location location; // location

    // The minimum distance to change Updates in meters
    private long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;

    // The minimum time between updates in milliseconds
    private long MIN_TIME_BW_UPDATES = 1000 * 6;

    // Declaring a Location Manager
    private LocationManager locationManager;

    private OnLocationChangedListener mLocationChangedListener;
    private LocationObserver mLlocationObserver;


    public GpsTracker(Context context, LocationObserver locationObserver) {
        mContext = context;
        mLlocationObserver = locationObserver;
//        if(Preferences.isLocationEnabled(context)) {
            MIN_DISTANCE_CHANGE_FOR_UPDATES = Long.valueOf(Preferences.locationUpdateDistance(context));
            MIN_TIME_BW_UPDATES = Long.valueOf(Preferences.locationUpdateTime(context));
            requestLocation();
//        }
    }

    public void cleanUp() {
        if(locationManager != null)
            locationManager.removeUpdates(this);
        stopSelf();
    }

    @SuppressWarnings("MissingPermission")
    private void requestLocation() {
        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                mLlocationObserver.canGetLocation(false);
            } else {
                mLlocationObserver.canGetLocation(true);

                if (isGPSEnabled)   // if GPS Enabled get lat/long using GPS Services
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                else
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged: " + location.toString());

        if(mLlocationObserver != null)
            mLlocationObserver.locationUpdate(location);

        if(mLocationChangedListener != null)
            mLocationChangedListener.onLocationChanged(location);

    }

    static final int TIME_DIFFERENCE_THRESHOLD = 60 * 1000;

    boolean isBetterLocation(Location oldLocation, Location newLocation) {
        // If there is no old location, of course the new location is better.
        if(oldLocation == null) {
            return true;
        }

        // Check if new location is newer in time.
        boolean isNewer = newLocation.getTime() > oldLocation.getTime();

        // Check if new location more accurate. Accuracy is radius in meters, so less is better.
        boolean isMoreAccurate = newLocation.getAccuracy() < oldLocation.getAccuracy();
        if(isMoreAccurate && isNewer) {
            // More accurate and newer is always better.
            return true;
        } else if(isMoreAccurate && !isNewer) {
            // More accurate but not newer can lead to bad fix because of user movement.
            // Let us set a threshold for the maximum tolerance of time difference.
            long timeDifference = newLocation.getTime() - oldLocation.getTime();

            // If time difference is not greater then allowed threshold we accept it.
            if(timeDifference > -TIME_DIFFERENCE_THRESHOLD) {
                return true;
            }
        }

        return false;
    }


    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "onProviderDisabled: " + provider);
        mLlocationObserver.providerEnabled(false, provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "onProviderEnabled: " + provider);
        mLlocationObserver.providerEnabled(true, provider);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, "onStatusChanged: ");
        mLlocationObserver.statusChanged(provider, status, extras);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }





    @Override
    public void canGetLocation(boolean canGet) {

    }

    @Override
    public void locationUpdate(Location location) {

    }

    @Override
    public void statusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void providerEnabled(boolean enabled, String provider) {

    }

    @Override
    public void activate(OnLocationChangedListener listener) {
        mLocationChangedListener = listener;
    }

    @Override
    public void deactivate() {
        cleanUp();
    }
}
