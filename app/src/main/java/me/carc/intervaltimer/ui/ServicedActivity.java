package me.carc.intervaltimer.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.ArrayRes;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import me.carc.intervaltimer.data.local.ProgramDAOSqlite;
import me.carc.intervaltimer.db.AppDatabase;
import me.carc.intervaltimer.model.EffortLevel;
import me.carc.intervaltimer.model.Exercise;
import me.carc.intervaltimer.model.HistoryItem;
import me.carc.intervaltimer.model.WorkoutGroup;
import me.carc.intervaltimer.model.WorkoutItem;
import me.carc.intervaltimer.model.WorkoutMetaData;
import me.carc.intervaltimer.services.ProgramRunService;
import me.carc.intervaltimer.services.interfaces.CountDownObserver;
import me.carc.intervaltimer.services.interfaces.ProgramBinder;
import me.carc.intervaltimer.settings.Preferences;
import me.carc.intervaltimer.settings.SettingsActivity;
import me.carc.intervaltimer.ui.adapters.HistoryAdapter;
import me.carc.intervaltimer.ui.listeners.ClickListener;
import me.carc.intervaltimer.utils.Commons;
import me.carc.intervaltimer.utils.ViewUtil;
import me.carc.intervaltimer.widgets.AutoResizeTextView;
import me.carc.intervaltimer.widgets.HistoryViewerDialog;
import me.carc.intervaltimer.widgets.NumberPickerBuilder;
import me.carc.intervaltimer.widgets.PickerPrefDialog;
import me.carc.intervaltimer.widgets.circle_progress.DonutProgress;
import me.carc.intervaltimer.widgets.listeners.HistoryItemLockListener;
import me.carc.intervaltimer.widgets.listeners.NumberSetListener;

public class ServicedActivity extends Activity implements SensorEventListener {


    private enum Running {WORK, PAUSE}

    private static final String TAG = ServicedActivity.class.getName();
    public static final String ACTION_CONTINUE_RUN = "CONTINUE_RUN";
    private final int PREP_TIME_MILLI = 7000;

    private final int RESULT_PREFERENCES = 159;

    private String runTime;
    private long workTimeMillis, restTimeMillis, debounceCounter, totalWorkoutTime;
    private AnimationDrawable bgAnimation;
    private int roundCurrent, roundsTotal;
    private boolean prepEnabled, proximity;

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
    @BindView(R.id.timerRemaining)  TextView timerRemaining;
    @BindView(R.id.timerMessage)    AutoResizeTextView timerMessage;
    @BindView(R.id.round_number)    TextView textViewRounds;
    @BindView(R.id.fabSettings)     FloatingActionButton fabSettings;
    @BindView(R.id.fabTimer)        FloatingActionButton fabTimer;
    @BindView(R.id.resetLayer)      RelativeLayout resetLayer;
    @BindView(R.id.resetBtn)        Button resetBtn;
    @BindView(R.id.recyclerView)    RecyclerView recyclerView;
    @BindView(R.id.fabDonutProgress)DonutProgress fabDonutProgress;


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


