package me.carc.intervaltimer.model;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import me.carc.intervaltimer.R;
import me.carc.intervaltimer.ui.activities.FitnessActivity;
import me.carc.intervaltimer.ui.activities.HistoryActivity;
import me.carc.intervaltimer.ui.activities.ServicedActivity;

/**
 * Created by bamptonm on 27/02/2018.
 */

public enum MainMenuItems {

    TIMER(R.drawable.mnu_athletics, R.string.timer, R.string.timer_desc, ServicedActivity.class),
    FITNESS(R.drawable.mnu_weightlifting, R.string.fitness, R.string.fitness_desc, FitnessActivity.class),
    HISTORY(R.drawable.mnu_cardio, R.string.history, R.string.history_desc, HistoryActivity.class);

    final int iconDrawable;
    final int titleResourceId;
    final int subTitleResourceId;
    final Class launcher;

    MainMenuItems(@DrawableRes int drawable, @StringRes int titleResourceId, @StringRes int subTitleResourceId, Class activity) {
        this.iconDrawable = drawable;
        this.titleResourceId = titleResourceId;
        this.subTitleResourceId = subTitleResourceId;
        this.launcher = activity;
    }
    public int getIconDrawable() {
        return iconDrawable;
    }

    public int getTitleResourceId() {
        return titleResourceId;
    }

    public int getSubTitleResourceId() {
        return subTitleResourceId;
    }

    public Class getLauncher() { return launcher; }
}
