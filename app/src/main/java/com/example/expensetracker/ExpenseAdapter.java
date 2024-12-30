package com.example.expensetracker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private List<Expense> expenses = new ArrayList<>();
    private CurrencyFormatter currencyFormatter;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(Expense expense);
    }

    public ExpenseAdapter(Context context, List<Expense> expenses) {
        if (expenses != null) {
            this.expenses.addAll(expenses);
        }
        this.currencyFormatter = new CurrencyFormatter();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.expense_item, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenses.get(position);
        holder.titleTextView.setText(expense.getTitle());

        String formattedAmount = currencyFormatter.formatCurrency(holder.itemView.getContext(), expense.getAmount());
        formattedAmount = expense.isIncome() ? "+ " + formattedAmount : "- " + formattedAmount;

        holder.amountTextView.setText(formattedAmount);
        holder.categoryTextView.setText(expense.getCategory());

        String formattedDate = formatDate(expense.getDate());
        holder.dateTextView.setText(formattedDate);

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(expense);
            }
        });
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    public void deleteExpense(int position) {
        expenses.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreExpense(Expense expense, int position) {
        expenses.add(position, expense);
        notifyItemInserted(position);
    }

    public static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, amountTextView, categoryTextView, dateTextView;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            amountTextView = itemView.findViewById(R.id.amountTextView);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateExpenses(List<Expense> newExpenses) {
        if (newExpenses != null) {
            expenses.clear();
            expenses.addAll(newExpenses);
            notifyDataSetChanged();
        }
    }

    private String formatDate(long timestamp) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
        java.util.Date date = new java.util.Date(timestamp);
        return sdf.format(date);
    }
}
