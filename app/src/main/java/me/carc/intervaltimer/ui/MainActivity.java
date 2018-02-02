package me.carc.intervaltimer.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.annotation.ArrayRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
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
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import me.carc.intervaltimer.App;
import me.carc.intervaltimer.ui.adapters.HistoryAdapter;
import me.carc.intervaltimer.R;
import me.carc.intervaltimer.db.AppDatabase;
import me.carc.intervaltimer.model.HistoryItem;
import me.carc.intervaltimer.settings.Preferences;
import me.carc.intervaltimer.settings.SettingsActivity;
import me.carc.intervaltimer.utils.Commons;
import me.carc.intervaltimer.utils.ViewUtil;
import me.carc.intervaltimer.widgets.PickerPrefDialog;

public class MainActivity extends Activity implements SensorEventListener {
    public interface ClickListener {
        void onClick(HistoryItem item);

        void onLongClick(HistoryItem item);
    }

    private static final String TAG = MainActivity.class.getName();
    private final int SECONDS_MILLI = 1000;
    private final int PREP_TIME_MILLI = 5000;

    private boolean isRunning = false;
    private String runTime;
    private long workTimeMillis, restTimeMillis, endRoundWarnMillis, proximityDebounce;

    private enum State {REST, WORK, PREP, DONE}

    private enum Running {WORK, PAUSE}

    private State state = null;
    private int roundCurrent, roundsTotal;

    private Counter workTimer, restTimer, prepTimer;

    private SoundPool soundPool;
    private boolean loaded, mute, proximity;
    private int soundPrepId, soundFinishedId;

    private Vibrator vibrator;
    private long[] vPattern = {0, 300, 100, 300, 100, 300};
    private SensorManager sensorManager;
    private Sensor proximitySensor;

    // TempTimer object for pausing and resuming
    private long tempMillisLeft = 0;
    // SensorEventListener Override Methods

    Observable<Long> observable;
    Disposable elapsedTimeDisposable;


    @BindView(R.id.elapsedTime)         TextView elapsedTime;
    @BindView(R.id.timerBackground)     RelativeLayout timerBackground;
    @BindView(R.id.workPreviewText)     TextView workPreviewText;
    @BindView(R.id.restPreviewText)     TextView restPreviewText;
    @BindView(R.id.time_panel)          LinearLayout timeLeftPanel;
    @BindView(R.id.time_left_textView)  TextView textViewTime;
    @BindView(R.id.timerMessage)        TextView timerMessage;
    @BindView(R.id.round_number)        TextView textViewRounds;
    @BindView(R.id.fabSettings)         FloatingActionButton fabSettings;
    @BindView(R.id.fab)                 FloatingActionButton fab;
    @BindView(R.id.resetLayer)          RelativeLayout resetLayer;
    @BindView(R.id.resetBtn)            Button resetBtn;
    @BindView(R.id.recyclerView)        RecyclerView recyclerView;


    Observer<Long> observerTime = new Observer<Long>() {
        @Override
        public void onSubscribe(Disposable d) {
            elapsedTime.setVisibility(View.VISIBLE);
            elapsedTimeDisposable = d;
        }

        @Override
        public void onNext(Long value) {
            long millis = value * 1000;
            long second = (millis / 1000) % 60;
            long minute = (millis / (1000 * 60)) % 60;
            long hour = (millis / (1000 * 60 * 60)) % 24;
            elapsedTime.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", hour, minute, second));
        }

        @Override
        public void onError(Throwable e) {
            Log.e(TAG, "onError: ");
        }

        @Override
        public void onComplete() {
            elapsedTime.setVisibility(View.INVISIBLE);
            elapsedTimeDisposable.dispose();
        }
    };

