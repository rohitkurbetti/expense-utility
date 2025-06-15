package com.example.expenseutility.notification;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.expenseutility.database.DatabaseHelper;

public class NotificationActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("ACTION_IGNORE".equals(intent.getAction())) {
            double amount = intent.getDoubleExtra("amount", 0);
            String dateTime = intent.getStringExtra("dateTime");
            int notificationId = intent.getIntExtra("notificationId", 0);

            // Save to txn_ignore table
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            dbHelper.insertIgnoredTransaction(amount, dateTime);
            Toast.makeText(context, "Ignored "+amount+" Date: "+dateTime, Toast.LENGTH_SHORT).show();
            // Cancel the notification
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel(notificationId);
        }
    }
}

