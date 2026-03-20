package com.example.expenseutility;

import static com.example.expenseutility.constants.ExpenseConstants.ANN_INCOME;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expenseutility.entityadapter.ExpenseItem;
import com.example.expenseutility.firebaseview.MainAdapter;
import com.example.expenseutility.firebaseview.MainItem;
import com.example.expenseutility.firebaseview.NestedItem;
import com.example.expenseutility.utility.Commons;
import com.example.expenseutility.utility.ThemeHelper;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class RealtimeFirebaseActivity extends AppCompatActivity {

    public static Map<String, List<ExpenseItem>> map = new HashMap<>();
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(Build.MODEL + "/" + "expenses");
    LinearProgressIndicator loadingProgressBar;
    TextView yearTotalTextView, finalTotalTextView;
    List<ExpenseItem> tempList = new ArrayList<>();
    private RecyclerView recyclerView;
    private TextView errorHelperTextView;
    private ImageView errorImageView;
    private MainAdapter mainAdapter;
    private CopyOnWriteArrayList<MainItem> mainItemList;
    private AppCompatSpinner yearSpinner;
    private List<ExpenseItem> expenseList = new ArrayList<>();

    // Year progress views
    private TextView yearProgressText;
    private TextView yearProgressPercent;
    private ProgressBar yearProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realtime_firebase);
        yearSpinner = findViewById(R.id.yearSpinner);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        recyclerView = findViewById(R.id.recyclerview);
        errorHelperTextView = findViewById(R.id.errorHelperTextView);
        errorImageView = findViewById(R.id.errorImageView);
        yearTotalTextView = findViewById(R.id.yearTotalTextView);
        finalTotalTextView = findViewById(R.id.finalTotalTextView);

        // Initialize year progress views
        yearProgressText = findViewById(R.id.yearProgressText);
        yearProgressPercent = findViewById(R.id.yearProgressPercent);
        yearProgressBar = findViewById(R.id.yearProgressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mainItemList = new CopyOnWriteArrayList<>();
        loadSpinner(yearSpinner);

        // Initialize year progress
        updateYearProgress();

        yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedYear = parent.getItemAtPosition(position).toString();
                mainItemList.clear();
                fetchFromFirebase(map);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle no selection
            }
        });

        mainAdapter = new MainAdapter(mainItemList);
        recyclerView.setAdapter(mainAdapter);
    }

    private void updateYearProgress() {
        Calendar calendar = Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_YEAR);
        int totalDaysInYear = calendar.getActualMaximum(Calendar.DAY_OF_YEAR);

        // Calculate percentage
        double percentage = (currentDay * 100.0) / totalDaysInYear;
        DecimalFormat df = new DecimalFormat("#.##");
        String formattedPercentage = df.format(percentage);

        // Update UI
        yearProgressText.setText("Day " + currentDay + "/" + totalDaysInYear);
        yearProgressPercent.setText(formattedPercentage + "%");
        yearProgressBar.setProgress((int) Math.round(percentage));

        // Optional: Change color based on progress percentage
        updateProgressColor(percentage);
    }

    private void updateProgressColor(double percentage) {
        int color;
        if (percentage > 75) {
            color = getResources().getColor(android.R.color.holo_red_dark);
        } else if (percentage > 50) {
            color = getResources().getColor(android.R.color.holo_orange_dark);
        } else {
            color = getResources().getColor(android.R.color.holo_green_dark);
        }
        yearProgressPercent.setTextColor(color);
    }

    private void loadSpinner(AppCompatSpinner yearSpinner) {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        List<String> years = new ArrayList<>();
        for (int year = 2019; year <= currentYear + 5; year++) {
            years.add(String.valueOf(year));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                years
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(adapter);
        yearSpinner.setSelection(adapter.getPosition(String.valueOf(currentYear)));
    }

    private void fetchFromFirebase(Map<String, List<ExpenseItem>> map) {
        loadingProgressBar.setVisibility(View.VISIBLE);
        float income = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).getFloat("monthlyIncome", ANN_INCOME);

        databaseReference.addValueEventListener(new ValueEventListener() {
            private String key;

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                loadingProgressBar.setVisibility(View.GONE);
                Long expAmt = 0L;
                Long expAmtMonth = 0L;
                expenseList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot d1 : dataSnapshot.getChildren()) {
                        List<NestedItem> nestedItems1 = new ArrayList<>();
                        for (DataSnapshot d2 : d1.getChildren()) {
                            key = d2.getKey();
                            expAmt = 0L;
                            tempList.clear();
                            List<ExpenseItem> tList = new ArrayList<>();
                            for (DataSnapshot d3 : d2.getChildren()) {
                                ExpenseItem expense = d3.getValue(ExpenseItem.class);
                                expenseList.add(expense);
                                expAmt += expense.getExpenseAmount();
                                tList.add(expense);
                            }
                            nestedItems1.add(new NestedItem(d2.getKey(), "\u20B9" + expAmt,
                                    String.format("(%.2f%%)", ((double) expAmt / (income / 30)) * 100), expAmt));
                            expAmtMonth += expAmt;
                            map.put(key, tList);
                        }
                        mainItemList.add(new MainItem(d1.getKey(), nestedItems1,
                                String.format("\u20B9" + expAmtMonth + " (%.2f%%)", ((double) expAmtMonth / income) * 100)));
                        expAmtMonth = 0L;
                    }
                }

                // Calculate totals
                AtomicInteger yearGrandTotal = new AtomicInteger(0);
                mainItemList.forEach(i -> {
                    i.getNestedItemList().forEach(nestedItem -> {
                        yearGrandTotal.addAndGet(Math.toIntExact(nestedItem.getSubItemExpenseTotalAmount()));
                    });
                });

                String formattedAmount = Commons.getFormattedCurrency(yearGrandTotal.get());
                finalTotalTextView.setText(String.valueOf("Final Total  " + formattedAmount));

                // Filter by selected year
                String spinnerMonth = yearSpinner.getSelectedItem().toString();
                for (MainItem y : mainItemList) {
                    if (!y.getTitle().contains(spinnerMonth)) {
                        mainItemList.remove(y);
                    }
                }

                AtomicInteger yearTotal = new AtomicInteger();
                mainItemList.forEach(i -> {
                    i.getNestedItemList().forEach(nestedItem -> {
                        yearTotal.addAndGet(Math.toIntExact(nestedItem.getSubItemExpenseTotalAmount()));
                    });
                });
                yearTotalTextView.setText("Total  \u20B9" + yearTotal);
                mainAdapter.notifyDataSetChanged();

                if (mainItemList.isEmpty()) {
                    errorHelperTextView.setText("No data available on cloud");
                    errorImageView.setVisibility(View.VISIBLE);
                    errorHelperTextView.setVisibility(View.VISIBLE);
                } else {
                    errorImageView.setVisibility(View.GONE);
                    errorHelperTextView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadingProgressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "Failed to load data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update progress when activity resumes
        updateYearProgress();
    }
}