package com.example.expensetracker;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GestureDetectorCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private LineChart lineChart;
    private ViewPager2 monthPager;
    private TextView monthYearTextView;
    private DatabaseHelper databaseHelper;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ImageView leftArrow, rightArrow;
    private int currentYear;
    private MonthPagerAdapter monthPagerAdapter;
    private GestureDetectorCompat gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chart);

        initializeViews();
        setupToolbar();
        setupNavigationDrawer();
        setupMonthPager();
        setupCharts();
        setupGestureDetector();
    }

    private void initializeViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        lineChart = findViewById(R.id.lineChart);
        monthPager = findViewById(R.id.monthPager);
        monthYearTextView = findViewById(R.id.monthYearTextView);
        leftArrow = findViewById(R.id.leftArrow);
        rightArrow = findViewById(R.id.rightArrow);
        databaseHelper = new DatabaseHelper(this);
        currentYear = Calendar.getInstance().get(Calendar.YEAR);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Financial Analytics");
        }
    }

    private void setupNavigationDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setupMonthPager() {
        monthPagerAdapter = new MonthPagerAdapter();
        monthPager.setAdapter(monthPagerAdapter);

        int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        monthPager.setCurrentItem(currentMonth, false);
        updateMonthYearTextView(currentMonth);

        monthPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateMonthYearTextView(position);
                updatePieChart(position + 1);  // +1 because Calendar.MONTH is 0-based
            }
        });

        leftArrow.setOnClickListener(view -> {
            int previousItem = monthPager.getCurrentItem() - 1;
            if (previousItem >= 0) {
                monthPager.setCurrentItem(previousItem, true);
            }
        });

        rightArrow.setOnClickListener(view -> {
            int nextItem = monthPager.getCurrentItem() + 1;
            if (nextItem < monthPagerAdapter.getItemCount()) {
                monthPager.setCurrentItem(nextItem, true);
            }
        });
    }

    private void setupGestureDetector() {
        gestureDetector = new GestureDetectorCompat(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(e2.getY() - e1.getY())) {
                    if (diffX > 0) {
                        // Swipe right
                        int previousItem = monthPager.getCurrentItem() - 1;
                        if (previousItem >= 0) {
                            monthPager.setCurrentItem(previousItem, true);
                        }
                    } else {
                        // Swipe left
                        int nextItem = monthPager.getCurrentItem() + 1;
                        if (nextItem < monthPagerAdapter.getItemCount()) {
                            monthPager.setCurrentItem(nextItem, true);
                        }
                    }
                    return true;
                }
                return false;
            }
        });

        monthYearTextView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
    }

    private void updateMonthYearTextView(int monthIndex) {
        String[] months = getResources().getStringArray(R.array.months_array);
        monthYearTextView.setText(months[monthIndex] + " " + currentYear);
    }

    private void setupCharts() {
        List<MonthlyFinancialData> monthlyData = fetchMonthlyData(currentYear);
        setupLineChart(monthlyData);

        int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
        updatePieChart(currentMonth);
    }

    private void updatePieChart(int month) {
        Map<String, Double> categoryExpenses = fetchCategoryExpenses(month, currentYear);
        PieChart pieChart = monthPagerAdapter.getPieChartForMonth(month - 1);
        if (pieChart != null) {
            setupPieChart(pieChart, categoryExpenses);
        } else {
            Log.e("AnalyticsActivity", "PieChart for month " + month + " is null");
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Intent intent = null;

        if (id == R.id.nav_home) {
            intent = new Intent(this, MainActivity.class);
        } else if (id == R.id.nav_expenses) {
            intent = new Intent(this, CategoryManagerActivity.class);
        } else if (id == R.id.nav_recurring_transactions) {
            intent = new Intent(this, RecurringTransactionsActivity.class);
        } else if (id == R.id.nav_budgets) {
            intent = new Intent(this, BudgetManagerActivity.class);
        } else if (id == R.id.nav_settings) {
            intent = new Intent(this, SettingsActivity.class);
        } else if (id == R.id.nav_chart) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }

        if (intent != null) {
            startActivity(intent);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private List<MonthlyFinancialData> fetchMonthlyData(int year) {
        List<MonthlyFinancialData> monthlyData = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            List<Expense> monthlyExpenses = databaseHelper.getExpensesForMonth(month, year);
            double totalIncome = 0;
            double totalExpense = 0;
            for (Expense expense : monthlyExpenses) {
                if (expense.isIncome()) {
                    totalIncome += expense.getAmount();
                } else {
                    totalExpense += expense.getAmount();
                }
            }
            monthlyData.add(new MonthlyFinancialData(getMonthName(month), totalIncome, totalExpense));
        }
        return monthlyData;
    }

    private Map<String, Double> fetchCategoryExpenses(int month, int year) {
        Map<String, Double> categoryExpenses = new HashMap<>();
        List<Expense> monthlyExpenses = databaseHelper.getExpensesForMonth(month, year);

        for (Expense expense : monthlyExpenses) {
            if (!expense.isIncome()) {
                String category = expense.getCategory();
                double amount = expense.getAmount();
                categoryExpenses.put(category, categoryExpenses.getOrDefault(category, 0.0) + amount);
            }
        }

        return categoryExpenses;
    }

    private void setupLineChart(List<MonthlyFinancialData> monthlyData) {
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        List<Entry> incomeEntries = new ArrayList<>();
        List<Entry> expenseEntries = new ArrayList<>();

        for (int i = 0; i < monthlyData.size(); i++) {
            MonthlyFinancialData data = monthlyData.get(i);
            incomeEntries.add(new Entry(i, (float) data.getIncome()));
            expenseEntries.add(new Entry(i, (float) data.getExpense()));
            labels.add(data.getMonth());
        }

        LineDataSet incomeDataSet = new LineDataSet(incomeEntries, "Income");
        styleDataSet(incomeDataSet, Color.GREEN);
        incomeDataSet.setDrawFilled(true);
        incomeDataSet.setFillColor(Color.GREEN);
        incomeDataSet.setFillAlpha(50);

        LineDataSet expenseDataSet = new LineDataSet(expenseEntries, "Expense");
        styleDataSet(expenseDataSet, Color.RED);
        expenseDataSet.setDrawFilled(true);
        expenseDataSet.setFillColor(Color.RED);
        expenseDataSet.setFillAlpha(50);

        dataSets.add(incomeDataSet);
        dataSets.add(expenseDataSet);

        LineData lineData = new LineData(dataSets);
        lineChart.setData(lineData);

        styleLineChart(labels);
        lineChart.animateXY(1500, 1500);
    }

    private void styleLineChart(ArrayList<String> labels) {
        lineChart.getDescription().setEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setDrawBorders(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);
        lineChart.setBackgroundColor(Color.WHITE);
        lineChart.setExtraOffsets(10f, 10f, 10f, 20f);

        Legend legend = lineChart.getLegend();
        legend.setEnabled(true);
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextSize(12f);
        legend.setTextColor(Color.BLACK);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelRotationAngle(45f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(12f);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawAxisLine(true);
        xAxis.setAxisLineColor(Color.BLACK);
        xAxis.setAxisLineWidth(1f);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.LTGRAY);
        leftAxis.setGridLineWidth(0.5f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextSize(12f);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setDrawAxisLine(true);
        leftAxis.setAxisLineColor(Color.BLACK);
        leftAxis.setAxisLineWidth(1f);

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);

        for (ILineDataSet set : lineChart.getData().getDataSets()) {
            LineDataSet lineDataSet = (LineDataSet) set;
            lineDataSet.setValueTextSize(10f);
            lineDataSet.setValueTextColor(Color.BLACK);
            lineDataSet.setDrawValues(true);
        }

        lineChart.invalidate();
    }

    private void styleDataSet(LineDataSet dataSet, int color) {
        dataSet.setColor(color);
        dataSet.setCircleColor(color);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(true);
        dataSet.setCircleHoleRadius(2f);
        dataSet.setCircleHoleColor(Color.WHITE);
    }

    private void setupPieChart(PieChart pieChart, Map<String, Double> categoryExpenses) {
        if (pieChart == null) {
            Log.e("AnalyticsActivity", "setupPieChart called with null PieChart");
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categoryExpenses.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Expense Categories");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueFormatter(new PercentFormatter(pieChart));

        PieData data = new PieData(dataSet);

        pieChart.setData(data);
        stylePieChart(pieChart);

        pieChart.animateY(1000, Easing.EaseInOutCubic);
        pieChart.invalidate();
    }

    private void stylePieChart(PieChart pieChart) {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setDrawCenterText(true);
        pieChart.setHapticFeedbackEnabled(true);
        pieChart.setBackgroundColor(Color.WHITE);
    }

    private String getMonthName(int month) {
        String[] months = getResources().getStringArray(R.array.months_array);
        return months[month - 1];
    }

    private class MonthPagerAdapter extends RecyclerView.Adapter<MonthPagerAdapter.MonthViewHolder> {
        private final PieChart[] pieCharts = new PieChart[12];

        @NonNull
        @Override
        public MonthViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            PieChart pieChart = new PieChart(parent.getContext());
            pieChart.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            return new MonthViewHolder(pieChart);
        }

        @Override
        public void onBindViewHolder(@NonNull MonthViewHolder holder, int position) {
            pieCharts[position] = holder.pieChart;
            updatePieChart(position + 1);
        }

        @Override
        public int getItemCount() {
            return 12;
        }

        PieChart getPieChartForMonth(int monthIndex) {
            return pieCharts[monthIndex];
        }

        class MonthViewHolder extends RecyclerView.ViewHolder {
            PieChart pieChart;

            MonthViewHolder(View itemView) {
                super(itemView);
                pieChart = (PieChart) itemView;
            }
        }
    }

    private static class MonthlyFinancialData {
        private final String month;
        private final double income;
        private final double expense;

        MonthlyFinancialData(String month, double income, double expense) {
            this.month = month;
            this.income = income;
            this.expense = expense;
        }

        String getMonth() { return month; }
        double getIncome() { return income; }
        double getExpense() { return expense; }
    }


}