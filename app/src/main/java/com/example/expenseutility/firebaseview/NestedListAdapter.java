package com.example.expenseutility.firebaseview;


import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.example.expenseutility.MainActivity;
import com.example.expenseutility.R;
import com.example.expenseutility.RealtimeFirebaseActivity;
import com.example.expenseutility.entityadapter.ExpenseItem;
import com.example.expenseutility.entityadapter.NestedItemAdapter;
import com.example.expenseutility.utility.SpinnerItem;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class NestedListAdapter extends BaseAdapter {

    private Context context;
    private List<NestedItem> nestedItemList;
    public static ImageButton btnDeletePopup;

    public NestedListAdapter(Context context, List<NestedItem> nestedItemList) {
        this.context = context;
        this.nestedItemList = nestedItemList;
    }

    @Override
    public int getCount() {
        return nestedItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return nestedItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_firebase, parent, false);
        }

        TextView nestedItemTitle = convertView.findViewById(R.id.nestedItemTitle);
        TextView nestedItemExpense = convertView.findViewById(R.id.nestedItemExpense);
        TextView nestedItemExpensePer = convertView.findViewById(R.id.nestedItemExpensePer);
        ImageButton btnDeleteDateEntry = convertView.findViewById(R.id.btnDeleteDateEntry);
        CheckBox chkBoxDay = convertView.findViewById(R.id.chkBoxDay);

        NestedItem nestedItem = nestedItemList.get(position);
//        nestedItemTitle.setText(nestedItem.getTitle());
        nestedItemTitle.setText(nestedItemList.get(position).getSubItemName());
        nestedItemExpense.setText(nestedItemList.get(position).getSubItemExpense());
        nestedItemExpensePer.setText(nestedItemList.get(position).getSubItemExpensePer());

        // Handle button click event
        convertView.setOnClickListener(v -> {

            List<ExpenseItem> nestedItemList = RealtimeFirebaseActivity.map.get(nestedItem.getSubItemName());

            populateNestedItemsPopup(nestedItemList, nestedItem.getSubItemName(), nestedItem.getSubItemExpenseTotalAmount());


        });

        chkBoxDay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    btnDeleteDateEntry.setVisibility(View.VISIBLE);
                } else {
                    btnDeleteDateEntry.setVisibility(View.GONE);
                }
            }
        });

        btnDeleteDateEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Reference to the Firebase Realtime Database
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference databaseReference = database.getReference("expenses");

                String childPath = "";
                String expDate = nestedItem.getSubItemName();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate date = LocalDate.parse(expDate, formatter);

                int year = date.getYear();

                DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM"); // "MMM" gives abbreviated month
                String month = monthFormatter.format(date);

                childPath = "/"+year +"/"+(month+"-"+year)+"/"+expDate;
                databaseReference.child(childPath).removeValue()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "Entries deleted on "+expDate, Toast.LENGTH_LONG).show();
                            // Navigate back to MainActivity after successful deletion
                            Intent intent = new Intent(context, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Optional: Clear the activity stack
                            context.startActivity(intent);
                        } else {
                            Toast.makeText(context, "Failed to delete entry", Toast.LENGTH_SHORT).show();
                        }
                    });


            }
        });

        return convertView;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void populateNestedItemsPopup(List<ExpenseItem> expenseItemList, String subItemName, Long subItemExpenseTotalAmount) {

        List<SpinnerItem> items = new ArrayList<>();
        items.add(new SpinnerItem("Select Options", 0));
        items.add(new SpinnerItem("Housing Expenses", R.drawable.house_to_rent_svgrepo_com));
        items.add(new SpinnerItem("Transportation", R.drawable.ground_transportation_svgrepo_com));
        items.add(new SpinnerItem("Food", R.drawable.meal_easter_svgrepo_com));
        items.add(new SpinnerItem("Healthcare", R.drawable.healthcare_hospital_medical_9_svgrepo_com));
        items.add(new SpinnerItem("Fuel", R.drawable.fuel_station));
        items.add(new SpinnerItem("Debt Payments", R.drawable.money_svgrepo_com__1_));
        items.add(new SpinnerItem("Entertainment", R.drawable.entertainment_svgrepo_com));
        items.add(new SpinnerItem("Savings and Investments", R.drawable.piggybank_pig_svgrepo_com));
        items.add(new SpinnerItem("Grocery", R.drawable.shopping_basket));
        items.add(new SpinnerItem("Clothing and Personal Care", R.drawable.clothes_clothing_formal_wear_svgrepo_com));
        items.add(new SpinnerItem("Education", R.drawable.education_graduation_learning_school_study_svgrepo_com));
        items.add(new SpinnerItem("Charity and Gifts", R.drawable.loving_charity_svgrepo_com));
        items.add(new SpinnerItem("Travel", R.drawable.travel_svgrepo_com__1_));
        items.add(new SpinnerItem("Insurance", R.drawable.employee_svgrepo_com));
        items.add(new SpinnerItem("Childcare and Education", R.drawable.woman_pushing_stroller_svgrepo_com));
        items.add(new SpinnerItem("Miscellaneous", R.drawable.healthcare_hospital_medical_9_svgrepo_com));


        expenseItemList.forEach(e -> {
            List<SpinnerItem> items1 = items.stream().filter(i -> i.getText().equalsIgnoreCase(e.getExpenseCategory())).collect(Collectors.toList());
            e.setId(items1.get(0).getImageResourceId());
        });



        // Inflate the dialog layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.nested_items_popup, null);

        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);

