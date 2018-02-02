package me.carc.intervaltimer.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.Nullable;

import me.carc.intervaltimer.R;


/**
 * System settings
 * Created by bamptonm on 7/11/17.
 */

public class SettingsTabFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        // set some default values
        EditTextPreference pref = (EditTextPreference) findPreference(Preferences.ROUNDS_CNT);
        pref.setSummary(String.format(getString(R.string.num_round_summary), pref.getText()));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        switch (key) {
            case Preferences.PREP_TIME_ENABLED: {
                CheckBoxPreference pref = (CheckBoxPreference) findPreference(key);
                if (pref.isChecked())
                    pref.setSummary(R.string.prep_time_summary);
                else
                    pref.setSummary(R.string.prep_time_summary_disabled);

                break;
            }
            case Preferences.WARNING_TIME: {
                ListPreference pref = (ListPreference) findPreference(key);
                if (pref.getEntry().equals(getString(R.string.warn_time_no_warning)))
                    pref.setSummary(pref.getEntry());
                else
                    pref.setSummary(String.format(getString(R.string.warn_time_summary), pref.getEntry()));

                break;
            }
            case Preferences.ROUNDS_CNT: {
                EditTextPreference pref = (EditTextPreference) findPreference(key);
                pref.setSummary(String.format(getString(R.string.num_round_summary), pref.getText()));

                break;
            }
            case Preferences.MUTE: {
                CheckBoxPreference pref = (CheckBoxPreference) findPreference(key);
                if (pref.isChecked())
                    pref.setSummary(R.string.mute_summary_disabled);
                else
                    pref.setSummary(R.string.mute_summary);

                break;
            }
            case Preferences.QUOTES: {
                CheckBoxPreference pref = (CheckBoxPreference) findPreference(key);
                if (pref.isChecked())
                    pref.setSummary(R.string.quotes_summary);
                else
                    pref.setSummary(R.string.quotes_summary_disabled);

                break;
            }
            case Preferences.STAY_AWAKE: {
                CheckBoxPreference pref = (CheckBoxPreference) findPreference(key);
                if (pref.isChecked())
                    pref.setSummary(R.string.keep_awake_summary);
                else
                    pref.setSummary(R.string.keep_awake_summary_disbled);
                break;
            }
            case Preferences.PROX_SENSOR: {
                CheckBoxPreference pref = (CheckBoxPreference) findPreference(key);
                if (pref.isChecked())
                    pref.setSummary(R.string.proximity_sensor_summary);
                else
                    pref.setSummary(R.string.proximity_sensor_summary_disabled);
                break;
            }
            case Preferences.VIBRATE: {
                CheckBoxPreference pref = (CheckBoxPreference) findPreference(key);
                if (pref.isChecked())
                    pref.setSummary(R.string.use_vibrate_summary);
                else
                    pref.setSummary(R.string.use_vibrate_summary_disabled);
                break;
            }
            case Preferences.HISTORY: {
                CheckBoxPreference pref = (CheckBoxPreference) findPreference(key);
                if (pref.isChecked())
                    pref.setSummary(R.string.history_summary);
                else
                    pref.setSummary(R.string.history_summary_disabled);
                break;
            }
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if(preference.getKey().equals(Preferences.REPORT_BUG)) {
            Intent launchBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.bugtracker)));
            startActivity(launchBrowser);
            return true;
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
