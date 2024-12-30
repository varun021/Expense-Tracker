package com.example.expensetracker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateFormatter {

    // Format a date to a string in the format "dd/MM/yyyy"
    public static String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(date);
    }

    // Format a date from a long timestamp
    public static String formatDate(long timestamp) {
        return formatDate(new Date(timestamp));
    }
}
