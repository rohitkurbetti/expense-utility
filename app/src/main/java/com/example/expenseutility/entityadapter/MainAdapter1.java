package com.example.expenseutility.entityadapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expenseutility.R;
import com.example.expenseutility.dto.MainItem1;

import java.util.List;

public class MainAdapter1 extends RecyclerView.Adapter<MainAdapter1.ViewHolder> {

    private List<MainItem1> mainItems;
    private OnItemClickListener listener;
    private int selectedPosition = -1;

    public MainAdapter1(List<MainItem1> mainItems, OnItemClickListener listener) {
        this.mainItems = mainItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_main_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MainItem1 item = mainItems.get(position);

        holder.tvTitle.setText(item.getTitle());
        holder.tvSubCount.setText(item.getSubItems().size() + " sub-items");

        // Highlight selected item
        if (selectedPosition == position) {
            holder.itemView.setBackgroundColor(Color.parseColor("#E3F2FD"));
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        holder.itemView.setOnClickListener(v -> {
            // Update selected position
            int previousPosition = selectedPosition;
            selectedPosition = position;

            // Notify item change for previous and current
            if (previousPosition != -1) {
                notifyItemChanged(previousPosition);
            }
            notifyItemChanged(selectedPosition);

            // Trigger click listener
            if (listener != null) {
                listener.onItemClick(item, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mainItems.size();
    }

    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(MainItem1 item, int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvSubCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSubCount = itemView.findViewById(R.id.tvSubCount);
        }
    }
}
