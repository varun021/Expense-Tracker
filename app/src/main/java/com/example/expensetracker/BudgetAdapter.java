package com.example.expensetracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

    private List<Budget> budgets;
    private final OnBudgetClickListener onBudgetClickListener;
    private final OnBudgetDeleteListener onBudgetDeleteListener;

    public interface OnBudgetClickListener {
        void onBudgetClick(Budget budget);
    }

    public interface OnBudgetDeleteListener {
        void onBudgetDelete(Budget budget, int position);
    }

    public BudgetAdapter(List<Budget> budgets, OnBudgetClickListener onBudgetClickListener, OnBudgetDeleteListener onBudgetDeleteListener) {
        this.budgets = budgets;
        this.onBudgetClickListener = onBudgetClickListener;
        this.onBudgetDeleteListener = onBudgetDeleteListener;
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_budget, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        Budget budget = budgets.get(position);
        holder.bind(budget);
    }

    @Override
    public int getItemCount() {
        return budgets.size();
    }

    public void updateBudgets(List<Budget> newBudgets) {
        this.budgets = newBudgets;
        notifyDataSetChanged();
    }

    class BudgetViewHolder extends RecyclerView.ViewHolder {
        TextView categoryTextView, amountTextView, dateRangeTextView, remainingTextView;
        ProgressBar budgetProgressBar;
        Button deleteButton;

        BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
            amountTextView = itemView.findViewById(R.id.amountTextView);
            dateRangeTextView = itemView.findViewById(R.id.dateRangeTextView);
            remainingTextView = itemView.findViewById(R.id.remainingTextView);
            budgetProgressBar = itemView.findViewById(R.id.budgetProgressBar);
            deleteButton = itemView.findViewById(R.id.deleteButton);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    onBudgetClickListener.onBudgetClick(budgets.get(position));
                }
            });

            deleteButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    onBudgetDeleteListener.onBudgetDelete(budgets.get(position), position);
                }
            });
        }

        void bind(Budget budget) {
            categoryTextView.setText(budget.getCategory());
            amountTextView.setText(CurrencyFormatter.formatCurrency(itemView.getContext(), budget.getAmount()));
            dateRangeTextView.setText(String.format("%s - %s",
                    DateFormatter.formatDate(budget.getStartDate()),
                    DateFormatter.formatDate(budget.getEndDate())));

            double spentAmount = budget.getSpentAmount(itemView.getContext());
            double totalAmount = budget.getAmount();
            int progress = (int) ((spentAmount / totalAmount) * 100);
            double remainingAmount = totalAmount - spentAmount;

            remainingTextView.setText(String.format("%s remaining",
                    CurrencyFormatter.formatCurrency(itemView.getContext(), remainingAmount)));
            budgetProgressBar.setProgress(progress);
        }
    }
}
