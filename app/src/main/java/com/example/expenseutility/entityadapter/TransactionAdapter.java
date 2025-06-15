package com.example.expenseutility.entityadapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.expenseutility.R;
import com.example.expenseutility.dto.Transaction;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TransactionAdapter extends ArrayAdapter<Transaction> {

    private Context context;
    private List<Transaction> transactions;

    public TransactionAdapter(Context context, List<Transaction> transactions) {
        super(context, 0, transactions);
        this.context = context;
        this.transactions = transactions;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_transaction, parent, false);
        }

        Transaction transaction = transactions.get(position);

        TextView srNoTextView = convertView.findViewById(R.id.srNoTextView);
        CheckBox checkBox = convertView.findViewById(R.id.checkBox);
        TextView date = convertView.findViewById(R.id.date);
        TextView particulars = convertView.findViewById(R.id.particulars);
        TextView amount = convertView.findViewById(R.id.debitAmount);

        srNoTextView.setText(String.valueOf(position+1));
        date.setText(transaction.getDate());

        String particularStr = extractParticular(transaction.getParticulars());

        particulars.setText(particularStr.isBlank()?"Txn":particularStr.substring(0, Math.min(15,particularStr.length())));
        amount.setText("\u20B9"+transaction.getDebitAmount());

        checkBox.setChecked(transaction.isSelected());
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> transaction.setSelected(isChecked));

        return convertView;
    }

    public static String extractParticular(String particulars) {
        // 1. Remove newlines and normalize whitespace
        String input = particulars.replaceAll("[\\n]+", " ").replaceAll(" {2,}", " ").trim();

        // 2. Consolidated regex
        String regex =
                // UPI/IMPS pattern
                "(?:UPI|IMPS)/(?:P2M|P2A)/\\d+/(.*?)/(?:UPI|Paymen|YBP|\\w{2,}\\s?\\w*)"
                        // BRN-PYMT-CARD etc.
                        + "|^([A-Z]+(?:-[A-Z]+)+)-\\d+$"
                        // ACH-DR-Groww etc.
                        + "|^([A-Z]+(?:- ?[A-Za-z]+)*?)- ?[A-Z0-9]{6,}(?:- ?[A-Z0-9]{4,})?$"
                        // CreditCard Payment
                        + "|^(CreditCard Payment)\\b";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                if (matcher.group(i) != null) {
                    return matcher.group(i).trim();
                }
            }
        }

        return "";
    }
}