    @OnClick(R.id.fab)
    void fabClick() {
        fabSettings.hide();
        fab.setKeepScreenOn(Preferences.stayAwake(this));
        timerMessage.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        if (prepTimer == null && roundCurrent == 1 && state == State.PREP) {
            prepTimer = new Counter(PREP_TIME_MILLI, SECONDS_MILLI);
            isRunning = true;
            prepTimer.start();
            setViewColors();
            changeFabAppearance(Running.PAUSE);

            observable = Observable.interval(1, TimeUnit.SECONDS, Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
            observable.subscribe(observerTime);

        } else if (workTimer == null && roundCurrent == 1 && state == State.WORK) {
            workTimer = new Counter(workTimeMillis, SECONDS_MILLI);
            isRunning = true;
            workTimer.start();
            setViewColors();
            changeFabAppearance(Running.PAUSE);

            observable = Observable.interval(1, TimeUnit.SECONDS, Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
            observable.subscribe(observerTime);

        } else {
            if (!isRunning) {
                if (state != State.DONE)
                    hideView(resetLayer);

                if (state == State.WORK)
                    workTimerResume();
                else if (state == State.REST)
                    restTimerResume();
                else if (state == State.PREP)
                    prepTimerResume();

            } else {
                if (state == State.WORK)
                    workTimerPause();
                else if (state == State.REST)
                    restTimerPause();
                else if (state == State.PREP)
                    prepTimerPause();

                showView(resetLayer);
            }
        }
    }

    @OnLongClick(R.id.resetBtn)
    boolean reset() {
        if (isRunning) {
            Commons.Toast(MainActivity.this, R.string.stop_toast, ContextCompat.getColor(this, R.color.darkRed), Toast.LENGTH_SHORT);
        } else {

            addToDatabase();

            timerMessage.setVisibility(View.GONE);
            if(Preferences.showHistory(this))
                recyclerView.setVisibility(View.VISIBLE);

            observerTime.onComplete();

            cancelNullTimers();
            tempMillisLeft = workTimeMillis;
            roundCurrent = 1;
            setRoundTextView();
            textViewTime.setText(runTime);
            if (Preferences.isPrepEnabled(this)) {
                state = State.PREP;
            } else {
                state = State.WORK;
            }
            isRunning = false;
            changeFabAppearance(Running.WORK);
            setViewColors();

            hideView(resetLayer);
            fabSettings.show();
            fab.setKeepScreenOn(false);
        }
        return true;
    }

    private int getDataUUID() {
        return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
    }


    private void addToDatabase() {
        final int id = getDataUUID();
        final String date = String.format(Locale.US, getString(R.string.history_item_date), Commons.readableDate(System.currentTimeMillis()));
        final String elaspedTime = elapsedTime.getText().toString();
        final int roundsCompleted = this.roundCurrent;
        final int roundsTotal = this.roundsTotal;
        final String workTime = Preferences.getWorkTime(this);
        final String restTime = Preferences.getRestTime(this);
        final HistoryItem historyItem = new HistoryItem(id, date, elaspedTime, roundsCompleted, roundsTotal, workTime, restTime);

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
                if(Commons.isNotNull(item))
                    db.historyDao().delete(item);
            }
        });
    }

    private void nukeDatabase() {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                AppDatabase db = ((App) getApplicationContext()).getDB();
                db.historyDao().nukeTable();
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
        if (isRunning) {
            Commons.Toast(MainActivity.this, R.string.stop_toast, ContextCompat.getColor(this, R.color.darkRed), Toast.LENGTH_SHORT);
        } else {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivityForResult(intent, 159);
        }
    }

    private void changeFabAppearance(Running state) {
        if (state == Running.WORK) {
//            ViewUtil.changeFabColour(this, fab, R.color.green);
            ViewUtil.changeFabIcon(this, fab, android.R.drawable.ic_media_play);
        } else if (state == Running.PAUSE) {
//            ViewUtil.changeFabColour(this, fab, R.color.red);
            ViewUtil.changeFabIcon(this, fab, android.R.drawable.ic_media_pause);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.values[0] == 0 && proximity && proximityDebounce < System.currentTimeMillis()) {
            proximityDebounce = System.currentTimeMillis() + 1000;  //
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

        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                loaded = true;
            }
        });
        soundPrepId = soundPool.load(this, R.raw.single_beep, 1);
        soundFinishedId = soundPool.load(this, R.raw.single_beep, 1);

        roundCurrent = 1;

        // Load shared preferences
        loadPreferences();

        // Initialize Vibration on phone
        vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
        textViewTime.setText(runTime);
        setRoundTextView();
        changeFabAppearance(Running.WORK);

        setViewColors();
        hideView(resetLayer);

        HistoryAdapter adapter = new HistoryAdapter(new MainActivity.ClickListener() {
            @Override
            public void onClick(HistoryItem item) {
            }

            @Override
            public void onLongClick(final HistoryItem item) {
                AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this)
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

        if(Preferences.isFirstRun(this)) {
            buildDummmyDatabaseEntry();
        }
    }


    private void removeAllItems() {

        AlertDialog.Builder dlg = new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_delete_all)
                .setTitle("Remove All Entries?")
                .setMessage("Do you really want to delete all entries? You can not undo this")
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                })
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((HistoryAdapter)recyclerView.getAdapter()).removeAll();
                        nukeDatabase();
                        dialog.dismiss();
                    }
                });
        dlg.show();
    }

    private HistoryItem buildDummmyDatabaseEntry() {
        String date = "Hold to Delete Entry";
        String elaspedTime = "Test Data";
        int roundsCompleted = 0;
        int roundsTotal = 0;
        String workTime = "03:00";
        String restTime = "01:00";
        return new HistoryItem(1, date, elaspedTime, roundsCompleted, roundsTotal, workTime, restTime);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 159:
                loadPreferences();
                textViewTime.setText(runTime);
                break;
            default:
        }
    }

    public void cancelNullTimers() {
        if (restTimer != null) {
            restTimer.cancel();
            restTimer = null;
        }
        if (workTimer != null) {
            workTimer.cancel();
            workTimer = null;
        }
        if (prepTimer != null) {
            prepTimer.cancel();
            prepTimer = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (proximity) {
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
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
    public void finish() {
        cancelNullTimers();
        super.finish();
    }

    @Override
    protected void onDestroy() {
        finish();
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
        if (item.getItemId() == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void playFinishedSound() {
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        float actualVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float volume = actualVolume / maxVolume;
        if (loaded && !mute) {
            soundPool.play(soundPrepId, volume, volume, 1, 0, 1f);
        }
        if (Preferences.useVibrate(this))
            vibrator.vibrate(vPattern, -1);
    }


    private void playPrepSound() {
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        float actualVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float volume = actualVolume / maxVolume;
        if (loaded && !mute) {
            soundPool.play(soundPrepId, volume, volume, 1, 0, 1f);
        }
        if (Preferences.useVibrate(this))
            vibrator.vibrate(vPattern, -1);
    }


    // TODO Clean up code : set restTimer/WorkTimer to null

    private void restTimerPause() {
        tempMillisLeft = restTimer.getMillisLeft();
        restTimer.cancel();
        changeFabAppearance(Running.WORK);
        isRunning = false;
    }

    private void restTimerResume() {
        restTimer = new Counter(tempMillisLeft, SECONDS_MILLI);
        isRunning = true;
        changeFabAppearance(Running.PAUSE);
        restTimer.start();
    }

    private void prepTimerPause() {
        tempMillisLeft = prepTimer.getMillisLeft();
        prepTimer.cancel();
        changeFabAppearance(Running.WORK);
        isRunning = false;
    }

    private void prepTimerResume() {
        prepTimer = new Counter(tempMillisLeft, SECONDS_MILLI);
        isRunning = true;
        changeFabAppearance(Running.PAUSE);
        prepTimer.start();
    }

    private void workTimerPause() {
        tempMillisLeft = workTimer.getMillisLeft();
        workTimer.cancel();
        changeFabAppearance(Running.WORK);
        isRunning = false;
    }

    private void workTimerResume() {
        workTimer = new Counter(tempMillisLeft, SECONDS_MILLI);
        isRunning = true;
        changeFabAppearance(Running.PAUSE);
        workTimer.start();
    }

    private void setRoundTextView() {
        textViewRounds.setText(" " + roundCurrent + "/" + roundsTotal);
    }


    private void setViewColors() {
        Boolean showQuotes = Preferences.showQuotes(this);
        if (!isRunning) {
            timerBackground.setBackgroundResource(R.drawable.timer_bg_primary);
            timerMessage.setTextColor(ContextCompat.getColor(this, R.color.colorMainBackgroundSecondary));

        } else if (state == State.WORK) {
            timerBackground.setBackgroundResource(R.drawable.timer_bg_work);
            timerMessage.setTextColor(ContextCompat.getColor(this, R.color.md_green_300));
            if (showQuotes)
                timerMessage.setText(getRandomString(R.array.workQuotes));
            else
                timerMessage.setText(R.string.workDefaultMsg);

        } else if (state == State.REST) {
            timerBackground.setBackgroundResource(R.drawable.timer_bg_rest);
            timerMessage.setTextColor(ContextCompat.getColor(this, R.color.md_light_blue_300));
            if (showQuotes)
                timerMessage.setText(getRandomString(R.array.restQuotes));
            else
                timerMessage.setText(R.string.restDefaultMsg);

        } else if (state == State.PREP) {
            timerBackground.setBackgroundResource(R.drawable.timer_bg_prep);
            timerMessage.setTextColor(ContextCompat.getColor(this, R.color.md_amber_200));
            if (showQuotes)
                timerMessage.setText(getRandomString(R.array.prepQuotes));
            else
                timerMessage.setText(R.string.prepDefaultMsg);
        }

        if (!showQuotes)
            timerMessage.setTextSize(40);
        else
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(timerMessage, 20, 50, 5, TypedValue.COMPLEX_UNIT_SP);

    }

    private String getRandomString(@ArrayRes int quotesArray) {
        String[] quotes = getResources().getStringArray(quotesArray);
        Random rand = new Random();
        int i = rand.nextInt(quotes.length);
        return quotes[i];
    }


    private void loadPreferences() {

        proximity = Preferences.useProximitySensor(this);
        if (proximity) {
            sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
            proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        }
        roundsTotal = Integer.parseInt(Preferences.getRoundsCount(this));
        workTimeMillis = PickerPrefDialog.getMillis(Preferences.getWorkTime(this));
        restTimeMillis = PickerPrefDialog.getMillis(Preferences.getRestTime(this));

        mute = Preferences.isMuted(this);
        if (Preferences.isPrepEnabled(this)) {
            state = State.PREP;
        } else {
            state = State.WORK;
        }
        endRoundWarnMillis = Long.parseLong(Preferences.getWarningTime(this)) * 1000; //sp.getString("warn_time_key", "5")) * 1000;

        runTime = Commons.formatString(TimeUnit.MILLISECONDS.toMinutes(workTimeMillis), TimeUnit.MILLISECONDS.toSeconds(workTimeMillis) % 60);
        workPreviewText.setText(String.format(getString(R.string.work), runTime));
        workPreviewText.setText(String.format(getString(R.string.work), runTime));
        String restTime = Commons.formatString(TimeUnit.MILLISECONDS.toMinutes(restTimeMillis), TimeUnit.MILLISECONDS.toSeconds(restTimeMillis) % 60);
        restPreviewText.setText(String.format(getString(R.string.rest), restTime));

        if(!Preferences.showHistory(this))
            recyclerView.setVisibility(View.GONE);
    }

    // Counter CLASS Section

    private class Counter extends CountDownTimer {
        private long millisLeft;
        private long mins, secs;

        Counter(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            if (state == State.PREP) {
                if (prepTimer != null) {
                    prepTimer = null;
                }
                state = State.WORK;
                workTimer = new Counter(workTimeMillis, SECONDS_MILLI);
                workTimer.start();
                setViewColors();
            } else if (state == State.REST && roundCurrent < roundsTotal) {
                ++roundCurrent;
                if (restTimer != null) {
                    restTimer.cancel();
                    restTimer = null;
                }
                state = State.WORK;
                setRoundTextView();
                setViewColors();
                workTimer = new Counter(workTimeMillis, SECONDS_MILLI);
                workTimer.start();
            } else if (state == State.WORK && roundCurrent < roundsTotal) {
                if (workTimer != null) {
                    workTimer.cancel();
                    workTimer = null;
                }
                if (restTimeMillis > 0) {
                    state = State.REST;
                    setRoundTextView();
                    setViewColors();
                    restTimer = new Counter(restTimeMillis, SECONDS_MILLI);
                    restTimer.start();

                } else {
                    ++roundCurrent;
                    state = State.WORK;
                    setRoundTextView();
                    setViewColors();
                    workTimer = new Counter(workTimeMillis, SECONDS_MILLI);
                    workTimer.start();
                }
            } else if (roundCurrent == roundsTotal) {

                // TODO Allow to share number of rounds/total time worked out on FB, Twitter, G+

                playFinishedSound();

                textViewTime.setText("--:--!");
                state = State.DONE;
                setRoundTextView();
                setViewColors();
                isRunning = false;
                cancelNullTimers();
                showView(resetLayer);

                observerTime.onComplete();
            }
        }

        @Override
        public void onTick(long millisUntilFinished) {
            if (millisUntilFinished < endRoundWarnMillis) {
                playPrepSound();
            }

            millisLeft = millisUntilFinished;
            mins = TimeUnit.MILLISECONDS.toMinutes(millisLeft);
            secs = TimeUnit.MILLISECONDS.toSeconds(millisLeft) % 60;
            textViewTime.setText(Commons.formatString(mins, secs));
        }

        long getMillisLeft() {
            return millisLeft;
        }
    }

    private String getElaspedTime(long millis) {
        long second = (millis / 1000) % 60;
        long minute = (millis / (1000 * 60)) % 60;
        long hour = (millis / (1000 * 60 * 60)) % 24;

        if (hour > 0)
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", hour, minute, second);
        else
            return String.format(Locale.getDefault(), "%02d:%02d", minute, second);
    }
}
