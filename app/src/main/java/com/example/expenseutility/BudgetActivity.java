package com.example.expenseutility;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.expenseutility.database.BudgetHelper;
import com.example.expenseutility.dto.Budget;
import com.example.expenseutility.entityadapter.BudgetAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BudgetActivity extends AppCompatActivity {

    // UI Components
    private ListView listViewBudgets;
    private FloatingActionButton fabAddBudget;
    private View emptyView;
    private Toolbar toolbar; // Add Toolbar reference

    // Adapter and Data
    private BudgetAdapter budgetAdapter;
    private List<Budget> budgetList;
    private BudgetHelper budgetHelper;

    // Month mapping
    private Map<String, Integer> monthMap;
    private String[] months = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };

    public static int getDaysRemainingInMonth() {
        Calendar calendar = Calendar.getInstance();

        // Get current day of month
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        // Get total days in current month
        int totalDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Calculate remaining days
        int remainingDays = totalDaysInMonth - currentDay;

        return remainingDays;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyUserTheme();  // Apply before setContentView
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        // Initialize Database Helper
        budgetHelper = new BudgetHelper(this);

        // Initialize month mapping
        initializeMonthMap();

        // Initialize UI Components
        initializeViews();

        // Setup toolbar
        setupToolbar(); // Add this method call

        // Setup listeners
        setupListeners();

        // Load budgets
        loadBudgets();

        // Print logs
//        printLogs();

    }