//        ImageView iconView = dialogView.findViewById(R.id.iconView);
        ListView popupListView = dialogView.findViewById(R.id.popupListView);

        NestedItemAdapter adapter = new NestedItemAdapter(context, expenseItemList);
        popupListView.setAdapter(adapter);


        TextView tvDate = dialogView.findViewById(R.id.tvItemDatePopup);
        TextView tvItemTotalPopup = dialogView.findViewById(R.id.tvItemTotalPopup);
        Button btnClose = dialogView.findViewById(R.id.btnClosePopup);
        btnDeletePopup = dialogView.findViewById(R.id.btnDeletePopup);

        // Set the details to the dialog views
//        iconView.setImageDrawable(getDrawable(selectedExpense.getId()));

        if(subItemName != null && !subItemName.isEmpty() && !subItemName.equals("")) {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate date = LocalDate.parse(subItemName, dateTimeFormatter);

            String frmtteddate = DateTimeFormatter.ofPattern("dd-MMM-yy").format(date);
            tvDate.setText(String.valueOf(frmtteddate));
        } else {
            tvDate.setText("");
        }

        tvItemTotalPopup.setText(String.valueOf("\u20B9"+subItemExpenseTotalAmount));
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

        btnDeletePopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expenseItemList.forEach(i -> {
                    if(i.getChecked()) {
                        deleteFromFirebaseCloud(i);
                    }
                });
                dialog.dismiss();
                // Navigate back to MainActivity after successful deletion
                Intent intent = new Intent(context, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Optional: Clear the activity stack
                context.startActivity(intent);
            }
        });


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void deleteFromFirebaseCloud(ExpenseItem i) {
        // Reference to the Firebase Realtime Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference("expenses");

        String childPath = "";
        String expDate = i.getExpenseDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(expDate, formatter);

        int year = date.getYear();

        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM"); // "MMM" gives abbreviated month
        String month = monthFormatter.format(date);

        childPath = "/"+year +"/"+(month+"-"+year)+"/"+expDate;


        String finalChildPath = childPath;
        databaseReference.child(childPath).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChildren()) {
                    boolean dataExists = false;
                    for (DataSnapshot d3 : snapshot.getChildren()) {
                        ExpenseItem expenseItem = d3.getValue(ExpenseItem.class);
                        if (expenseItem.getExpenseParticulars().equalsIgnoreCase(i.getExpenseParticulars()) &&
                                expenseItem.getExpenseCategory().equalsIgnoreCase(i.getExpenseCategory()) &&
                                expenseItem.getExpenseAmount().toString().equals(i.getExpenseAmount().toString()) &&
                                expenseItem.getExpenseDateTime().equalsIgnoreCase(i.getExpenseDateTime()) &&
                                expenseItem.getExpenseDate().equalsIgnoreCase(i.getExpenseDate())
                        ) {
                            dataExists = true;
                            databaseReference.child(finalChildPath +"/"+d3.getKey()).removeValue();
                            Toast.makeText(context, "Deleted "+i.getExpenseParticulars() +" "+i.getExpenseDateTime()+" "+i.getExpenseAmount(),
                                    Toast.LENGTH_LONG).show();

                            break;
                        } else {
                            dataExists = false;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

//        // Delete the specific object (e.g., user2)
//        String userIdToDelete = "user2";
//        usersRef.child(userIdToDelete).removeValue()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        Toast.makeText(context, "Entry deleted successfully", Toast.LENGTH_SHORT).show();
//                    } else {
//                        Toast.makeText(context, "Failed to delete entry", Toast.LENGTH_SHORT).show();
//                    }
//                });

    }

}
