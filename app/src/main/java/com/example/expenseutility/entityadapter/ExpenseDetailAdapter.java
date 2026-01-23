package com.example.expenseutility.entityadapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.expenseutility.R;
import com.example.expenseutility.dto.ExpenseDetail;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class ExpenseDetailAdapter extends RecyclerView.Adapter<ExpenseDetailAdapter.ViewHolder> {

    private List<ExpenseDetail> expenseList;
    private OnExpenseClickListener listener;

    public ExpenseDetailAdapter(List<ExpenseDetail> expenseList, OnExpenseClickListener listener) {
        this.expenseList = expenseList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExpenseDetail expenseDetail = expenseList.get(position);

        holder.tvItemName.setText(expenseDetail.getItemName());
        holder.tvAmount.setText(String.format("₹%,.2f", expenseDetail.getAmount()));
        holder.tvDate.setText("Date: " + expenseDetail.getDate());
        holder.tvDescription.setText(expenseDetail.getDescription());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onExpenseClick(expenseDetail);
            }
        });
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    public interface OnExpenseClickListener {
        void onExpenseClick(ExpenseDetail expenseDetail);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemName, tvAmount, tvDate, tvDescription;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tv_item_name);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvDescription = itemView.findViewById(R.id.tv_description);
        }
    }
}
