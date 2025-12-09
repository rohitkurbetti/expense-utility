package com.example.expenseutility.entityadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.example.expenseutility.R;

import java.util.ArrayList;
import java.util.List;

public class SuggestionV2Adapter extends ArrayAdapter<String> implements Filterable {
    private List<String> originalItems;
    private List<String> filteredItems;
    private LayoutInflater inflater;
    private CustomFilter filter;

    public SuggestionV2Adapter(@NonNull Context context, @NonNull List<String> items) {
        super(context, 0, new ArrayList<>(items)); // Use mutable ArrayList
        this.originalItems = new ArrayList<>(items); // Ensure mutable list
        this.filteredItems = new ArrayList<>(items);
        this.inflater = LayoutInflater.from(context);
    }

    public void updateSuggestionList(List<String> updatedList) {
        originalItems.clear();
        originalItems.addAll(updatedList);
        filteredItems.clear();
        filteredItems.addAll(updatedList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_suggestion, parent, false);
            holder = new ViewHolder();
            holder.cardView = convertView.findViewById(R.id.card_view);
            holder.textView = convertView.findViewById(R.id.suggestion_text);
            holder.icon = convertView.findViewById(R.id.icon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String item = getItem(position);
        if (item != null) {
            holder.textView.setText(item);
        }

        return convertView;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new CustomFilter();
        }
        return filter;
    }

    // Override clear method to make it safe
    @Override
    public void clear() {
        // Do nothing or clear only if needed
        // This prevents the UnsupportedOperationException
    }

    private static class ViewHolder {
        CardView cardView;
        TextView textView;
        ImageView icon;
    }

    private class CustomFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            List<String> filtered = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filtered.addAll(originalItems);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (String item : originalItems) {
                    if (item.toLowerCase().contains(filterPattern)) {
                        filtered.add(item);
                    }
                }
            }

            results.values = filtered;
            results.count = filtered.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            // Clear the existing data safely
            SuggestionV2Adapter.super.clear();

            // Add the filtered results
            if (results.values != null) {
                @SuppressWarnings("unchecked")
                List<String> filteredList = (List<String>) results.values;
                SuggestionV2Adapter.super.addAll(filteredList);
            }

            notifyDataSetChanged();
        }
    }
}