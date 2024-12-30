package com.example.expensetracker;

import android.content.Context;
import java.util.Calendar;
import java.util.List;

public class RecurringTransactionManager {

    public static void checkAndAddDueTransactions(Context context) {
        DatabaseHelper db = new DatabaseHelper(context);
        List<RecurringTransaction> recurringTransactions = db.getAllRecurringTransactions();
        long currentTime = System.currentTimeMillis();

        for (RecurringTransaction rt : recurringTransactions) {
            if (rt.getNextDueDate() <= currentTime) {
                Expense expense = new Expense(rt.getTitle(), rt.getAmount(), rt.getCategory(), rt.isIncome(), currentTime);
                db.addExpense(expense);

                long nextDueDate = calculateNextDueDate(rt.getNextDueDate(), rt.getFrequency());
                db.updateRecurringTransactionNextDueDate(rt.getId(), nextDueDate);

                if (shouldDeleteTransaction(rt)) {
                    deleteRecurringTransaction(context, rt.getId());
                }
            }
        }
    }

    private static long calculateNextDueDate(long currentDueDate, String frequency) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(currentDueDate);

        switch (frequency.toUpperCase()) {
            case "DAILY":
                cal.add(Calendar.DAY_OF_YEAR, 1);
                break;
            case "WEEKLY":
                cal.add(Calendar.WEEK_OF_YEAR, 1);
                break;
            case "MONTHLY":
                cal.add(Calendar.MONTH, 1);
                break;
            case "YEARLY":
                cal.add(Calendar.YEAR, 1);
                break;
            default:
                throw new IllegalArgumentException("Unknown frequency: " + frequency);
        }

        return cal.getTimeInMillis();
    }

    private static boolean shouldDeleteTransaction(RecurringTransaction rt) {
        return "ONCE".equalsIgnoreCase(rt.getFrequency());
    }

    public static int deleteRecurringTransaction(Context context, int transactionId) {
        DatabaseHelper db = new DatabaseHelper(context);
        return db.deleteRecurringTransaction(transactionId);
    }
}
