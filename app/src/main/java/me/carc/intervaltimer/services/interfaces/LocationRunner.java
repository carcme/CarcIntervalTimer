package me.carc.intervaltimer.services.interfaces;

import android.location.Location;
import android.os.Bundle;

public interface LocationRunner {

	void canGetLocation(boolean canGet);

	void locationUpdate(Location location);

	void statusChanged(String provider, int status, Bundle extras);

	void providerEnabled(boolean enabled, String provider);
}