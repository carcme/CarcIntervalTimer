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

package me.carc.intervaltimer.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import me.carc.intervaltimer.R;
import me.carc.intervaltimer.data.local.ProgramDAOSqlite;
import me.carc.intervaltimer.model.EffortLevel;
import me.carc.intervaltimer.model.Exercise;
import me.carc.intervaltimer.model.WorkoutGroup;
import me.carc.intervaltimer.model.WorkoutItem;
import me.carc.intervaltimer.model.WorkoutMetaData;
import me.carc.intervaltimer.services.interfaces.CountDownObserver;
import me.carc.intervaltimer.services.interfaces.ProgramBinder;
import me.carc.intervaltimer.services.interfaces.ProgramRunner;
import me.carc.intervaltimer.settings.Preferences;
import me.carc.intervaltimer.sound.SoundPlayer;
import me.carc.intervaltimer.sound.TextToSpeechPlayer;
import me.carc.intervaltimer.ui.MainActivity;
import me.carc.intervaltimer.utils.ViewUtil;


public class ProgramRunService extends Service {
	/**
	 * We will only update the notification once for every TICK_RATE_DIVISOR ticks - should result
	 * in being notified once per second.
	 */
	private static final int TICK_RATE_DIVISOR = 1000 / ProgramRunner.TICK_RATE;
	private static final int NOTIFICATION_ID = 2;
	private static final IntentFilter noisyIntentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);

	private final MasterCountDownObserver masterObserver = new MasterCountDownObserver();

	private String notificationTitle;
	private NotificationManager notificationManager;
	private WakeLock wakeLock;
	private WorkoutGroup program;
	private ProgramRunner programRunner;
	private SoundPlayer soundPlayer;
	private int duration;
    private long endRoundWarnMillis;

	private final List<CountDownObserver> observers = new ArrayList<CountDownObserver>();

	@Override
	public void onCreate() {
		super.onCreate();

		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");

		AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
		soundPlayer = new TextToSpeechPlayer(getApplicationContext(), audioManager);

		observers.add(soundObserver);
		observers.add(notificationObserver);

		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        endRoundWarnMillis = Long.parseLong(Preferences.getWarningTime(this)) * 1000;
	}

	@Override
	public void onDestroy() {
		stop();
		cleanUp();
		soundPlayer.cleanUp();

		super.onDestroy();
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		long programId = intent.getLongExtra(WorkoutMetaData.PROGRAM_ID_NAME, 1L);

		program = ProgramDAOSqlite.getInstance(getApplicationContext()).getProgram(programId, false);
		duration = program.getAssociatedNode().getDuration();
		notificationTitle = getString(R.string.app_name) + ": " + program.getName();

		start();

		return START_REDELIVER_INTENT;
	}

	private void pause() {
		unregisterReceiver(headphonesUnpluggedReceiver);
		programRunner.pause();
	}

	private void start() {
		registerReceiver(headphonesUnpluggedReceiver, noisyIntentFilter);

		if (duration <= 0) {
			masterObserver.onError(CountDownObserver.ProgramError.ZERO_DURATION);
			cleanUp();
			return;
		}

		if (programRunner == null || programRunner.isStopped()) {
			wakeLock.acquire();

			startForeground(NOTIFICATION_ID, buildNotification());

			programRunner = new TimerImpl(program, masterObserver);

			programRunner.start();

		} else if (programRunner.isPaused()) {
			programRunner.resume();
		} else if (programRunner.isRunning()) {
			Log.wtf(getPackageName(), "Trying to start a run when one is already running, this is STRICTLY VERBOTEN");
		}
	}

	private void stop() {
		if (programRunner != null && !programRunner.isStopped()) {
			programRunner.stop();
			programRunner = null;
		}
        stopSelf(NOTIFICATION_ID);
	}

	private void cleanUp() {
		try {
			unregisterReceiver(headphonesUnpluggedReceiver);
		} catch (IllegalArgumentException e) {
			// Already unregistered, oh well.
		}

		if (wakeLock != null && wakeLock.isHeld()) {
			wakeLock.release();
		}

		stopForeground(true);
		stopSelf();
	}

	private Notification buildNotification() {
		Intent continueIntent = new Intent(getApplicationContext(), MainActivity.class);
		continueIntent.setAction(MainActivity.ACTION_CONTINUE_RUN);
//		continueIntent.putExtra(MainActivity.ARG_PROGRAM_ID, program.getId());
//		continueIntent.putExtra(MainActivity.ARG_PROGRAM_NAME, program.getName());

		Notification.Builder builder = new Notification.Builder(getBaseContext());
		builder.setContentTitle(notificationTitle);

		if (programRunner != null) {
			builder.setContentText(getNotificationText());
			builder.setProgress(duration, (int) (duration - programRunner.getProgramMsRemaining()), false);
		}

		builder.setSmallIcon(R.mipmap.ic_launcher);
		builder.setOngoing(true);
		builder.setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, continueIntent, PendingIntent.FLAG_CANCEL_CURRENT));

		return builder.build();
	}

	private String getNotificationText() {
		StringBuilder builder = new StringBuilder();

		if (programRunner.getCurrentExercise().getName() != null) {
			builder.append(programRunner.getCurrentExercise().getName()).append(" ");
		}

		if (programRunner.getCurrentExercise().getEffortLevel() != EffortLevel.NONE) {
			builder.append(programRunner.getCurrentExercise().getEffortLevel().getString(getApplicationContext())).append(" ");
		}

		builder.append(ViewUtil.getTimeText(programRunner.getExerciseMsRemaining()));

		return builder.toString();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new ProgramBinderImpl();
	}

	private void updateNotification() {
		notificationManager.notify(NOTIFICATION_ID, buildNotification());
	}

	private BroadcastReceiver headphonesUnpluggedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
				ProgramRunService.this.pause();
			}
		}
	};

	public class ProgramBinderImpl extends Binder implements ProgramBinder {
		@Override
		public void start() {
			ProgramRunService.this.start();
		}

		@Override
		public void stop() {
			ProgramRunService.this.stop();
		}

        @Override
        public void resume() {
            ProgramRunService.this.start();
        }

        @Override
		public void pause() {
			ProgramRunService.this.pause();
		}

		@Override
		public boolean isRunning() {
			return programRunner != null ? programRunner.isRunning() : false;
		}

		@Override
		public void registerCountDownObserver(CountDownObserver observer) {
			observers.add(observer);
            observer.onStart();
		}

		@Override
		public void unregisterCountDownObserver(CountDownObserver observer) {
			observers.remove(observer);
		}

		@Override
		public WorkoutItem getCurrentNode() {
			return programRunner.getCurrentNode();
		}

		@Override
		public Exercise getCurrentExercise() {
			return programRunner.getCurrentExercise();
		}

		@Override
		public boolean isActive() {
			return programRunner != null && (programRunner.isRunning() || programRunner.isPaused());
		}

		@Override
		public boolean isStopped() {
			return programRunner != null && programRunner.isStopped();
		}

		@Override
		public boolean isPaused() {
			return programRunner != null && programRunner.isPaused();
		}

		@Override
		public long getProgramMsRemaining() {
			return programRunner.getProgramMsRemaining();
		}

		@Override
		public int getExerciseMsRemaining() {
			return programRunner.getExerciseMsRemaining();
		}

		@Override
		public Exercise getNextExercise() {
			return programRunner.getNextExercise();
		}
	}

	private CountDownObserver notificationObserver = new CountDownObserver() {
		private int tickCount = 0;

		@Override
		public void onStart() {
		}

		@Override
		public void onTick(long exerciseMsRemaining, long programMsRemaining) {
			tickCount++;

			// Only update the notification every second.
			if (tickCount >= TICK_RATE_DIVISOR) {
				tickCount = 0;
				updateNotification();
			}
		}

		@Override
		public void onExerciseStart(Exercise exercise) {
			updateNotification();
		}

		@Override
		public void onProgramFinish() {
		}

		@Override
		public void onPause() {

		}

        @Override
        public void onResume() {

        }

		@Override
		public void onError(ProgramError error) {
		}
	};

	private CountDownObserver soundObserver = new CountDownObserver() {
		@Override
		public void onStart() {
			soundPlayer.playExerciseStart(programRunner.getCurrentExercise());
		}

		@Override
		public void onTick(long exerciseMsRemaining, long programMsRemaining) {
            if (exerciseMsRemaining < endRoundWarnMillis)
                soundPlayer.playWarningBeep();
        }

		@Override
		public void onExerciseStart(Exercise exercise) {
			soundPlayer.playExerciseStart(programRunner.getCurrentExercise());
		}

		@Override
		public void onProgramFinish() {
			soundPlayer.playEnd();
		}

        @Override
        public void onResume() {

        }

		@Override
		public void onPause() {

		}

		@Override
		public void onError(ProgramError error) {
		}
	};

	/**
	 * Listens for count down events and proxies them to a number of other {@link CountDownObserver}s.
	 */
	private class MasterCountDownObserver implements CountDownObserver {
		@Override
		public void onTick(long exerciseMsRemaining, long programMsRemaining) {
			for (CountDownObserver observer : observers) {
				observer.onTick(exerciseMsRemaining, programMsRemaining);
			}
		}

		@Override
		public void onExerciseStart(Exercise exercise) {
			for (CountDownObserver observer : observers) {
				observer.onExerciseStart(exercise);
			}
		}

		@Override
		public void onProgramFinish() {
			for (CountDownObserver observer : observers) {
				observer.onProgramFinish();
			}

			cleanUp();
		}

        @Override
        public void onResume() {
			for (CountDownObserver observer : observers) {
				observer.onResume();
			}
        }

		@Override
		public void onPause() {
			for (CountDownObserver observer : observers) {
				observer.onPause();
			}
		}

		@Override
		public void onStart() {
			for (CountDownObserver observer : observers) {
				observer.onStart();
			}
		}

		@Override
		public void onError(ProgramError error) {
			for (CountDownObserver observer : observers) {
				observer.onError(error);
			}
		}
	}
}
