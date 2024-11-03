package com.example.expenseutility.firebaseview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expenseutility.R;

import java.util.List;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainViewHolder> {

    private List<MainItem> mainItemList;

    public MainAdapter(List<MainItem> mainItemList) {
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
        ImageView chevronIcon;

        public MainViewHolder(@NonNull View itemView) {
            super(itemView);
            mainItemTitle = itemView.findViewById(R.id.mainItemTitle);
            mainItemTotalExpense = itemView.findViewById(R.id.mainItemTotalExpense);
            nestedListView = itemView.findViewById(R.id.nestedListView);
            chevronIcon = itemView.findViewById(R.id.chevronIcon);
        }
    }
}
