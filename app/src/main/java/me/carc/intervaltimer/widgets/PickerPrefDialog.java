package me.carc.intervaltimer.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;

import java.sql.Time;
import java.util.Calendar;
import java.util.Locale;

import me.carc.intervaltimer.R;

/**
 * Time picker for Prefereences
 * Created by bamptonm on 30/01/2018.
 */

public class PickerPrefDialog extends DialogPreference {

    private Context mContext;
    private TimePick mTimePicker = null;

    private int lastHour   = 0;
    private int lastMinute = 3;
    private int lastSecond = 0;


    private int getHours(String time) {
        String[] pieces = time.split(":");
        if(pieces.length > 2)
            return (Integer.parseInt(pieces[0]));
        else
            return 0;
    }


    private int getMinute(String time) {
        String[] pieces = time.split(":");
        if(pieces.length > 2)
            return (Integer.parseInt(pieces[1]));
        else
            return (Integer.parseInt(pieces[0]));
    }

    private int getSecond(String time) {
        String[] pieces = time.split(":");
        if(pieces.length > 2)
            return (Integer.parseInt(pieces[2]));
        else
            return (Integer.parseInt(pieces[1]));
    }

    public static long getMillis(String time) {
        String[] pieces = time.split(":");

        long timeMilli;

        if(pieces.length > 2) {
            int hours   = Integer.parseInt(pieces[0]) * 60 * 60;
            int minutes = Integer.parseInt(pieces[1]) * 60;
            int seconds = Integer.parseInt(pieces[2]);
            timeMilli = ((long)hours + (long)minutes + (long)seconds) * 1000;
        } else {
            int minutes = Integer.parseInt(pieces[0]) * 60;
            int seconds = Integer.parseInt(pieces[1]);
            timeMilli = ((long)minutes + (long)seconds) * 1000;
        }
        return timeMilli;
    }


    public PickerPrefDialog(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        mContext = context;
        setPositiveButtonText(R.string.set);
        setNegativeButtonText(R.string.cancel);
    }

    @Override
    protected View onCreateDialogView() {
        Calendar now = Calendar.getInstance();
        mTimePicker = new TimePick(mContext);
        mTimePicker.setIs24HourView(true);

        return (mTimePicker);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        mTimePicker.setCurrentHour(lastHour);
        mTimePicker.setCurrentMinute(lastMinute);
        mTimePicker.setCurrentSecond(lastSecond);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            lastHour = mTimePicker.getCurrentHour();
            lastMinute = mTimePicker.getCurrentMinute();
            lastSecond = mTimePicker.getCurrentSeconds();

            String time;
            if(lastHour > 0)
                time = String.valueOf(lastHour) + ":" + String.valueOf( lastMinute) + ":" + String.valueOf(lastSecond);
            else
                time = String.valueOf(lastMinute) + ":" + String.valueOf(lastSecond);

            if (callChangeListener(time)) {
                persistString(time);
            }
            super.setSummary(getSummary());
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return (a.getString(index));
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        String time;
        if (restoreValue) {
            if (defaultValue == null) {
                time = getPersistedString("00:00");
            } else {
                time = getPersistedString(defaultValue.toString());
            }
        } else {
            time = defaultValue.toString();
        }

        lastHour = getHours(time);
        lastMinute = getMinute(time);
        lastSecond = getSecond(time);
    }


    @Override
    public CharSequence getSummary() {
//        long timeMilli = ((lastHour * 60 * 60) + (lastMinute * 60) + (lastSecond)) * 1000;
        Time time = new Time(lastHour, lastMinute, lastSecond);
        if(lastHour > 0)
            return String.format(Locale.US, "%02d:%02d:%02d", time.getHours(), time.getMinutes(), time.getSeconds());
        else
            return String.format(Locale.US, "%02d:%02d", time.getMinutes(), time.getSeconds());
    }
}
