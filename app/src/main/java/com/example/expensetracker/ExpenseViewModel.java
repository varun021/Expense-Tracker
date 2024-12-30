package com.example.expensetracker;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ExpenseViewModel extends AndroidViewModel {
    private final DatabaseHelper databaseHelper;
    private final ExecutorService executorService;

    public ExpenseViewModel(@NonNull Application application) {
        super(application);
        this.databaseHelper = new DatabaseHelper(application);
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Expense>> getExpensesForMonth(int month, int year) {
        MutableLiveData<List<Expense>> expensesLiveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<Expense> expenses = databaseHelper.getExpensesForMonth(month, year);
            expensesLiveData.postValue(expenses);
        });
        return expensesLiveData;
    }

    public LiveData<List<CategoryExpense>> getCategoryExpensesForMonth(int month, int year) {
        MutableLiveData<List<CategoryExpense>> categoryExpensesLiveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<CategoryExpense> categoryExpenses = databaseHelper.getCategoryExpensesForMonth(month, year);
            categoryExpensesLiveData.postValue(categoryExpenses);
        });
        return categoryExpensesLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(1, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }

    public LiveData<List<CategoryExpense>> getCategoryExpensesForDateRange(String category, LocalDate startDate, LocalDate endDate) {
        MutableLiveData<List<CategoryExpense>> categoryExpensesLiveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<CategoryExpense> categoryExpenses = databaseHelper.getCategoryExpensesForDateRange(category, startDate, endDate);
            categoryExpensesLiveData.postValue(categoryExpenses);
        });
        return categoryExpensesLiveData;
    }

    public LiveData<List<Expense>> getExpensesForDateRange(LocalDate startDate, LocalDate endDate) {
        MutableLiveData<List<Expense>> expensesLiveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<Expense> expenses = databaseHelper.getExpensesForDateRange(startDate, endDate);
            expensesLiveData.postValue(expenses);
        });
        return expensesLiveData;
    }
}
