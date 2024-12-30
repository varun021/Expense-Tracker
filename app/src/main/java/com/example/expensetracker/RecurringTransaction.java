package com.example.expensetracker;

import java.io.Serializable;

public class RecurringTransaction implements Serializable {
    private int id;
    private String title;
    private double amount;
    private String category;
    private boolean isIncome;
    private String frequency;
    private long nextDueDate;

    public RecurringTransaction(int id, String title, double amount, String category, boolean isIncome, String frequency, long nextDueDate) {
        this.id = id;
        this.title = title;
        this.amount = amount;
        this.category = category;
        this.isIncome = isIncome;
        this.frequency = frequency;
        this.nextDueDate = nextDueDate;
    }

    public RecurringTransaction(String title, double amount, String category, boolean isIncome, String frequency, long nextDueDate) {
        this(-1, title, amount, category, isIncome, frequency, nextDueDate);
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public boolean isIncome() { return isIncome; }
    public void setIncome(boolean income) { isIncome = income; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public long getNextDueDate() { return nextDueDate; }
    public void setNextDueDate(long nextDueDate) { this.nextDueDate = nextDueDate; }
}