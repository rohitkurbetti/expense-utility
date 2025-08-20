package com.example.expenseutility;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.anychart.enums.Sort;
import com.example.expenseutility.entityadapter.ExpenseItem;
import com.example.expenseutility.entityadapter.Expenses;
import com.example.expenseutility.entityadapter.FirebaseExpenseAdapter;
import com.example.expenseutility.utility.SpinnerItem;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FirebaseList extends AppCompatActivity {

    private ListView expenseListView;
    private List<ExpenseItem> expenseList = new ArrayList<>();
    private FirebaseExpenseAdapter expenseAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase_list);

        expenseListView = findViewById(R.id.expenseListView);

        // Fetch data from Firebase
        fetchFromFirebase();

        // Initialize the adapter and set it to the ListView
        expenseAdapter = new FirebaseExpenseAdapter(this, expenseList);
        expenseListView.setAdapter(expenseAdapter);

        List<SpinnerItem> items = new ArrayList<>();

        items = FirstFragment.fetchAllSpinnerOptions(items);


        List<SpinnerItem> finalItems = items;
        expenseListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ExpenseItem selectedExpense = (ExpenseItem) parent.getItemAtPosition(position);
                List<SpinnerItem> items1 = finalItems.stream().filter(i -> i.getText().equalsIgnoreCase(selectedExpense.getExpenseCategory())).collect(Collectors.toList());

                selectedExpense.setId(items1.get(0).getImageResourceId());
                showItemDetailsDialog(selectedExpense);
            }

            private void showItemDetailsDialog(ExpenseItem selectedExpense) {
                // Inflate the dialog layout
                LayoutInflater inflater = LayoutInflater.from(FirebaseList.this);
                View dialogView = inflater.inflate(R.layout.dialog_item_details, null);

                // Create the dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(FirebaseList.this);
                builder.setView(dialogView);
                // Reference views in the dialog
                ImageView iconView = dialogView.findViewById(R.id.iconView);
                TextView tvCategory = dialogView.findViewById(R.id.tvItemCategory);
                TextView tvAmount = dialogView.findViewById(R.id.tvItemAmount);
                TextView tvDate = dialogView.findViewById(R.id.tvItemDate);
                Button btnClose = dialogView.findViewById(R.id.btnClose);

                // Set the details to the dialog views
                iconView.setImageDrawable(getDrawable(selectedExpense.getId()));
                tvCategory.setText("Category: " + selectedExpense.getExpenseCategory());
                tvAmount.setText("Amount: \u20B9" + selectedExpense.getExpenseAmount());
                tvDate.setText("Date: " + selectedExpense.getExpenseDate());

                // Show the dialog
                AlertDialog dialog = builder.create();
                dialog.show();

                // Close button handler
                btnClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
        });

    }

    private void fetchFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(Build.MODEL +"/"+"expenses");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                expenseList.clear(); // Clear previous list
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    for(DataSnapshot d1 : dataSnapshot.getChildren()) {
                        for(DataSnapshot d2 : d1.getChildren()) {
                            for(DataSnapshot d3 : d2.getChildren()) {
                                ExpenseItem expense = d3.getValue(ExpenseItem.class);
                                expenseList.add(expense);
                            }
                        }
                    }
                }

                expenseList = expenseList.stream().sorted(Comparator.comparing(ExpenseItem::getExpenseDate).reversed())
                        .collect(Collectors.toList());
                expenseAdapter.notifyDataSetChanged(); // Refresh the list
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Failed to load data.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}