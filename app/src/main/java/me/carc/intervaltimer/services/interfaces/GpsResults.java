package me.carc.intervaltimer.services.interfaces;

import android.location.Location;

import java.util.ArrayList;

public interface GpsResults {

	void sendResults(ArrayList<Location> locations);
}