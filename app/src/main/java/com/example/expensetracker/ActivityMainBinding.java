package com.example.expensetracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

/**
 * Custom binding class to bind views for the MainActivity.
 */
public class ActivityMainBinding {
    public final View root;
    public final Toolbar toolbar;
    public final RecyclerView expenseRecyclerView;
    public final TextView totalExpenseTextView;
    public final TextView totalIncomeTextView;
    public final TextView balanceTextView;
    public final FloatingActionButton fab;
    public final DrawerLayout drawerLayout;
    public final NavigationView navView;

    private ActivityMainBinding(View root, Toolbar toolbar, RecyclerView expenseRecyclerView,
                                TextView totalExpenseTextView, TextView totalIncomeTextView,
                                TextView balanceTextView, FloatingActionButton fab,
                                DrawerLayout drawerLayout, NavigationView navView) {
        this.root = root;
        this.toolbar = toolbar;
        this.expenseRecyclerView = expenseRecyclerView;
        this.totalExpenseTextView = totalExpenseTextView;
        this.totalIncomeTextView = totalIncomeTextView;
        this.balanceTextView = balanceTextView;
        this.fab = fab;
        this.drawerLayout = drawerLayout;
        this.navView = navView;
    }

    /**
     * Inflates the layout and binds the views.
     *
     * @param inflater LayoutInflater to inflate the layout.
     * @param parent ViewGroup to attach the inflated layout to.
     * @param attachToParent Whether to attach the inflated layout to the parent.
     * @return An instance of ActivityMainBinding with the bound views.
     */
    public static ActivityMainBinding inflate(LayoutInflater inflater, ViewGroup parent, boolean attachToParent) {
        View root = inflater.inflate(R.layout.activity_main, parent, attachToParent);
        return bind(root);
    }

    /**
     * Binds the views from the root view.
     *
     * @param root Root view containing the views to be bound.
     * @return An instance of ActivityMainBinding with the bound views.
     */
    public static ActivityMainBinding bind(View root) {
        Toolbar toolbar = root.findViewById(R.id.toolbar);
        RecyclerView expenseRecyclerView = root.findViewById(R.id.expenseRecyclerView);
        TextView totalExpenseTextView = root.findViewById(R.id.totalExpenseTextView);
        TextView totalIncomeTextView = root.findViewById(R.id.totalIncomeTextView);
        TextView balanceTextView = root.findViewById(R.id.balanceTextView);
        FloatingActionButton fab = root.findViewById(R.id.fab);
        DrawerLayout drawerLayout = root.findViewById(R.id.drawer_layout);
        NavigationView navView = root.findViewById(R.id.nav_view);

        return new ActivityMainBinding(root, toolbar, expenseRecyclerView, totalExpenseTextView,
                totalIncomeTextView, balanceTextView, fab, drawerLayout, navView);
    }
}