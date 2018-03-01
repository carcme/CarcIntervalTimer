package me.carc.intervaltimer.services.interfaces;

import android.location.Location;
import android.os.Bundle;

/**
 * Created by bamptonm on 22/02/2018.
 */

public abstract class LocationObserver implements LocationRunner {
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
}
