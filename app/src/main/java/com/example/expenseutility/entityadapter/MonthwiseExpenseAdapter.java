package com.example.expenseutility.entityadapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.expenseutility.R;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MonthwiseExpenseAdapter extends BaseAdapter {
    private Context context;
    private List<String> months;
    private HashMap<String, List<ExpenseItem>> transactionsMap;
    private HashMap<Integer, Boolean> expandedStates;
    ListView expenseDetailsListView;
    CopyOnWriteArrayList<ExpenseItem> expenseItems;
    ExpenseDetailsAdapter expenseDetailsAdapter;


    public MonthwiseExpenseAdapter(Context context, List<String> months, HashMap<String, List<ExpenseItem>> transactionsMap,
                                   ExpenseDetailsAdapter expenseDetailsAdapter, ListView expenseDetailsListView, CopyOnWriteArrayList<ExpenseItem> expenseItems) {
        this.context = context;
        this.months = months;
        this.transactionsMap = transactionsMap;
        this.expandedStates = new HashMap<>();
        this.expenseDetailsAdapter = expenseDetailsAdapter;
        this.expenseDetailsListView = expenseDetailsListView;
        this.expenseItems = expenseItems;

    }

    @Override
    public int getCount() {
        return months.size();
    }

    @Override
    public Object getItem(int position) {
        return months.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_test, parent, false);
        }

        TextView monthTextView = convertView.findViewById(R.id.monthTextView);
        ImageView chevronIcon = convertView.findViewById(R.id.chevronIcon);
        LinearLayout transactionsLayout = convertView.findViewById(R.id.transactionsLayout);

        String month = months.get(position);
        monthTextView.setText(month);

        // Populate transactions for the month
        transactionsLayout.removeAllViews();
        List<ExpenseItem> transactions = transactionsMap.get(month);
        if (transactions != null) {
            for (ExpenseItem transaction : transactions) {
                TextView transactionView = new TextView(context);
                String transactionDetails = transaction.getExpenseCategory() + " - ₹" + transaction.getExpenseAmount() + " (" + transaction.getExpenseDate() + ")";
                transactionView.setText(transactionDetails);
                transactionView.setTextSize(16f);
                transactionView.setPadding(0, 5, 0, 5);
                transactionsLayout.addView(transactionView);
            }
        }

        // Set the ListView adapter
        expenseDetailsAdapter = new ExpenseDetailsAdapter(context, expenseItems);
        expenseDetailsListView.setAdapter(expenseDetailsAdapter);

        // Handle expand/collapse
        boolean isExpanded = expandedStates.getOrDefault(position, false);
        transactionsLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        chevronIcon.setImageResource(isExpanded ? R.drawable.baseline_arrow_drop_up_24 : R.drawable.baseline_arrow_drop_down_24);

        chevronIcon.setOnClickListener(v -> {
            boolean currentState = expandedStates.getOrDefault(position, false);
            expandedStates.put(position, !currentState);
            notifyDataSetChanged(); // Refresh the ListView
        });

        return convertView;
    }
}
