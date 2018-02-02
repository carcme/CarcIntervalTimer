package me.carc.intervaltimer.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.widget.TextView;


/**
 * Created by Carc.me on 05.06.16.
 * <p/>
 * View helper functions stolen from across the net
 */
public class ViewUtil {
    private static final float INTERPOLATOR_FACTOR = 2.0f;

    private final static int COLOR_ANIMATION_DURATION = 1000;
    private final static int DEFAULT_DELAY = 0;


    private ViewUtil() {
        throw new AssertionError();
    }


    public static int getStatusBarHeight(Context ctx) {
        int result = 0;
        int resourceId = ctx.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = ctx.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }


    @SuppressWarnings("deprecation")
    public static void setBackground(View view, Drawable drawable) {
        if (Build.VERSION.SDK_INT >= 16) {
            view.setBackground(drawable);
        } else {
            view.setBackgroundDrawable(drawable);
        }
    }

    public static boolean isGone(View view) {
        return view.getVisibility() == View.GONE;
    }

    public static void setViewHeight(View view, int height, boolean layout) {
        view.getLayoutParams().height = height;
        if (layout) {
            view.requestLayout();
        }
    }

    public static void setViewWidth(View view, int width, boolean layout) {
        view.getLayoutParams().width = width;
        if (layout) {
            view.requestLayout();
        }
    }

