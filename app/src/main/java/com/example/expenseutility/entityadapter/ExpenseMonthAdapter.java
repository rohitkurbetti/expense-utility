package com.example.expenseutility.entityadapter;

// ExpenseAdapter.java

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expenseutility.R;
import com.example.expenseutility.dto.CategoryItem;
import com.example.expenseutility.dto.ExpenseMonth;

import java.util.ArrayList;
import java.util.List;

public class ExpenseMonthAdapter extends RecyclerView.Adapter<ExpenseViewHolder> {

    private final Context context;
    private List<ExpenseMonth> expenseList;

    private RecyclerView categoryRecyclerView;
    private CategoryAdapter categoryAdapter;
    private List<CategoryItem> categoryItems;
    private TextView totalExpenseTextView;


    public ExpenseMonthAdapter(Context context, List<ExpenseMonth> expenseList) {
        this.context = context;
        this.expenseList = expenseList;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense_month, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        ExpenseMonth expense = expenseList.get(position);

        // Set data
        holder.tvMonthYear.setText(expense.getMonthYear());
        holder.tvTotalExpense.setText("₹" + expense.getTotalExpense());

        // Setup RecyclerView
        categoryItems = new ArrayList<>();


        categoryAdapter = new CategoryAdapter(expense.getCategoryItems());

        holder.categoryRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        holder.categoryRecyclerView.setAdapter(categoryAdapter);


        // Set chevron icon based on expanded state
        if (expense.isExpanded()) {
            holder.expandableLayout.setVisibility(View.VISIBLE);
            holder.ivChevron.setImageResource(R.drawable.baseline_arrow_drop_up_24);
        } else {
            holder.expandableLayout.setVisibility(View.GONE);
            holder.ivChevron.setImageResource(R.drawable.baseline_arrow_drop_down_24);
        }

        // Set click listener for chevron
        holder.ivChevron.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleExpandableLayout(holder, expense);
            }
        });

        // Optional: Also make the entire card clickable
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleExpandableLayout(holder, expense);
            }
        });
    }

    private void toggleExpandableLayout(ExpenseViewHolder holder, ExpenseMonth expense) {
        // Toggle expanded state
        boolean isExpanded = expense.isExpanded();
        expense.setExpanded(!isExpanded);

        // Animate chevron rotation
        rotateChevron(holder.ivChevron, !isExpanded);

        // Show/hide expandable layout with animation
        if (!isExpanded) {
            holder.expandableLayout.setVisibility(View.VISIBLE);
            // You can add slide animation here if needed
        } else {
            holder.expandableLayout.setVisibility(View.GONE);
        }

        // Notify adapter of change (optional, for smooth animations)
        notifyItemChanged(expenseList.indexOf(expense));
    }

    private void rotateChevron(ImageView imageView, boolean expand) {
        float fromRotation = expand ? 0f : 180f;
        float toRotation = expand ? 180f : 0f;

        RotateAnimation rotateAnimation = new RotateAnimation(
                fromRotation, toRotation,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        rotateAnimation.setDuration(300);
        rotateAnimation.setFillAfter(true);
        imageView.startAnimation(rotateAnimation);
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    // Update data method
    public void updateData(List<ExpenseMonth> newExpenseList) {
        expenseList.clear();
        expenseList.addAll(newExpenseList);
        notifyDataSetChanged();
    }
}
