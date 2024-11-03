package com.example.expenseutility.entityadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.expenseutility.R;

import java.util.List;

public class FirebaseExpenseAdapter extends BaseAdapter {

    private Context context;
    private List<ExpenseItem> expenseList;

    public FirebaseExpenseAdapter(Context context, List<ExpenseItem> expenseList) {
        this.context = context;
        this.expenseList = expenseList;
    }



    @Override
    public int getCount() {
        return expenseList.size();
    }

    @Override
    public Object getItem(int position) {
        return expenseList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.firebase_list_item, parent, false);
        }

        ExpenseItem expense = (ExpenseItem) getItem(position);

        TextView textViewCounter = convertView.findViewById(R.id.textViewCounter);
        TextView pertTextView = convertView.findViewById(R.id.pertTextView);
        TextView categoryTextView = convertView.findViewById(R.id.categoryTextView);
        TextView amountTextView = convertView.findViewById(R.id.amountTextView);
        TextView dateTextView = convertView.findViewById(R.id.dateTextView);
        TextView dailyIncPerTextView = convertView.findViewById(R.id.dailyIncPerTextView);
        TextView monthlyIncPerTextView = convertView.findViewById(R.id.monthlyIncPerTextView);

        textViewCounter.setText("#"+(position+1));
        pertTextView.setText(expense.getExpenseParticulars());
        categoryTextView.setText(expense.getExpenseCategory());
        amountTextView.setText(String.valueOf("\u20B9"+expense.getExpenseAmount()));
        dateTextView.setText(expense.getExpenseDate());

        populateExpByDailyAndMontlyIncome(expense.getExpenseAmount(), dailyIncPerTextView, monthlyIncPerTextView);


        return convertView;
    }

    private void populateExpByDailyAndMontlyIncome(Long expenseAmount, TextView dailyIncPerTextView, TextView monthlyIncPerTextView) {

        double expPercentDaily = (double) expenseAmount / (62000 / 30);
        double expPercentMonthly = (double) expenseAmount / 62000;

        dailyIncPerTextView.setText(String.format("(%.2f%%)",expPercentDaily*100));
        monthlyIncPerTextView.setText(String.format("(%.2f%%)",expPercentMonthly*100));

    }
}
