package me.carc.intervaltimer.ui.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.ArrayRes;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import me.carc.intervaltimer.App;
import me.carc.intervaltimer.BuildConfig;
import me.carc.intervaltimer.R;
import me.carc.intervaltimer.data.local.AppDatabase;
import me.carc.intervaltimer.data.local.HistoryItem;
import me.carc.intervaltimer.data.local.ProgramDAOSqlite;
import me.carc.intervaltimer.data.local.prefs.Preferences;
import me.carc.intervaltimer.model.LatLon;
import me.carc.intervaltimer.model.program.EffortLevel;
import me.carc.intervaltimer.model.program.Exercise;
import me.carc.intervaltimer.model.program.WorkoutItem;
import me.carc.intervaltimer.model.program.WorkoutMetaData;
import me.carc.intervaltimer.model.program.WorkoutProgram;
import me.carc.intervaltimer.services.ProgramRunService;
import me.carc.intervaltimer.services.interfaces.CountDownObserver;
import me.carc.intervaltimer.services.interfaces.ProgramBinder;
import me.carc.intervaltimer.ui.fragments.HistoryListDialogFragment;
import me.carc.intervaltimer.utils.Commons;
import me.carc.intervaltimer.utils.MapUtils;
import me.carc.intervaltimer.utils.ViewUtil;
import me.carc.intervaltimer.widgets.AutoResizeTextView;
import me.carc.intervaltimer.widgets.NumberPickerBuilder;
import me.carc.intervaltimer.widgets.PickerPrefDialog;
import me.carc.intervaltimer.widgets.circle_progress.DonutProgress;
import me.carc.intervaltimer.widgets.listeners.HistoryItemViewerListener;
import me.carc.intervaltimer.widgets.listeners.NumberSetListener;
import me.toptas.fancyshowcase.FancyShowCaseQueue;
import me.toptas.fancyshowcase.FancyShowCaseView;
import me.toptas.fancyshowcase.FocusShape;
import me.toptas.fancyshowcase.OnCompleteListener;

/**
 * Main Interval Timer
 */
public class ServicedActivity extends BaseActivity implements SensorEventListener, HistoryItemViewerListener {

    private enum Running {WORK, PAUSE}

    private static final String TAG = ServicedActivity.class.getName();
    public static final String ACTION_CONTINUE_RUN = "CONTINUE_RUN";
    private final int PREP_TIME_MILLI = 7000;

    private final int RESULT_PREFERENCES = 159;
    private final int RESULT_LOCATION_PERMISSION  = 150;
    private final int RESULT_DYNAMIC_LOCATION_PERMISSION = 151;
    private final int RESULT_LOCATION_SOURCE_SETTINGS = 1510;

    public static final String PREF_TOTAL_DISTANCE = "PREF_TOTAL_DISTANCE";
    public static final String PREF_TOTAL_TIME     = "PREF_TOTAL_TIME";

    private final int SIMPLE_RUN_PROGRAM = 1;

    private String runTime;
    private long workTimeMillis, restTimeMillis, debounceCounter, totalWorkoutTime;
    private AnimationDrawable bgAnimation;
    private int roundCurrent, roundsTotal;
    private boolean prepEnabled, proximity;

    private ArrayList<LatLon> mLocations;

    private SensorManager sensorManager;
    private Sensor proximitySensor;

    private Handler handler = new Handler();

    private ProgramBinder programBinder;
    private RunnerServiceConnection connection;

    @BindView(R.id.workoutTime)     TextView workoutTime;
    @BindView(R.id.elapsedTime)     TextView elapsedTime;
    @BindView(R.id.timerBackground) RelativeLayout timerBackground;
    @BindView(R.id.workPreviewText) TextView workPreviewText;
    @BindView(R.id.restPreviewText) TextView restPreviewText;
    @BindView(R.id.timerRemaining)  AutoResizeTextView timerRemaining;
    @BindView(R.id.timerMessage)    AutoResizeTextView timerMessage;
    @BindView(R.id.round_number)    TextView textViewRounds;
    @BindView(R.id.fabSettings)     FloatingActionButton fabSettings;
    @SuppressLint("ClickableViewAccessibility")
    @BindView(R.id.fabTimer)        FloatingActionButton fabTimer;
    @BindView(R.id.resetLayer)      RelativeLayout resetLayer;
    @BindView(R.id.resetBtn)        Button resetBtn;
    @BindView(R.id.fabDonutProgress)DonutProgress fabDonutProgress;
    @BindView(R.id.stepCountView)   TextView stepCountView;

    @BindView(R.id.summaryFrame)    RelativeLayout summaryFrame;
    @BindView(R.id.summaryDistance) AutoResizeTextView summaryDistance;
    @BindView(R.id.summaryTime)     AutoResizeTextView summaryTime;



