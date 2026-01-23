package com.example.expenseutility;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expenseutility.database.DatabaseHelper;
import com.example.expenseutility.dto.CategoryData;
import com.example.expenseutility.dto.ExpenseGroupingService;
import com.example.expenseutility.dto.ExpenseItem1;
import com.example.expenseutility.dto.MonthData;
import com.example.expenseutility.dto.YearData;
import com.example.expenseutility.entityadapter.CategoryAdapter1;
import com.example.expenseutility.entityadapter.ExpenseItem;
import com.example.expenseutility.entityadapter.MonthAdapter;
import com.example.expenseutility.entityadapter.YearAdapter;
import com.google.android.material.card.MaterialCardView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NestedViewActivity extends AppCompatActivity {

    private RecyclerView rvYears, rvMonths, rvCategories;
    private CardView yearColumn, monthColumn, categoryColumn, detailsColumn;
    private LinearLayout detailsContainer;
    private TextView tvCurrentSelection, tvDetailsTitle;
    private HorizontalScrollView horizontalScroll;

    private YearAdapter yearAdapter;
    private MonthAdapter monthAdapter;
    private CategoryAdapter1 categoryAdapter;
    private List<YearData> yearDataList;
    private NumberFormat currencyFormat;
    // All columns in order
    private CardView[] allColumns;
    // Screen dimensions
    private int screenWidth;
    private int columnSpacing;

    // Current selections
    private YearData selectedYear;
    private MonthData selectedMonth;
    private CategoryData selectedCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nested_view);

        initializeViews();
        getScreenDimensions();
        setupRecyclerViews();
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

        // Initialize column array
        allColumns = new CardView[]{yearColumn, monthColumn, categoryColumn, detailsColumn};

        // Load sample data
        loadSampleData();

        // Setup adapters
        setupAdapters();

        // Set initial column widths
        setInitialColumnWidths();

    }

    private void initializeViews() {
        rvYears = findViewById(R.id.rv_years);
        rvMonths = findViewById(R.id.rv_months);
        rvCategories = findViewById(R.id.rv_categories);

        yearColumn = findViewById(R.id.year_column);
        monthColumn = findViewById(R.id.month_column);
        categoryColumn = findViewById(R.id.category_column);
        detailsColumn = findViewById(R.id.details_column);

        detailsContainer = findViewById(R.id.details_container);
        tvCurrentSelection = findViewById(R.id.tv_current_selection);
        tvDetailsTitle = findViewById(R.id.tv_details_title);
        horizontalScroll = findViewById(R.id.horizontal_scroll);
    }

    private void getScreenDimensions() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;

        // Convert 4dp to pixels for column spacing
        columnSpacing = dpToPx(4);
    }

    private void setupRecyclerViews() {
        rvYears.setLayoutManager(new LinearLayoutManager(this));
        rvMonths.setLayoutManager(new LinearLayoutManager(this));
        rvCategories.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadSampleData() {
        yearDataList = new ArrayList<>();
        List<ExpenseItem> expenseItems = new ArrayList<>();
        try (DatabaseHelper db = new DatabaseHelper(this)) {
            expenseItems = db.getAllExpensesDataList();
        }

        // Get grouped data as List<YearData>
        yearDataList = ExpenseGroupingService.groupExpensesByYearMonthCategory(expenseItems);


    }

    private void setupAdapters() {
        // Year Adapter
        yearAdapter = new YearAdapter(yearDataList, new YearAdapter.OnYearClickListener() {
            @Override
            public void onYearClick(YearData yearData) {
                selectedYear = yearData;
                // Reset deeper selections
                selectedMonth = null;
                selectedCategory = null;

                updateCurrentSelection();
                showMonths(yearData.getMonths());
            }
        });
        rvYears.setAdapter(yearAdapter);
    }

    private void setInitialColumnWidths() {
        // Calculate base column width based on screen size
        int baseColumnWidth = Math.min(dpToPx(400), (int) (screenWidth * 0.9));
        setColumnWidth(yearColumn, baseColumnWidth);
    }

    private void showMonths(List<MonthData> monthList) {
        // Close any columns to the right
        if (categoryColumn.getVisibility() == View.VISIBLE) {
            collapseCategory();
        }
        if (detailsColumn.getVisibility() == View.VISIBLE) {
            collapseDetails();
        }

        // Update month adapter with new data
        monthAdapter = new MonthAdapter(monthList, new MonthAdapter.OnMonthClickListener() {
            @Override
            public void onMonthClick(MonthData monthData) {
                selectedMonth = monthData;
                selectedCategory = null; // Reset category selection

                updateCurrentSelection();
                showCategories(monthData.getCategories());
            }
        });
        rvMonths.setAdapter(monthAdapter);

        // Make month column visible and calculate widths
        if (monthColumn.getVisibility() != View.VISIBLE) {
            monthColumn.setVisibility(View.VISIBLE);
            setColumnWidth(monthColumn, 0); // Start with 0 width
        }

        // Calculate optimal widths for columns
        calculateAndSetColumnWidths(1); // 1 = month column index
    }

    private void showCategories(List<CategoryData> categoryList) {
        // Close details column if open
        if (detailsColumn.getVisibility() == View.VISIBLE) {
            collapseDetails();
        }

        // Update category adapter with new data
        categoryAdapter = new CategoryAdapter1(categoryList, new CategoryAdapter1.OnCategoryClickListener() {
            @Override
            public void onCategoryClick(CategoryData categoryData) {
                selectedCategory = categoryData;

                updateCurrentSelection();
                showExpenseDetails(categoryData);
            }
        });
        rvCategories.setAdapter(categoryAdapter);

        // Make category column visible and calculate widths
        if (categoryColumn.getVisibility() != View.VISIBLE) {
            categoryColumn.setVisibility(View.VISIBLE);
            setColumnWidth(categoryColumn, 0); // Start with 0 width
        }

        // Calculate optimal widths for columns
        calculateAndSetColumnWidths(2); // 2 = category column index
    }

    private void showExpenseDetails(CategoryData categoryData) {
        tvDetailsTitle.setText(categoryData.getCategoryName() + " Details");
        detailsContainer.removeAllViews();

        // Add category summary card
        addDetailCard("Category Summary",
                currencyFormat.format(categoryData.getTotalAmount()),
                categoryData.getExpenses().size() + " expense items",
                "#E3F2FD");

        // Add each expense as a card
        if (categoryData.getExpenses() != null && !categoryData.getExpenses().isEmpty()) {
            for (ExpenseItem1 expense : categoryData.getExpenses()) {
                addExpenseCard(expense);
            }
        } else {
            addEmptyStateCard("No expenses found for this category");
        }

        // Make details column visible and calculate widths
        if (detailsColumn.getVisibility() != View.VISIBLE) {
            detailsColumn.setVisibility(View.VISIBLE);
            setColumnWidth(detailsColumn, 0); // Start with 0 width
        }

        // Calculate optimal widths for columns
        calculateAndSetColumnWidths(3); // 3 = details column index
    }

    private void updateCurrentSelection() {
        StringBuilder selectionText = new StringBuilder();

        if (selectedYear != null) {
            selectionText.append("Year: ").append(selectedYear.getYear())
                    .append(" (Total: ").append(currencyFormat.format(selectedYear.getTotalAmount())).append(")");
        }

        if (selectedMonth != null) {
            selectionText.append(" → Month: ").append(selectedMonth.getMonthName())
                    .append(" (Total: ").append(currencyFormat.format(selectedMonth.getTotalAmount())).append(")");
        }

        if (selectedCategory != null) {
            selectionText.append(" → Category: ").append(selectedCategory.getCategoryName())
                    .append(" (Total: ").append(currencyFormat.format(selectedCategory.getTotalAmount())).append(")");
        }

        if (selectionText.length() == 0) {
            tvCurrentSelection.setText("Select a year to begin");
        } else {
            tvCurrentSelection.setText(selectionText.toString());
        }
    }

    private void calculateAndSetColumnWidths(int expandingColumnIndex) {
        // Count visible columns
        int visibleColumns = 0;
        for (CardView column : allColumns) {
            if (column.getVisibility() == View.VISIBLE) {
                visibleColumns++;
            }
        }

        // Calculate available width (screen width minus spacings)
        int totalSpacing = columnSpacing * (visibleColumns - 1);
        int availableWidth = screenWidth - totalSpacing;

        // Distribute widths based on column importance
        // Expanded column gets more space, previous columns get less
        for (int i = 0; i < allColumns.length; i++) {
            CardView column = allColumns[i];

            if (column.getVisibility() == View.VISIBLE) {
                int columnWidth;

                if (i == expandingColumnIndex) {
                    // Expanding column gets 50-60% of available width
                    columnWidth = (int) (availableWidth * 0.6);
                } else if (i < expandingColumnIndex) {
                    // Previous columns get progressively smaller
                    float factor = 0.4f / (expandingColumnIndex);
                    columnWidth = (int) (availableWidth * factor);
                } else {
                    // Columns to the right (shouldn't happen in normal flow)
                    columnWidth = (int) (availableWidth * 0.3);
                }

                // Set minimum and maximum bounds
                int minWidth = dpToPx(150);
                int maxWidth = dpToPx(500);
                columnWidth = Math.max(minWidth, Math.min(columnWidth, maxWidth));

                // Animate to new width
                animateColumnWidth(column, columnWidth);
            }
        }

        // Scroll to show the expanded column
        scrollToColumn(allColumns[expandingColumnIndex]);
    }

    private void animateColumnWidth(final CardView column, final int targetWidth) {
        final ViewGroup.LayoutParams params = column.getLayoutParams();
        final int startWidth = params.width;

        android.animation.ValueAnimator animator = android.animation.ValueAnimator.ofInt(startWidth, targetWidth);
        animator.setDuration(300);
        animator.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
        animator.addUpdateListener(new android.animation.ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(android.animation.ValueAnimator animation) {
                int width = (int) animation.getAnimatedValue();
                params.width = width;
                column.setLayoutParams(params);
                column.requestLayout();
            }
        });
        animator.start();
    }

    private void setColumnWidth(CardView column, int width) {
        ViewGroup.LayoutParams params = column.getLayoutParams();
        params.width = width;
        column.setLayoutParams(params);
    }

    private void scrollToColumn(final CardView column) {
        horizontalScroll.postDelayed(new Runnable() {
            @Override
            public void run() {
                int scrollTo = column.getLeft();
                horizontalScroll.smoothScrollTo(scrollTo, 0);
            }
        }, 100);
    }

    private void addDetailCard(String title, String amount, String info, String color) {
        MaterialCardView card = new MaterialCardView(this);
        card.setCardBackgroundColor(android.graphics.Color.parseColor(color));
        card.setRadius(16);
        card.setCardElevation(4);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 24, 24, 24);

        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextSize(18);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        titleView.setTextColor(android.graphics.Color.parseColor("#333333"));
        layout.addView(titleView);

        TextView amountView = new TextView(this);
        amountView.setText(amount);
        amountView.setTextSize(22);
        amountView.setTypeface(null, android.graphics.Typeface.BOLD);
        amountView.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
        amountView.setPadding(0, 8, 0, 0);
        layout.addView(amountView);

        if (info != null && !info.isEmpty()) {
            TextView infoView = new TextView(this);
            infoView.setText(info);
            infoView.setTextSize(14);
            infoView.setTextColor(android.graphics.Color.parseColor("#666666"));
            infoView.setPadding(0, 4, 0, 0);
            layout.addView(infoView);
        }

        card.addView(layout);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 16);
        card.setLayoutParams(params);

        detailsContainer.addView(card);
    }

    private void addExpenseCard(ExpenseItem1 expense) {
        MaterialCardView card = new MaterialCardView(this);
        card.setCardBackgroundColor(android.graphics.Color.WHITE);
        card.setRadius(12);
        card.setCardElevation(2);
        card.setStrokeWidth(1);
        card.setStrokeColor(android.graphics.Color.parseColor("#E0E0E0"));

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);

        // Top row: Title and Amount
        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);

        TextView titleView = new TextView(this);
        titleView.setText(expense.getTitle());
        titleView.setTextSize(16);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        titleView.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));

        TextView amountView = new TextView(this);
        amountView.setText(currencyFormat.format(expense.getAmount()));
        amountView.setTextSize(16);
        amountView.setTypeface(null, android.graphics.Typeface.BOLD);
        amountView.setTextColor(android.graphics.Color.parseColor("#4CAF50"));

        topRow.addView(titleView);
        topRow.addView(amountView);
        layout.addView(topRow);

        // Date
        TextView dateView = new TextView(this);
        dateView.setText("📅 " + expense.getDate());
        dateView.setTextSize(14);
        dateView.setTextColor(android.graphics.Color.parseColor("#666666"));
        dateView.setPadding(0, 8, 0, 0);
        layout.addView(dateView);

        // Description
        if (expense.getDescription() != null && !expense.getDescription().isEmpty()) {
            TextView descView = new TextView(this);
            descView.setText("📝 " + expense.getDescription());
            descView.setTextSize(14);
            descView.setTextColor(android.graphics.Color.parseColor("#666666"));
            descView.setPadding(0, 4, 0, 0);
            layout.addView(descView);
        }

        card.addView(layout);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 12);
        card.setLayoutParams(params);

        detailsContainer.addView(card);
    }

    private void addEmptyStateCard(String message) {
        MaterialCardView card = new MaterialCardView(this);
        card.setCardBackgroundColor(android.graphics.Color.parseColor("#FAFAFA"));
        card.setRadius(12);

        TextView textView = new TextView(this);
        textView.setText(message);
        textView.setTextSize(16);
        textView.setTextColor(android.graphics.Color.parseColor("#9E9E9E"));
        textView.setGravity(android.view.Gravity.CENTER);
        textView.setPadding(40, 40, 40, 40);

        card.addView(textView);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 20, 0, 0);
        card.setLayoutParams(params);

        detailsContainer.addView(card);
    }

    private void collapseDetails() {
        // Hide details column
        detailsColumn.setVisibility(View.GONE);
        selectedCategory = null; // Clear category selection

        // Recalculate widths for remaining columns
        calculateAndSetColumnWidths(2); // Category column becomes the expanded one

        // Update selection text
        updateCurrentSelection();
    }

    private void collapseCategory() {
        // Hide category column
        categoryColumn.setVisibility(View.GONE);
        selectedCategory = null; // Clear category selection

        // Recalculate widths for remaining columns
        calculateAndSetColumnWidths(1); // Month column becomes the expanded one

        // Update selection text
        updateCurrentSelection();
    }

    private void collapseMonth() {
        // Hide month column
        monthColumn.setVisibility(View.GONE);
        selectedMonth = null; // Clear month selection
        selectedCategory = null; // Clear category selection

        // Set year column to full width
        int newWidth = Math.min(dpToPx(400), (int) (screenWidth * 0.9));
        animateColumnWidth(yearColumn, newWidth);

        // Update selection text
        updateCurrentSelection();

        // Scroll to year column
        scrollToColumn(yearColumn);
    }

    @Override
    public void onBackPressed() {
        if (detailsColumn.getVisibility() == View.VISIBLE) {
            collapseDetails();
        } else if (categoryColumn.getVisibility() == View.VISIBLE) {
            collapseCategory();
        } else if (monthColumn.getVisibility() == View.VISIBLE) {
            collapseMonth();
        } else {
            super.onBackPressed();
        }
    }

    private int dpToPx(int dp) {
        Resources resources = getResources();
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                resources.getDisplayMetrics()
        );
    }
}