/*
 * HIIT Me, a High Intensity Interval Training app for Android
 * Copyright (C) 2015 Alex Gilleran
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.carc.intervaltimer.services.interfaces;

import java.util.ArrayList;

import me.carc.intervaltimer.model.LatLon;
import me.carc.intervaltimer.model.program.Exercise;

public interface CountDownObserver {
	void onStart();

	void onResume();

	void onTick(long exerciseMsRemaining, long programMsRemaining, long stepCount);

	void onExerciseStart(Exercise exercise);

	void onProgramFinish();

	void onPause();

	void onError(CountDownObserver.ProgramError error);

	void onLocationsUpdate(ArrayList<LatLon> locations);

	void onGpsResults(ArrayList<LatLon> locations);

	public enum ProgramError {
		ZERO_DURATION;
	}
}