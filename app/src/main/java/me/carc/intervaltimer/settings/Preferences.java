package me.carc.intervaltimer.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import me.carc.intervaltimer.R;


public class Preferences {

    private final static String PREFS = "CarcIntervalTimerPrefs";

    private final static String FIRST_RUN = "FIRST_RUN";

    final static String PREP_TIME_ENABLED = "prep_time_key";
    private final static String WORK_TIME = "round_time_key";
    private final static String REST_TIME = "rest_time_key";
    final static String WARNING_TIME = "warn_time_key";
    final static String ROUNDS_CNT = "number_rounds_key";
    final static String MUTE = "mute_key";
    final static String VOICE = "voice_key";
    final static String QUOTES = "quotes_key";
    final static String HISTORY = "history_key";


    final static String STAY_AWAKE = "keep_awake_key";
    final static String PROX_SENSOR = "proximity_sensor_key";
    final static String VIBRATE = "use_vibrate_key";

    final static String REPORT_BUG = "report_bug";

    public static boolean getPref(Context context, String pref, boolean defValue) {
        SharedPreferences settings = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return settings.getBoolean(pref, defValue);
    }

    public static void setPref(Context context, String pref, boolean value) {
        SharedPreferences settings = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(pref, value);
        editor.apply();
    }

    public static void firstRunComplete(Context context) {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putBoolean(FIRST_RUN, false);
        prefs.apply();
    }

    public static Boolean isFirstRun(Context context) {
        SharedPreferences value = PreferenceManager.getDefaultSharedPreferences(context);
        return value.getBoolean(FIRST_RUN, true);
    }

    public static Boolean isPrepEnabled(Context context) {
        SharedPreferences value = PreferenceManager.getDefaultSharedPreferences(context);
        return value.getBoolean(PREP_TIME_ENABLED, true);
    }

    public static void setWorkTime(Context context, String value) {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putString(WORK_TIME, value);
        prefs.commit();
    }

    public static String getWorkTime(Context context) {
        SharedPreferences value = PreferenceManager.getDefaultSharedPreferences(context);
        return value.getString(WORK_TIME, context.getString(R.string.default_work_time_value));
    }

    public static void setRestTime(Context context, String value) {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putString(REST_TIME, value);
        prefs.commit();
    }

    public static String getRestTime(Context context) {
        SharedPreferences value = PreferenceManager.getDefaultSharedPreferences(context);
        return value.getString(REST_TIME, context.getString(R.string.default_rest_time_value));
    }

    public static String getWarningTime(Context context) {
        SharedPreferences value = PreferenceManager.getDefaultSharedPreferences(context);
        return value.getString(WARNING_TIME, context.getString(R.string.warn_time_values_default));
    }

    public static void setRoundsCount(Context context, String value) {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putString(ROUNDS_CNT, value);
        prefs.commit();
    }

    public static String getRoundsCount(Context context) {
        SharedPreferences value = PreferenceManager.getDefaultSharedPreferences(context);
        return value.getString(ROUNDS_CNT, "9");
    }

    public static Boolean useSounds(Context context) {
        SharedPreferences value = PreferenceManager.getDefaultSharedPreferences(context);
        return value.getBoolean(MUTE, false);
    }

    public static Boolean useVoices(Context context) {
        SharedPreferences value = PreferenceManager.getDefaultSharedPreferences(context);
        return value.getBoolean(VOICE, false);
    }

    public static Boolean showQuotes(Context context) {
        SharedPreferences value = PreferenceManager.getDefaultSharedPreferences(context);
        return value.getBoolean(QUOTES, true);
    }

    public static Boolean showHistory(Context context) {
        SharedPreferences value = PreferenceManager.getDefaultSharedPreferences(context);
        return value.getBoolean(HISTORY, true);
    }



    /* Phone Settings */

    public static Boolean stayAwake(Context context) {
        SharedPreferences value = PreferenceManager.getDefaultSharedPreferences(context);
        return value.getBoolean(STAY_AWAKE, true);
    }

    public static Boolean useProximitySensor(Context context) {
        SharedPreferences value = PreferenceManager.getDefaultSharedPreferences(context);
        return value.getBoolean(PROX_SENSOR, false);
    }

    public static Boolean useVibrate(Context context) {
        SharedPreferences value = PreferenceManager.getDefaultSharedPreferences(context);
        return value.getBoolean(VIBRATE, false);
    }
}
