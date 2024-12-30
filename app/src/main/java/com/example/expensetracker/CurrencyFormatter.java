package com.example.expensetracker;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

public class CurrencyFormatter {

    public static String formatCurrency(Context context, double amount) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String currency = sharedPreferences.getString("currency", "USD");

        // Format currency based on preference
        switch (currency) {
            case "USD":
                return "$" + String.format("%.2f", amount);
            case "EUR":
                return "€" + String.format("%.2f", amount);
            case "GBP":
                return "£" + String.format("%.2f", amount);
            case "INR":
                return "₹" + String.format("%.2f", amount);
            default:
                return "$" + String.format("%.2f", amount);
        }
    }


}