    @OnClick(R.id.fabTimer)
    void fabClick() {
        fabTimer.setOnTouchListener(fabTouchListener);
        changeFabAppearance(Running.PAUSE);

        if (programBinder == null) {
            if (totalWorkoutTime == 0) {
                Log.d(TAG, "instance initializer: ");
            } else {

                Intent serviceIntent = new Intent(this, ProgramRunService.class);
                serviceIntent.putExtra(WorkoutMetaData.PROGRAM_ID_NAME, 2);

                startService(serviceIntent);
                getApplicationContext().bindService(serviceIntent, connection, Context.BIND_ABOVE_CLIENT);

                isRunning = true;

                timerMessage.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);

                fabSettings.hide();
                keepScreenOn(true);
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

        try {
            getApplicationContext().unbindService(connection);
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, "Service Close Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            // Service was never bound, meh
        }

        addWorkoutToDatabase();
        unlockOrientation();

        timerMessage.setVisibility(View.GONE);
        if (Preferences.showHistory(this))
            recyclerView.setVisibility(View.VISIBLE);


        roundCurrent = 0;
        setRoundTextView();
        timerRemaining.setText(runTime);
        elapsedTime.setText("");

        changeFabAppearance(Running.WORK);
        setViewColors(null);

        hideView(resetLayer);
        fabSettings.show();

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


    private int getDataUUID() {
        return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
    }


    private void addWorkoutToDatabase() {
        final int id = getDataUUID();
        final String date = Commons.readableDate(System.currentTimeMillis());
        final String remainTitle = workoutTime.getText().toString();
        final String elaspedTime = elapsedTime.getText().toString();
        final int roundsCompleted = this.roundCurrent;
        final int roundsTotal = this.roundsTotal;
        final String workTime = Preferences.getWorkTime(this);
        final String restTime = Preferences.getRestTime(this);
        final HistoryItem historyItem = new HistoryItem(id, date, "Rename Me", remainTitle, elaspedTime, roundsCompleted, roundsTotal, workTime, restTime, false);

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                AppDatabase db = ((App) getApplicationContext()).getDB();
                db.historyDao().insert(historyItem);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((HistoryAdapter) recyclerView.getAdapter()).addItem(historyItem);
                        recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
                    }
                });
            }
        });
    }

    private void updateItemToDatabase(final HistoryItem historyItem) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                AppDatabase db = ((App) getApplicationContext()).getDB();
                db.historyDao().insert(historyItem);
            }
        });
    }

    /**
     * // TODO: 05/02/2018 show progress dialog as this can take time
     *
     * @param keyId the history item index
     * @return the history item
     */
    private HistoryItem getFromDatabase(int keyId) {
        AppDatabase db = ((App) getApplicationContext()).getDB();
        return db.historyDao().findByIndex(keyId);
    }

    private void loadDatabase() {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                AppDatabase db = ((App) getApplicationContext()).getDB();
                final List<HistoryItem> items = db.historyDao().getAllEntries();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((HistoryAdapter) recyclerView.getAdapter()).addItems(items);
                    }
                });
            }
        });
    }

    private void removeFromDatabase(final int itemIndex) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                AppDatabase db = ((App) getApplicationContext()).getDB();
                HistoryItem item = db.historyDao().findByIndex(itemIndex);
                if (Commons.isNotNull(item))
                    db.historyDao().delete(item);
            }
        });
    }

    private void nukeDatabase() {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                AppDatabase db = ((App) getApplicationContext()).getDB();

                List<HistoryItem> allItems = db.historyDao().getAllEntries();

                for (HistoryItem item : allItems) {
                    if(!item.isLocked()){
                        db.historyDao().delete(item);
                    }
                }
                loadDatabase();
//                db.historyDao().nukeTable();
            }
        });
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
        Intent intent = new Intent(ServicedActivity.this, SettingsActivity.class);
        startActivityForResult(intent, RESULT_PREFERENCES);
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
        if (event.values[0] == 0 && proximity && debounceCounter < System.currentTimeMillis()) {
            debounceCounter = System.currentTimeMillis() + 1000;  //
            fabClick();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // Sets the hardware button to control music volume
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        hideView(resetLayer);
        roundCurrent = 0;

        loadPreferences(savedInstanceState);

        setRoundTextView();
        changeFabAppearance(Running.WORK);

        setViewColors(null);

        initHistory();

    }

    @Override
    public void onBackPressed() {
        if (programBinder != null && programBinder.isActive())
            showSnack("Stop and Reset timer to exit", R.color.md_red_700);
        else if (debounce())
            super.onBackPressed();
        else
            showSnack("Press twice to exit", R.color.md_red_700);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_PREFERENCES:
                loadPreferences(null);
                break;
            default:
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        connection = new RunnerServiceConnection();
    }

    @Override
    protected void onResume() {
        ((App) getApplication()).setActive(true);
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
        ((App) getApplication()).setActive(false);
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


    private void initHistory() {
        HistoryAdapter adapter = new HistoryAdapter(new ClickListener() {
            @Override
            public void onClick(final HistoryItem item) {
                HistoryViewerDialog viewer = new HistoryViewerDialog(ServicedActivity.this, item, new HistoryItemLockListener() {
                    @Override
                    public void onLockItme(boolean lock) {
                        item.setLocked(lock);
                        updateItemToDatabase(item);
                    }
                });
                viewer.show();
            }

            @Override
            public void onLongClick(final HistoryItem item) {
                AlertDialog.Builder dlg = new AlertDialog.Builder(ServicedActivity.this)
                        .setIcon(R.drawable.ic_delete)
                        .setTitle("Delete Entry?")
                        .setMessage("Do you really want to remove this entry?")
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                            }
                        })
                        .setNeutralButton("Remove All", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                removeAllItems();
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                removeFromDatabase(item.getKeyID());
                                HistoryAdapter historyAdapter = (HistoryAdapter) recyclerView.getAdapter();
                                int index = historyAdapter.getItemPosition(item);
                                if (historyAdapter.removeItem(index)) {
                                    historyAdapter.notifyItemRemoved(index);
                                    historyAdapter.notifyItemRangeChanged(index, historyAdapter.getItemCount());
                                }
                            }
                        });
                dlg.show();
            }
        });

        recyclerView.setAdapter(adapter);

        // set reverse layout for the items
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(mLayoutManager);

        loadDatabase();
    }


    private void setRoundTextView() {
        textViewRounds.setText(" " + roundCurrent + "/" + roundsTotal);
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
                recyclerView.setVisibility(View.GONE);          // hide history
                timerMessage.setVisibility(View.VISIBLE);       // show motivation quotes box - filled with text on exercise change
                fabTimer.setOnTouchListener(fabTouchListener);
                fabSettings.hide();
                changeFabAppearance(Running.WORK);
                roundCurrent = bundle.getInt("roundCurrent") - 1;
            } else {
                if (!Preferences.showHistory(this))
                    recyclerView.setVisibility(View.GONE);
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

            if (!Preferences.showHistory(this))
                recyclerView.setVisibility(View.GONE);
        }

        prepEnabled = Preferences.isPrepEnabled(this);

        setDisplayViews();

        if(bundle == null) {
            WorkoutGroup program = ProgramDAOSqlite.getInstance(getApplicationContext()).getProgram(1, true);
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
            workoutTime.setText(getElaspedTime(totalWorkoutTime));
            timerRemaining.setText(prepEnabled ? getElaspedTime(PREP_TIME_MILLI) : runTime);
        }
    }


    private class RunnerServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            programBinder = (ProgramBinder) service;
            programBinder.registerCountDownObserver(countDownObserver);

            if (programBinder.isActive()) {

                // TODO: 09/02/2018 clean up the rounds couner

                if(programBinder.getCurrentExercise().getEffortLevel().equals(EffortLevel.EASY)) {
                    roundCurrent = 0;
                }
                if(programBinder.getCurrentExercise().getEffortLevel().equals(EffortLevel.REST)) {
                    roundCurrent++;
                }
                setRoundTextView();
                setViewColors(programBinder.getCurrentExercise().getEffortLevel());
            } else {
                Log.d(TAG, "programBinder.NOT Active: ");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            programBinder = null;
        }
    }

    public void doBindService() {
        Intent serviceIntent = new Intent(this, ProgramRunService.class);
        serviceIntent.putExtra(WorkoutMetaData.PROGRAM_ID_NAME, 1);
        getApplicationContext().bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
    }

    private final CountDownObserver countDownObserver = new CountDownObserver() {
        private long prepTime;

        @Override
        public void onTick(long exerciseMsRemaining, long programMsRemaining) {
            long mins = TimeUnit.MILLISECONDS.toMinutes(exerciseMsRemaining);
            long secs = TimeUnit.MILLISECONDS.toSeconds(exerciseMsRemaining) % 60;
            timerRemaining.setText(Commons.formatString(mins, secs));

            workoutTime.setText(getElaspedTime(programMsRemaining - restTimeMillis));  // remove the last rest period

            elapsedTime.setText(getElaspedTime(totalWorkoutTime - programMsRemaining + restTimeMillis - prepTime));
        }

        @Override
        public void onExerciseStart(Exercise exercise) {
            if(roundCurrent == roundsTotal && exercise.getEffortLevel().equals(EffortLevel.REST)) {
                Log.d(TAG, "onExerciseStart: ");
                programBinder.stop();
            } else
                setViewColors(exercise.getEffortLevel());
        }

        @Override
        public void onProgramFinish() {
            setRoundTextView();
            timerRemaining.setText("--:--");

            lockOrientation();
            if(isRunning) {
                showView(resetLayer);
                fabTimer.hide();
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
        }

        @Override
        public void onError(ProgramError error) {
            Toast.makeText(ServicedActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
        }
    };


    private String getElaspedTime(long millis) {
        long second = (millis / 1000) % 60;
        long minute = (millis / (1000 * 60)) % 60;
        long hour = (millis / (1000 * 60 * 60)) % 24;

        if (hour > 0)
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", hour, minute, second);
        else
            return String.format(Locale.getDefault(), "%02d:%02d", minute, second);
    }

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
//            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//            PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, getPackageName());
            if(keepOn) {
//                wakeLock.acquire();
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            } else {
//                wakeLock.release();
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
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


    private void removeAllItems() {

        AlertDialog.Builder dlg = new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_delete_all)
                .setTitle("Remove Unlocked Entries?")
                .setMessage("This will delete all unlocked entries? You can not undo this")
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                })
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((HistoryAdapter) recyclerView.getAdapter()).removeAll();
                        nukeDatabase();
                        dialog.dismiss();
                    }
                });
        dlg.show();
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

