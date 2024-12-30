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

import java.util.List;

public class AddExpenseActivity extends AppCompatActivity {

    private EditText titleEditText, amountEditText;
    private Spinner categorySpinner;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch incomeSwitch;
    private Button addButton;
    private DatabaseHelper databaseHelper;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        // Initialize views
        titleEditText = findViewById(R.id.titleEditText);
        amountEditText = findViewById(R.id.amountEditText);
        categorySpinner = findViewById(R.id.categorySpinner);
        incomeSwitch = findViewById(R.id.incomeSwitch);
        addButton = findViewById(R.id.addButton);
        databaseHelper = new DatabaseHelper(this);

        // Initialize toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Populate category spinner
        populateCategorySpinner();

        // Set up Add button click listener
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleAddButtonClick();
            }
        });

        // Initialize navigation drawer (if needed)
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    startActivity(new Intent(AddExpenseActivity.this, MainActivity.class));
                } else if (id == R.id.nav_expenses) {
                    startActivity(new Intent(AddExpenseActivity.this, AddExpenseActivity.class));
                } else if (id == R.id.nav_recurring_transactions) {
                    startActivity(new Intent(AddExpenseActivity.this, RecurringTransactionsActivity.class));
                } else if (id == R.id.nav_budgets) {
                    startActivity(new Intent(AddExpenseActivity.this, BudgetManagerActivity.class));
                } else if (id == R.id.nav_chart) {
                    startActivity(new Intent(AddExpenseActivity.this, AnalyticsActivity.class));
                } else if (id == R.id.nav_settings) {
                    startActivity(new Intent(AddExpenseActivity.this, SettingsActivity.class));
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }

    private void populateCategorySpinner() {
        List<String> categories = databaseHelper.getAllCategories();
        if (categories.isEmpty()) {
            categories.add("No categories available");
        }
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
    }

    private void handleAddButtonClick() {
        String title = titleEditText.getText().toString().trim();
        String amountStr = amountEditText.getText().toString().trim();
        String category = categorySpinner.getSelectedItem().toString();
        boolean isIncome = incomeSwitch.isChecked();

        if (title.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            long currentDate = System.currentTimeMillis(); // Get current timestamp
            Expense expense = new Expense(title, amount, category, isIncome, currentDate); // Use the constructor with date
            long newRowId = databaseHelper.addExpense(expense);
            if (newRowId != -1) {
                Toast.makeText(this, "Transaction added with ID: " + newRowId, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error adding transaction", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount format", Toast.LENGTH_SHORT).show();
        }
    }
}
