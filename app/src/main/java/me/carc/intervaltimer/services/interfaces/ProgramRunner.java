package me.carc.intervaltimer.services.interfaces;

import me.carc.intervaltimer.model.program.Exercise;
import me.carc.intervaltimer.model.program.WorkoutProgram;
import me.carc.intervaltimer.model.program.WorkoutItem;

public interface ProgramRunner {
	public static final int TICK_RATE = 1000;

	void start();

	void stop();

	void resume();

	void pause();

	boolean isRunning();

	boolean isPaused();

	boolean isStopped();

	long getProgramMsRemaining();

	int getExerciseMsRemaining();

	Exercise getCurrentExercise();

	Exercise getNextExercise();

	WorkoutItem getCurrentNode();

	WorkoutProgram getProgram();
}