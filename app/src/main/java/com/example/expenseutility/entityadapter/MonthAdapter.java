package com.example.expenseutility.entityadapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expenseutility.R;
import com.example.expenseutility.dto.MonthData;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class MonthAdapter extends RecyclerView.Adapter<MonthAdapter.ViewHolder> {

    private List<MonthData> monthList;
    private OnMonthClickListener listener;
    private NumberFormat currencyFormat;

    public MonthAdapter(List<MonthData> monthList, OnMonthClickListener listener) {
        this.monthList = monthList;
        this.listener = listener;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_month, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MonthData monthData = monthList.get(position);

        holder.tvMonth.setText(monthData.getMonthName());
        holder.tvMonthTotal.setText(currencyFormat.format(monthData.getTotalAmount()));

        int categoryCount = monthData.getCategories() != null ? monthData.getCategories().size() : 0;
        holder.tvCategoryCount.setText(categoryCount + " categories");

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
                    listener.onMonthClick(monthData);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return monthList.size();
    }

    public interface OnMonthClickListener {
        void onMonthClick(MonthData monthData);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvMonth, tvMonthTotal, tvCategoryCount;
        ImageView ivArrow;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            tvMonth = itemView.findViewById(R.id.tv_month);
            tvMonthTotal = itemView.findViewById(R.id.tv_month_total);
            tvCategoryCount = itemView.findViewById(R.id.tv_category_count);
            ivArrow = itemView.findViewById(R.id.iv_arrow);
        }
    }
}
