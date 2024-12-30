package com.example.expensetracker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.util.Arrays;
import java.util.List;

public class AddRecurringTransactionActivity extends AppCompatActivity {
    private EditText transactionNameEditText, amountEditText;
    private Spinner categorySpinner, frequencySpinner;
    private Button saveButton;
    private DatabaseHelper databaseHelper;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch isIncomeSwitch;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recurring_transaction);

        initializeViews();
        databaseHelper = new DatabaseHelper(this);

        loadCategories();
        setupFrequencySpinner();
        setupNavigationDrawer();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveRecurringTransaction();
            }
        });
    }

    private void initializeViews() {
        transactionNameEditText = findViewById(R.id.transactionNameEditText);
        amountEditText = findViewById(R.id.amountEditText);
        categorySpinner = findViewById(R.id.categorySpinner);
        frequencySpinner = findViewById(R.id.frequencySpinner);
        saveButton = findViewById(R.id.saveButton);
        isIncomeSwitch = findViewById(R.id.isIncomeSwitch); // Initialize the Switch
    }

    private void loadCategories() {
        List<String> categories = databaseHelper.getAllCategories();
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
    }

    private void setupFrequencySpinner() {
        List<String> frequencies = Arrays.asList("daily", "weekly", "monthly", "yearly");
        ArrayAdapter<String> frequencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, frequencies);
        frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        frequencySpinner.setAdapter(frequencyAdapter);
    }

    private void saveRecurringTransaction() {
        String transactionName = transactionNameEditText.getText().toString().trim();
        String amountStr = amountEditText.getText().toString().trim();
        String category = categorySpinner.getSelectedItem().toString();
        String frequency = frequencySpinner.getSelectedItem().toString();
        boolean isIncome = isIncomeSwitch.isChecked(); // Get the state of the Switch

        if (transactionName.isEmpty() || amountStr.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        RecurringTransaction recurringTransaction = new RecurringTransaction(transactionName, amount, category, isIncome, frequency, System.currentTimeMillis());
        long result = databaseHelper.addRecurringTransaction(recurringTransaction);

        if (result != -1) {
            Toast.makeText(this, "Recurring transaction added successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to add recurring transaction", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupNavigationDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    startActivity(new Intent(AddRecurringTransactionActivity.this, MainActivity.class));
                } else if (id == R.id.nav_expenses) {
                    startActivity(new Intent(AddRecurringTransactionActivity.this, CategoryManagerActivity.class));
                } else if (id == R.id.nav_recurring_transactions) {
                    startActivity(new Intent(AddRecurringTransactionActivity.this, RecurringTransactionsActivity.class));
                } else if (id == R.id.nav_budgets) {
                    startActivity(new Intent(AddRecurringTransactionActivity.this, BudgetManagerActivity.class));
                } else if (id == R.id.nav_chart) {
                    startActivity(new Intent(AddRecurringTransactionActivity.this, AnalyticsActivity.class));
                } else if (id == R.id.nav_settings) {
                    startActivity(new Intent(AddRecurringTransactionActivity.this, SettingsActivity.class));
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }
}