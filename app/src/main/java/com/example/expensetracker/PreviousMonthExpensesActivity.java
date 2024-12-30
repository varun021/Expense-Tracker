package com.example.expensetracker;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.List;

public class PreviousMonthExpensesActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private ExpenseAdapter expenseAdapter;
    private List<Expense> previousMonthExpenses;
    private RecyclerView recyclerView;
    private TextView totalExpenseTextView, totalIncomeTextView, balanceTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.applyTheme(this);

        setContentView(R.layout.activity_previous_month_expenses);

        databaseHelper = new DatabaseHelper(this);

        recyclerView = findViewById(R.id.previous_month_expenses_recycler_view);
        totalExpenseTextView = findViewById(R.id.total_expense_text_view);
        totalIncomeTextView = findViewById(R.id.total_income_text_view);
        balanceTextView = findViewById(R.id.balance_text_view);

        setupRecyclerView();
        updateExpenseList();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        expenseAdapter = new ExpenseAdapter(this, previousMonthExpenses);
        recyclerView.setAdapter(expenseAdapter);
    }

    private void updateExpenseList() {
        previousMonthExpenses = databaseHelper.getExpensesForPreviousMonth();
        if (expenseAdapter != null) {
            expenseAdapter.updateExpenses(previousMonthExpenses);
        }
        updateTotals(previousMonthExpenses);
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
        totalExpenseTextView.setText(String.format(" %s", formatter.formatCurrency(this, totalExpense)));
        totalIncomeTextView.setText(String.format(" %s", formatter.formatCurrency(this, totalIncome)));
        balanceTextView.setText(String.format(" %s", formatter.formatCurrency(this, totalIncome - totalExpense)));
    }
}