    public static void showView(final View view, int duration) {
        view.animate()
                .translationY(0)
                .translationX(0)
                .setDuration(duration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                    }
                });
    }

    public static void hideView(final View view, final int duration, final int margin) {
        view.animate()
                .translationY(-10)
                .setDuration(30)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        view.animate()
                                .translationY(view.getHeight() + margin + 10)
                                .setDuration(duration)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                    }
                                });
                    }
                });
    }

    /**
     * Full screen
     *
     * @param window
     */
    public static void setHideyBar(Window window) {
        int newUiOptions = window.getDecorView().getSystemUiVisibility();

        if (Build.VERSION.SDK_INT >= 14)
            newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        if (Build.VERSION.SDK_INT >= 16)
            newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        if (Build.VERSION.SDK_INT >= 18)
            newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        window.getDecorView().setSystemUiVisibility(newUiOptions);
    }



    public static ViewPropertyAnimator createAlphaAnimator(View v, boolean show, int dur) {
        float alpha = show ? 1.0f : 0.0f;
        return v.animate().alpha(alpha).setDuration(dur);
    }

    public static Animator toggleViewFade(final View target, final boolean show, int duration,
                                          @Nullable final Runnable startRunnable, @Nullable final Runnable endRunnable) {
        Animator animator;
        float alpha = show ? 1.0f : 0.0f;
        animator = ObjectAnimator.ofFloat(target, "alpha", alpha);
        animator.setDuration(duration);

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (show) {
                    target.setVisibility(View.VISIBLE);

                    if (startRunnable != null) {
                        startRunnable.run();
                    }
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!show) {
                    target.setVisibility(View.GONE);
                }

                if (endRunnable != null) {
                    endRunnable.run();
                }
            }
        });

        return animator;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Animator toggleViewCircular(View source, final View target, final boolean show,
                                              float radius, int duration, @Nullable final Runnable startRunnable, @Nullable final Runnable endRunnable) {

        Animator animator;

        // get the center for the clipping circle
        int cx = (source.getLeft() + source.getRight()) / 2;
        int cy = (source.getTop() + (int) source.getTranslationY() + source.getBottom());

        float radiusStart = show ? 0 : radius;
        float radiusEnd = show ? radius : 0;

        animator = ViewAnimationUtils.createCircularReveal(target, cx, cy, radiusStart, radiusEnd);
        animator.setDuration(duration);
        animator.setInterpolator(createInterpolator(show));

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (show) {
                    target.setVisibility(View.VISIBLE);

                    if (startRunnable != null) {
                        startRunnable.run();
                    }
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!show) {
                    target.setVisibility(View.GONE);
                }

                if (endRunnable != null) {
                    endRunnable.run();
                }
            }
        });

        return animator;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Animator toggleViewCircularTouch(View source, int cx, int cy, final View target, final boolean show,
                                                   float radius, int duration, @Nullable final Runnable startRunnable, @Nullable final Runnable endRunnable) {
        Animator animator;

        float radiusStart = show ? 0 : radius;
        float radiusEnd = show ? radius : 0;

        animator = ViewAnimationUtils.createCircularReveal(target, cx, cy, radiusStart, radiusEnd);
        animator.setDuration(duration);
        animator.setInterpolator(createInterpolator(show));

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (show) {
                    target.setVisibility(View.VISIBLE);

                    if (startRunnable != null) {
                        startRunnable.run();
                    }
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!show) {
                    target.setVisibility(View.GONE);
                }

                if (endRunnable != null) {
                    endRunnable.run();
                }
            }
        });

        return animator;
    }

    private static Interpolator createInterpolator(boolean show) {
        if (show) {
            return new AccelerateInterpolator(INTERPOLATOR_FACTOR);
        }

        return new DecelerateInterpolator(INTERPOLATOR_FACTOR);
    }


    public static void setMargins (View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            view.requestLayout();
        }
    }

    /**
     * calculate the optimal width for the image
     */
    public static int optimalImageWidth(int screenWidth) {
        int preOptimalWidth = screenWidth / 2;

        if (preOptimalWidth >= 720) {
            return 720;
        } else if (preOptimalWidth >= 540) {
            return 540;
        } else if (preOptimalWidth >= 360) {
            return 360;
        } else {
            return 360;
        }
    }


    public static void animateViewColor(View v, int startColor, int endColor) {

        ObjectAnimator animator = ObjectAnimator.ofObject(v, "backgroundColor",
                new ArgbEvaluator(), startColor, endColor);

        if (Build.VERSION.SDK_INT >= 21) {
            animator.setInterpolator(new PathInterpolator(0.4f, 0f, 1f, 1f));
        }
        animator.setDuration(COLOR_ANIMATION_DURATION);
        animator.start();
    }


    public static void configuredHideYView(View v) {
        v.setScaleY(0);
        v.setPivotY(0);
    }

    public static ViewPropertyAnimator hideViewByScaleXY(View v) {

        return hideViewByScale(v, DEFAULT_DELAY, 0, 0);
    }

    public static ViewPropertyAnimator hideViewByScaleY(View v) {

        return hideViewByScale(v, DEFAULT_DELAY, 1, 0);
    }


    public static ViewPropertyAnimator hideViewByScalyInX(View v) {

        return hideViewByScale(v, DEFAULT_DELAY, 0, 1);
    }

    private static ViewPropertyAnimator hideViewByScale(View v, int delay, int x, int y) {
        return v.animate().setStartDelay(delay).scaleX(x).scaleY(y);
    }

    public static ViewPropertyAnimator showViewByScale(View v) {
        return v.animate().setStartDelay(DEFAULT_DELAY).scaleX(1).scaleY(1);
    }

    public static float pxFromDp(Context context, float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }
    public static float dpFromPx(Context context, float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void changeFabIcon(Context ctx, android.support.design.widget.FloatingActionButton fab, int res) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fab.setImageDrawable(ctx.getResources().getDrawable(res, ctx.getTheme()));
        } else {
            fab.setImageDrawable(ctx.getResources().getDrawable(res));
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void changeFabColour(Context ctx, android.support.design.widget.FloatingActionButton fab, int colour) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fab.setBackgroundTintList(ContextCompat.getColorStateList(ctx, colour));
        } else {
            fab.getBackground().setColorFilter(colour, PorterDuff.Mode.MULTIPLY);
//            fab.setBackgroundTintList(ctx.getResources().getColorStateList(colour));
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static void changeTextViewIconColour(Context ctx, TextView textview, int colour) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ColorStateList colorStateList = ResourcesCompat.getColorStateList(ctx.getResources(), colour, null);
            textview.setCompoundDrawableTintList(colorStateList);
        } else {
            Drawable[] drawables = textview.getCompoundDrawablesRelative();
            DrawableCompat.setTint(drawables[0], ContextCompat.getColor(ctx, colour));
        }
    }

    public static Drawable changeIconColor(Context context, @DrawableRes int res, @ColorRes int color) {
        Drawable mDrawable = ContextCompat.getDrawable(context, res);
        int mColor = ContextCompat.getColor(context, color);
        mDrawable.setColorFilter(new PorterDuffColorFilter(mColor, PorterDuff.Mode.MULTIPLY));
        return mDrawable;
    }

    public static Drawable changeIconColor(Context context, @DrawableRes int res, String color) {
        Drawable mDrawable = ContextCompat.getDrawable(context, res);
        int mColor = Color.parseColor("#" + color);
        mDrawable.setColorFilter(new PorterDuffColorFilter(mColor, PorterDuff.Mode.MULTIPLY));
        return mDrawable;
    }
}
