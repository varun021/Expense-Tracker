package com.example.expensetracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

public class ThemeManager {

    private static final String PREF_THEME = "theme_option";
    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";
    public static final String THEME_SYSTEM = "system";

    /**
     * Apply the theme based on the current preference.
     *
     * @param context The application context.
     */
    public static void applyTheme(Context context) {
        String theme = getThemePreference(context);
        Log.d("ThemeManager", "Applying theme: " + theme);
        switch (theme) {
            case THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case THEME_SYSTEM:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    /**
     * Set the theme preference and apply the theme.
     *
     * @param context The application context.
     * @param theme   The selected theme.
     * @return
     */
    public static boolean setTheme(Context context, String theme) {
        if (isValidTheme(theme)) {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            editor.putString(PREF_THEME, theme);
            editor.apply();
            applyTheme(context);
        } else {
            Log.e("ThemeManager", "Invalid theme: " + theme);
        }
        return false;
    }

    /**
     * Get the current theme preference.
     *
     * @param context The application context.
     * @return The current theme preference.
     */
    public static String getThemePreference(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(PREF_THEME, THEME_SYSTEM);
    }

    /**
     * Validate the theme string.
     *
     * @param theme The theme to validate.
     * @return True if the theme is valid, false otherwise.
     */
    private static boolean isValidTheme(String theme) {
        return THEME_LIGHT.equals(theme) || THEME_DARK.equals(theme) || THEME_SYSTEM.equals(theme);
    }
}
