package me.carc.intervaltimer.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.Nullable;

import me.carc.intervaltimer.R;
import me.carc.intervaltimer.data.local.prefs.Preferences;


/**
 * System settings
 * Created by bamptonm on 7/11/17.
 */

public class SettingsTabFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private boolean attached;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        attached = true;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        attached = false;
    }

    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        // set some default values
        EditTextPreference pref = (EditTextPreference) findPreference(Preferences.ROUNDS_CNT);
        if(pref != null)
            pref.setSummary(String.format(getString(R.string.num_round_summary), pref.getText()));
    }



    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (attached) {
            switch (key) {
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
 /*               case Preferences.MIN_DISTANCE_CHANGE_FOR_UPDATES: {
                    ListPreference pref = (ListPreference) findPreference(key);
                    if (pref.getEntry().equals(getString(R.string.warn_time_no_warning)))
                        pref.setSummary(pref.getEntry());
                    else
                        pref.setSummary(String.format(getString(R.string.warn_time_summary), pref.getEntry()));

                    break;
                }
                case Preferences.MIN_TIME_BW_UPDATES: {
                    ListPreference pref = (ListPreference) findPreference(key);
                    if (pref.getEntry().equals(getString(R.string.warn_time_no_warning)))
                        pref.setSummary(pref.getEntry());
                    else
                        pref.setSummary(String.format(getString(R.string.warn_time_summary), pref.getEntry()));

                    break;
                }
 */           }
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference.getKey().equals(Preferences.REPORT_BUG)) {
            Intent launchBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.bugtracker)));
            startActivity(launchBrowser);
            return true;
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