//   private Painter bgPaint;


    private void setViewColors(@Nullable EffortLevel lvl) {
        Boolean showQuotes = Preferences.showQuotes(this);

//        if(bgPaint != null)
//            bgPaint.stopAnimating();

        if (lvl == null) {
            timerBackground.setBackgroundResource(R.drawable.timer_bg_primary);
            timerBackground.setBackgroundResource(R.drawable.animation_list);
//            bgPaint = new Painter(Painter.getRandColor());
//            bgPaint.animate(timerBackground, Painter.getRandColor());

            timerMessage.setTextColor(ContextCompat.getColor(this, R.color.colorMainBackgroundSecondary));

        } else if (lvl == EffortLevel.HARD) {
            timerBackground.setBackgroundResource(R.drawable.timer_bg_work);
            timerMessage.setTextColor(ContextCompat.getColor(this, R.color.md_green_300));
            if (showQuotes)
                timerMessage.setText(getRandomString(R.array.workQuotes));
            else
                timerMessage.setText(R.string.workDefaultMsg);

            ++roundCurrent;
            setRoundTextView();

//            bgPaint = new Painter(ContextCompat.getColor(this, R.color.md_light_green_700));
//            bgPaint.animate(timerBackground, ContextCompat.getColor(this, R.color.md_green_700));


        } else if (lvl == EffortLevel.REST) {
            timerBackground.setBackgroundResource(R.drawable.timer_bg_rest);
            timerMessage.setTextColor(ContextCompat.getColor(this, R.color.md_light_blue_300));
            if (showQuotes)
                timerMessage.setText(getRandomString(R.array.restQuotes));
            else
                timerMessage.setText(R.string.restDefaultMsg);

//            bgPaint = new Painter(ContextCompat.getColor(this, R.color.md_light_blue_600));
//            bgPaint.animate(timerBackground, ContextCompat.getColor(this, R.color.md_blue_700));

        } else if (lvl == EffortLevel.EASY) {
            timerBackground.setBackgroundResource(R.drawable.timer_bg_prep);
            timerMessage.setTextColor(ContextCompat.getColor(this, R.color.md_amber_200));
            if (showQuotes)
                timerMessage.setText(getRandomString(R.array.prepQuotes));
            else
                timerMessage.setText(R.string.prepDefaultMsg);

//            bgPaint = new Painter(ContextCompat.getColor(this, R.color.md_amber_400));
//            bgPaint.animate(timerBackground, ContextCompat.getColor(this, R.color.md_amber_600));

        } else if (lvl == EffortLevel.NONE) {
            timerBackground.setBackgroundResource(R.drawable.timer_bg_primary);
            timerMessage.setTextColor(ContextCompat.getColor(this, R.color.white));
            timerMessage.setText(R.string.workoutComplete);
        }

        bgAnimation(lvl);
    }
}
