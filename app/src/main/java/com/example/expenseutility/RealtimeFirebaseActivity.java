package com.example.expenseutility;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class RealtimeFirebaseActivity extends AppCompatActivity {

    public static Map<String, List<ExpenseItem>> map = new HashMap<>();
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("expenses");
    ProgressDialog progressDialog;
    TextView yearTotalTextView,finalTotalTextView;
    List<ExpenseItem> tempList = new ArrayList<>();
    private RecyclerView recyclerView;
    private TextView errorHelperTextView;
    private ImageView errorImageView;
    private MainAdapter mainAdapter;
    private CopyOnWriteArrayList<MainItem> mainItemList;
    private AppCompatSpinner yearSpinner;
    private List<ExpenseItem> expenseList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realtime_firebase);
        yearSpinner = findViewById(R.id.yearSpinner);
        progressDialog = new ProgressDialog(this);
        recyclerView = findViewById(R.id.recyclerview);
        errorHelperTextView = findViewById(R.id.errorHelperTextView);
        errorImageView = findViewById(R.id.errorImageView);
        yearTotalTextView = findViewById(R.id.yearTotalTextView);
        finalTotalTextView = findViewById(R.id.finalTotalTextView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Populate main item list with sample data
        mainItemList = new CopyOnWriteArrayList<>();
        loadSpinner(yearSpinner);

//        fetchFromFirebase(map);

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


        // Set the adapter
        mainAdapter = new MainAdapter(mainItemList);
        recyclerView.setAdapter(mainAdapter);



    }

    private void loadSpinner(AppCompatSpinner yearSpinner) {
        // Get the current year
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        // Generate a list of years (e.g., from 1900 to current year)
        List<String> years = new ArrayList<>();
        for (int year = 2019; year <= currentYear+3; year++) {
            years.add(String.valueOf(year));
        }

        // Create an ArrayAdapter for the Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                years
        );

        // Set the layout for dropdown items
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Attach the adapter to the Spinner
        yearSpinner.setAdapter(adapter);

        // Optionally, set the current year as the selected item
        yearSpinner.setSelection(adapter.getPosition(String.valueOf(currentYear)));
    }

    private void fetchFromFirebase(Map<String, List<ExpenseItem>> map) {
        progressDialog.setMessage("Fetching from cloud database");
        progressDialog.show();
        float income = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).getFloat("monthlyIncome", 87000.0f);

        databaseReference.addValueEventListener(new ValueEventListener() {
            private String key;

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.dismiss();
                Long expAmt = 0L;
                Long expAmtMonth = 0L;
                expenseList.clear(); // Clear previous list
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    for(DataSnapshot d1 : dataSnapshot.getChildren()) {
                        List<NestedItem> nestedItems1 = new ArrayList<>();
                        for(DataSnapshot d2 : d1.getChildren()) {
                            key = d2.getKey();
                            expAmt=0L;
                            tempList.clear();
                            List<ExpenseItem> tList = new ArrayList<>();
                            for(DataSnapshot d3 : d2.getChildren()) {
                                ExpenseItem expense = d3.getValue(ExpenseItem.class);
                                expenseList.add(expense);
                                expAmt += expense.getExpenseAmount();
                                tList.add(expense);
                            }
                            nestedItems1.add(new NestedItem(d2.getKey(), "\u20B9"+expAmt, String.format("(%.2f%%)", ((double) expAmt/(income/30))*100),expAmt));
                            expAmtMonth += expAmt;
                            map.put(key, tList);

//                            Log.i("Key val " ,key+"  "+tempList);

                        }
                        mainItemList.add(new MainItem(d1.getKey(), nestedItems1, String.format("\u20B9"+expAmtMonth+" (%.2f%%)", ((double) expAmtMonth/income)*100)));
                        expAmtMonth = 0L;
                    }
                }

                AtomicInteger yearGrandTotal = new AtomicInteger(0);
                mainItemList.forEach(i -> {
                    i.getNestedItemList().forEach(nestedItem -> {
                        yearGrandTotal.addAndGet(Math.toIntExact(nestedItem.getSubItemExpenseTotalAmount()));
                    });
                });

                finalTotalTextView.setText(String.valueOf("Final Total  \u20B9"+yearGrandTotal));

                String spinnerMonth = yearSpinner.getSelectedItem().toString();
                for (MainItem y: mainItemList) {
                    if(!y.getTitle().contains(spinnerMonth)) {
                        mainItemList.remove(y);
                    }
                }
                AtomicInteger yearTotal = new AtomicInteger();
                mainItemList.forEach(i -> {
                    i.getNestedItemList().forEach(nestedItem -> {
                        yearTotal.addAndGet(Math.toIntExact(nestedItem.getSubItemExpenseTotalAmount()));
                    });
                });
                yearTotalTextView.setText("Total  \u20B9"+yearTotal);
                mainAdapter.notifyDataSetChanged();

                if(mainItemList.isEmpty()) {
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
                progressDialog.setMessage("Firebase load failed");
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Failed to load data.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}