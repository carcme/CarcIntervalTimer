package me.carc.intervaltimer.widgets;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.util.Locale;

import me.carc.intervaltimer.R;
import me.carc.intervaltimer.widgets.listeners.NumberSetListener;

import static me.carc.intervaltimer.widgets.TimePick.TWO_DIGIT_FORMATTER;

/**
 * Created by bamptonm on 11/02/2018.
 */

public class NumberPickerBuilder extends Dialog {

    public static final int SECS = 0;
    public static final int MINS = 1;
    public static final int HOURS = 2;

    private Context mContext;
    private int mPickersCount;

    private int mHours;
    private int mMinutes;
    private int mSeconds;

    private NumberSetListener mNumberSetListener;

    private TextView mTitle;
    private NumberPicker hoursPicker;
    private NumberPicker minsPicker;
    private NumberPicker secsPicker;


    public NumberPickerBuilder(@NonNull Context context) {
        super(context);
        mContext = context;
        setContentView(R.layout.number_picker_dialog);

        Button setBtn = findViewById(R.id.setBtn);
        Button cancelBtn = findViewById(R.id.cancelBtn);
        mTitle = findViewById(R.id.pickerTitle);
        hoursPicker = findViewById(R.id.hours);
        minsPicker = findViewById(R.id.minutes);
        secsPicker = findViewById(R.id.seconds);

        hoursPicker.setMinValue(0);
        hoursPicker.setMaxValue(59);
        hoursPicker.setFormatter(TWO_DIGIT_FORMATTER);
        hoursPicker.setWrapSelectorWheel(true);

        minsPicker.setMinValue(0);
        minsPicker.setMaxValue(59);
        minsPicker.setFormatter(TWO_DIGIT_FORMATTER);
        minsPicker.setWrapSelectorWheel(true);

        secsPicker.setMinValue(0);
        secsPicker.setMaxValue(59);
        secsPicker.setFormatter(TWO_DIGIT_FORMATTER);
        secsPicker.setWrapSelectorWheel(true);

        /* Hours change listener  */
        hoursPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mHours = newVal;
            }
        });

        /* Minutes change listener  */
        minsPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mMinutes = newVal;
            }
        });

        /* Seconds change listener  */
        secsPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mSeconds = newVal;
            }
        });

        /* OK button click listener  */
        setBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mNumberSetListener == null)
                    throw new RuntimeException("A listener needs to be set!");

                if (mPickersCount == HOURS)
                    mNumberSetListener.onNumberSet(String.format(Locale.getDefault(), "%02d:%02d:%02d", mHours, mMinutes, mSeconds));
                else if (mPickersCount == MINS)
                    mNumberSetListener.onNumberSet(String.format(Locale.getDefault(), "%02d:%02d", mMinutes, mSeconds));
                else if (mPickersCount == SECS)
                    mNumberSetListener.onNumberSet(String.format(Locale.getDefault(), "%02d", mSeconds));
                else
                    throw new RuntimeException("Have you set the number of pickers?");
                dismiss();
            }
        });

        /* Cancel button click listener  */
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public NumberPickerBuilder setListener(NumberSetListener listener) {
        mNumberSetListener = listener;
        return this;
    }

    public NumberPickerBuilder setPickerTitle(String title) {
        mTitle.setText(title);
        return this;
    }

    public NumberPickerBuilder setPickerTitle(@StringRes int title) {
        mTitle.setText(title);
        return this;
    }

    public NumberPickerBuilder setPickerCount(int count) {
        if (mPickersCount > HOURS)
            throw new RuntimeException("Maximum of 3 number pickers available at present");

        mPickersCount = count;

        if (count == SECS) {
            hoursPicker.setVisibility(View.GONE);
            minsPicker.setVisibility(View.GONE);
        } else if (count == MINS) {
            hoursPicker.setVisibility(View.GONE);
        }

        return this;
    }

    public NumberPickerBuilder setValue(int TYPE, int value) {
        switch (TYPE) {
            case HOURS:
                hoursPicker.setValue(value);
                mHours = value;
                break;
            case MINS:
                minsPicker.setValue(value);
                mMinutes = value;
                break;
            case SECS:
                secsPicker.setValue(value);
                mSeconds = value;
                break;
            default:
                throw new RuntimeException("Specify which picker value you want to set");
        }
        return this;
    }


    public NumberPickerBuilder setMinMaxValue(int TYPE, int min, int max) {
        setMinValue(TYPE, min);
        setMaxValue(TYPE, max);
        return this;
    }

    private NumberPickerBuilder setMinValue(int TYPE, int value) {
        switch (TYPE) {
            case HOURS:
                hoursPicker.setMinValue(value);
                break;
            case MINS:
                minsPicker.setMinValue(value);
                break;
            case SECS:
                secsPicker.setMinValue(value);
                break;
            default:
                throw new RuntimeException("Specify which picker to set minimum value");
        }
        return this;
    }

    private NumberPickerBuilder setMaxValue(int TYPE, int value) {
        switch (TYPE) {
            case HOURS:
                hoursPicker.setMaxValue(value);
                break;
            case MINS:
                minsPicker.setMaxValue(value);
                break;
            case SECS:
                secsPicker.setMaxValue(value);
                break;
            default:
                throw new RuntimeException("Specify which picker to set maximum value");
        }
        return this;
    }
}
