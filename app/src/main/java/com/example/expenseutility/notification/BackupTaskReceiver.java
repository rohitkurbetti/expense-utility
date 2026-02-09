package com.example.expenseutility.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.expenseutility.R;
import com.example.expenseutility.database.DatabaseHelper;
import com.example.expenseutility.emailutility.EmailSender;
import com.example.expenseutility.entityadapter.ExpenseItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BackupTaskReceiver extends BroadcastReceiver {

    private static final String TAG = "BackupTaskReceiver";
    private static final String BACKUP_CHANNEL_ID = "BACKUP_TASK_CHANNEL";
    private static final int BACKUP_NOTIFICATION_ID = 1001;
    private static final int ERROR_NOTIFICATION_ID = 1002;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Backup task triggered at: " + new Date());

        // Create notification channel first
        createNotificationChannel(context);

        // Create a background thread to execute the backup
        new Thread(() -> {
            try {
                // Show starting notification
                showNotification(context,
                        "📱 Daily Backup Started",
                        "Exporting expense data to CSV...",
                        BACKUP_NOTIFICATION_ID
                );

                // Step 1: Export CSV
                String result = exportCSVBackground(context);

                // Show CSV export completion
                showNotification(context,
                        "✅ CSV Exported",
                        "CSV file created successfully. Preparing to send email...",
                        BACKUP_NOTIFICATION_ID
                );

                // Step 2: Send email with backup
                sendBackupEmailBackground(context);

                // Show final success notification
                showNotification(context,
                        "✅ Backup Completed",
                        "Daily backup completed successfully!\n\n" + result,
                        BACKUP_NOTIFICATION_ID
                );

                // Show toast on main thread
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(context, "✓ Daily backup completed", Toast.LENGTH_SHORT).show();
                });

                Log.i(TAG, "Backup completed successfully");

            } catch (Exception e) {
                Log.e(TAG, "Backup task failed: " + e.getMessage(), e);

                // Show error notification
                showNotification(context,
                        "❌ Backup Failed",
                        "Backup failed: " + e.getMessage(),
                        ERROR_NOTIFICATION_ID
                );

                // Show error toast
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(context, "✗ Backup failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String exportCSVBackground(Context context) throws IOException {
        DatabaseHelper db = new DatabaseHelper(context);
        Cursor sqlRows = db.getAllExpenseData();
        List<ExpenseItem> expenses = new ArrayList<>();

        if (sqlRows != null && sqlRows.getCount() > 0) {
            String fileName = "DailyBackup_" +
                    new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date()) + ".csv";

            // Create Backups directory in Downloads
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File backupDir = new File(downloadsDir, "Backups");
            if (!backupDir.exists()) {
                boolean created = backupDir.mkdirs();
                if (!created) {
                    throw new IOException("Failed to create backup directory");
                }
            }

            File csvFile = new File(backupDir, fileName);

            FileOutputStream fileOutputStream = new FileOutputStream(csvFile);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);

            outputStreamWriter.write("ID,ExpenseCategory,Particulars,Amount,DateTime,Date,Description,IsHomeExpense\n");

            int rowCount = 0;
            while (sqlRows.moveToNext()) {
                int id = sqlRows.getInt(0);
                String expCat = sqlRows.getString(1);
                String pert = sqlRows.getString(2);
                int amt = sqlRows.getInt(3);
                String dtm = sqlRows.getString(4);
                String dt = sqlRows.getString(5);
                String encodedPartDetails = sqlRows.getString(8);
                encodedPartDetails = (encodedPartDetails == null || encodedPartDetails.trim().isEmpty()) ?
                        "-" : encodedPartDetails.trim();
                boolean isHomeExpense = sqlRows.getInt(9) == 1;

                String row = id + "," + expCat + "," + pert + "," + amt + "," +
                        dtm + "," + dt + "," + encodedPartDetails + "," + isHomeExpense + "\n";
                outputStreamWriter.write(row);
                expenses.add(new ExpenseItem(id, pert, (long) amt, dtm, expCat, null, null, encodedPartDetails, isHomeExpense));
                rowCount++;
            }

            outputStreamWriter.close();
            fileOutputStream.close();
            sqlRows.close();
            db.close();

            // Calculate summary
            Map<String, Double> categorySummary = calculateCategorySummary(expenses);
            double totalAmount = calculateTotalAmount(expenses);

            DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
            StringBuilder summary = new StringBuilder();
            summary.append("• ").append(rowCount).append(" transactions backed up\n");
            summary.append("• Total amount: ₹").append(decimalFormat.format(totalAmount)).append("\n");

            if (!categorySummary.isEmpty()) {
                summary.append("• Categories: ");
                int count = 0;
                for (String category : categorySummary.keySet()) {
                    if (count > 0) summary.append(", ");
                    summary.append(category);
                    count++;
                    if (count >= 3) { // Show only first 3 categories
                        summary.append("...");
                        break;
                    }
                }
            }

            Log.i(TAG, "CSV exported: " + csvFile.getAbsolutePath() + ", Rows: " + rowCount);
            return summary.toString();

        } else {
            Log.w(TAG, "No data found in database for backup");
            return "No expense data found to backup";
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendBackupEmailBackground(Context context) {
        try {
            // Wait a bit to ensure file is written
            Thread.sleep(500);

            // Find the backup directory
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File backupDir = new File(downloadsDir, "Backups");

            if (!backupDir.exists() || !backupDir.isDirectory()) {
                Log.w(TAG, "Backup directory not found");
                return;
            }

            // Find today's backup file
            String todayPrefix = "DailyBackup_" +
                    new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
            File[] backupFiles = backupDir.listFiles((dir, name) ->
                    name.startsWith(todayPrefix) && name.endsWith(".csv"));

            if (backupFiles == null || backupFiles.length == 0) {
                Log.w(TAG, "No backup file found for today");
                return;
            }

            File backupFile = backupFiles[0];

            if (!backupFile.exists()) {
                Log.w(TAG, "Backup file doesn't exist: " + backupFile.getAbsolutePath());
                return;
            }

            // Prepare email details
            LocalDateTime localDateTime = LocalDateTime.now();
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss a");

            String subject = "Daily Expense Backup - " + dateTimeFormatter.format(localDateTime);
            String body = "Automated daily backup of expense data.\n\n" +
                    "File: " + backupFile.getName() + "\n" +
                    "Size: " + (backupFile.length() / 1024) + " KB\n" +
                    "Time: " + dateTimeFormatter.format(localDateTime) + "\n\n" +
                    "This is an automated email from Expense Utility app.";

            // Send email
            new EmailSender(
                    "rohitbackup47@gmail.com",
                    subject,
                    body,
                    backupFile,
                    context
            ).execute();

            Log.i(TAG, "Backup email sent: " + backupFile.getName());

        } catch (Exception e) {
            Log.e(TAG, "Failed to send backup email: " + e.getMessage(), e);
            throw new RuntimeException("Email sending failed: " + e.getMessage(), e);
        }
    }

    private double calculateTotalAmount(List<ExpenseItem> expenses) {
        double total = 0;
        for (ExpenseItem expense : expenses) {
            total += expense.getExpenseAmount();
        }
        return total;
    }

    private Map<String, Double> calculateCategorySummary(List<ExpenseItem> expenses) {
        Map<String, Double> categoryMap = new HashMap<>();
        for (ExpenseItem expense : expenses) {
            String category = expense.getExpenseCategory();
            double amount = expense.getExpenseAmount();
            categoryMap.put(category, categoryMap.getOrDefault(category, 0.0) + amount);
        }
        return categoryMap;
    }

    private void showNotification(Context context, String title, String message, int notificationId) {
        try {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, BACKUP_CHANNEL_ID)
                    .setSmallIcon(R.drawable.money_svgrepo_com__1_)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message));

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

            // For Android 13+, check if notifications are enabled
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (notificationManager.areNotificationsEnabled()) {
                    notificationManager.notify(notificationId, builder.build());
                    Log.i(TAG, "Notification shown: " + title);
                } else {
                    Log.w(TAG, "Notifications are disabled for this app");
                    // You might want to schedule a job or retry later
                }
            } else {
                notificationManager.notify(notificationId, builder.build());
                Log.i(TAG, "Notification shown: " + title);
            }

        } catch (Exception e) {
            Log.e(TAG, "Failed to show notification: " + e.getMessage(), e);
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Backup Alarms";
            String description = "Notifications for automated backup tasks";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(
                    "BACKUP_TASK_CHANNEL",
                    name,
                    importance
            );
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}