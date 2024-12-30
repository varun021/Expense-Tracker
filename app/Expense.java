package com.example.expensetracker;

import java.io.Serializable;
import java.util.Objects;

public class Expense implements Serializable {
    private final int id;
    private final String title;
    private final double amount;
    private final String category;
    private final boolean isIncome;
    private final long date;

    // Constructor with ID
    public Expense(int id, String title, double amount, String category, boolean isIncome, long date) {
        if (title == null || title.isEmpty()) throw new IllegalArgumentException("Title cannot be null or empty");
        if (category == null || category.isEmpty()) throw new IllegalArgumentException("Category cannot be null or empty");
        if (amount < 0) throw new IllegalArgumentException("Amount cannot be negative");

        this.id = id;
        this.title = title;
        this.amount = amount;
        this.category = category;
        this.isIncome = isIncome;
        this.date = date;
    }

    // Constructor without ID (for adding new expense)
    public Expense(String title, double amount, String category, boolean isIncome, long date) {
        if (title == null || title.isEmpty()) throw new IllegalArgumentException("Title cannot be null or empty");
        if (category == null || category.isEmpty()) throw new IllegalArgumentException("Category cannot be null or empty");
        if (amount < 0) throw new IllegalArgumentException("Amount cannot be negative");

        this.id = 0; // Default value, it will be updated by the database
        this.title = title;
        this.amount = amount;
        this.category = category;
        this.isIncome = isIncome;
        this.date = date;
    }

    // Getters (no setters for immutability)
    public int getId() { return id; }
    public String getTitle() { return title; }
    public double getAmount() { return amount; }
    public String getCategory() { return category; }
    public boolean isIncome() { return isIncome; }
    public long getDate() { return date; }

    @Override
    public String toString() {
        return "Expense{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", amount=" + amount +
                ", category='" + category + '\'' +
                ", isIncome=" + isIncome +
                ", date=" + date +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Expense expense = (Expense) o;
        return id == expense.id &&
                Double.compare(expense.amount, amount) == 0 &&
                isIncome == expense.isIncome &&
                date == expense.date &&
                title.equals(expense.title) &&
                category.equals(expense.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, amount, category, isIncome, date);
    }

    // Method to check if it is an expense
    public boolean isExpense() {
        return !isIncome; // If not income, then it is an expense
    }
}