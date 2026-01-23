package com.example.expenseutility.entityadapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expenseutility.R;
import com.google.android.material.chip.Chip;

import java.util.List;

public class RecentExpensesAdapter extends RecyclerView.Adapter<RecentExpensesAdapter.ViewHolder> {

    private List<ExpenseItem> expenseItems;

    public RecentExpensesAdapter(List<ExpenseItem> expenseItems) {
        this.expenseItems = expenseItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recent_item_expense, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExpenseItem expenseItem = expenseItems.get(position);
        holder.bind(expenseItem);
    }

    @Override
    public int getItemCount() {
        return expenseItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle, tvAmount, tvDate, tvDescription;
        private Chip chipCategory;
        private ImageView ivHomeExpense;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvExpenseTitle);
            chipCategory = itemView.findViewById(R.id.chipExpenseCategory);
            tvAmount = itemView.findViewById(R.id.tvExpenseAmount);
            tvDate = itemView.findViewById(R.id.tvExpenseDate);
            tvDescription = itemView.findViewById(R.id.tvExpenseDescription);
            ivHomeExpense = itemView.findViewById(R.id.ivHomeExpense);
        }

        public void bind(ExpenseItem expenseItem) {
            tvTitle.setText(expenseItem.getExpenseParticulars());
            chipCategory.setText(expenseItem.getExpenseCategory());

            // Set category-specific icon
            setCategoryIcon(expenseItem.getExpenseCategory());

            // Set category-specific background color
            setCategoryBackgroundColor(expenseItem.getExpenseCategory());

            if (expenseItem.getExpenseAmount() != null) {
                // Assuming amount is stored in cents (e.g., 2550 for $25.50)
                double amount = expenseItem.getExpenseAmount() / 100.0;
                tvAmount.setText(String.format("$%.2f", amount));
            } else {
                tvAmount.setText("$0.00");
            }

            tvDate.setText(expenseItem.getExpenseDate());

            // Show description if available
            if (expenseItem.getPartDetails() != null && !expenseItem.getPartDetails().isEmpty()) {
                tvDescription.setText(expenseItem.getPartDetails());
                tvDescription.setVisibility(View.VISIBLE);
            } else {
                tvDescription.setVisibility(View.GONE);
            }

            // Show home expense indicator
            if (expenseItem.isHomeExpense()) {
                ivHomeExpense.setVisibility(View.VISIBLE);
            } else {
                ivHomeExpense.setVisibility(View.GONE);
            }
        }

        private void setCategoryIcon(String category) {
            int iconResId = R.drawable.ic_more; // Default icon

            if (category != null) {
                switch (category) {
                    case "Food":
                        iconResId = R.drawable.ic_food;
                        break;
                    case "Transport":
                        iconResId = R.drawable.ic_transport;
                        break;
                    case "Shopping":
                        iconResId = R.drawable.ic_shopping;
                        break;
                    case "Bills":
                        iconResId = R.drawable.ic_receipt;
                        break;
                    default:
                        iconResId = R.drawable.ic_more;
                        break;
                }
            }

            chipCategory.setChipIconResource(iconResId);
            chipCategory.setChipIconVisible(true);

            // Set icon tint to white for better visibility on colored background
            chipCategory.setChipIconTintResource(android.R.color.white);
        }

        private void setCategoryBackgroundColor(String category) {
            int colorResId = R.color.category_other; // Default color

            if (category != null) {
                switch (category) {
                    case "Food":
                        colorResId = R.color.category_food;
                        break;
                    case "Transport":
                        colorResId = R.color.category_transport;
                        break;
                    case "Shopping":
                        colorResId = R.color.category_shopping;
                        break;
                    case "Bills":
                        colorResId = R.color.category_bills;
                        break;
                    default:
                        colorResId = R.color.category_other;
                        break;
                }
            }

            chipCategory.setChipBackgroundColorResource(colorResId);
        }
    }
}