//    private void printLogs() {
//
//        DatabaseHelper databaseHelper = new DatabaseHelper(this);
//
//        int monthExpenses = (int) databaseHelper.getTotalExpenseForCurrentMonth();
//
//        int year = Calendar.getInstance().get(Calendar.YEAR);
//        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
//        int budget = budgetHelper.getBudget(year, month);
//
//        int remainingBalance = (int) (budget - monthExpenses);
//
//        Log.i("budget", "" + budget);
//        Log.i("monthExpenses", "" + monthExpenses);
//        Log.i("remainingBalance", "" + remainingBalance);
//
//        int remDays = getDaysRemainingInMonth();
//        Log.i("remainingDays", "" + remDays);
//
//        int dailyLimit = remainingBalance / remDays;
//
//        Log.i("final", "\u20B9" + dailyLimit);
//        FirstFragment.updateLimitText(String.valueOf(dailyLimit));
//    }

    private void initializeMonthMap() {
        monthMap = new HashMap<>();
        for (int i = 0; i < months.length; i++) {
            monthMap.put(months[i], i + 1);
        }
    }

    private void initializeViews() {
        listViewBudgets = findViewById(R.id.listViewBudgets);
        fabAddBudget = findViewById(R.id.fabAddBudget);
        emptyView = findViewById(R.id.emptyView);
        toolbar = findViewById(R.id.toolbar); // Initialize toolbar

        budgetList = new ArrayList<>();
        budgetAdapter = new BudgetAdapter(this, budgetList);
        listViewBudgets.setAdapter(budgetAdapter);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Set back button click listener
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void setupListeners() {
        fabAddBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddBudgetDialog();
            }
        });

        // Long click listener for deleting budgets
        listViewBudgets.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Budget budget = budgetList.get(position);
                showDeleteDialog(budget);
                return true;
            }
        });

        // Optional: Add click listener for editing budgets
        listViewBudgets.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Budget budget = budgetList.get(position);
                showEditBudgetDialog(budget);
            }
        });
    }

    private void loadBudgets() {
        budgetList.clear();
        budgetList.addAll(budgetHelper.getAllBudgets());
        budgetAdapter.notifyDataSetChanged();

        // Show/hide empty state
        if (budgetList.isEmpty()) {
            listViewBudgets.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            listViewBudgets.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    private void showAddBudgetDialog() {
        showBudgetDialog(null);
    }

    private void showEditBudgetDialog(Budget budget) {
        showBudgetDialog(budget);
    }

    private void showBudgetDialog(final Budget existingBudget) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_budget, null);

        // Initialize dialog views
        AutoCompleteTextView autoCompleteYear = dialogView.findViewById(R.id.autoCompleteYear);
        AutoCompleteTextView autoCompleteMonth = dialogView.findViewById(R.id.autoCompleteMonth);
        TextInputEditText editTextBudget = dialogView.findViewById(R.id.editTextBudget);
        TextInputLayout budgetInputLayout = dialogView.findViewById(R.id.budgetInputLayout);

        // Setup dropdowns
        setupYearDropdown(autoCompleteYear);
        setupMonthDropdown(autoCompleteMonth);

        // Setup budget formatting (uncomment if needed)
        // setupBudgetFormatting(editTextBudget);

        // If editing existing budget, pre-fill the fields
        if (existingBudget != null) {
            autoCompleteYear.setText(String.valueOf(existingBudget.getYear()));
            autoCompleteMonth.setText(existingBudget.getMonthName());
            editTextBudget.setText(String.valueOf(existingBudget.getBudget()));
            builder.setTitle("Edit Budget");
        } else {
            builder.setTitle("Add Budget");
        }

        builder.setView(dialogView)
                .setPositiveButton(existingBudget != null ? "Update" : "Save", null)
                .setNegativeButton("Cancel", null);

        if (existingBudget != null) {
            builder.setNeutralButton("Delete", null);
        }

        AlertDialog dialog = builder.create();
        dialog.show();

        // Override positive button click to prevent dialog dismissal
        Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedYear = autoCompleteYear.getText().toString().trim();
                String selectedMonth = autoCompleteMonth.getText().toString().trim();
                String budgetString = editTextBudget.getText().toString().trim();

                if (saveBudget(selectedYear, selectedMonth, budgetString, budgetInputLayout, existingBudget)) {
                    dialog.dismiss();
                }
            }
        });

        // Override delete button if editing
        if (existingBudget != null) {
            Button deleteButton = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    showDeleteDialog(existingBudget);
                }
            });
            deleteButton.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    private void setupYearDropdown(AutoCompleteTextView autoCompleteYear) {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        String[] years = new String[11];
        for (int i = 0; i <= 10; i++) {
            years[i] = String.valueOf(currentYear + i);
        }

        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                years
        );
        autoCompleteYear.setAdapter(yearAdapter);
        autoCompleteYear.setText(years[0], false);
    }

    private void setupMonthDropdown(AutoCompleteTextView autoCompleteMonth) {
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                months
        );
        autoCompleteMonth.setAdapter(monthAdapter);

        int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        autoCompleteMonth.setText(months[currentMonth], false);
    }

    private void setupBudgetFormatting(TextInputEditText editTextBudget) {
        editTextBudget.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                editTextBudget.removeTextChangedListener(this);

                String originalString = s.toString().replaceAll("[₹,]", "");

                if (!originalString.isEmpty()) {
                    try {
                        double value = Double.parseDouble(originalString);
                        String formatted = formatIndianCurrency(value);
                        editTextBudget.setText(formatted);
                        editTextBudget.setSelection(formatted.length());
                    } catch (NumberFormatException e) {
                        // Ignore
                    }
                }

                editTextBudget.addTextChangedListener(this);
            }
        });
    }

    private boolean saveBudget(String yearStr, String monthStr, String budgetStr,
                               TextInputLayout budgetInputLayout, Budget existingBudget) {
        // Validation
        if (yearStr.isEmpty()) {
            Toast.makeText(this, "Please select a year", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (monthStr.isEmpty()) {
            Toast.makeText(this, "Please select a month", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (budgetStr.isEmpty()) {
            budgetInputLayout.setError("Please enter budget amount");
            return false;
        }

        try {
            // Parse inputs
            int year = Integer.parseInt(yearStr);
            Integer monthNumber = monthMap.get(monthStr);

            if (monthNumber == null) {
                Toast.makeText(this, "Invalid month selected", Toast.LENGTH_SHORT).show();
                return false;
            }

            // Clean and parse budget amount
            String cleanBudgetStr = budgetStr.replaceAll("[₹,]", "");
            double budgetAmount = Double.parseDouble(cleanBudgetStr);

            if (budgetAmount <= 0) {
                budgetInputLayout.setError("Budget must be greater than 0");
                return false;
            }

            // Convert to integer
            int budgetInRupees = (int) budgetAmount;

            // Save to database
            long result = budgetHelper.addOrUpdateBudget(year, monthNumber, budgetInRupees);

            if (result > 0) {
                // Clear error and show success
                budgetInputLayout.setError(null);

                // Check if this was an update or new entry
                boolean existed = existingBudget != null || budgetHelper.budgetExists(year, monthNumber);
                String message = existed ? "Budget updated successfully!" : "Budget saved successfully!";
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

                // Reload budgets
                loadBudgets();

                // Call printLogs after saving
//                printLogs();  // <-- Add this line


                return true;
            } else {
                Toast.makeText(this, "Failed to save budget", Toast.LENGTH_SHORT).show();
                return false;
            }

        } catch (NumberFormatException e) {
            budgetInputLayout.setError("Please enter a valid amount");
            return false;
        }
    }

    private void showDeleteDialog(final Budget budget) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Budget");
        builder.setMessage("Are you sure you want to delete budget for " +
                budget.getMonthName() + " " + budget.getYear() + "?");

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int result = budgetHelper.deleteBudgetById(budget.getId());
                if (result > 0) {
                    Toast.makeText(BudgetActivity.this, "Budget deleted", Toast.LENGTH_SHORT).show();
                    loadBudgets();

                    // Call printLogs after saving
//                    printLogs();  // <-- Add this line

                }
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private String formatIndianCurrency(double value) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        return format.format(value);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Optional: Add animation
        finish();
    }

    @Override
    protected void onDestroy() {
        budgetHelper.close();
        super.onDestroy();
    }

    private void applyUserTheme() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = prefs.getString("app_theme", "Theme.ExpenseUtility");

        switch (theme) {
            case "Default":
                setTheme(R.style.Base_Theme_ExpenseUtility);
                break;
            case "Red":
                setTheme(R.style.AppTheme_Red);
                break;
            case "Blue":
                setTheme(R.style.AppTheme_Blue);
                break;
            case "Green":
                setTheme(R.style.AppTheme_Green);
                break;
            case "GreenParrot":
                setTheme(R.style.AppTheme_GreenParrot);
                break;
            case "Purple":
                setTheme(R.style.AppTheme_Purple);
                break;
            case "Orange":
                setTheme(R.style.AppTheme_Orange);
                break;
            case "Teal":
                setTheme(R.style.AppTheme_Teal);
                break;
            case "Pink":
                setTheme(R.style.AppTheme_Pink);
                break;
            case "Cyan":
                setTheme(R.style.AppTheme_Cyan);
                break;
            case "Lime":
                setTheme(R.style.AppTheme_Lime);
                break;
            case "Brown":
                setTheme(R.style.AppTheme_Brown);
                break;
            case "Mint":
                setTheme(R.style.AppTheme_Mint);
                break;
            case "Coral":
                setTheme(R.style.AppTheme_Coral);
                break;
            case "Steel":
                setTheme(R.style.AppTheme_Steel);
                break;
            case "Lavender":
                setTheme(R.style.AppTheme_Lavender);
                break;
            case "Mustard":
                setTheme(R.style.AppTheme_Mustard);
                break;
            default:
                setTheme(R.style.Base_Theme_ExpenseUtility);
                break;
        }
    }
}