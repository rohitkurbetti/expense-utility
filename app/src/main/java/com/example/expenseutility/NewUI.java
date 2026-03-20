package com.example.expenseutility;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expenseutility.entityadapter.ExpenseItem;
import com.example.expenseutility.entityadapter.RecentExpensesAdapter;
import com.example.expenseutility.utility.ThemeHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NewUI extends AppCompatActivity {

    // UI Components
    private TextView tvCurrentBalance, tvMonthlyIncome, tvMonthlyExpenses;
    private TextView tvMonthYear, tvBudgetPercentage, tvRemainingBudget, tvSpentBudget;
    private LinearProgressIndicator progressBudget;
    private MaterialButton btnPreviousMonth, btnNextMonth, btnViewDetails;
    private MaterialButton btnAddExpenseQuick, btnViewReport;
    private MaterialCardView cardExpenseForm;
    private ChipGroup chipGroupCategory;
    private Chip chipFood, chipTransport, chipShopping, chipBills, chipOther;
    private TextInputEditText etExpenseTitle, etAmount, etDate, etDescription;
    private TextInputLayout tilExpenseTitle, tilAmount, tilDate, tilDescription;
    private MaterialButton btnAddExpense;
    private RecyclerView rvRecentExpenses;
    private LinearLayout layoutEmptyState;
    private TextView tvViewAll;
    private ImageView btnNotifications, btnBack;
    private FloatingActionButton fabAssistant;
    // Adapters and Data
    private RecentExpensesAdapter recentExpensesAdapter;
    private List<ExpenseItem> expenseItems = new ArrayList<>();
    // Calendar
    private Calendar currentMonth = Calendar.getInstance();
    private Calendar selectedDate = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_ui);

        initializeViews();
        setupChipIcons(); // Add this line
        setupListeners();
        setupRecyclerView();
        updateUI();

    }

    private void setupChipIcons() {
        // Set chip icon tint to white for all chips
        chipFood.setChipIconTintResource(android.R.color.white);
        chipTransport.setChipIconTintResource(android.R.color.white);
        chipShopping.setChipIconTintResource(android.R.color.white);
        chipBills.setChipIconTintResource(android.R.color.white);
        chipOther.setChipIconTintResource(android.R.color.white);

        // Ensure icons are visible
        chipFood.setChipIconVisible(true);
        chipTransport.setChipIconVisible(true);
        chipShopping.setChipIconVisible(true);
        chipBills.setChipIconVisible(true);
        chipOther.setChipIconVisible(true);
    }

    private void initializeViews() {
        // Header
        btnBack = findViewById(R.id.btnBack);
        btnNotifications = findViewById(R.id.btnNotifications);

        // Balance Overview
        tvCurrentBalance = findViewById(R.id.tvCurrentBalance);
        tvMonthlyIncome = findViewById(R.id.tvMonthlyIncome);
        tvMonthlyExpenses = findViewById(R.id.tvMonthlyExpenses);

        // Monthly Budget
        tvMonthYear = findViewById(R.id.tvMonthYear);
        tvBudgetPercentage = findViewById(R.id.tvBudgetPercentage);
        tvRemainingBudget = findViewById(R.id.tvRemainingBudget);
        tvSpentBudget = findViewById(R.id.tvSpentBudget);
        progressBudget = findViewById(R.id.progressBudget);
        btnPreviousMonth = findViewById(R.id.btnPreviousMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        btnViewDetails = findViewById(R.id.btnViewDetails);

        // Quick Actions
        btnAddExpenseQuick = findViewById(R.id.btnAddExpenseQuick);
        btnViewReport = findViewById(R.id.btnViewReport);

        // Expense Form
        cardExpenseForm = findViewById(R.id.cardExpenseForm);
        chipGroupCategory = findViewById(R.id.chipGroupCategory);
        chipFood = findViewById(R.id.chipFood);
        chipTransport = findViewById(R.id.chipTransport);
        chipShopping = findViewById(R.id.chipShopping);
        chipBills = findViewById(R.id.chipBills);
        chipOther = findViewById(R.id.chipOther);

        tilExpenseTitle = findViewById(R.id.tilExpenseTitle);
        tilAmount = findViewById(R.id.tilAmount);
        tilDate = findViewById(R.id.tilDate);
        tilDescription = findViewById(R.id.tilDescription);

        etExpenseTitle = findViewById(R.id.etExpenseTitle);
        etAmount = findViewById(R.id.etAmount);
        etDate = findViewById(R.id.etDate);
        etDescription = findViewById(R.id.etDescription);

        btnAddExpense = findViewById(R.id.btnAddExpense);

        // Recent Expenses
        rvRecentExpenses = findViewById(R.id.rvRecentExpenses);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        tvViewAll = findViewById(R.id.tvViewAll);

        // FAB
        fabAssistant = findViewById(R.id.fabAssistant);
    }

    private void setupListeners() {
        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Month Navigation
        btnPreviousMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, -1);
            updateMonthDisplay();
            updateBudgetData();
        });

        btnNextMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, 1);
            updateMonthDisplay();
            updateBudgetData();
        });

        btnViewDetails.setOnClickListener(v -> {
            Toast.makeText(this, "Showing budget details", Toast.LENGTH_SHORT).show();
        });

        // Quick Actions
        btnAddExpenseQuick.setOnClickListener(v -> {
            cardExpenseForm.setVisibility(
                    cardExpenseForm.getVisibility() == View.VISIBLE ?
                            View.GONE : View.VISIBLE
            );
        });

        btnViewReport.setOnClickListener(v -> {
            Toast.makeText(this, "Showing expense report", Toast.LENGTH_SHORT).show();
        });

        // Date Picker
        etDate.setOnClickListener(v -> showDatePicker());

        // Add Expense
        btnAddExpense.setOnClickListener(v -> addExpense());

        // View All Expenses
        tvViewAll.setOnClickListener(v -> {
            Toast.makeText(this, "Showing all expenses", Toast.LENGTH_SHORT).show();
        });

        // Notifications
        btnNotifications.setOnClickListener(v -> {
            Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show();
        });

        // AI Assistant FAB
        fabAssistant.setOnClickListener(v -> {
            Toast.makeText(this, "AI Assistant activated", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupRecyclerView() {
        recentExpensesAdapter = new RecentExpensesAdapter(expenseItems);
        rvRecentExpenses.setLayoutManager(new LinearLayoutManager(this));
        rvRecentExpenses.setAdapter(recentExpensesAdapter);

        // Add sample data for demonstration
        addSampleExpenses();
    }

    private void updateUI() {
        updateMonthDisplay();
        updateBudgetData();
        updateEmptyState();
        setCurrentDate();
    }

    private void updateMonthDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM yyyy", Locale.getDefault());
        tvMonthYear.setText(sdf.format(currentMonth.getTime()));
    }

    private void updateBudgetData() {
        // TODO: Replace with actual data from database
        double monthlyIncome = 5000.00;
        double totalExpenses = calculateTotalExpenses();
        double remainingBudget = monthlyIncome - totalExpenses;
        double budgetPercentage = (totalExpenses / monthlyIncome) * 100;

        tvMonthlyIncome.setText(String.format("$%.2f", monthlyIncome));
        tvMonthlyExpenses.setText(String.format("$%.2f", totalExpenses));
        tvCurrentBalance.setText(String.format("$%.2f", remainingBudget));

        tvBudgetPercentage.setText(String.format("%.0f%%", budgetPercentage));
        tvRemainingBudget.setText(String.format("$%.0f left", remainingBudget));
        tvSpentBudget.setText(String.format("$%.0f spent", totalExpenses));

        progressBudget.setProgress((int) budgetPercentage);

        // Change progress color based on percentage
        if (budgetPercentage > 80) {
            progressBudget.setIndicatorColor(getResources().getColor(R.color.error));
        } else if (budgetPercentage > 50) {
            progressBudget.setIndicatorColor(getResources().getColor(R.color.warning));
        } else {
            progressBudget.setIndicatorColor(getResources().getColor(R.color.success));
        }
    }

    private double calculateTotalExpenses() {
        double total = 0;
        for (ExpenseItem item : expenseItems) {
            if (item.getExpenseAmount() != null) {
                total += item.getExpenseAmount();
            }
        }
        return total;
    }

    private void updateEmptyState() {
        if (expenseItems.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            rvRecentExpenses.setVisibility(View.GONE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            rvRecentExpenses.setVisibility(View.VISIBLE);
        }
    }

    private void setCurrentDate() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        SimpleDateFormat sdfDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentDate = sdfDate.format(new Date());
        String currentDateTime = sdfDateTime.format(new Date());
        etDate.setText(currentDate);
    }

    private void showDatePicker() {
        int year = selectedDate.get(Calendar.YEAR);
        int month = selectedDate.get(Calendar.MONTH);
        int day = selectedDate.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                    etDate.setText(sdf.format(selectedDate.getTime()));
                },
                year, month, day
        );

        datePickerDialog.show();
    }

    private void addExpense() {
        String title = etExpenseTitle.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        // Validation
        if (title.isEmpty()) {
            tilExpenseTitle.setError("Please enter expense title");
            return;
        } else {
            tilExpenseTitle.setError(null);
        }

        if (amountStr.isEmpty()) {
            tilAmount.setError("Please enter amount");
            return;
        } else {
            tilAmount.setError(null);
        }

        // Check category selection
        int selectedChipId = chipGroupCategory.getCheckedChipId();
        if (selectedChipId == -1) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Long amount = Long.valueOf(amountStr);
            String category = getCategoryFromChipId(selectedChipId);

            // Create date strings
            SimpleDateFormat sdfDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            SimpleDateFormat sdfDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date currentDate = new Date();
            String expenseDate = sdfDate.format(currentDate);
            String expenseDateTime = sdfDateTime.format(currentDate);

            // Create new ExpenseItem
            ExpenseItem expenseItem = new ExpenseItem(
                    category,
                    title,
                    amount.toString(),
                    expenseDateTime,
                    expenseDate,
                    null, // fileName
                    null, // fileBytes
                    description,
                    false // isHomeExpense
            );

            expenseItems.add(0, expenseItem);
            recentExpensesAdapter.notifyItemInserted(0);

            // Update UI
            updateBudgetData();
            updateEmptyState();

            // Clear form and hide it
            clearForm();
            cardExpenseForm.setVisibility(View.GONE);

            Toast.makeText(this, "Expense added successfully", Toast.LENGTH_SHORT).show();

        } catch (NumberFormatException e) {
            tilAmount.setError("Invalid amount format");
        }
    }

    private String getCategoryFromChipId(int chipId) {
        if (chipId == R.id.chipFood) return "Food";
        if (chipId == R.id.chipTransport) return "Transport";
        if (chipId == R.id.chipShopping) return "Shopping";
        if (chipId == R.id.chipBills) return "Bills";
        if (chipId == R.id.chipOther) return "Other";
        return "Other";
    }

    private void clearForm() {
        etExpenseTitle.setText("");
        etAmount.setText("");
        etDescription.setText("");
        setCurrentDate();
        chipGroupCategory.clearCheck();
    }

    private void addSampleExpenses() {
        // Add some sample expenses for demonstration
        SimpleDateFormat sdfDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        SimpleDateFormat sdfDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        // Sample date: 3 days ago
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -3);
        String date1 = sdfDate.format(cal.getTime());
        String dateTime1 = sdfDateTime.format(cal.getTime());

        expenseItems.add(new ExpenseItem(
                "Food",
                "Lunch at Restaurant",
                "2550",
                dateTime1,
                date1,
                null,
                null,
                "Restaurant with colleagues",
                false
        ));

        // Sample date: 2 days ago
        cal.add(Calendar.DAY_OF_YEAR, 1);
        String date2 = sdfDate.format(cal.getTime());
        String dateTime2 = sdfDateTime.format(cal.getTime());

        expenseItems.add(new ExpenseItem(
                "Transport",
                "Uber Ride",
                "1875",
                dateTime2,
                date2,
                null,
                null,
                "Office to home",
                false
        ));

        // Sample date: 1 day ago
        cal.add(Calendar.DAY_OF_YEAR, 1);
        String date3 = sdfDate.format(cal.getTime());
        String dateTime3 = sdfDateTime.format(cal.getTime());

        expenseItems.add(new ExpenseItem(
                "Shopping",
                "Groceries",
                "6530",
                dateTime3,
                date3,
                null,
                null,
                "Weekly groceries",
                false
        ));

        // Sample date: today
        String date4 = sdfDate.format(new Date());
        String dateTime4 = sdfDateTime.format(new Date());

        expenseItems.add(new ExpenseItem(
                "Bills",
                "Netflix Subscription",
                "1599",
                dateTime4,
                date4,
                null,
                null,
                "Monthly subscription",
                false
        ));

        recentExpensesAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

}