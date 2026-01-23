package com.example.expenseutility.entityadapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expenseutility.R;
import com.example.expenseutility.dto.YearData;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class YearAdapter extends RecyclerView.Adapter<YearAdapter.ViewHolder> {

    private List<YearData> yearList;
    private OnYearClickListener listener;
    private NumberFormat currencyFormat;

    public YearAdapter(List<YearData> yearList, OnYearClickListener listener) {
        this.yearList = yearList;
        this.listener = listener;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_year, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        YearData yearData = yearList.get(position);

        holder.tvYear.setText(yearData.getYear());
        holder.tvYearTotal.setText(currencyFormat.format(yearData.getTotalAmount()));

        int monthCount = yearData.getMonths() != null ? yearData.getMonths().size() : 0;
        holder.tvMonthCount.setText(monthCount + " months");

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
                    listener.onYearClick(yearData);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return yearList.size();
    }

    public interface OnYearClickListener {
        void onYearClick(YearData yearData);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvYear, tvYearTotal, tvMonthCount;
        ImageView ivArrow;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            tvYear = itemView.findViewById(R.id.tv_year);
            tvYearTotal = itemView.findViewById(R.id.tv_year_total);
            tvMonthCount = itemView.findViewById(R.id.tv_month_count);
            ivArrow = itemView.findViewById(R.id.iv_arrow);
        }
    }
}