package com.example.expensetracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.expensetracker.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ActivityMainBinding binding;
    private DatabaseHelper databaseHelper;
    private ExpenseAdapter expenseAdapter;
    private List<Expense> expenses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.applyTheme(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar; // Use Toolbar from androidx.appcompat.widget
        setSupportActionBar(toolbar);

        databaseHelper = new DatabaseHelper(this);

        // Check and reset expenses for the new month
        checkAndResetForNewMonth();

        setupNavigationDrawer();
        setupFloatingActionButton();
        setupRecyclerView();

        RecurringTransactionManager.checkAndAddDueTransactions(this);

        // Inflate the toolbar menu
        toolbar.inflateMenu(R.menu.main_toolbar_menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateExpenseList();
    }

    private void setupNavigationDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, binding.drawerLayout, binding.toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        binding.navView.setNavigationItemSelectedListener(this);
    }

    private void setupFloatingActionButton() {
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, AddExpenseActivity.class));
            }
        });
    }

    private void setupRecyclerView() {
        binding.expenseRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadCurrentMonthExpenses();
        expenseAdapter = new ExpenseAdapter(this, expenses);
        binding.expenseRecyclerView.setAdapter(expenseAdapter);

        setupSwipeToDelete();
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false; // We don't want to support moving items up/down in the list
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Expense deletedExpense = expenses.get(position);

                // Delete the expense from the database
                databaseHelper.deleteExpense(deletedExpense.getId());

                // Remove the item from the adapter
                expenseAdapter.deleteExpense(position);

                // Show Snackbar with the option to undo the delete action
                showUndoSnackbar(deletedExpense, position);
            }
        });

        itemTouchHelper.attachToRecyclerView(binding.expenseRecyclerView);
    }

    private void showUndoSnackbar(final Expense deletedExpense, final int position) {
        Snackbar snackbar = Snackbar.make(binding.expenseRecyclerView, "Expense deleted", Snackbar.LENGTH_LONG)
                .setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Restore the deleted expense in the adapter and database
                        expenseAdapter.restoreExpense(deletedExpense, position);
                        databaseHelper.addExpense(deletedExpense);
                        binding.expenseRecyclerView.scrollToPosition(position);
                    }
                });

        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                    // Remove the expense permanently only if "Undo" was not clicked
                    databaseHelper.deleteExpense(deletedExpense.getId());
                }
            }
        });

        snackbar.show();
    }

    private void updateExpenseList() {
        loadCurrentMonthExpenses();
        Log.d("MainActivity", "Number of expenses: " + expenses.size());

        if (expenseAdapter != null) {
            expenseAdapter.updateExpenses(expenses);
        }

        updateTotals(expenses);

        // Check budget alerts
        BudgetManager.checkBudgetAlerts(this, expenses);
    }

    private void updateTotals(List<Expense> expenses) {
        double totalExpense = 0;
        double totalIncome = 0;
        for (Expense expense : expenses) {
            if (expense.isIncome()) {
                totalIncome += expense.getAmount();
            } else {
                totalExpense += expense.getAmount();
            }
        }

        CurrencyFormatter formatter = new CurrencyFormatter();
        binding.totalExpenseTextView.setText(String.format("Total Expense: %s", formatter.formatCurrency(this, totalExpense)));
        binding.totalIncomeTextView.setText(String.format("Total Income: %s", formatter.formatCurrency(this, totalIncome)));
        binding.balanceTextView.setText(String.format("Balance: %s", formatter.formatCurrency(this, totalIncome - totalExpense)));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Already on the home screen
        } else if (id == R.id.nav_expenses) {
            startActivity(new Intent(this, CategoryManagerActivity.class));
        } else if (id == R.id.nav_recurring_transactions) {
            startActivity(new Intent(this, RecurringTransactionsActivity.class));
        } else if (id == R.id.nav_budgets) {
            startActivity(new Intent(this, BudgetManagerActivity.class));
        } else if (id == R.id.nav_chart) {
            startActivity(new Intent(this, AnalyticsActivity.class));
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void checkAndResetForNewMonth() {
        SharedPreferences prefs = getSharedPreferences("ExpenseTracker", MODE_PRIVATE);
        int lastMonth = prefs.getInt("lastMonth", -1);
        int lastYear = prefs.getInt("lastYear", -1);

        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        int currentYear = calendar.get(Calendar.YEAR);

        if (lastMonth != currentMonth || lastYear != currentYear) {
            // Clear the expenses on the main screen
            clearItems();
            prefs.edit().putInt("lastMonth", currentMonth).putInt("lastYear", currentYear).apply();
        }
    }

    private void loadCurrentMonthExpenses() {
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        int currentYear = calendar.get(Calendar.YEAR);

        expenses = databaseHelper.getExpensesForMonth(currentMonth, currentYear);
    }

    private void clearItems() {
        if (expenses != null) {
            expenses.clear();
            expenseAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_view_history) {
            startActivity(new Intent(this, PreviousMonthExpensesActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
