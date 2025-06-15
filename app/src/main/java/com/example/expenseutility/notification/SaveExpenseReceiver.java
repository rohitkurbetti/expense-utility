package com.example.expenseutility.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class SaveExpenseReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String particulars = intent.getStringExtra("particulars");
        String category = intent.getStringExtra("category");
        double amount = intent.getDoubleExtra("amount", 0);
        String dateTime = intent.getStringExtra("dateTime");

//        ExpenseDBHelper dbHelper = new ExpenseDBHelper(context);
//        dbHelper.insertExpense(particulars, category, amount, dateTime);

        Toast.makeText(context, "Insert expense called", Toast.LENGTH_SHORT).show();

        Toast.makeText(context, "Expense saved", Toast.LENGTH_SHORT).show();
    }
}

