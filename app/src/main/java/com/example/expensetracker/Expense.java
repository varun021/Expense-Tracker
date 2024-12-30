package com.example.expensetracker;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class Expense implements Serializable {
    private final int id;
    private final String title;
    private final double amount;
    private final String category;
    private final boolean isIncome;
    private final long date; // Timestamp in milliseconds
    private final int month;
    private final int year;

    // Constructor with ID
    public Expense(int id, String title, double amount, String category, boolean isIncome, long date) {
        this(id, title, amount, category, isIncome, date, getMonthFromDate(date), getYearFromDate(date));
    }

    // Constructor without ID (for adding new expense)
    public Expense(String title, double amount, String category, boolean isIncome, long date) {
        this(0, title, amount, category, isIncome, date, getMonthFromDate(date), getYearFromDate(date));
    }

    // Constructor with all fields
    public Expense(int id, String title, double amount, String category, boolean isIncome, long date, int month, int year) {
        if (title == null || title.isEmpty()) throw new IllegalArgumentException("Title cannot be null or empty");
        if (category == null || category.isEmpty()) throw new IllegalArgumentException("Category cannot be null or empty");
        if (amount < 0) throw new IllegalArgumentException("Amount cannot be negative");

        this.id = id;
        this.title = title;
        this.amount = amount;
        this.category = category;
        this.isIncome = isIncome;
        this.date = date;
        this.month = month;
        this.year = year;
    }

    // Getters (no setters for immutability)
    public int getId() { return id; }
    public String getTitle() { return title; }
    public double getAmount() { return amount; }
    public String getCategory() { return category; }
    public boolean isIncome() { return isIncome; }
    public long getDate() { return date; }
    public int getMonth() { return month; }
    public int getYear() { return year; }

    // Method to get the formatted date
    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
        return sdf.format(new Date(date));
    }

    @Override
    public String toString() {
        return "Expense{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", amount=" + amount +
                ", category='" + category + '\'' +
                ", isIncome=" + isIncome +
                ", date=" + getFormattedDate() +
                ", month=" + month +
                ", year=" + year +
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
                month == expense.month &&
                year == expense.year &&
                title.equals(expense.title) &&
                category.equals(expense.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, amount, category, isIncome, date, month, year);
    }

    // Method to check if it is an expense
    public boolean isExpense() {
        return !isIncome; // If not income, then it is an expense
    }

    // Method to create a new Expense with an updated amount
    public Expense withUpdatedAmount(double newAmount) {
        return new Expense(id, title, newAmount, category, isIncome, date, month, year);
    }

    private static int getMonthFromDate(long date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        return calendar.get(Calendar.MONTH) + 1; // Months are 0-based in Calendar
    }

    private static int getYearFromDate(long date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        return calendar.get(Calendar.YEAR);
    }

}
