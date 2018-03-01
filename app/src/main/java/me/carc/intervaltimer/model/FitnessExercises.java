package me.carc.intervaltimer.model;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import me.carc.intervaltimer.R;

/**
 * Created by bamptonm on 27/02/2018.
 */

public enum FitnessExercises  {

    BURPEE(R.drawable.ic_icon_rest, R.string.burpee, R.string.burpee_desc, R.string.burpee_instruction, R.string.burpee_url),
    JUMPJACK(R.drawable.ic_icon_rest, R.string.jacks, R.string.jacksDesc, R.string.jacksInstruction, R.string.jacksUrl),
    PLANK(R.drawable.ic_icon_rest, R.string.planks, R.string.planksDesc, R.string.planksInstruction, R.string.planksUrl),
    SITUP(R.drawable.ic_icon_rest, R.string.situps, R.string.situpsDesc, R.string.situpsInstruction, R.string.situpsUrl),
    DIPS(R.drawable.ic_icon_rest, R.string.dips, R.string.dipsDesc, R.string.dipsInstruction, R.string.dipsUrl),
    PUSHUP(R.drawable.ic_icon_none, R.string.pushup, R.string.pushupDesc, R.string.pushupInstruction, R.string.pushupUrl);


    final int iconDrawable;
    final int title;
    final int desc;
    final int instruct;
    final int urlExt;

    FitnessExercises(@DrawableRes int drawable, @StringRes int title, @StringRes int desc, @StringRes int instruct, @StringRes int urlExt) {
        this.iconDrawable = drawable;
        this.title = title;
        this.desc = desc;
        this.instruct = instruct;
        this.urlExt = urlExt;
    }
    public int getTitle() {
        return title;
    }

    public int getDesc() {
        return desc;
    }

    public int getInstruct() { return instruct; }

    public int getUrl() { return urlExt; }

    public int getIconDrawable() {
        return iconDrawable;
    }
}
