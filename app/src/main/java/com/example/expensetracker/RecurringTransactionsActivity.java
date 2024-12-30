package com.example.expensetracker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.android.material.navigation.NavigationView;

import java.util.List;

public class RecurringTransactionsActivity extends AppCompatActivity {

    private RecyclerView recurringTransactionsRecyclerView;
    private DatabaseHelper databaseHelper;
    private RecurringTransactionAdapter recurringAdapter;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recurring_transactions);

        // Initialize views
        Button addRecurringButton = findViewById(R.id.addRecurringButton);
        recurringTransactionsRecyclerView = findViewById(R.id.recurringTransactionsRecyclerView);
        databaseHelper = new DatabaseHelper(this);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Recurring Transactions");
        }
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));

        // Set up the navigation drawer
        setupNavigationDrawer(toolbar);

        // Set up RecyclerView
        recurringTransactionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadRecurringTransactions();

        addRecurringButton.setOnClickListener(v ->
                startActivity(new Intent(RecurringTransactionsActivity.this, AddRecurringTransactionActivity.class))
        );
    }

    private void setupNavigationDrawer(Toolbar toolbar) {
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(android.R.color.white));
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(RecurringTransactionsActivity.this, MainActivity.class));
            } else if (id == R.id.nav_expenses) {
                startActivity(new Intent(RecurringTransactionsActivity.this, CategoryManagerActivity.class));
            } else if (id == R.id.nav_recurring_transactions) {
                Toast.makeText(RecurringTransactionsActivity.this, "Already in Recurring Transactions", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_budgets) {
                startActivity(new Intent(RecurringTransactionsActivity.this, BudgetManagerActivity.class));
            } else if (id == R.id.nav_chart) {
                startActivity(new Intent(RecurringTransactionsActivity.this, AnalyticsActivity.class));
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(RecurringTransactionsActivity.this, SettingsActivity.class));
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecurringTransactions();
    }

    private void loadRecurringTransactions() {
        List<RecurringTransaction> recurringTransactions = databaseHelper.getAllRecurringTransactions();
        View emptyView = findViewById(R.id.emptyView);
        if (recurringTransactions.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
            recurringAdapter = new RecurringTransactionAdapter(this, recurringTransactions);
            recurringTransactionsRecyclerView.setAdapter(recurringAdapter);
        }
    }

    // Custom adapter for displaying recurring transactions
    private class RecurringTransactionAdapter extends RecyclerView.Adapter<RecurringTransactionAdapter.ViewHolder> {
        private final List<RecurringTransaction> transactions;
        private final LayoutInflater inflater;

        public RecurringTransactionAdapter(Context context, List<RecurringTransaction> transactions) {
            this.transactions = transactions;
            this.inflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.item_recurring_transaction, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            RecurringTransaction transaction = transactions.get(position);

            holder.titleTextView.setText(transaction.getTitle());
            holder.amountTextView.setText(CurrencyFormatter.formatCurrency(holder.itemView.getContext(), transaction.getAmount()));
            holder.frequencyTextView.setText(transaction.getFrequency());

            holder.deleteButton.setOnClickListener(v -> deleteRecurringTransaction(transaction));
        }

        @Override
        public int getItemCount() {
            return transactions.size();
        }

        private class ViewHolder extends RecyclerView.ViewHolder {
            TextView titleTextView;
            TextView amountTextView;
            TextView frequencyTextView;
            MaterialButton deleteButton; // Updated to MaterialButton

            ViewHolder(View itemView) {
                super(itemView);
                titleTextView = itemView.findViewById(R.id.titleTextView);
                amountTextView = itemView.findViewById(R.id.amountTextView);
                frequencyTextView = itemView.findViewById(R.id.frequencyTextView);
                deleteButton = itemView.findViewById(R.id.deleteButton); // Updated to MaterialButton
            }
        }
    }

    private void deleteRecurringTransaction(RecurringTransaction transaction) {
        int rowsDeleted = RecurringTransactionManager.deleteRecurringTransaction(this, transaction.getId());
        if (rowsDeleted > 0) {
            Toast.makeText(this, "Transaction deleted successfully", Toast.LENGTH_SHORT).show();
            loadRecurringTransactions();
        } else {
            Toast.makeText(this, "Failed to delete the transaction", Toast.LENGTH_SHORT).show();
        }
    }
}
