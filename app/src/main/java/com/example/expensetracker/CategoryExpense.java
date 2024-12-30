package com.example.expensetracker;

public class CategoryExpense {
    private String category;
    private double amount;
    private boolean isIncome;

    public CategoryExpense(String category, double amount, boolean isIncome) {
        this.category = category;
        this.amount = amount;
        this.isIncome = isIncome;
    }

    // Getter and setter methods
    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

    public boolean isIncome() {
        return isIncome;
    }
}
