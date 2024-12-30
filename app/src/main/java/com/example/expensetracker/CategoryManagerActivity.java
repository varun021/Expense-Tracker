package com.example.expensetracker;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class CategoryManagerActivity extends AppCompatActivity {
    private EditText newCategoryEditText;
    private Button addCategoryButton;
    private ListView categoriesListView;
    private DatabaseHelper databaseHelper;
    private ArrayAdapter<String> categoryAdapter;

    private androidx.drawerlayout.widget.DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_manager);

        newCategoryEditText = findViewById(R.id.newCategoryEditText);
        addCategoryButton = findViewById(R.id.addCategoryButton);
        categoriesListView = findViewById(R.id.categoriesListView);
        databaseHelper = new DatabaseHelper(this);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));

        // Initialize the navigation drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(android.R.color.white)); // Set hamburger icon color to white
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    startActivity(new Intent(CategoryManagerActivity.this, MainActivity.class));
                } else if (id == R.id.nav_expenses) {
                    Toast.makeText(CategoryManagerActivity.this, "Already in Category Manager", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.nav_recurring_transactions) {
                    startActivity(new Intent(CategoryManagerActivity.this, RecurringTransactionsActivity.class));
                } else if (id == R.id.nav_budgets) {
                    startActivity(new Intent(CategoryManagerActivity.this, BudgetManagerActivity.class));
                }else if (id == R.id.nav_chart) {
                        startActivity(new Intent(CategoryManagerActivity.this, AnalyticsActivity.class));
                } else if (id == R.id.nav_settings) {
                    startActivity(new Intent(CategoryManagerActivity.this, SettingsActivity.class));
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        loadCategories();

        addCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newCategory = newCategoryEditText.getText().toString().trim();
                if (validateCategoryInput(newCategory)) {
                    long result = databaseHelper.addCategory(newCategory);
                    if (result != -1) {
                        showSnackbar("Category added successfully");
                        newCategoryEditText.setText("");
                        loadCategories();
                    } else {
                        showSnackbar("Failed to add category");
                    }
                }
            }
        });

        categoriesListView.setOnItemLongClickListener((parent, view, position, id) -> {
            String categoryToDelete = categoryAdapter.getItem(position);
            if (categoryToDelete != null) {
                showDeleteCategoryDialog(categoryToDelete);
            }
            return true;
        });
    }

    private void loadCategories() {
        List<String> categories = databaseHelper.getAllCategories();
        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, categories);
        categoriesListView.setAdapter(categoryAdapter);
    }

    private boolean validateCategoryInput(String category) {
        if (TextUtils.isEmpty(category)) {
            showSnackbar("Please enter a category name");
            return false;
        } else if (databaseHelper.isCategoryExists(category)) {
            showSnackbar("Category already exists");
            return false;
        }
        return true;
    }

    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }

    private void showDeleteCategoryDialog(String category) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Category")
                .setMessage("Are you sure you want to delete this category?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    databaseHelper.deleteCategory(category);
                    showSnackbar("Category deleted");
                    loadCategories();
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
