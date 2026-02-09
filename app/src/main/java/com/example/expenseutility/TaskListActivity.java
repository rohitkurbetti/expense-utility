package com.example.expenseutility;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expenseutility.database.TaskDatabaseHelper;
import com.example.expenseutility.dto.Task;
import com.example.expenseutility.entityadapter.TaskAdapter;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TaskListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvEmptyView, tvNoResults, tvDateFilterStatus;
    private EditText etSearch;
    private ChipGroup chipGroupCategories;
    private Button btnClearFilters, btnClearAll;
    private Button btnStartDate, btnEndDate, btnClearDateFilter;
    private ImageView btnToggleFilters;
    private LinearLayout filterSection;
    private TaskDatabaseHelper dbHelper;
    private TaskAdapter adapter;

    // Lists for filtering
    private List<Task> originalTaskList = new ArrayList<>();
    private List<Task> filteredTaskList = new ArrayList<>();
    private Set<String> selectedCategories = new HashSet<>();
    private String currentSearchQuery = "";

    // Date filter variables
    private Date startDate = null;
    private Date endDate = null;
    private SimpleDateFormat dateDisplayFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private SimpleDateFormat dateDatabaseFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private Calendar calendar = Calendar.getInstance();

    // Toggle state
    private boolean isFilterSectionVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyUserTheme();  // Apply before setContentView
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        // Setup Toolbar
        setupToolbar();

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        tvEmptyView = findViewById(R.id.tvEmptyView);
        tvNoResults = findViewById(R.id.tvNoResults);
        tvDateFilterStatus = findViewById(R.id.tvDateFilterStatus);
        etSearch = findViewById(R.id.etSearch);
        chipGroupCategories = findViewById(R.id.chipGroupCategories);
        btnClearFilters = findViewById(R.id.btnClearFilters);
        btnClearAll = findViewById(R.id.btnClearAll);
        btnStartDate = findViewById(R.id.btnStartDate);
        btnEndDate = findViewById(R.id.btnEndDate);
        btnClearDateFilter = findViewById(R.id.btnClearDateFilter);
        btnToggleFilters = findViewById(R.id.btnToggleFilters);
        filterSection = findViewById(R.id.filterSection);

        // Initialize database helper
        dbHelper = new TaskDatabaseHelper(this);

        // Setup click listeners
        setupClickListeners();

        // Setup search functionality
        setupSearchFunctionality();

        // Setup RecyclerView
        setupRecyclerView();

        // Load tasks
        loadTasks();
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

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("All Tasks");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void setupClickListeners() {
        btnToggleFilters.setOnClickListener(v -> toggleFilterSection());

        btnClearAll.setOnClickListener(v -> {
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(TaskListActivity.this)
                    .setTitle("Delete All Tasks")
                    .setMessage("Are you sure you want to permanently delete all tasks? This cannot be undone.")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        dbHelper.deleteAllTasks();
                        loadTasks();
                        Toast.makeText(this, "All tasks deleted", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .setIcon(R.drawable.warning_svgrepo_com)
                    .show();
        });

        Button btnRefresh = findViewById(R.id.btnRefresh);
        btnRefresh.setOnClickListener(v -> {
            loadTasks();
            Toast.makeText(this, "Refreshed", Toast.LENGTH_SHORT).show();
        });

        btnClearFilters.setOnClickListener(v -> {
            clearAllFilters();
        });

        // Setup date filter buttons
        btnStartDate.setOnClickListener(v -> showDatePicker(true));
        btnEndDate.setOnClickListener(v -> showDatePicker(false));
        btnClearDateFilter.setOnClickListener(v -> clearDateFilter());

        // Setup All chip listener
        Chip chipAll = findViewById(R.id.chipAll);
        chipAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                clearCategoryFilters();
                applyFilters();
            }
        });
    }

    private void toggleFilterSection() {
        isFilterSectionVisible = !isFilterSectionVisible;

        if (isFilterSectionVisible) {
            filterSection.setVisibility(View.VISIBLE);
            btnToggleFilters.setImageResource(R.drawable.ic_filter_list_filled);
            // Update filter icon if needed
        } else {
            filterSection.setVisibility(View.GONE);
            btnToggleFilters.setImageResource(R.drawable.ic_filter_list);
            // Hide keyboard when collapsing filter section
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
        }
    }

    private void showDatePicker(final boolean isStartDate) {
        // Get current date or existing date
        Calendar currentCalendar = Calendar.getInstance();
        if (isStartDate && startDate != null) {
            currentCalendar.setTime(startDate);
        } else if (!isStartDate && endDate != null) {
            currentCalendar.setTime(endDate);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(year, month, dayOfMonth);

                    if (isStartDate) {
                        startDate = selectedCalendar.getTime();
                        btnStartDate.setText(dateDisplayFormat.format(startDate));
                    } else {
                        endDate = selectedCalendar.getTime();
                        btnEndDate.setText(dateDisplayFormat.format(endDate));
                    }

                    updateDateFilterStatus();
                    applyFilters();
                },
                currentCalendar.get(Calendar.YEAR),
                currentCalendar.get(Calendar.MONTH),
                currentCalendar.get(Calendar.DAY_OF_MONTH)
        );

        // Set min/max dates if needed (optional validation)
        if (!isStartDate && startDate != null) {
            datePickerDialog.getDatePicker().setMinDate(startDate.getTime());
        }

        datePickerDialog.show();
    }

    private void updateDateFilterStatus() {
        boolean hasDateFilter = startDate != null || endDate != null;

        if (hasDateFilter) {
            btnClearDateFilter.setVisibility(View.VISIBLE);
            tvDateFilterStatus.setVisibility(View.VISIBLE);

            StringBuilder status = new StringBuilder("Showing tasks ");

            if (startDate != null && endDate != null) {
                status.append("between ").append(dateDisplayFormat.format(startDate))
                        .append(" and ").append(dateDisplayFormat.format(endDate));
            } else if (startDate != null) {
                status.append("from ").append(dateDisplayFormat.format(startDate)).append(" onwards");
            } else if (endDate != null) {
                status.append("up to ").append(dateDisplayFormat.format(endDate));
            }

            tvDateFilterStatus.setText(status.toString());
        } else {
            btnClearDateFilter.setVisibility(View.GONE);
            tvDateFilterStatus.setVisibility(View.GONE);
            btnStartDate.setText("Start Date");
            btnEndDate.setText("End Date");
        }
    }

    private void clearDateFilter() {
        startDate = null;
        endDate = null;
        updateDateFilterStatus();
        applyFilters();
    }

    private void setupSearchFunctionality() {
        // Text change listener for search
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().toLowerCase().trim();
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Set search action on keyboard
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                // Hide keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerView() {
        // Set layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize adapter with empty list
        adapter = new TaskAdapter(this, filteredTaskList);
        recyclerView.setAdapter(adapter);
    }

    private void loadTasks() {
        // Get all tasks from database
        originalTaskList = dbHelper.getAllTasks();

        if (originalTaskList.isEmpty()) {
            // Show empty view
            recyclerView.setVisibility(View.GONE);
            tvEmptyView.setVisibility(View.VISIBLE);
            tvNoResults.setVisibility(View.GONE);
            chipGroupCategories.setVisibility(View.GONE);
            btnToggleFilters.setVisibility(View.GONE);
        } else {
            // Hide empty view
            recyclerView.setVisibility(View.VISIBLE);
            tvEmptyView.setVisibility(View.GONE);
            tvNoResults.setVisibility(View.GONE);
            chipGroupCategories.setVisibility(View.VISIBLE);
            btnToggleFilters.setVisibility(View.VISIBLE);

            // Setup category chips
            setupCategoryChips(originalTaskList);

            // Apply current filters
            applyFilters();
        }
    }

    private void setupCategoryChips(List<Task> tasks) {
        // Clear existing chips except "All"
        chipGroupCategories.removeViews(1, chipGroupCategories.getChildCount() - 1);

        // Get unique categories
        Set<String> categories = new HashSet<>();
        for (Task task : tasks) {
            if (task.getType() != null && !task.getType().isEmpty()) {
                categories.add(task.getType());
            }
        }

        // Create chips for each category
        for (String category : categories) {
            Chip chip = new Chip(this);
            chip.setText(category);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.chip_background_color);

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedCategories.add(category);
                    // Uncheck "All" chip
                    Chip allChip = findViewById(R.id.chipAll);
                    allChip.setChecked(false);
                } else {
                    selectedCategories.remove(category);

                    // If no categories selected, check "All" chip
                    if (selectedCategories.isEmpty()) {
                        Chip allChip = findViewById(R.id.chipAll);
                        allChip.setChecked(true);
                    }
                }
                applyFilters();
            });

            chipGroupCategories.addView(chip);
        }
    }

    private void applyFilters() {
        filteredTaskList.clear();

        for (Task task : originalTaskList) {
            boolean matchesSearch = true;
            boolean matchesCategory = true;
            boolean matchesDate = true;

            // Apply search filter
            if (!currentSearchQuery.isEmpty()) {
                String title = task.getTitle() != null ? task.getTitle().toLowerCase() : "";
                String description = task.getDescription() != null ? task.getDescription().toLowerCase() : "";

                matchesSearch = title.contains(currentSearchQuery) ||
                        description.contains(currentSearchQuery);
            }

            // Apply category filter
            if (!selectedCategories.isEmpty() && task.getType() != null) {
                matchesCategory = selectedCategories.contains(task.getType());
            }

            // Apply date filter
            matchesDate = matchesDateFilter(task);

            if (matchesSearch && matchesCategory && matchesDate) {
                filteredTaskList.add(task);
            }
        }

        // Update adapter
        adapter.updateTasks(filteredTaskList);

        // Show/hide appropriate views
        updateFilteredViews();
    }

    private boolean matchesDateFilter(Task task) {
        if (startDate == null && endDate == null) {
            return true; // No date filter applied
        }

        if (task.getDueDate() == null || task.getDueDate().isEmpty()) {
            return false; // Task has no due date, doesn't match any date filter
        }

        try {
            // Parse task due date
            Date taskDueDate = dateDatabaseFormat.parse(task.getDueDate());
            if (taskDueDate == null) {
                return false;
            }

            // Check if task due date is within range
            boolean afterStart = true;
            boolean beforeEnd = true;

            if (startDate != null) {
                // Clear time part for comparison
                Calendar taskCal = Calendar.getInstance();
                taskCal.setTime(taskDueDate);
                clearTime(taskCal);

                Calendar startCal = Calendar.getInstance();
                startCal.setTime(startDate);
                clearTime(startCal);

                afterStart = !taskCal.before(startCal);
            }

            if (endDate != null) {
                // Clear time part for comparison
                Calendar taskCal = Calendar.getInstance();
                taskCal.setTime(taskDueDate);
                clearTime(taskCal);

                Calendar endCal = Calendar.getInstance();
                endCal.setTime(endDate);
                clearTime(endCal);

                beforeEnd = !taskCal.after(endCal);
            }

            return afterStart && beforeEnd;

        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void clearTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private void updateFilteredViews() {
        if (filteredTaskList.isEmpty() && !originalTaskList.isEmpty()) {
            tvNoResults.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            tvEmptyView.setVisibility(View.GONE);
        } else if (filteredTaskList.isEmpty()) {
            tvEmptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            tvNoResults.setVisibility(View.GONE);
        } else {
            tvEmptyView.setVisibility(View.GONE);
            tvNoResults.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        // Show/hide clear filters button
        boolean hasActiveFilters = !currentSearchQuery.isEmpty() ||
                !selectedCategories.isEmpty() ||
                startDate != null ||
                endDate != null;

        btnClearFilters.setVisibility(hasActiveFilters ? View.VISIBLE : View.GONE);
    }

    private void clearAllFilters() {
        // Clear search
        etSearch.setText("");
        currentSearchQuery = "";

        // Clear category filters
        clearCategoryFilters();

        // Clear date filters
        clearDateFilter();

        // Apply filters (which will show all tasks)
        applyFilters();

        // Show toast
        Toast.makeText(this, "All filters cleared", Toast.LENGTH_SHORT).show();
    }

    private void clearCategoryFilters() {
        selectedCategories.clear();

        // Check "All" chip
        Chip allChip = findViewById(R.id.chipAll);
        allChip.setChecked(true);

        // Uncheck all other chips
        for (int i = 1; i < chipGroupCategories.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupCategories.getChildAt(i);
            chip.setChecked(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload tasks when activity resumes
        loadTasks();
    }
}