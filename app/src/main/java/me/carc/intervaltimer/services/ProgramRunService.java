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
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationProvider;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import me.carc.intervaltimer.R;
import me.carc.intervaltimer.data.local.ProgramDAOSqlite;
import me.carc.intervaltimer.data.local.prefs.Preferences;
import me.carc.intervaltimer.model.LatLon;
import me.carc.intervaltimer.model.program.EffortLevel;
import me.carc.intervaltimer.model.program.Exercise;
import me.carc.intervaltimer.model.program.WorkoutItem;
import me.carc.intervaltimer.model.program.WorkoutMetaData;
import me.carc.intervaltimer.model.program.WorkoutProgram;
import me.carc.intervaltimer.services.interfaces.CountDownObserver;
import me.carc.intervaltimer.services.interfaces.LocationObserver;
import me.carc.intervaltimer.services.interfaces.ProgramBinder;
import me.carc.intervaltimer.services.interfaces.ProgramRunner;
import me.carc.intervaltimer.services.location.GpsTracker;
import me.carc.intervaltimer.sound.SoundPlayer;
import me.carc.intervaltimer.sound.TextToSpeechPlayer;
import me.carc.intervaltimer.ui.activities.ServicedActivity;
import me.carc.intervaltimer.utils.NotificationUtils;
import me.carc.intervaltimer.utils.PeekaheadQueue;
import me.carc.intervaltimer.utils.ViewUtil;

import static java.lang.Math.sqrt;


public class ProgramRunService extends Service {

    private static final String TAG = ProgramRunService.class.getName();

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
    private WorkoutProgram program;
    private ProgramRunner programRunner;
    private SoundPlayer soundPlayer;
    private GpsTracker gpsTracker;
    private SensorManager mSensorManager;
    private int duration, finalRestDuration;
    private long endRoundWarnMillis;
    private int sprintsTime;
    private long mStepCount;


    private ArrayList<LatLon> mLocations;

    private final List<CountDownObserver> observers = new ArrayList<CountDownObserver>();


    /* STEP COUNTER */
    float[] cachedAccelerometer = {0, 0, 0};
    float cachedAcceleration = 0;
    public static final float ALPHA = (float) 0.7;
    private long lastStepCountTime = 0;

    private float STEP_PEAK = 0;

    private final SensorEventListener stepSensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            Sensor mySensor = event.sensor;
            long currTime = System.currentTimeMillis();

            if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                // Low-pass filter to remove noise
                cachedAcceleration = (float) ((1 - ALPHA) * cachedAcceleration + ALPHA * sqrt(x * x + y * y + z * z));

                // Copy new values into the cachedAccelerometer array (We didn't use these values for step counter)
                System.arraycopy(event.values, 0, cachedAccelerometer, 0, event.values.length);

                // Peak is substantial enough to be correlated to a step
                if (cachedAcceleration > STEP_PEAK) {
                    // There needs to be at least 300ms between two peaks, otherwise it isn't a step.
                    if (currTime - lastStepCountTime > 300) {
                        mStepCount++;
                        lastStepCountTime = currTime;
                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");

        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        soundPlayer = new TextToSpeechPlayer(getApplicationContext(), audioManager);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (sensor != null) {
            STEP_PEAK = Preferences.getStepSensitivity(this);
            mSensorManager.registerListener(stepSensorListener, sensor, SensorManager.SENSOR_DELAY_FASTEST);
        }

        observers.add(soundObserver);
        observers.add(notificationObserver);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        endRoundWarnMillis = Long.parseLong(Preferences.getWarningTime(this)) * 1000;
        sprintsTime = Preferences.getSprintsTime(this);
    }

    @Override
    public void onDestroy() {
        stop();
        cleanUp();
        soundPlayer.cleanUp();
        if(gpsTracker != null)
            gpsTracker.cleanUp();
        mSensorManager.unregisterListener(stepSensorListener);
        super.onDestroy();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int programId = intent.getIntExtra(WorkoutMetaData.PROGRAM_ID_NAME, 1);

        program = ProgramDAOSqlite.getInstance(getApplicationContext()).getProgram(programId, false);
        PeekaheadQueue<WorkoutItem> queue = program.asQueue();

        Exercise restExercise = queue.peek(queue.size() - 1).getAttachedExercise();
        if (restExercise.getEffortLevel().equals(EffortLevel.REST)) {
            finalRestDuration = restExercise.getDuration();
        }

        duration = program.getAssociatedNode().getDuration() - finalRestDuration;
        notificationTitle = getString(R.string.app_name);

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
        Intent continueIntent = new Intent(getApplicationContext(), ServicedActivity.class);
        continueIntent.setAction(ServicedActivity.ACTION_CONTINUE_RUN);
//		continueIntent.putExtra(MainActivity.ARG_PROGRAM_ID, program.getId());
//		continueIntent.putExtra(MainActivity.ARG_PROGRAM_NAME, program.getName());

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());  // register notification channel
            builder = new Notification.Builder(getApplicationContext(), NotificationUtils.ANDROID_CHANNEL_ID);
        } else {
            builder = new Notification.Builder(getBaseContext());
        }
        builder.setSmallIcon(R.drawable.ic_icon_none);

