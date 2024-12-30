package com.example.expensetracker;

import android.content.Context;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BudgetManager {

    public static void checkBudgetAlerts(Context context, List<Expense> expenses) {
        DatabaseHelper db = new DatabaseHelper(context);
        List<Budget> budgets = db.getAllBudgets();

//        if (budgets == null || budgets.isEmpty()) {
//            Toast.makeText(context, "No budgets found.", Toast.LENGTH_SHORT).show();
//            return;
//        }

        Map<String, Double> categorySpending = new HashMap<>();

        // Calculate spending for each category
        if (expenses != null) {
            for (Expense expense : expenses) {
                if (!expense.isIncome()) {
                    String category = expense.getCategory();
                    double amount = categorySpending.getOrDefault(category, 0.0);
                    categorySpending.put(category, amount + expense.getAmount());
                }
            }
        }

        // Check if spending exceeds budget
        for (Budget budget : budgets) {
            double spent = categorySpending.getOrDefault(budget.getCategory(), 0.0);
            double budgetAmount = budget.getAmount();

            if (spent > budgetAmount) {
                Toast.makeText(context,
                        "Budget alert: You have exceeded your budget for " +
                                budget.getCategory() + ". Spent: " + formatCurrency(context, spent) +
                                ", Budget: " + formatCurrency(context, budgetAmount),
                        Toast.LENGTH_LONG).show();
            } else if (spent > budgetAmount * 0.8) {
                Toast.makeText(context,
                        "Budget warning: You are approaching the budget limit for " +
                                budget.getCategory() + ". Spent: " + formatCurrency(context, spent) +
                                ", Budget: " + formatCurrency(context, budgetAmount),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    // Utility method for formatting currency
    private static String formatCurrency(Context context, double amount) {
        // Adjust this method to match your currency formatting requirements
        return CurrencyFormatter.formatCurrency(context, amount);
    }
}
