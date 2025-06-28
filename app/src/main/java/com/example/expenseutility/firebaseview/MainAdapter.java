package com.example.expenseutility.firebaseview;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expenseutility.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainViewHolder> {

    private List<MainItem> mainItemList;

    public
    MainAdapter(List<MainItem> mainItemList) {
        this.mainItemList = mainItemList;
    }

    @NonNull
    @Override
    public MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main, parent, false);
        return new MainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MainViewHolder holder, int position) {
        MainItem mainItem = mainItemList.get(position);

        // Set main item title
        holder.mainItemTitle.setText(mainItem.getTitle());
        holder.mainItemTotalExpense.setText(mainItem.getTotalExpense());

        // If the item is expanded, show the ListView and adjust its height
        if (mainItem.isExpanded()) {
            holder.nestedListView.setVisibility(View.VISIBLE);

            // Set the ListView adapter
            NestedListAdapter nestedListAdapter = new NestedListAdapter(holder.itemView.getContext(), mainItem.getNestedItemList());
            holder.nestedListView.setAdapter(nestedListAdapter);

            // Dynamically adjust the ListView height based on its content
            Utils.setListViewHeightBasedOnChildren(holder.nestedListView);

            holder.chevronIcon.setImageResource(R.drawable.baseline_arrow_drop_up_24); // Show up chevron
        } else {
            holder.nestedListView.setVisibility(View.GONE);
            holder.chevronIcon.setImageResource(R.drawable.baseline_arrow_drop_down_24); // Show down chevron
        }

        holder.monthDeleteCloudBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String monthStr = mainItem.getTitle();

                if(!monthStr.isBlank()) {
                    String year = monthStr.substring(monthStr.length()-4);
                    deleteCloudMonth(year, monthStr, v.getContext());
                }

            }

            private void deleteCloudMonth(String year, String monthStr, Context context) {

                // Reference to the Firebase Realtime Database
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference databaseReference = database.getReference("expenses");

                String path = year +"/"+monthStr;

//                  *** Uncomment below to enable delete month feature
                databaseReference.child(path).removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "Expense deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
                    }
                });



            }
        });

        // Toggle expansion on click
        holder.itemView.setOnClickListener(v -> {
            mainItem.setExpanded(!mainItem.isExpanded());
            notifyItemChanged(position);  // Refresh this item
        });
    }

    @Override
    public int getItemCount() {
        return mainItemList.size();
    }

    public static class MainViewHolder extends RecyclerView.ViewHolder {
        TextView mainItemTitle,mainItemTotalExpense;
        ListView nestedListView;
        ImageView chevronIcon,monthDeleteCloudBtn;

        public MainViewHolder(@NonNull View itemView) {
            super(itemView);
            mainItemTitle = itemView.findViewById(R.id.mainItemTitle);
            mainItemTotalExpense = itemView.findViewById(R.id.mainItemTotalExpense);
            nestedListView = itemView.findViewById(R.id.nestedListView);
            chevronIcon = itemView.findViewById(R.id.chevronIcon);
            monthDeleteCloudBtn = itemView.findViewById(R.id.monthDeleteCloudBtn);
        }
    }
}
