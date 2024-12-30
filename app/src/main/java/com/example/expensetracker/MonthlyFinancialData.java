package com.example.expensetracker;

public class MonthlyFinancialData {
    private String monthName;
    private double totalIncome;
    private double totalExpense;

    public MonthlyFinancialData(String monthName, double totalIncome, double totalExpense) {
        this.monthName = monthName;
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
    }

    public String getMonthName() {
        return monthName;
    }

    public double getTotalIncome() {
        return totalIncome;
    }

    public double getTotalExpense() {
        return totalExpense;
    }
}
