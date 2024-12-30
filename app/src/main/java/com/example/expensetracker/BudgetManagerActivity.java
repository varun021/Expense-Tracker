package com.example.expensetracker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BudgetManagerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private TextInputLayout amountInputLayout;
    private TextInputEditText amountEditText;
    private Spinner categorySpinner;
    private MaterialButton setStartDateButton, setEndDateButton, addBudgetButton;
    private RecyclerView budgetsRecyclerView;
    private DatabaseHelper databaseHelper;
    private BudgetAdapter budgetAdapter;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private long startDate, endDate;
    private Budget selectedBudget;
    private ArrayAdapter<String> categoryAdapter;
    private TextView remainingTextView;
    private ProgressBar budgetProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_manager);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize views and other components
        initializeViews();
        setupRecyclerView();
        setupListeners();
        loadCategories();  // Load categories into the spinner
        loadBudgets();    // Load budgets into the RecyclerView

        // Set up Navigation Drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
    }

    private void initializeViews() {
        amountInputLayout = findViewById(R.id.amountInputLayout);
        amountEditText = findViewById(R.id.amountEditText);
        categorySpinner = findViewById(R.id.categorySpinner);
        setStartDateButton = findViewById(R.id.setStartDateButton);
        setEndDateButton = findViewById(R.id.setEndDateButton);
        addBudgetButton = findViewById(R.id.addBudgetButton);
        budgetsRecyclerView = findViewById(R.id.budgetsRecyclerView);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        remainingTextView = findViewById(R.id.remainingTextView);
        budgetProgressBar = findViewById(R.id.budgetProgressBar);
        databaseHelper = new DatabaseHelper(this);
    }

    private void setupRecyclerView() {
        budgetAdapter = new BudgetAdapter(new ArrayList<>(), this::onBudgetItemClick, this::onBudgetDelete);
        budgetsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        budgetsRecyclerView.setAdapter(budgetAdapter);
    }

    private void setupListeners() {
        setStartDateButton.setOnClickListener(v -> showDatePicker(true));
        setEndDateButton.setOnClickListener(v -> showDatePicker(false));
        addBudgetButton.setOnClickListener(v -> addOrUpdateBudget());

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Handle category selection if needed
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle when no category is selected
            }
        });
    }

    private void showDatePicker(final boolean isStartDate) {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(isStartDate ? "Select start date" : "Select end date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            if (isStartDate) {
                startDate = selection;
                setStartDateButton.setText(DateFormatter.formatDate(new Date(startDate)));
            } else {
                endDate = selection;
                setEndDateButton.setText(DateFormatter.formatDate(new Date(endDate)));
            }
        });

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    private void loadCategories() {
        List<String> categories = databaseHelper.getAllCategories();
        categoryAdapter = new ArrayAdapter<>(this, R.layout.bg_spinner, R.id.spinner_item_text, categories);
        categorySpinner.setAdapter(categoryAdapter);
    }

    private void addOrUpdateBudget() {
        String category = (String) categorySpinner.getSelectedItem();
        String amountStr = amountEditText.getText().toString().trim();

        if (category == null || amountStr.isEmpty() || startDate == 0 || endDate == 0) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);

        if (selectedBudget != null) {
            // Update existing budget
            selectedBudget.setCategory(category);
            selectedBudget.setAmount(amount);
            selectedBudget.setStartDate(startDate);
            selectedBudget.setEndDate(endDate);

            if (databaseHelper.updateBudget(selectedBudget)) {
                Toast.makeText(this, "Budget updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to update budget", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Add new budget
            Budget newBudget = new Budget(category, amount, startDate, endDate);

            if (databaseHelper.addBudget(newBudget) != -1) {
                Toast.makeText(this, "Budget added successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to add budget", Toast.LENGTH_SHORT).show();
            }
        }

        clearInputFields();
        loadBudgets();  // Refresh the RecyclerView
    }

    private void clearInputFields() {
        amountEditText.setText("");
        categorySpinner.setSelection(0);
        setStartDateButton.setText("Set start date");
        setEndDateButton.setText("Set end date");
        startDate = 0;
        endDate = 0;
        selectedBudget = null;
    }

    private void loadBudgets() {
        List<Budget> budgets = databaseHelper.getAllBudgets();
        budgetAdapter.updateBudgets(budgets);
        updateRemainingAmountForAllBudgets(budgets);
    }

    private void updateRemainingAmountForAllBudgets(List<Budget> budgets) {
        for (Budget budget : budgets) {
            double spentAmount = databaseHelper.getSpentAmountForBudget(budget.getId());
            updateRemainingAmount(budget.getAmount(), spentAmount);
            updateProgressBar(budget.getAmount(), spentAmount);
        }
    }

    private void updateRemainingAmount(double budgetAmount, double spentAmount) {
        if (remainingTextView != null) {
            double remainingAmount = budgetAmount - spentAmount;
            String formattedRemainingAmount = CurrencyFormatter.formatCurrency(this, remainingAmount);
            remainingTextView.setText(formattedRemainingAmount);
        }
    }

    private void updateProgressBar(double budgetAmount, double spentAmount) {
        if (budgetProgressBar != null) {
            int progress = (int) ((spentAmount / budgetAmount) * 100);
            budgetProgressBar.setProgress(progress);
        }
    }

    private void onBudgetItemClick(Budget budget) {
        selectedBudget = budget;
        amountEditText.setText(String.valueOf(budget.getAmount()));
        categorySpinner.setSelection(categoryAdapter.getPosition(budget.getCategory()));
        setStartDateButton.setText(DateFormatter.formatDate(new Date(budget.getStartDate())));
        setEndDateButton.setText(DateFormatter.formatDate(new Date(budget.getEndDate())));
    }

    private void onBudgetDelete(Budget budget, int position) {
        if (databaseHelper.deleteBudget(budget.getId())) {
            Toast.makeText(this, "Budget deleted successfully", Toast.LENGTH_SHORT).show();
            budgetAdapter.updateBudgets(databaseHelper.getAllBudgets()); // Refresh the RecyclerView
        } else {
            Toast.makeText(this, "Failed to delete budget", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Already on the home screen
        } else if (id == R.id.nav_expenses) {
            startActivity(new Intent(BudgetManagerActivity.this, CategoryManagerActivity.class));
        } else if (id == R.id.nav_recurring_transactions) {
            startActivity(new Intent(BudgetManagerActivity.this, RecurringTransactionsActivity.class));
        } else if (id == R.id.nav_budgets) {
            // This is the current activity
        }else if (id == R.id.nav_chart) {
                startActivity(new Intent(BudgetManagerActivity.this, AnalyticsActivity.class));
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(BudgetManagerActivity.this, SettingsActivity.class));
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
