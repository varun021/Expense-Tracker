package com.example.expensetracker;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ExpenseManager.db";
    private static final int DATABASE_VERSION = 5;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    // Table and column names
    private static final String TABLE_EXPENSES = "expenses";
    private static final String TABLE_CATEGORIES = "categories";
    private static final String TABLE_RECURRING = "recurring_transactions";
    private static final String TABLE_BUDGETS = "budgets";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_AMOUNT = "amount";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_IS_INCOME = "is_income";
    private static final String COLUMN_FREQUENCY = "frequency";
    private static final String COLUMN_NEXT_DUE_DATE = "next_due_date";
    private static final String COLUMN_START_DATE = "start_date";
    private static final String COLUMN_END_DATE = "end_date";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_MONTH = "month";
    private static final String COLUMN_YEAR = "year";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_EXPENSES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT NOT NULL, " +
                COLUMN_AMOUNT + " REAL NOT NULL, " +
                COLUMN_CATEGORY + " TEXT NOT NULL, " +
                COLUMN_IS_INCOME + " INTEGER NOT NULL, " +
                COLUMN_DATE + " INTEGER NOT NULL, " +
                COLUMN_MONTH + " INTEGER NOT NULL, " +
                COLUMN_YEAR + " INTEGER NOT NULL)";
        db.execSQL(createTable);

        String createCategoriesTable = "CREATE TABLE " + TABLE_CATEGORIES + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_TITLE + " TEXT UNIQUE)";
        db.execSQL(createCategoriesTable);

        String createRecurringTable = "CREATE TABLE " + TABLE_RECURRING + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_TITLE + " TEXT, "
                + COLUMN_AMOUNT + " REAL, "
                + COLUMN_CATEGORY + " TEXT, "
                + COLUMN_IS_INCOME + " INTEGER, "
                + COLUMN_FREQUENCY + " TEXT, "
                + COLUMN_NEXT_DUE_DATE + " INTEGER)";
        db.execSQL(createRecurringTable);

        String createBudgetsTable = "CREATE TABLE " + TABLE_BUDGETS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_CATEGORY + " TEXT, "
                + COLUMN_AMOUNT + " REAL, "
                + COLUMN_START_DATE + " INTEGER, "
                + COLUMN_END_DATE + " INTEGER)";
        db.execSQL(createBudgetsTable);

        // Add default categories
        String[] defaultCategories = {"Food", "Transport", "Housing", "Entertainment", "Utilities", "Salary"};
        for (String category : defaultCategories) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_TITLE, category);
            db.insert(TABLE_CATEGORIES, null, values);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            Log.d("DatabaseHelper", "Upgrading database to version 2: Adding year column");
            db.execSQL("ALTER TABLE " + TABLE_EXPENSES + " ADD COLUMN " + COLUMN_YEAR + " INTEGER");
        }

        if (oldVersion < 4) {
            Log.d("DatabaseHelper", "Upgrading database to version 4: Adding next_due_date column");
            db.execSQL("ALTER TABLE " + TABLE_RECURRING + " ADD COLUMN " + COLUMN_NEXT_DUE_DATE + " INTEGER");
        }

        if (oldVersion < 5) {
            Log.d("DatabaseHelper", "Upgrading database to version 5: Checking for date column");
            Cursor cursor = db.rawQuery("PRAGMA table_info(" + TABLE_EXPENSES + ")", null);
            boolean dateColumnExists = false;
            while (cursor.moveToNext()) {
                @SuppressLint("Range") String columnName = cursor.getString(cursor.getColumnIndex("name"));
                if (COLUMN_DATE.equals(columnName)) {
                    dateColumnExists = true;
                    break;
                }
            }
            cursor.close();

            if (!dateColumnExists) {
                Log.d("DatabaseHelper", "Adding date column to expenses table");
                db.execSQL("ALTER TABLE " + TABLE_EXPENSES + " ADD COLUMN " + COLUMN_DATE + " INTEGER");
            }
        }
    }



    // Expense methods
    public long addExpense(Expense expense) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, expense.getTitle());
        values.put(COLUMN_AMOUNT, expense.getAmount());
        values.put(COLUMN_CATEGORY, expense.getCategory());
        values.put(COLUMN_IS_INCOME, expense.isIncome() ? 1 : 0);
        values.put(COLUMN_DATE, expense.getDate());
        values.put(COLUMN_MONTH, getMonthFromDate(expense.getDate()));
        values.put(COLUMN_YEAR, getYearFromDate(expense.getDate()));
        long id = db.insert(TABLE_EXPENSES, null, values);
        db.close();
        return id;
    }



    public List<Expense> getAllExpenses() {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {
                COLUMN_ID,
                COLUMN_TITLE,
                COLUMN_AMOUNT,
                COLUMN_CATEGORY,
                COLUMN_IS_INCOME,
                COLUMN_DATE
        };
        Cursor cursor = db.query(TABLE_EXPENSES, projection, null, null, null, null, COLUMN_DATE + " DESC");
        if (cursor.moveToFirst()) {
            do {
                Expense expense = createExpenseFromCursor(cursor);
                if (expense != null) {
                    expenses.add(expense);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return expenses;
    }



    public Expense getExpenseById(int id) {
        String selection = COLUMN_ID + "=?";
        String[] selectionArgs = {String.valueOf(id)};

        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.query(TABLE_EXPENSES, null, selection, selectionArgs, null, null, null)) {

            if (cursor != null && cursor.moveToFirst()) {
                return createExpenseFromCursor(cursor);
            }
        }
        return null;
    }

    public void updateExpense(Expense expense) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_TITLE, expense.getTitle());
            values.put(COLUMN_AMOUNT, expense.getAmount());
            values.put(COLUMN_CATEGORY, expense.getCategory());
            values.put(COLUMN_IS_INCOME, expense.isIncome() ? 1 : 0);
            values.put(COLUMN_DATE, expense.getDate()); // Update the date
            db.update(TABLE_EXPENSES, values, COLUMN_ID + " = ?", new String[]{String.valueOf(expense.getId())});
        }
    }


    public void deleteExpense(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_EXPENSES, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    private Expense createExpenseFromCursor(Cursor cursor) {
        try {
            int idIndex = cursor.getColumnIndexOrThrow(COLUMN_ID);
            int titleIndex = cursor.getColumnIndexOrThrow(COLUMN_TITLE);
            int amountIndex = cursor.getColumnIndexOrThrow(COLUMN_AMOUNT);
            int categoryIndex = cursor.getColumnIndexOrThrow(COLUMN_CATEGORY);
            int isIncomeIndex = cursor.getColumnIndexOrThrow(COLUMN_IS_INCOME);
            int dateIndex = cursor.getColumnIndexOrThrow(COLUMN_DATE);

            int id = cursor.getInt(idIndex);
            String title = cursor.getString(titleIndex);
            double amount = cursor.getDouble(amountIndex);
            String category = cursor.getString(categoryIndex);
            boolean isIncome = cursor.getInt(isIncomeIndex) > 0;
            long date = cursor.getLong(dateIndex);

            return new Expense(id, title, amount, category, isIncome, date);
        } catch (IllegalArgumentException e) {
            Log.e("DatabaseHelper", "Column not found in cursor", e);
            return null;
        }
    }

    private int getMonthFromDate(long date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        return calendar.get(Calendar.MONTH) + 1;
    }
    private int getYearFromDate(long date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        return calendar.get(Calendar.YEAR);
    }
    public List<Expense> getExpensesForMonthAndYear(int month, int year) {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_EXPENSES,
                new String[]{COLUMN_ID, COLUMN_TITLE, COLUMN_AMOUNT, COLUMN_CATEGORY, COLUMN_IS_INCOME, COLUMN_DATE, COLUMN_MONTH, COLUMN_YEAR},
                COLUMN_MONTH + " = ? AND " + COLUMN_YEAR + " = ?", new String[]{String.valueOf(month), String.valueOf(year)},
                null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Expense expense = createExpenseFromCursor(cursor);
                if (expense != null) {
                    expenses.add(expense);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return expenses;
    }



    // Category methods
    @SuppressLint("Range")
    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_CATEGORIES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                categories.add(cursor.getString(cursor.getColumnIndex(COLUMN_TITLE)));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return categories;
    }

    public long addCategory(String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, category);
        long newRowId = db.insert(TABLE_CATEGORIES, null, values);
        db.close();
        return newRowId;
    }

    // RecurringTransaction methods
    public long addRecurringTransaction(RecurringTransaction transaction) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, transaction.getTitle());
        values.put(COLUMN_AMOUNT, transaction.getAmount());
        values.put(COLUMN_CATEGORY, transaction.getCategory());
        values.put(COLUMN_IS_INCOME, transaction.isIncome() ? 1 : 0);
        values.put(COLUMN_FREQUENCY, transaction.getFrequency());
        values.put(COLUMN_NEXT_DUE_DATE, transaction.getNextDueDate());
        long newRowId = db.insert(TABLE_RECURRING, null, values);
        db.close();
        return newRowId;
    }

    public List<RecurringTransaction> getAllRecurringTransactions() {
        List<RecurringTransaction> transactions = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_RECURRING;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                @SuppressLint("Range") String title = cursor.getString(cursor.getColumnIndex(COLUMN_TITLE));
                @SuppressLint("Range") double amount = cursor.getDouble(cursor.getColumnIndex(COLUMN_AMOUNT));
                @SuppressLint("Range") String category = cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY));
                @SuppressLint("Range") boolean isIncome = cursor.getInt(cursor.getColumnIndex(COLUMN_IS_INCOME)) == 1;
                @SuppressLint("Range") String frequency = cursor.getString(cursor.getColumnIndex(COLUMN_FREQUENCY));
                @SuppressLint("Range") long nextDueDate = cursor.getLong(cursor.getColumnIndex(COLUMN_NEXT_DUE_DATE));

                RecurringTransaction transaction = new RecurringTransaction(id, title, amount, category, isIncome, frequency, nextDueDate);
                transactions.add(transaction);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return transactions;
    }

    public RecurringTransaction getRecurringTransactionById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_RECURRING, null, COLUMN_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") RecurringTransaction transaction = new RecurringTransaction(
                    cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_TITLE)),
                    cursor.getDouble(cursor.getColumnIndex(COLUMN_AMOUNT)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_IS_INCOME)) == 1,
                    cursor.getString(cursor.getColumnIndex(COLUMN_FREQUENCY)),
                    cursor.getLong(cursor.getColumnIndex(COLUMN_NEXT_DUE_DATE))
            );
            cursor.close();
            return transaction;
        }
        return null;
    }

    public long updateRecurringTransaction(RecurringTransaction transaction) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, transaction.getTitle());
        values.put(COLUMN_AMOUNT, transaction.getAmount());
        values.put(COLUMN_CATEGORY, transaction.getCategory());
        values.put(COLUMN_IS_INCOME, transaction.isIncome() ? 1 : 0);
        values.put(COLUMN_FREQUENCY, transaction.getFrequency());
        values.put(COLUMN_NEXT_DUE_DATE, transaction.getNextDueDate());
        return db.update(TABLE_RECURRING, values, COLUMN_ID + "=?", new String[]{String.valueOf(transaction.getId())});
    }

    public int deleteRecurringTransaction(int transactionId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_RECURRING, COLUMN_ID + " = ?", new String[]{String.valueOf(transactionId)});
        db.close();
        return rowsDeleted;
    }

    public long addBudget(Budget budget) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CATEGORY, budget.getCategory());
        values.put(COLUMN_AMOUNT, budget.getAmount());
        values.put(COLUMN_START_DATE, budget.getStartDate());
        values.put(COLUMN_END_DATE, budget.getEndDate());
        long newRowId = db.insert(TABLE_BUDGETS, null, values);
        db.close();
        return newRowId;
    }

    public List<Budget> getAllBudgets() {
        List<Budget> budgets = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_BUDGETS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") Budget budget = new Budget(
                        cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY)),
                        cursor.getDouble(cursor.getColumnIndex(COLUMN_AMOUNT)),
                        cursor.getLong(cursor.getColumnIndex(COLUMN_START_DATE)),
                        cursor.getLong(cursor.getColumnIndex(COLUMN_END_DATE))
                );
                budgets.add(budget);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return budgets;
    }

    public boolean deleteBudget(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_BUDGETS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rowsAffected > 0; // Return true if at least one row was deleted, otherwise false
    }


    public boolean updateBudget(Budget budget) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CATEGORY, budget.getCategory());
        values.put(COLUMN_AMOUNT, budget.getAmount());
        values.put(COLUMN_START_DATE, budget.getStartDate());
        values.put(COLUMN_END_DATE, budget.getEndDate());
        int rowsAffected = db.update(TABLE_BUDGETS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(budget.getId())});
        db.close();
        return rowsAffected > 0; // Return true if at least one row was updated
    }

    public void updateRecurringTransactionNextDueDate(int id, long nextDueDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("next_due_date", nextDueDate);

        int rowsAffected = db.update(
                "recurring_transactions", // Table name
                values,                   // ContentValues object with updated values
                "id = ?",                  // WHERE clause
                new String[]{String.valueOf(id)} // WHERE clause arguments
        );

        db.close(); // Close database connection
        if (rowsAffected <= 0) {
            // Handle the case where no rows were updated
            Log.e("DatabaseHelper", "Failed to update recurring transaction next due date.");
        }
    }

    public void deleteCategory(String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_CATEGORIES, COLUMN_TITLE + " = ?", new String[]{category});
        db.close();
        if (rowsDeleted == 0) {
            Log.e("DatabaseHelper", "Failed to delete category: " + category);
        }
    }


    public boolean isCategoryExists(String categoryName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT 1 FROM " + TABLE_CATEGORIES + " WHERE " + COLUMN_TITLE + " = ?", new String[]{categoryName});
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }


    @SuppressLint("Range")
    public double getSpentAmountForBudget(int budgetId) {
        double totalSpent = 0.0;
        String selectQuery = "SELECT SUM(" + COLUMN_AMOUNT + ") AS total FROM " + TABLE_EXPENSES + " WHERE " + COLUMN_CATEGORY + " IN (SELECT " + COLUMN_CATEGORY + " FROM " + TABLE_BUDGETS + " WHERE " + COLUMN_ID + " = ?)";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(budgetId)});

        if (cursor.moveToFirst()) {
            totalSpent = cursor.getDouble(cursor.getColumnIndex("total"));
        }

        cursor.close();
        db.close();
        return totalSpent;
    }

    public List<Expense> getExpensesForMonth(int month, int year) {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_EXPENSES +
                " WHERE strftime('%m', datetime(" + COLUMN_DATE + "/1000, 'unixepoch')) = ? " +
                "AND strftime('%Y', datetime(" + COLUMN_DATE + "/1000, 'unixepoch')) = ?";
        String[] selectionArgs = {String.format("%02d", month), String.valueOf(year)};
        Cursor cursor = db.rawQuery(query, selectionArgs);

        if (cursor.moveToFirst()) {
            do {
                Expense expense = createExpenseFromCursor(cursor);
                if (expense != null) {
                    expenses.add(expense);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return expenses;
    }

    // DatabaseHelper.java
    public List<CategoryExpense> getCategoryExpensesForMonth(int month, int year) {
        List<CategoryExpense> categoryExpenses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_CATEGORY + ", SUM(" + COLUMN_AMOUNT + ") as total " +
                "FROM " + TABLE_EXPENSES +
                " WHERE strftime('%m', datetime(" + COLUMN_DATE + "/1000, 'unixepoch')) = ? " +
                "AND strftime('%Y', datetime(" + COLUMN_DATE + "/1000, 'unixepoch')) = ? " +
                "GROUP BY " + COLUMN_CATEGORY;
        String[] selectionArgs = {String.format("%02d", month), String.valueOf(year)};
        Cursor cursor = db.rawQuery(query, selectionArgs);

        if (cursor.moveToFirst()) {
            do {
                String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("total"));
                categoryExpenses.add(new CategoryExpense(category, amount, false));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return categoryExpenses;
    }


    public LiveData<List<Expense>> getExpensesForMonthLiveData(int month, int year) {
        MutableLiveData<List<Expense>> expensesLiveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<Expense> expenses = getExpensesForMonth(month, year); // Assuming you have this method
            expensesLiveData.postValue(expenses);
        });
        return expensesLiveData;
    }

    public LiveData<List<CategoryExpense>> getCategoryExpensesForMonthLiveData(int month, int year) {
        MutableLiveData<List<CategoryExpense>> categoryExpensesLiveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<CategoryExpense> categoryExpenses = getCategoryExpensesForMonth(month, year); // Assuming you have this method
            categoryExpensesLiveData.postValue(categoryExpenses);
        });
        return categoryExpensesLiveData;
    }

    public List<Expense> getExpensesForDateRange(LocalDate startDate, LocalDate endDate) {
        List<Expense> expenses = new ArrayList<>();

        // Convert LocalDate to String in yyyy-MM-dd format
        String startDateStr = startDate.toString();
        String endDateStr = endDate.plusDays(1).toString(); // Include the end date as a full day

        String selection = "strftime('%Y-%m-%d', " + COLUMN_DATE + "/1000, 'unixepoch') BETWEEN ? AND ?";
        String[] selectionArgs = {startDateStr, endDateStr};

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_EXPENSES, null, selection, selectionArgs, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Expense expense = createExpenseFromCursor(cursor);
                if (expense != null) {
                    expenses.add(expense);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return expenses;
    }


    @SuppressLint("Range")
    public List<CategoryExpense> getCategoryExpensesForDateRange(String category, LocalDate startDate, LocalDate endDate) {
        List<CategoryExpense> categoryExpenses = new ArrayList<>();

        // Convert LocalDate to milliseconds
        long startMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endMillis = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

        String selection = COLUMN_CATEGORY + " = ? AND " + COLUMN_DATE + " BETWEEN ? AND ?";
        String[] selectionArgs = {category, String.valueOf(startMillis), String.valueOf(endMillis)};

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_EXPENSES, new String[]{COLUMN_AMOUNT}, selection, selectionArgs, null, null, null);

        double totalAmount = 0;
        if (cursor.moveToFirst()) {
            do {
                totalAmount += cursor.getDouble(cursor.getColumnIndex(COLUMN_AMOUNT));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        categoryExpenses.add(new CategoryExpense(category, totalAmount, false)); // Adjust based on the logic

        return categoryExpenses;
    }

    public void resetExpensesForNewMonth() {
    }
//    public void insertDummyData(SQLiteDatabase db) {
//        ContentValues values = new ContentValues();
//
//        // Define the dummy data
//        Object[][] data = {
//                {"Coffee Shop", 15.00, "Food", 0, getDateInMillis("05/01/2024"), 1, 2024},
//                {"Rent", 1200.00, "Housing", 0, getDateInMillis("01/01/2024"), 1, 2024},
//                {"Movie Tickets", 30.00, "Entertainment", 0, getDateInMillis("15/01/2024"), 1, 2024},
//                {"Gym Membership", 50.00, "Fitness", 0, getDateInMillis("10/02/2024"), 2, 2024},
//                {"Groceries", 90.00, "Food", 0, getDateInMillis("20/02/2024"), 2, 2024},
//                {"Internet Bill", 60.00, "Utilities", 0, getDateInMillis("05/02/2024"), 2, 2024},
//                {"Dining Out", 45.00, "Food", 0, getDateInMillis("08/03/2024"), 3, 2024},
//                {"Electricity Bill", 75.00, "Utilities", 0, getDateInMillis("15/03/2024"), 3, 2024},
//                {"Car Maintenance", 200.00, "Transport", 0, getDateInMillis("22/03/2024"), 3, 2024}
//        };
//
//        for (Object[] entry : data) {
//            values.put(COLUMN_TITLE, (String) entry[0]);
//            values.put(COLUMN_AMOUNT, (Double) entry[1]);
//            values.put(COLUMN_CATEGORY, (String) entry[2]);
//            values.put(COLUMN_IS_INCOME, (Integer) entry[3]);
//            values.put(COLUMN_DATE, (Long) entry[4]); // Timestamp in milliseconds
//            values.put(COLUMN_MONTH, (Integer) entry[5]);
//            values.put(COLUMN_YEAR, (Integer) entry[6]);
//
//            // Insert the data into the database
//            db.insert(TABLE_EXPENSES, null, values);
//
//            // Clear the values for the next insert
//            values.clear();
//        }
//    }
//
//    private long getDateInMillis(String dateString) {
//        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
//        try {
//            Date date = sdf.parse(dateString);
//            return date != null ? date.getTime() : 0;
//        } catch (ParseException e) {
//            e.printStackTrace();
//            return 0;
//        }

    public void resetMonthlyExpenses() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("amount", 0);

        // Assuming you have a column `date` in the format yyyy-MM-dd or timestamp
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH) + 1; // January is 1
        int currentYear = calendar.get(Calendar.YEAR);

        // Define the start and end dates for the current month
        String startDate = String.format("%d-%02d-01", currentYear, currentMonth);
        String endDate = String.format("%d-%02d-31", currentYear, currentMonth);

        // Update records only for the current month
        String whereClause = "date BETWEEN ? AND ?";
        String[] whereArgs = { startDate, endDate };

        db.update("expenses", contentValues, whereClause, whereArgs);
        db.close();
    }


    public boolean isEndOfMonth(int month, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, month - 1); // Months are 0-based in Calendar
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));

        return calendar.get(Calendar.DAY_OF_MONTH) == Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    }

    public List<Expense> getExpensesForPreviousMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        int previousMonth = calendar.get(Calendar.MONTH) + 1;
        int previousYear = calendar.get(Calendar.YEAR);
        return getExpensesForMonthAndYear(previousMonth, previousYear);
    }
}



