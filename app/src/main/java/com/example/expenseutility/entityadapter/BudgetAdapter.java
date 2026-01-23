package com.example.expenseutility.entityadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.expenseutility.R;
import com.example.expenseutility.dto.Budget;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class BudgetAdapter extends ArrayAdapter<Budget> {

    private Context context;
    private List<Budget> budgetList;
    private LayoutInflater inflater;

    public BudgetAdapter(@NonNull Context context, List<Budget> budgetList) {
        super(context, R.layout.list_item_budget, budgetList);
        this.context = context;
        this.budgetList = budgetList;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_budget, parent, false);
            holder = new ViewHolder();
            holder.tvMonthYear = convertView.findViewById(R.id.tvMonthYear);
            holder.tvBudgetAmount = convertView.findViewById(R.id.tvBudgetAmount);
            holder.tvMonthNumber = convertView.findViewById(R.id.tvMonthNumber);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Budget budget = budgetList.get(position);

        // Format month with leading zero if needed
        String monthStr = String.format("%02d", budget.getMonth());

        holder.tvMonthYear.setText(budget.getMonthName() + " " + budget.getYear());
        holder.tvMonthNumber.setText(monthStr + "/" + budget.getYear());

        // Format budget amount
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        String formattedBudget = format.format(budget.getBudget());
        holder.tvBudgetAmount.setText(formattedBudget);

        return convertView;
    }

    static class ViewHolder {
        TextView tvMonthYear;
        TextView tvBudgetAmount;
        TextView tvMonthNumber;
    }
}
