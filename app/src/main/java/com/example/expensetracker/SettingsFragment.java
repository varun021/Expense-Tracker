package com.example.expensetracker;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceManager;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load preferences from XML resource
        setPreferencesFromResource(R.xml.preferences, rootKey);

        // Set up theme preference listener
        ListPreference themePreference = findPreference("theme_option");
        if (themePreference != null) {
            themePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    ThemeManager.setTheme(getContext(), (String) newValue);
                    return true; // True to update the state of the preference
                }
            });
        }

        // Set up notifications preference listener
        SwitchPreferenceCompat notificationsPreference = findPreference("notifications_enabled");
        if (notificationsPreference != null) {
            notificationsPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    boolean isEnabled = (Boolean) newValue;
                    // Handle notification settings changes if needed
                    return true; // True to update the state of the preference
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Update preferences or UI elements if needed when the fragment resumes
    }
}
