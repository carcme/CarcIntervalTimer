package me.carc.intervaltimer.widgets;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.graphics.drawable.ArgbEvaluator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.Random;

/**
 * Animated background
 * Created by bamptonm on 12/02/2018.
 */

public class Painter {

    private static final int MIN = 20000;
    private static final int MAX = 60000;

    private int mDuration;
    private final Random random;
    private int baseColor;
    private boolean stopAmimating;

    public Painter(int color) {
        if(color != 0)
            baseColor = color;
        else
            baseColor = getRandColor();
        random = new Random();
    }

    public void setBaseColor(int color) {
        baseColor = color;
    }

    public void setDuration(int duration) {
        mDuration = duration;
    }

    public static int getRandColor() {
        Random rand = new Random();
        int r = rand.nextInt(200);
        int g = rand.nextInt(200);
        int b = rand.nextInt(200);
        return Color.rgb(r, g, b);
    }

    public void stopAnimating() {
        stopAmimating = true;
    }

    public void animate(@NonNull final View target, @ColorInt final int color) {
        stopAmimating = false;
        animate(target, baseColor, color);
    }

    private void animate(@NonNull final View target,
                 @ColorInt final int color1,
                 @ColorInt final int color2) {

        final ValueAnimator valueAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), color1, color2);

        valueAnimator.setDuration(mDuration != 0 ? mDuration : randInt(MIN, MAX));

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override public void onAnimationUpdate(ValueAnimator animation) {
                target.setBackgroundColor((int) animation.getAnimatedValue());
                if(stopAmimating) {
                    valueAnimator.removeAllListeners();
                }
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) {
                //reverse animation
                animate(target, color2, color1);
            }
        });

        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.start();
    }

    private int randInt(int min, int max) {
        return random.nextInt((max - min) + 1) + min;
    }
}