        if (programRunner != null) {
            builder.setContentTitle(getNotificationText());
            builder.setContentText(String.valueOf(mStepCount) + " steps");
            builder.setProgress(duration, (int) (duration - programRunner.getProgramMsRemaining() + finalRestDuration), false);

            builder.setOngoing(true);
            switch (programRunner.getCurrentExercise().getEffortLevel()) {
                case EASY:
                    builder.setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrep));
                    builder.setSmallIcon(R.drawable.ic_icon_prep);
                    break;
                case HARD:
                    builder.setSmallIcon(R.drawable.ic_icon_run_fast);
                    builder.setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorWork));
                    break;
                case REST:
                    builder.setSmallIcon(R.drawable.ic_icon_rest);
                    builder.setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorRest));
                    break;
            }
        } else {
            builder.setContentText("Workout Complete");
            builder.setColor(ContextCompat.getColor(getApplicationContext(), R.color.md_red_700));
        }

        builder.setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, continueIntent, PendingIntent.FLAG_CANCEL_CURRENT));

        return builder.build();
    }

    private String getNotificationText() {
        StringBuilder builder = new StringBuilder();

        if (programRunner.getCurrentExercise().getName() != null) {
            builder.append(programRunner.getCurrentExercise().getName()).append(" ");
        }

//        if (programRunner.getCurrentExercise().getEffortLevel() != EffortLevel.NONE) {
//            builder.append(programRunner.getCurrentExercise().getEffortLevel().getString(getApplicationContext())).append(" ");
//        }

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


    private final LocationObserver locationObserver = new LocationObserver() {

        @Override
        public void canGetLocation(boolean canGet) {
            Log.d(TAG, "canGetLocation: " + canGet);
            if (canGet && mLocations == null) {
                mLocations = new ArrayList<>();
            }
        }

        @Override
        public void locationUpdate(Location location) {
            Log.d(TAG, "locationUpdate: " + location.toString());
            mLocations.add(new LatLon(location));

            for (CountDownObserver observer : observers) {
                observer.onLocationsUpdate(mLocations);
            }
        }

        @Override
        public void statusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d(TAG, "statusChanged: " + provider + " = OUT_OF_SERVICE");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d(TAG, "statusChanged: " + provider + " = TEMPORARILY_UNAVAILABLE");
                    break;
                case LocationProvider.AVAILABLE:
                    Log.d(TAG, "statusChanged: " + provider + " = AVAILABLE");
                    break;
            }
        }

        @Override
        public void providerEnabled(boolean enabled, String provider) {
            Log.d(TAG, "providerEnabled: " + provider + " is " + enabled);
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
            return programRunner != null && programRunner.isRunning();
        }

        @Override
        public void registerCountDownObserver(CountDownObserver observer) {
            observers.add(observer);
            observer.onStart();
            registerGpsUpdates();
        }

        @Override
        public void registerGpsUpdates() {
            if(Preferences.isLocationEnabled(getApplicationContext()))
                gpsTracker = new GpsTracker(ProgramRunService.this, locationObserver);
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
        public void onTick(long exerciseMsRemaining, long programMsRemaining, long stepCount) {
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
        public void onLocationsUpdate(ArrayList<LatLon> locations) {

        }

        @Override
        public void onGpsResults(ArrayList<LatLon> locations) {

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
        public void onTick(long exerciseMsRemaining, long programMsRemaining, long stepCount) {
            if (exerciseMsRemaining < endRoundWarnMillis)
                soundPlayer.playWarningBeep();

            if(sprintsTime != 0 && programRunner.getCurrentExercise().getEffortLevel().equals(EffortLevel.HARD)) {
                if(exerciseMsRemaining > (sprintsTime - 500) && (sprintsTime + 500 )> exerciseMsRemaining)
                    soundPlayer.playSprintsBeep();
            }
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
        public void onLocationsUpdate(ArrayList<LatLon> locations) {
        }

        @Override
        public void onGpsResults(ArrayList<LatLon> locations) {
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
        public void onTick(long exerciseMsRemaining, long programMsRemaining, long stepCount) {
            for (CountDownObserver observer : observers) {
                observer.onTick(exerciseMsRemaining, programMsRemaining, mStepCount);
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
                observer.onGpsResults(mLocations);
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
        public void onLocationsUpdate(ArrayList<LatLon> locations) {
        }

        @Override
        public void onGpsResults(ArrayList<LatLon> locations) {
        }

        @Override
        public void onError(ProgramError error) {
            for (CountDownObserver observer : observers) {
                observer.onError(error);
            }
        }
    }
}
