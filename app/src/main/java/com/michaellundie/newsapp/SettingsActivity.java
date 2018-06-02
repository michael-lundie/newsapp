package com.michaellundie.newsapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;

/**
 * Settings activity class allowing a user to alter various shared preferences.
 */
public class SettingsActivity extends AppCompatActivity {

    private static final String LOG_TAG = SettingsActivity.class.getSimpleName();
    private static boolean settingsChanged = false;
    /** Stores the initial value (upon access of SettingActivity) of
     * {@link QueryPreferenceFragment#returnQuantity}.*/
    private static String returnQuantityInitialValue;
    /** Stores the initial value (upon access of SettingActivity) of
     * {@link QueryPreferenceFragment#returnOrder}.*/
    private static String returnOrderInitialValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("settingsChanged",settingsChanged);
            setResult(Activity.RESULT_OK, returnIntent);
            onBackPressed();
            return true;
        }
        return false;
    }

    public static class QueryPreferenceFragment extends PreferenceFragment
            implements Preference.OnPreferenceChangeListener {

        Preference returnQuantity;
        Preference returnOrder;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.settings_main);
            returnQuantity = findPreference(getString(R.string.settings_return_quantity_key));
            bindPreferenceSummaryToValue(returnQuantity);
            returnOrder = findPreference(getString(R.string.settings_orderby_key));
            bindPreferenceSummaryToValue(returnOrder);
        }

        /**
         * Links/Binds the UI description (through the use of a key value) with the current preference
         * value.
         * @param preference A preference object to bind
         */
        private void bindPreferenceSummaryToValue(Preference preference) {
            preference.setOnPreferenceChangeListener(this);
            SharedPreferences preferences =
                    PreferenceManager.getDefaultSharedPreferences(preference.getContext());
            String preferenceValue = preferences.getString(preference.getKey(), "");
            if (preference == returnQuantity) {
                returnQuantityInitialValue = preferenceValue;
            } else {
                returnOrderInitialValue = preferenceValue;
            }
            onPreferenceChange(preference, preferenceValue);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int prefIndex = listPreference.findIndexOfValue(stringValue);
                if (prefIndex >= 0) {
                    CharSequence[] labels = listPreference.getEntries();
                    preference.setSummary(labels[prefIndex]);
                }
            } else {
                preference.setSummary(stringValue);
            }

            String key = preference.getKey();  // Check this returns string and not id

            if (key.equals(getString(R.string.settings_orderby_key))) {
                if (value.equals(returnOrderInitialValue)) {
                    settingsChanged = false;
                } else {
                    settingsChanged = true;
                }
            } else if (key.equals(getString(R.string.settings_return_quantity_key))) {
                if (value.equals(returnQuantityInitialValue)) {
                    settingsChanged = false;
                } else if (Integer.parseInt(stringValue) < 51) {
                    settingsChanged = true;
                } else {
                    Toast.makeText(this.getActivity(),
                            getString(R.string.return_quantity_value_input_error),
                            Toast.LENGTH_SHORT).show();
                }
            }
            return true;
        }
    }
}