    private void helpShowcase() {
        int borderColor = ContextCompat.getColor(ServicedActivity.this, R.color.colorAccent);
        Animation enterAnimation = AnimationUtils.loadAnimation(ServicedActivity.this, R.anim.fade_in);

        final FancyShowCaseView workout = new FancyShowCaseView.Builder(ServicedActivity.this)
                .title(Commons.fromHtml("<p><em>Tap</em> to change your <em>WORKOUT</em> time</p>"))
                .fitSystemWindows(true)
                .focusBorderColor(borderColor)
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .focusBorderSize(5)
                .enterAnimation(enterAnimation)
                .focusOn(findViewById(R.id.workPanel))
                .build();

        final FancyShowCaseView rest = new FancyShowCaseView.Builder(ServicedActivity.this)
                .title(Commons.fromHtml("<p><em>Tap</em> to change your <em>REST</em> time</p>"))
                .focusBorderColor(borderColor)
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .focusBorderSize(5)
                .enterAnimation(enterAnimation)
                .focusOn(findViewById(R.id.restPanel))
                .build();

        final FancyShowCaseView rounds = new FancyShowCaseView.Builder(ServicedActivity.this)
                .title(Commons.fromHtml("<p><em>Touch</em> here to adjust the number of <em>ROUNDS</em> for your workout</p>"))
                .focusBorderColor(borderColor)
                .focusBorderSize(5)
                .enterAnimation(enterAnimation)
                .focusOn(findViewById(R.id.roundPanel))
                .build();

        final FancyShowCaseView settings = new FancyShowCaseView.Builder(ServicedActivity.this)
                .title(Commons.fromHtml("<p>Change </em>SETTINGS</em></p><p>You can configure pretty much everything</p>"))
                .focusBorderColor(borderColor)
                .focusBorderSize(5)
                .enterAnimation(enterAnimation)
                .focusOn(fabSettings)
                .build();

        final FancyShowCaseView start = new FancyShowCaseView.Builder(ServicedActivity.this)
                .title(Commons.fromHtml("<p>Where the magic happens.<p></p><em>Touch</em> to <em>START</em> your workout.</p><p>During your workout, <em>Hold</em> to <em>PAUSE</em> the workout</p>"))
                .focusBorderColor(borderColor)
                .focusBorderSize(5)
                .enterAnimation(enterAnimation)
                .focusOn(fabTimer)
                .build();

        final FancyShowCaseView steps = new FancyShowCaseView.Builder(ServicedActivity.this)
                .title(Commons.fromHtml("<p>Adjust the step sensitivity</p><p>This varies for each person so play with it to find what value suits your pace</p>"))
                .focusBorderColor(borderColor)
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .focusBorderSize(5)
                .enterAnimation(enterAnimation)
                .focusOn(stepCountView)
                .build();

        final FancyShowCaseView distance = new FancyShowCaseView.Builder(ServicedActivity.this)
                .title(Commons.fromHtml("<p>This is the total distance you\'ve covered while exercising</p><p>Requires the GPS to be enabled</p>"))
                .focusBorderColor(borderColor)
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .focusBorderSize(5)
                .enterAnimation(enterAnimation)
                .focusOn(summaryDistance)
                .build();

        final FancyShowCaseView time = new FancyShowCaseView.Builder(ServicedActivity.this)
                .title(Commons.fromHtml("<p>The cummulative time spent exercising</p><p></p>"))
                .focusBorderColor(borderColor)
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .focusBorderSize(5)
                .enterAnimation(enterAnimation)
                .focusOn(summaryTime)
                .build();

        final FancyShowCaseView summary = new FancyShowCaseView.Builder(ServicedActivity.this)
                .title(Commons.fromHtml("<p>Touch to view your workout history</p><p></p>"))
                .focusBorderColor(borderColor)
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .focusBorderSize(5)
                .enterAnimation(enterAnimation)
                .focusOn(summaryFrame)
                .build();

        FancyShowCaseQueue queue = new FancyShowCaseQueue()
                .add(workout)
                .add(rest)
                .add(rounds)
                .add(settings)
                .add(start)
                .add(steps)
                .add(distance)
                .add(time)
                .add(summary);

        queue.setCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete() {
                Log.d(TAG, "onComplete: ");
                Preferences.firstRunComplete(ServicedActivity.this);
                calculateSummary(null, null);
            }
        });
        queue.show();
    }


    private final Runnable longPressToPause = new Runnable() {
        public void run() {
            try {
                int increment = BuildConfig.DEBUG ? 2 : 2;
                fabDonutProgress.setProgress(fabDonutProgress.getProgress() + increment);
                if (fabDonutProgress.getProgress() == 100) {

                    fabTimer.setOnTouchListener(null);
                    changeFabAppearance(Running.WORK);
                    fabDonutProgress.setProgress(0);

                    programBinder.pause();
                    showView(resetLayer);

                } else
                    handler.postDelayed(this, 10);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    FloatingActionButton.OnTouchListener fabTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    longPressToPause.run();
                    return true;

                case MotionEvent.ACTION_UP:
                    handler.removeCallbacks(longPressToPause);
                    fabDonutProgress.setProgress(0);
                    return true;
            }
            return false;
        }
    };


    boolean isRunning = false;
    boolean isPause = false;

    @OnClick(R.id.summaryFrame)
    void showSummary() {
        HistoryListDialogFragment fragment = getHistoryListDialogFragment();
        if(Commons.isNotNull(fragment))
            fragment.show();
        else
            HistoryListDialogFragment.showInstance(getApplicationContext());
    }

    private HistoryListDialogFragment getHistoryListDialogFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(HistoryListDialogFragment.ID_TAG);
        return fragment != null && !fragment.isDetached() && !fragment.isRemoving() ? (HistoryListDialogFragment) fragment : null;
    }

    @Override
    public void onUpdateHistoryItem(HistoryItem historyItem) {
        HistoryListDialogFragment fragment = getHistoryListDialogFragment();

        if(Commons.isNotNull(fragment)) {
            fragment.updateHistoryItem(historyItem);
        }
    }

    @OnClick(R.id.fabTimer)
    void fabClick() {
/*
        if(BuildConfig.DEBUG && programBinder == null) {
            AlertDialog.Builder dlg = new AlertDialog.Builder(ServicedActivity.this)
                    .setIcon(R.drawable.ic_delete)
                    .setTitle("Use DEBUG Data?")
                    .setMessage("Use LIVE or DEBUG data")
                    .setNegativeButton("Debug", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Preferences.useDebug(ServicedActivity.this, true);
                            run();
                        }
                    })
                        .setPositiveButton("Live", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Preferences.useDebug(ServicedActivity.this, false);
                            run();
                        }
                    });
            dlg.show();
        } else {
            run();
        }
    }

    private void run() {
*/
        if (Preferences.isLocationEnabled(this)) {
            gpsPermission();
        } else
            startPauseTimer();
    }

    private void startPauseTimer () {
        fabTimer.setOnTouchListener(fabTouchListener);
        changeFabAppearance(Running.PAUSE);

        if (programBinder == null) {

            if (totalWorkoutTime == 0) {
                Log.d(TAG, "instance initializer: ");
            } else {
                roundCurrent = 1;

                Intent serviceIntent = new Intent(this, ProgramRunService.class);
                serviceIntent.putExtra(WorkoutMetaData.PROGRAM_ID_NAME, SIMPLE_RUN_PROGRAM);

                startService(serviceIntent);
                getApplicationContext().bindService(serviceIntent, connection, Context.BIND_ABOVE_CLIENT);

                isRunning = true;

                timerMessage.setVisibility(View.VISIBLE);
                summaryFrame.setVisibility(View.GONE);

                switchFabSettings();
                //fabSettings.hide();

                keepScreenOn(true);
                stepCountView.setText("");
            }
        } else {
            programBinder.resume();
        }
    }

    @OnLongClick(R.id.resetBtn)
    boolean reset() {
        isRunning = false;

        if (programBinder != null) {
            programBinder.stop();
            if (programBinder != null) {
                programBinder.unregisterCountDownObserver(countDownObserver);
                programBinder = null;
            }
        }

        // Unbind if the service is bound, ignore Exception if its not bound
        try {
            getApplicationContext().unbindService(connection);
        } catch (IllegalArgumentException e) { /* EMPTY */ }

        addToDatabase();
        unlockOrientation();

        timerMessage.setVisibility(View.GONE);
        if (Preferences.showSummary(this))
            summaryFrame.setVisibility(View.VISIBLE);


        roundCurrent = 0;
        setRoundTextView();
        timerRemaining.setText(runTime);
        elapsedTime.setText("");

        changeFabAppearance(Running.WORK);
        setViewColors(null);

        hideView(resetLayer);
        switchFabSettings();
        //fabSettings.show();

        fabTimer.setOnTouchListener(null);
        fabTimer.show();

        keepScreenOn(false);

        fabDonutProgress.setProgress(0);

        setDisplayViews();

/*
        totalWorkoutTime = (workTimeMillis * roundsTotal) + (restTimeMillis * (roundsTotal - 1));
        workoutTime.setText(getElaspedTime(totalWorkoutTime));
*/

        return true;
    }


    @OnClick(R.id.stepCountView)
    void adjustStepSensitivity() {
            new NumberPickerBuilder(this)
                    .setPickerTitle(R.string.step_sensitivity)
                    .setPickerCount(NumberPickerBuilder.SECS)
                    .setValue(NumberPickerBuilder.SECS, Preferences.getStepSensitivity(this))
                    .setMinMaxValue(NumberPickerBuilder.SECS, 8, 40)
                    .setListener(new NumberSetListener() {
                        @Override
                        public void onNumberSet(String value) {
                            Preferences.setStepSensitivity(ServicedActivity.this, Integer.valueOf(value));
                        }
                    })
                    .show();
    }

    @OnClick(R.id.workPanel)
    void adjustWorkTime() {
        if(programBinder == null) {
            long initValue = PickerPrefDialog.getMillis(Preferences.getWorkTime(this));
            int minutes = (int)(initValue /1000 / 60);
            int seconds = (int)((initValue / 1000) % 60);

            new NumberPickerBuilder(this)
                    .setPickerTitle(R.string.round_time_title)
                    .setPickerCount(NumberPickerBuilder.MINS)
                    .setValue(NumberPickerBuilder.MINS, minutes)
                    .setValue(NumberPickerBuilder.SECS, seconds)
                    .setMinMaxValue(NumberPickerBuilder.MINS, 0, 59)
                    .setListener(new NumberSetListener() {
                        @Override
                        public void onNumberSet(String time) {
                            Preferences.setWorkTime(ServicedActivity.this, time);
                            loadPreferences(null);
                        }
                    })
                    .show();
        }
    }

    @OnClick(R.id.restPanel)
    void adjustRestTime() {
        if(programBinder == null) {
            long initValue = PickerPrefDialog.getMillis(Preferences.getRestTime(this));
            int minutes = (int)(initValue /1000 / 60);
            int seconds = (int)((initValue / 1000) % 60);

            new NumberPickerBuilder(this)
                    .setPickerTitle(R.string.rest_time_title)
                    .setPickerCount(NumberPickerBuilder.MINS)
                    .setValue(NumberPickerBuilder.MINS, minutes)
                    .setValue(NumberPickerBuilder.SECS, seconds)
                    .setMinMaxValue(NumberPickerBuilder.MINS, 0, 59)
                    .setListener(new NumberSetListener() {
                        @Override
                        public void onNumberSet(String time) {
                            Preferences.setRestTime(ServicedActivity.this, time);
                            loadPreferences(null);
                        }
                    })
                    .show();
        }
    }

    @OnClick(R.id.roundPanel)
    void adjustRoundsCounter() {
        if(programBinder == null) {
            int rounds = Integer.valueOf(Preferences.getRoundsCount(this));

            new NumberPickerBuilder(this)
                    .setPickerTitle(R.string.num_round_title)
                    .setPickerCount(NumberPickerBuilder.SECS)
                    .setValue(NumberPickerBuilder.SECS, rounds)
                    .setMinMaxValue(NumberPickerBuilder.SECS, 0, 99)
                    .setListener(new NumberSetListener() {
                        @Override
                        public void onNumberSet(String count) {
                            Preferences.setRoundsCount(ServicedActivity.this, count);
                            loadPreferences(null);
                        }
                    })
                    .show();
        }
    }

    private void addToDatabase() {
        final String date = Commons.readableDate(System.currentTimeMillis());
        final String remainTime = workoutTime.getText().toString();
        final String elaspedTime = elapsedTime.getText().toString();
        final int roundsCompleted = this.roundCurrent;
        final int roundsTotal = this.roundsTotal;
        final String workTime = Preferences.getWorkTime(this);
        final String restTime = Preferences.getRestTime(this);

        String stepsStr = stepCountView.getText().toString();
        final int steps = Integer.valueOf(stepsStr.substring(stepsStr.indexOf(":") + 2, stepsStr.length()));

        final HistoryItem historyItem = new HistoryItem();

        historyItem.setDate(date);
        historyItem.setTitle("");
        historyItem.setTimeRemaining(remainTime);
        historyItem.setElaspedTime(elaspedTime);
        historyItem.setRoundsCompleted(roundsCompleted);
        historyItem.setRoundsTotal(roundsTotal);
        historyItem.setWorkTime(workTime);
        historyItem.setRestTime(restTime);
        historyItem.setLocked(false);
        historyItem.setSteps(steps);

//        if(Preferences.useDebug(this)) {
//            historyItem.setLocations(TestLocationsArray.BuildTestLocationsArray());
//        } else {
            if(mLocations != null && mLocations.size() > 1) {
                historyItem.setLocations(mLocations);
                historyItem.setDistance(MapUtils.getDistance(mLocations));
                historyItem.setDistanceFmt(MapUtils.getFormattedDistance(historyItem.getDistance()));
            }
//        }

        calculateSummary(null, historyItem);

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                AppDatabase db = ((App) getApplicationContext()).getDB();
                historyItem.setKeyID((int)db.historyDao().insert(historyItem));
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                    }
//                });
            }
        });
    }

    private void loadDatabase() {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                AppDatabase db = ((App) getApplicationContext()).getDB();
                final List<HistoryItem> items = db.historyDao().getAllEntries();

                for (HistoryItem item : items) {
                    if(item.getLocations().size() > 1 && TextUtils.isEmpty(item.getDistanceFmt())) {
                        item.setDistance(MapUtils.getDistance(item.getLocations()));
                        item.setDistanceFmt(MapUtils.getFormattedDistance(item.getDistance()));
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // TODO: 25/02/2018 move this - no need to load database in this activity once the code change have propagated across all users
                        calculateSummary(items, null);
                        if(Preferences.isFirstRun(ServicedActivity.this)) {
                            helpShowcase();
                        }
                    }
                });
            }
        });
    }

    private void calculateSummary(List<HistoryItem> items, HistoryItem historyItem) {

        double savedDistance = Preferences.getPrefDouble(this, PREF_TOTAL_DISTANCE);
        long savedTime = Preferences.getPrefLong(this, PREF_TOTAL_TIME);

        double allExerciseDistance = 0;
        long allExerciseTime = 0;

        if((savedDistance != 0.0 || savedTime != 0) && Commons.isNull(historyItem)) {
            allExerciseDistance = savedDistance;
            allExerciseTime = savedTime;
        } else if(Commons.isNotNull(items)) {
            for (HistoryItem item : items) {
                allExerciseDistance += item.getDistance();
                allExerciseTime += Commons.dateParseRegExp(item.getElaspedTime());
            }
        } else if(Commons.isNotNull(historyItem)) {
            allExerciseDistance = savedDistance + MapUtils.getDistance(historyItem.getLocations());
            allExerciseTime = savedTime + Commons.dateParseRegExp(historyItem.getElaspedTime());
        }

        if(Preferences.isFirstRun(this)) {
            summaryDistance.setText(R.string.distance_demo);
            summaryTime.setText(R.string.time_demo);
        } else {
            summaryDistance.setText(MapUtils.getFormattedDistance(allExerciseDistance));

            if (allExerciseTime == 0)
                summaryTime.setText(R.string.time_empty);
            else
                summaryTime.setText(Commons.formatTimeString(allExerciseTime));

            Preferences.putPrefDouble(this, PREF_TOTAL_DISTANCE, allExerciseDistance);
            Preferences.putPrefLong(this, PREF_TOTAL_TIME, allExerciseTime);
        }
    }

    public void hideView(View v) {
        float temp = getResources().getDimension(R.dimen.fab_margin);
        int duration = getResources().getInteger(R.integer.gallery_alpha_duration);
        ViewUtil.hideView(v, duration, (int) temp);
    }

    public void showView(View v) {
        int duration = getResources().getInteger(R.integer.gallery_alpha_duration);
        ViewUtil.showView(v, duration);
    }

    @OnClick(R.id.fabSettings)
    void showSettings() {

        if(!isRunning) {
            Intent intent = new Intent(ServicedActivity.this, SettingsActivity.class);
            startActivityForResult(intent, RESULT_PREFERENCES);
        } else {
            if(mLocations != null && mLocations.size() > 1) {
                Intent mapIntent = new Intent(this, GoogleMapsActivity.class);
                mapIntent.putExtra(GoogleMapsActivity.MAP_SHOW_MY_LOCATION, true);
                mapIntent.putParcelableArrayListExtra(GoogleMapsActivity.MAP_POINTS, mLocations);
                mapIntent.putExtra(GoogleMapsActivity.MAP_TIME, elapsedTime.getText().toString());
                mapIntent.putExtra(GoogleMapsActivity.MAP_TITLE, "Current Run");
                startActivity(mapIntent);
            } else {
                if(Preferences.isLocationEnabled(this)) {
                    showSnack("Waiting for GPS readings...", R.color.md_red_700);
                } else {
                    Snackbar snackbar = Snackbar.make(fabTimer, "GPS is disabled", Snackbar.LENGTH_LONG);
                    View view = snackbar.getView();
                    view.setBackgroundColor(ContextCompat.getColor(this, R.color.md_red_700));
                    snackbar.setAction("Enable", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (ActivityCompat.checkSelfPermission(ServicedActivity.this,
                                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(ServicedActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, RESULT_DYNAMIC_LOCATION_PERMISSION);
                            } else {
                                Preferences.enableLocationServices(ServicedActivity.this, true);
                                programBinder.registerGpsUpdates();
                            }
                        }
                    }).setActionTextColor(getResources().getColor(R.color.black));
                    snackbar.show();
                }
            }
        }
    }

    public void switchFabSettings() {
        if (isRunning) {
            ViewUtil.changeFabColour(this, fabSettings, R.color.colorPrimary);
            ViewUtil.changeFabIcon(this, fabSettings, R.drawable.ic_gps);
        } else {
            // POI list is empty
            ViewUtil.changeFabColour(this, fabSettings, R.color.colorPrimary);
            ViewUtil.changeFabIcon(this, fabSettings, R.drawable.ic_settings);
        }
    }

    private void changeFabAppearance(Running state) {
        if (state == Running.WORK) {
            ViewUtil.changeFabIcon(this, fabTimer, android.R.drawable.ic_media_play);
        } else if (state == Running.PAUSE) {
            ViewUtil.changeFabIcon(this, fabTimer, android.R.drawable.ic_media_pause);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.values[0] == 0 && proximity && debounce()) {
            fabClick();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serviced);
        ButterKnife.bind(this);

        if (ACTION_CONTINUE_RUN.equals(getIntent().getAction())) {

            Log.d(TAG, "onCreate: Call from Notification");

        } else {
            // Sets the hardware button to control music volume
            setVolumeControlStream(AudioManager.STREAM_MUSIC);

            hideView(resetLayer);
            roundCurrent = 0;

            loadPreferences(savedInstanceState);

            setRoundTextView();
            changeFabAppearance(Running.WORK);

            setViewColors(null);

            loadDatabase();

            if(!isRunning)  showRatingDialog();
        }
    }

    @Override
    public void onBackPressed() {
        if (programBinder != null && programBinder.isActive())
            showSnack("Stop and Reset timer to exit", R.color.md_red_700);
        else
            super.onBackPressed();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_PREFERENCES:
                loadPreferences(null);
                break;
            case RESULT_LOCATION_SOURCE_SETTINGS:
                if(resultCode == RESULT_OK)
                    showSnack("Route logging enabled", R.color.md_green_700);
                else
                    showSnack("Continuing without GPS Route Logging", R.color.md_red_700);
                startPauseTimer();
                break;
            default:
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(connection == null)
            connection = new RunnerServiceConnection();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (proximity) {
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if(isRunning) {
            doBindService();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (proximity) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            getApplicationContext().unbindService(connection);
        } catch (IllegalArgumentException e) { /* EMPTY */ }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setRoundTextView() {
        textViewRounds.setText(String.format(Locale.US, "%d/%d", roundCurrent, roundsTotal));
    }

    private String getRandomString(@ArrayRes int quotesArray) {
        String[] quotes = getResources().getStringArray(quotesArray);
        Random rand = new Random();
        int i = rand.nextInt(quotes.length);
        return quotes[i];
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isRunning", isRunning);
        outState.putInt("roundCurrent", roundCurrent);
        outState.putLong("workTimeMillis", workTimeMillis);
        outState.putLong("restTimeMillis", restTimeMillis);
        outState.putString("timerRemaining", timerRemaining.getText().toString());

        outState.putString("workoutTime", workoutTime.getText().toString());
        outState.putString("elapsedTime", elapsedTime.getText().toString());

        if(programBinder != null) {
            outState.putBoolean("isPaused", programBinder.isPaused());
        }
    }

    private void loadPreferences(Bundle bundle) {
        proximity = Preferences.useProximitySensor(this);
        if (proximity) {
            sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
            proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        }

        roundCurrent = 0;

        if(bundle != null) {
            isRunning = bundle.getBoolean("isRunning");
            keepScreenOn(isRunning);

            if(isRunning) {
                summaryFrame.setVisibility(View.GONE);          // hide history
                timerMessage.setVisibility(View.VISIBLE);       // show motivation quotes box - filled with text on exercise change
                fabTimer.setOnTouchListener(fabTouchListener);
                switchFabSettings();
                //fabSettings.hide();
                changeFabAppearance(Running.WORK);
                roundCurrent = bundle.getInt("roundCurrent");
            } else {
                if (!Preferences.showSummary(this))
                    summaryFrame.setVisibility(View.GONE);
            }

            isPause = bundle.getBoolean("isPaused", false);
            if(isPause) {
                showView(resetLayer);
                fabTimer.setOnTouchListener(null);
            }

            workTimeMillis = bundle.getLong("workTimeMillis");
            restTimeMillis = bundle.getLong("restTimeMillis");

            timerRemaining.setText(bundle.getString("timerRemaining"));

            workoutTime.setText(bundle.getString("workoutTime"));
            elapsedTime.setText(bundle.getString("elapsedTime"));

        }else {
            workTimeMillis = PickerPrefDialog.getMillis(Preferences.getWorkTime(this));
            restTimeMillis = PickerPrefDialog.getMillis(Preferences.getRestTime(this));

            if (!Preferences.showSummary(this))
                summaryFrame.setVisibility(View.GONE);
        }

        prepEnabled = Preferences.isPrepEnabled(this);

        setDisplayViews();

        if(bundle == null) {
            WorkoutProgram program = ProgramDAOSqlite.getInstance(getApplicationContext()).getProgram(SIMPLE_RUN_PROGRAM, true);
            while (program.getAssociatedNode().hasChildren()) {
                final List<WorkoutItem> oldItems = program.getAssociatedNode().getChildren();
                program.getAssociatedNode().removeChild(oldItems.get(0));
            }
            if (prepEnabled) {
                program.getAssociatedNode().setTotalReps(1);
                program.getAssociatedNode().addChildExercise("Preparation", PREP_TIME_MILLI, EffortLevel.EASY);
                WorkoutItem exercises = program.getAssociatedNode().addChildNode(roundsTotal);
                exercises.addChildExercise("Run", (int) workTimeMillis, EffortLevel.HARD);
                exercises.addChildExercise("Walk", (int) restTimeMillis, EffortLevel.REST);

            } else {
                program.getAssociatedNode().setTotalReps(roundsTotal);
                program.getAssociatedNode().addChildExercise("Run", (int) workTimeMillis, EffortLevel.HARD);
                program.getAssociatedNode().addChildExercise("Walk", (int) restTimeMillis, EffortLevel.REST);
            }
            ProgramDAOSqlite.getInstance(getApplicationContext()).saveProgram(program);
        }
    }

    private void setDisplayViews() {
        roundsTotal = Integer.parseInt(Preferences.getRoundsCount(this));
        setRoundTextView();

        totalWorkoutTime = (workTimeMillis * roundsTotal) + (restTimeMillis * (roundsTotal - 1)) + (prepEnabled ? PREP_TIME_MILLI : 0);
        runTime = Commons.formatString(TimeUnit.MILLISECONDS.toMinutes(workTimeMillis), TimeUnit.MILLISECONDS.toSeconds(workTimeMillis) % 60);
        workPreviewText.setText(String.format(getString(R.string.work), runTime));
        String restTime = Commons.formatString(TimeUnit.MILLISECONDS.toMinutes(restTimeMillis), TimeUnit.MILLISECONDS.toSeconds(restTimeMillis) % 60);
        restPreviewText.setText(String.format(getString(R.string.rest), restTime));

        if(!isRunning) {
            workoutTime.setText(Commons.formatTimeString(totalWorkoutTime));
            timerRemaining.setText(prepEnabled ? Commons.formatTimeString(PREP_TIME_MILLI) : runTime);
            stepCountView.setText(String.format(getString(R.string.step_settings), Preferences.getStepSensitivity(this)));
        }
    }


    private class RunnerServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            programBinder = (ProgramBinder) service;
            programBinder.registerCountDownObserver(countDownObserver);

            if (programBinder.isActive()) {
                setRoundTextView();
                setViewColors(programBinder.getCurrentExercise().getEffortLevel());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            programBinder = null;
        }
    }

    public void doBindService() {
        Intent serviceIntent = new Intent(this, ProgramRunService.class);
        serviceIntent.putExtra(WorkoutMetaData.PROGRAM_ID_NAME, SIMPLE_RUN_PROGRAM);
        getApplicationContext().bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
    }

    private final CountDownObserver countDownObserver = new CountDownObserver() {
        private long prepTime;
        private EffortLevel previousEffortLvl = EffortLevel.HARD;

        @Override
        public void onTick(long exerciseMsRemaining, long programMsRemaining, long stepCount) {
            if(isRunning) {

                // scale down the prep time, just showing off really
                if(programBinder.getCurrentExercise().getEffortLevel().equals(EffortLevel.EASY)) {
                    Animation animation = AnimationUtils.loadAnimation(ServicedActivity.this, R.anim.scale_down);
                    animation.setDuration(1000);
                    timerRemaining.setAnimation(animation);
                    animation.start();
                }

                long mins = TimeUnit.MILLISECONDS.toMinutes(exerciseMsRemaining);
                long secs = TimeUnit.MILLISECONDS.toSeconds(exerciseMsRemaining) % 60;
                timerRemaining.setText(Commons.formatString(mins, secs));

                workoutTime.setText(Commons.formatTimeString(programMsRemaining - restTimeMillis));  // remove the last rest period
                elapsedTime.setText(Commons.formatTimeString(totalWorkoutTime - programMsRemaining + restTimeMillis - prepTime));

                stepCountView.setText(String.format(getString(R.string.steps_count), stepCount));
            }
        }

        @Override
        public void onExerciseStart(Exercise exercise) {

            if(previousEffortLvl.equals(EffortLevel.REST) && exercise.getEffortLevel().equals(EffortLevel.HARD)) {
                roundCurrent++;
                setRoundTextView();
                setViewColors(exercise.getEffortLevel());
            } else if(roundCurrent == roundsTotal && exercise.getEffortLevel().equals(EffortLevel.REST)) {
                Log.d(TAG, "onExerciseStart: ");
                programBinder.stop();
            } else
                setViewColors(exercise.getEffortLevel());

            previousEffortLvl = exercise.getEffortLevel();
        }

        @Override
        public void onProgramFinish() {
            timerRemaining.setText("--:--");
            setViewColors(EffortLevel.NONE);

            lockOrientation();
            if(isRunning) {
                showView(resetLayer);
                fabTimer.hide();
                isRunning = false;
            }
        }

        @Override
        public void onPause() {
        }

        @Override
        public void onResume() {
            hideView(resetLayer);
        }

        @Override
        public void onStart() {
            prepTime = prepEnabled ? PREP_TIME_MILLI : -1000;
            mLocations = new ArrayList<>();
        }


        @Override
        public void onLocationsUpdate(ArrayList<LatLon> locations) {
            mLocations = locations;
        }

        @Override
        public void onGpsResults(ArrayList<LatLon> locations) {
            mLocations = locations;
        }

        @Override
        public void onError(ProgramError error) {
            Toast.makeText(ServicedActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
        }
    };

    private boolean debounce() {
        if (debounceCounter < System.currentTimeMillis()) {
            debounceCounter = System.currentTimeMillis() + 1000;
            return false;
        }
        return true;
    }

    private void showSnack(String text, @ColorRes int color) {
        Snackbar snackbar = Snackbar.make(fabTimer, text, Snackbar.LENGTH_LONG);
        View view = snackbar.getView();
        view.setBackgroundColor(ContextCompat.getColor(this, color));
        snackbar.show();
    }

    private void keepScreenOn(boolean keepOn) {
        if (Preferences.stayAwake(this)) {
            fabTimer.setKeepScreenOn(keepOn);
            if(keepOn)
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            else
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        }
    }

    private void lockOrientation() {
        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        else
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
    }

    private void unlockOrientation() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }


    private void bgAnimation(@Nullable EffortLevel lvl) {
        if(lvl == null) {
            if(bgAnimation != null)
                bgAnimation.stop();
            bgAnimation = (AnimationDrawable) timerBackground.getBackground();
            bgAnimation.setExitFadeDuration(6000);
        }
        if (bgAnimation != null)
            if(!bgAnimation.isRunning() && lvl == null) {
                bgAnimation.start();
            } else {
                bgAnimation.stop();
                bgAnimation = null;
            }
    }

    private void setViewColors(@Nullable EffortLevel lvl) {
        Boolean showQuotes = Preferences.showQuotes(this);
        if(!showQuotes)
            timerMessage.setTextSize(getResources().getDimension(R.dimen.auto_size_text_default));

        if (lvl == null) {
//            timerBackground.setBackgroundResource(R.drawable.timer_bg_primary);
            timerBackground.setBackgroundResource(R.drawable.animation_list);
            timerMessage.setTextColor(ContextCompat.getColor(this, R.color.colorWork));

        } else if (lvl == EffortLevel.HARD) {
            timerBackground.setBackgroundResource(R.drawable.timer_bg_work);
            timerMessage.setTextColor(ContextCompat.getColor(this, R.color.md_green_300));
            if (showQuotes)
                timerMessage.setText(getRandomString(R.array.workQuotes));
            else
                timerMessage.setText(R.string.workDefaultMsg);

        } else if (lvl == EffortLevel.REST) {
            timerBackground.setBackgroundResource(R.drawable.timer_bg_rest);
            timerMessage.setTextColor(ContextCompat.getColor(this, R.color.md_light_blue_300));
            if (showQuotes)
                timerMessage.setText(getRandomString(R.array.restQuotes));
            else
                timerMessage.setText(R.string.restDefaultMsg);

        } else if (lvl == EffortLevel.EASY) {
            timerBackground.setBackgroundResource(R.drawable.timer_bg_prep);
            timerMessage.setTextColor(ContextCompat.getColor(this, R.color.md_amber_200));
            if (showQuotes)
                timerMessage.setText(getRandomString(R.array.prepQuotes));
            else
                timerMessage.setText(R.string.prepDefaultMsg);

        } else if (lvl == EffortLevel.NONE) {
            timerBackground.setBackgroundResource(R.drawable.timer_bg_primary);
            timerMessage.setTextColor(ContextCompat.getColor(this, R.color.white));
            timerMessage.setText(R.string.workoutComplete);
        }

        bgAnimation(lvl);
    }

    /**************************************/




    private void gpsPermission() {
        String mPermission = Manifest.permission.ACCESS_FINE_LOCATION;
        try {
            if (ActivityCompat.checkSelfPermission(this, mPermission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{mPermission}, RESULT_LOCATION_PERMISSION );
            }else{
                if (!canGetLocation()) {
                    showSettingsAlert();
                } else
                    startPauseTimer();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case RESULT_LOCATION_PERMISSION :
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (!canGetLocation()) {
                        showSettingsAlert();
                    } else
                        startPauseTimer();
                } else {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                    alertDialog.setTitle("GPS settings");
                    alertDialog.setIcon(R.drawable.ic_location_off);
                    alertDialog.setMessage("Location services will be disabled. You can enable it in the settings");
                    alertDialog.setPositiveButton("Disable", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Preferences.enableLocationServices(ServicedActivity.this, false);
                            startPauseTimer();
                            dialog.dismiss();
                        }
                    });

                    alertDialog.setNegativeButton("Retry", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            gpsPermission();
                            dialog.dismiss();
                        }
                    });
                    alertDialog.show();
                }
                break;

            case RESULT_DYNAMIC_LOCATION_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && programBinder != null) {
                    Preferences.enableLocationServices(ServicedActivity.this, true);
                    programBinder.registerGpsUpdates();
                } else
                    Log.d(TAG, "onRequestPermissionsResult: GPS disabled");
                break;

        }
    }


    public boolean canGetLocation() {
        return isNetworkEnabled() || isGPSEnabled();
    }

    private boolean isNetworkEnabled() {
        return ((LocationManager) getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private boolean isGPSEnabled() {
        return ((LocationManager) getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * Function to show settings alert dialog
     * */
    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("GPS settings");
        alertDialog.setIcon(R.drawable.ic_location_off);
        alertDialog.setMessage("GPS is not enabled. Enable GPS or Cancel to continue without route recording");
        alertDialog.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent, RESULT_LOCATION_SOURCE_SETTINGS);
            }
        });

        alertDialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                startPauseTimer();
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }
}
