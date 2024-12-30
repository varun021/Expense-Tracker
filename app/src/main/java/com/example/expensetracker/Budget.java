package com.example.expensetracker;

import android.content.Context;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class Budget {
    private int id;
    private String category;
    private double amount;
    private long startDate;
    private long endDate;

    // Default constructor
    public Budget() {}

    // Constructor without ID
    public Budget(String category, double amount, long startDate, long endDate) {
        this.category = category;
        this.amount = amount;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Constructor with ID
    public Budget(int id, String category, double amount, long startDate, long endDate) {
        this.id = id;
        this.category = category;
        this.amount = amount;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public long getStartDate() { return startDate; }
    public void setStartDate(long startDate) { this.startDate = startDate; }
    public long getEndDate() { return endDate; }
    public void setEndDate(long endDate) { this.endDate = endDate; }

    @Override
    public String toString() {
        return String.format("Category: %s, Amount: %s, From: %s To: %s",
                category,
                formatCurrency(amount),
                formatDate(startDate),
                formatDate(endDate));
    }

    // Utility method for formatting currency
    public String formatCurrency(double amount) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
        return currencyFormat.format(amount);
    }

    // Utility method for formatting date
    public String formatDate(long dateInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateInMillis);
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Budget budget = (Budget) o;
        return id == budget.id &&
                Double.compare(budget.amount, amount) == 0 &&
                startDate == budget.startDate &&
                endDate == budget.endDate &&
                category.equals(budget.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, category, amount, startDate, endDate);
    }

    public double getSpentAmount(Context context) {
        DatabaseHelper db = new DatabaseHelper(context); // Pass the context to the constructor
        return db.getSpentAmountForBudget(id); // Adjust this method to fit your actual implementation
    }
}