package com.example.expensetracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import com.google.android.material.navigation.NavigationView;

public class SettingsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.applyTheme(this);
        setContentView(R.layout.activity_settings);

        // Initialize UI components
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        // Set up Toolbar
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));

        // Setup Navigation Drawer
        setupNavigationDrawer();

        // Load settings fragment
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment())
                .commit();
    }

    private void setupNavigationDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(android.R.color.white));
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            startActivity(new Intent(this, MainActivity.class));
        } else if (id == R.id.nav_expenses) {
            startActivity(new Intent(this, CategoryManagerActivity.class));
        } else if (id == R.id.nav_recurring_transactions) {
            startActivity(new Intent(this, RecurringTransactionsActivity.class));
        } else if (id == R.id.nav_budgets) {
            startActivity(new Intent(this, BudgetManagerActivity.class));
        } else if (id == R.id.nav_chart) {
            startActivity(new Intent(this, AnalyticsActivity.class));
        } else if (id == R.id.nav_settings) {
            // Already in Settings
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);

            Preference themePreference = findPreference("theme_option");
            if (themePreference != null) {
                themePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        ThemeManager.setTheme(requireContext(), (String) newValue);
                        requireActivity().recreate(); // Recreate activity to apply theme change
                        return true;
                    }
                });
            }
        }
    }
}
