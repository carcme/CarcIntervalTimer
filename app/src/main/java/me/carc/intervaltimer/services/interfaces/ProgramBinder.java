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


import me.carc.intervaltimer.model.program.Exercise;
import me.carc.intervaltimer.model.program.WorkoutItem;

public interface ProgramBinder {

	void start();

	void stop();

	void resume();

	void pause();

	boolean isRunning();

	boolean isActive();

	boolean isStopped();

	boolean isPaused();

	void registerCountDownObserver(CountDownObserver observer);

	void registerGpsUpdates();

	void unregisterCountDownObserver(CountDownObserver observer);

	WorkoutItem getCurrentNode();

	Exercise getCurrentExercise();

	Exercise getNextExercise();

	long getProgramMsRemaining();

	int getExerciseMsRemaining();
}