package com.example.expenseutility.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.expenseutility.R;
import com.example.expenseutility.database.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        double totalExpense = dbHelper.getTotalExpenseForToday();

        // Create a notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "expense_channel";
        CharSequence channelName = "Expense Notifications";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        // Create an intent for the dismiss action
        Intent dismissIntent = new Intent(context, DismissNotificationReceiver.class);
        dismissIntent.putExtra("notification_id", 1);
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                dismissIntent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ? PendingIntent.FLAG_IMMUTABLE : 0
        );

        String dateFrmt = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        String dataTxt = "Total Expense: \u20B9" + totalExpense+"\n" +
                "Average spent (day income) "+String.format("%.2f",(totalExpense/(60000/30))*100)+"%\n"+
                "Average spent (monthly income) "+String.format("%.2f",(totalExpense/60000)*100)+"%";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.money_svgrepo_com__1_) // Add your own icon here
                .setContentTitle("Today's Total Expense")
                .setSubText(dateFrmt)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(dataTxt))
                .addAction(R.drawable.ic_launcher_foreground, "Close", dismissPendingIntent); // Add dismiss action button

        notificationManager.notify(1, builder.build());
    }
}

