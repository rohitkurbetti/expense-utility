package com.example.expenseutility.entityadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.expenseutility.R;
import com.example.expenseutility.dto.Transaction1;

import java.util.List;

public class TransactionAdapter1 extends ArrayAdapter<Transaction1> {
    private Context context;
    private List<Transaction1> transactionList;
    private LayoutInflater inflater;
    private CheckBox headerCheckBox;

    public TransactionAdapter1(Context context, List<Transaction1> transactions, CheckBox headerCheckBox) {
        super(context, 0, transactions);
        this.context = context;
        this.transactionList = transactions;
        this.inflater = LayoutInflater.from(context);
        this.headerCheckBox = headerCheckBox;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.transaction_item, parent, false);
            holder = new ViewHolder();

            // Initialize views
            holder.serialNoTextView = convertView.findViewById(R.id.tv_serial_no);
            holder.dateTextView = convertView.findViewById(R.id.tv_date);
            holder.particularsTextView = convertView.findViewById(R.id.tv_particulars);
            holder.debitTextView = convertView.findViewById(R.id.tv_debit);
            holder.categoryTextView = convertView.findViewById(R.id.tv_category);
            holder.checkBox = convertView.findViewById(R.id.cb_select);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Transaction1 transaction = transactionList.get(position);

        // Set data to views - position + 1 for serial number starting from 1
        holder.serialNoTextView.setText(String.valueOf(position + 1));
        holder.dateTextView.setText(transaction.getFormattedDate());
        holder.particularsTextView.setText(transaction.getParsedParticulars());
        holder.debitTextView.setText(transaction.getDebit());
        holder.categoryTextView.setText(transaction.getCategory());
        holder.checkBox.setChecked(transaction.isChecked());

        // Set checkbox listener
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                transaction.setChecked(isChecked);
                updateHeaderCheckboxState();
            }
        });

        return convertView;
    }

    public List<Transaction1> getTransactionList() {
        return transactionList;
    }

    public void selectAll(boolean selectAll) {
        for (Transaction1 transaction : transactionList) {
            transaction.setChecked(selectAll);
        }
        notifyDataSetChanged();
    }

    private void updateHeaderCheckboxState() {
        if (headerCheckBox == null) return;

        boolean allChecked = true;
        for (Transaction1 transaction : transactionList) {
            if (!transaction.isChecked()) {
                allChecked = false;
                break;
            }
        }

        headerCheckBox.setChecked(allChecked);
    }

    private static class ViewHolder {
        TextView serialNoTextView;
        TextView dateTextView;
        TextView particularsTextView;
        TextView debitTextView;
        TextView categoryTextView;
        CheckBox checkBox;
    }
}