package me.carc.intervaltimer.data.local.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.carc.intervaltimer.R;


public class Preferences {

    private final static String PREFS = "CarcIntervalTimerPrefs";

    private final static String FIRST_RUN = "FIRST_RUN";

    private final static String SPRINTS = "sprints_time_key";

    public final static String WARNING_TIME = "warn_time_key";
    public final static String ROUNDS_CNT = "number_rounds_key";
    public final static String REPORT_BUG = "report_bug";
    public final static String LOCATION_ENABLED = "enable_location_logging";
    public final static String GPS_ENABLED = "gps_enable";
    public final static String HISTORY_TITLES = "HISTORY_TITLES";
    public final static String MIN_DISTANCE_CHANGE_FOR_UPDATES = "gps_min_dist_key";
    public final static String MIN_TIME_BW_UPDATES = "gps_min_time_key";

    private final static String PREP_TIME_ENABLED = "prep_time_key";
    private final static String WORK_TIME = "round_time_key";
    private final static String REST_TIME = "rest_time_key";
    private final static String MUTE = "mute_key";
    private final static String VOICE = "voice_key";
    private final static String QUOTES = "quotes_key";
    private final static String HISTORY = "history_key";
    private final static String SUMMARY = "show_summary_key";

    private final static String STAY_AWAKE = "keep_awake_key";
    private final static String PROX_SENSOR = "proximity_sensor_key";
    private final static String VIBRATE = "use_vibrate_key";

    private final static String STEP_SENSITIVITY = "STEP_SENSITIVITY";



    public static final String PREF_TOTAL_DISTANCE = "PREF_TOTAL_DISTANCE";
    public static final String PREF_TOTAL_TIME     = "PREF_TOTAL_TIME";



    public static int getSprintsTime(Context context) {
        SharedPreferences value = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.valueOf(value.getString(SPRINTS, "0"));
    }



    public static double getTotalDistance(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return Double.longBitsToDouble(pref.getLong(PREF_TOTAL_DISTANCE, 0));
    }
    public static long getTotalTime(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return pref.getLong(PREF_TOTAL_TIME, 0);
    }


    public static double getPrefDouble(Context context, String key) {
        SharedPreferences pref = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return Double.longBitsToDouble(pref.getLong(key, 0));
    }

    public static void putPrefDouble(Context context, String key, double value) {
        SharedPreferences pref = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putLong(key, Double.doubleToRawLongBits(value));
        editor.apply();
    }

    public static long getPrefLong(Context context, String key) {
        SharedPreferences pref = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return pref.getLong(key, 0);
    }

    public static void putPrefLong(Context context, String key, long value) {
        SharedPreferences pref = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public static boolean getPrefBoolean(Context context, String key, boolean defValue) {
        SharedPreferences pref = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return pref.getBoolean(key, defValue);
    }

    public static void putPrefBoolean(Context context, String pref, boolean value) {
        SharedPreferences settings = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(pref, value);
        editor.apply();
    }

    public static void removeKey(Context context, String key) {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.remove(key).apply();
    }


    public static void putHistoryTitleArray(Context context, List<String> titles) {
        // Remove duplicates and empty strings
        Set<String> hs = new HashSet<>();
        hs.addAll(titles);
        hs.remove("");
        titles.clear();
        titles.addAll(hs);
        // add list to preferences
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        Gson gson = new Gson();
        String json = gson.toJson(titles);
        prefs.putString(HISTORY_TITLES, json);
        prefs.apply();
    }

    public static ArrayList<String> getHistoryTitleArray(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.contains(HISTORY_TITLES)) {
            String prefStr = prefs.getString(HISTORY_TITLES, null);
            Gson gson = new Gson();
            String[] list = gson.fromJson(prefStr, String[].class);
            return new ArrayList<>(Arrays.asList(list));
        } else {
            return new ArrayList<>();
        }
    }

    public static void enableLocationServices(Context context, boolean enable) {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putBoolean(LOCATION_ENABLED, enable);
        prefs.apply();
    }

    public static Boolean isLocationEnabled(Context context) {
        SharedPreferences value = PreferenceManager.getDefaultSharedPreferences(context);
        return value.getBoolean(LOCATION_ENABLED, false);
    }

/*
    public static Boolean isGpsEnabled(Context context) {
        SharedPreferences value = PreferenceManager.getDefaultSharedPreferences(context);
        return value.getBoolean(GPS_ENABLED, true);
    }
*/

    public static String locationUpdateDistance(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(MIN_DISTANCE_CHANGE_FOR_UPDATES, String.valueOf(10));
    }

    public static String locationUpdateTime(Context context) {
        SharedPreferences value = PreferenceManager.getDefaultSharedPreferences(context);
        return value.getString(MIN_TIME_BW_UPDATES, String.valueOf(5000));
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

    public static void setStepSensitivity(Context context, int value) {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putInt(STEP_SENSITIVITY, value);
        prefs.apply();
    }
    public static int getStepSensitivity(Context context) {
        SharedPreferences value = PreferenceManager.getDefaultSharedPreferences(context);
        return value.getInt(STEP_SENSITIVITY, 15);
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
        return value.getBoolean(MUTE, true);
    }

    public static Boolean useVoices(Context context) {
        SharedPreferences value = PreferenceManager.getDefaultSharedPreferences(context);
        return value.getBoolean(VOICE, true);
    }

    public static Boolean showQuotes(Context context) {
        SharedPreferences value = PreferenceManager.getDefaultSharedPreferences(context);
        return value.getBoolean(QUOTES, false);
    }

    public static Boolean showHistory(Context context) {
        SharedPreferences value = PreferenceManager.getDefaultSharedPreferences(context);
        return value.getBoolean(HISTORY, false);
    }
    public static Boolean showSummary(Context context) {
        SharedPreferences value = PreferenceManager.getDefaultSharedPreferences(context);
        return value.getBoolean(SUMMARY, true);
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
