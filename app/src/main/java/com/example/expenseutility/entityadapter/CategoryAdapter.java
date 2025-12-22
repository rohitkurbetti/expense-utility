package com.example.expenseutility.entityadapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expenseutility.R;
import com.example.expenseutility.dto.CategoryItem;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private List<CategoryItem> categoryItems;

    public CategoryAdapter(List<CategoryItem> categoryItems) {
        this.categoryItems = categoryItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryItem item = categoryItems.get(position);

        holder.categoryName.setText(item.getCategoryName());
        holder.categoryAmount.setText(item.getFormattedAmount());
        holder.categoryPercentage.setText(item.getFormattedPercentage());

        // Optional: Add click listener
        holder.itemView.setOnClickListener(v -> {
            // Handle item click here
            // You could show more details, edit, etc.
        });
    }

    @Override
    public int getItemCount() {
        return categoryItems != null ? categoryItems.size() : 0;
    }

    public void updateData(List<CategoryItem> newItems) {
        categoryItems = newItems;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName;
        TextView categoryAmount;
        TextView categoryPercentage;

        ViewHolder(View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.categoryName);
            categoryAmount = itemView.findViewById(R.id.categoryAmount);
            categoryPercentage = itemView.findViewById(R.id.categoryPercentage);
        }
    }
}
