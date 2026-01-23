package com.example.expenseutility.entityadapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expenseutility.R;
import com.example.expenseutility.dto.CategoryData;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CategoryAdapter1 extends RecyclerView.Adapter<CategoryAdapter1.ViewHolder> {

    private List<CategoryData> categoryList;
    private OnCategoryClickListener listener;
    private NumberFormat currencyFormat;

    public CategoryAdapter1(List<CategoryData> categoryList, OnCategoryClickListener listener) {
        this.categoryList = categoryList;
        this.listener = listener;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryData categoryData = categoryList.get(position);

        holder.tvCategory.setText(categoryData.getCategoryName());
        holder.tvCategoryTotal.setText(currencyFormat.format(categoryData.getTotalAmount()));

        int expenseCount = categoryData.getExpenses() != null ? categoryData.getExpenses().size() : 0;
        holder.tvExpenseCount.setText(expenseCount + " expenses");
        holder.categoryIcon.setImageResource(categoryData.getCategoryIcon());

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Scale animation
                v.animate()
                        .scaleX(0.95f)
                        .scaleY(0.95f)
                        .setDuration(100)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                v.animate()
                                        .scaleX(1.0f)
                                        .scaleY(1.0f)
                                        .setDuration(100)
                                        .start();
                            }
                        })
                        .start();

                if (listener != null) {
                    listener.onCategoryClick(categoryData);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public interface OnCategoryClickListener {
        void onCategoryClick(CategoryData categoryData);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvCategory, tvCategoryTotal, tvExpenseCount;
        ImageView ivArrow, categoryIcon;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvCategoryTotal = itemView.findViewById(R.id.tv_category_total);
            tvExpenseCount = itemView.findViewById(R.id.tv_expense_count);
            ivArrow = itemView.findViewById(R.id.iv_arrow);
            categoryIcon = itemView.findViewById(R.id.category_icon);
        }
    }
}
