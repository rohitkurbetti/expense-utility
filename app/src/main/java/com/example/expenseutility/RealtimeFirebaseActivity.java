package com.example.expenseutility;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RealtimeFirebaseActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView errorHelperTextView;
    private ImageView errorImageView;
    private MainAdapter mainAdapter;
    private List<MainItem> mainItemList;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("expenses");

    private List<ExpenseItem> expenseList = new ArrayList<>();

    public static Map<String, List<ExpenseItem>> map = new HashMap<>();

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realtime_firebase);

        progressDialog = new ProgressDialog(this);
        recyclerView = findViewById(R.id.recyclerview);
        errorHelperTextView = findViewById(R.id.errorHelperTextView);
        errorImageView = findViewById(R.id.errorImageView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Populate main item list with sample data
        mainItemList = new ArrayList<>();



        fetchFromFirebase(map);


//        nestedItems1.add(new NestedItem("2024-10-12", "\u20B920", String.format("(%.2f%%)", (double) 20/2000)));
//        nestedItems1.add(new NestedItem("2024-10-17","\u20B9120",String.format("(%.2f%%)", (double)120/2000)));
//        nestedItems1.add(new NestedItem("2024-10-18","\u20B9420",String.format("(%.2f%%)", (double)420/2000)));
//        nestedItems1.add(new NestedItem("2024-10-19","\u20B9220",String.format("(%.2f%%)",(double) 220/2000)));
//        nestedItems1.add(new NestedItem("2024-10-20","\u20B950",String.format("(%.2f%%)", (double)50/2000)));


        List<NestedItem> nestedItems2 = new ArrayList<>();
//        nestedItems2.add(new NestedItem("Sub Item 2A","\u20B92300",(double) 2300/2000));
//        nestedItems2.add(new NestedItem("Sub Item 2B","\u20B92300",(double) 2300/2000));
//        nestedItems2.add(new NestedItem("Sub Item 2C","\u20B92300",(double) 2300/2000));

//        mainItemList.add(new MainItem("Oct-2024", nestedItems1, String.format("\u20B912000 (%.2f%%)", ((double) 12000/62000)*100)));
//        mainItemList.add(new MainItem("Main Item 2", nestedItems2));
//        mainItemList.add(new MainItem("Main Item 3", new ArrayList<>()));

        // Set the adapter
        mainAdapter = new MainAdapter(mainItemList);
        recyclerView.setAdapter(mainAdapter);



    }
    List<ExpenseItem> tempList = new ArrayList<>();

    private void fetchFromFirebase(Map<String, List<ExpenseItem>> map) {
        progressDialog.setMessage("Fetching from cloud database");
        progressDialog.show();
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
                            nestedItems1.add(new NestedItem(d2.getKey(), "\u20B9"+expAmt, String.format("(%.2f%%)", (double) expAmt/2000),expAmt));
                            expAmtMonth += expAmt;
                            map.put(key, tList);
//                            tempList.clear();

                            Log.i("Key val " ,key+"  "+tempList);

                        }
                        mainItemList.add(new MainItem(d1.getKey(), nestedItems1, String.format("\u20B9"+expAmtMonth+" (%.2f%%)", ((double) expAmtMonth/62000)*100)));
                    }
                }
                mainAdapter.notifyDataSetChanged();

                if(!snapshot.hasChildren()) {
                    errorHelperTextView.setText("No data on cloud");
                    errorImageView.setVisibility(View.VISIBLE);
                    errorHelperTextView.setVisibility(View.VISIBLE);
//                    Toast.makeText(RealtimeFirebaseActivity.this, "", Toast.LENGTH_SHORT).show();
                } else {
                    errorImageView.setVisibility(View.GONE);
                    errorHelperTextView.setVisibility(View.GONE);
                }

//                expenseList = expenseList.stream().sorted(Comparator.comparing(ExpenseItem::getExpenseDate).reversed())
//                        .collect(Collectors.toList());
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