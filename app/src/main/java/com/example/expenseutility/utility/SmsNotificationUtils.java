package com.example.expenseutility.utility;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.expenseutility.ExpenseInputActivity;
import com.example.expenseutility.R;
import com.example.expenseutility.notification.NotificationActionReceiver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsNotificationUtils {
    public static final String CHANNEL_ID = "expense_reminder_channel";



    public static void showInputNotification(Context context, String message, int id) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(id); // cancel if already shown, ensures no stacking
        double amount = parseAmount(message); // e.g., extract "100.00"
        String dateTime = parseDateTime(message); // extract date and time

        Intent inputIntent = new Intent(context, ExpenseInputActivity.class);
        inputIntent.putExtra("amount", amount);
        inputIntent.putExtra("dateTime", dateTime);
        inputIntent.putExtra("notificationId", id);
        inputIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, id, inputIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);


        // Ignore action intent
        Intent ignoreIntent = new Intent(context, NotificationActionReceiver.class);
        ignoreIntent.setAction("ACTION_IGNORE");
        ignoreIntent.putExtra("amount", amount);
        ignoreIntent.putExtra("dateTime", dateTime);
        ignoreIntent.putExtra("notificationId", id);
        PendingIntent ignorePendingIntent = PendingIntent.getBroadcast(
                context, id, ignoreIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);



        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("New Transaction Detected")
                .setContentText("Tap to add category/particulars and save")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setSmallIcon(R.drawable.messages_communication_svgrepo_com)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .addAction(R.drawable.piggybank_pig_svgrepo_com, "Ignore", ignorePendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        createNotificationChannel(manager);

        NotificationManagerCompat.from(context).notify(id, builder.build());
    }

    private static void createNotificationChannel(NotificationManager manager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Expense Reminder", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }
    }

    public static double parseAmount(String msg) {
        try {
            Pattern[] patterns = new Pattern[] {
                    Pattern.compile("(?i)Debit INR\\s*([0-9,]+\\.\\d{2})"),            // Debit INR 500.00
                    Pattern.compile("(?i)INR\\s*([0-9,]+\\.\\d{2})\\s*debited"),       // INR 5000.00 debited
                    Pattern.compile("(?i)debited.*INR\\s*([0-9,]+\\.\\d{2})"),         // debited ... INR xxx.xx
//                    Pattern.compile("(?i)INR\\s*([0-9,]+\\.\\d{2})"),                  // generic INR xxx.xx
                    Pattern.compile("(?i)INR\\s*([0-9,]+)\\s*$")                       // fallback for whole numbers: INR 664
            };

            for (Pattern pattern : patterns) {
                Matcher matcher = pattern.matcher(msg);
                if (matcher.find()) {
                    return Double.parseDouble(matcher.group(1).replace(",", ""));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String parseDateTime(String msg) {
        try {
            Pattern[] patterns = new Pattern[] {
                    Pattern.compile("(\\d{2}-\\d{2}-\\d{2,4}),?\\s*(\\d{2}:\\d{2}:\\d{2})"),  // 07-06-25, 10:43:24 or 03-06-25 18:57:13
            };

            for (Pattern pattern : patterns) {
                Matcher matcher = pattern.matcher(msg);
                if (matcher.find()) {
                    String datePart = matcher.group(1).replace(",", "").trim();
                    String timePart = matcher.group(2).trim();
                    return datePart + " " + timePart;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


}

