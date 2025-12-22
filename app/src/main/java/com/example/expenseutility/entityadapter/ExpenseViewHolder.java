package com.example.expenseutility.entityadapter;

// ExpenseViewHolder.java

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expenseutility.R;

public class ExpenseViewHolder extends RecyclerView.ViewHolder {

    TextView tvMonthYear, tvTotalExpense;
    ImageView ivChevron;
    LinearLayout expandableLayout;
    RecyclerView categoryRecyclerView;

    public ExpenseViewHolder(@NonNull View itemView) {
        super(itemView);

        tvMonthYear = itemView.findViewById(R.id.tvMonthYear);
        tvTotalExpense = itemView.findViewById(R.id.tvTotalExpense);
        categoryRecyclerView = itemView.findViewById(R.id.categoryRecyclerView);
        ivChevron = itemView.findViewById(R.id.ivChevron);
        expandableLayout = itemView.findViewById(R.id.expandableLayout);
    }
}