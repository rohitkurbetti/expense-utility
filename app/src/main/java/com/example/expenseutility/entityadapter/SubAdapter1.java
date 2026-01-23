package com.example.expenseutility.entityadapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expenseutility.R;
import com.example.expenseutility.dto.SubItem1;

import java.util.List;

public class SubAdapter1 extends RecyclerView.Adapter<SubAdapter1.ViewHolder> {

    private List<SubItem1> subItems;

    public SubAdapter1(List<SubItem1> subItems) {
        this.subItems = subItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sub_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SubItem1 item = subItems.get(position);
        holder.tvSubName.setText(item.getName());
        holder.tvDescription.setText(item.getDescription());

        holder.itemView.setOnClickListener(v -> {
            // Handle sub-item click if needed
        });
    }

    @Override
    public int getItemCount() {
        return subItems.size();
    }

    public void updateList(List<SubItem1> newList) {
        subItems = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubName;
        TextView tvDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubName = itemView.findViewById(R.id.tvSubName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
        }
    }
}
