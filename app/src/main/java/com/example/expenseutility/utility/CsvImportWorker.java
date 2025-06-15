package com.example.expenseutility.utility;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.expenseutility.database.DatabaseHelper;
import com.example.expenseutility.entityadapter.ExpenseItem;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.stream.Stream;

public class CsvImportWorker extends Worker {
    private static final String CHANNEL_ID = "CSV_IMPORT_CHANNEL";

    public CsvImportWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        createNotificationChannel();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @NonNull
    @Override
    public Result doWork() {
        long startTime = System.currentTimeMillis();

        showNotification("CSV Import Started", "Reading and inserting expenses...");

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "expenses.csv");
        if (!file.exists()) {
            showNotification("CSV Import Failed", "CSV file not found.");
            return Result.failure();
        }
        int totalRows = getCsvRowCountWithCommonsCSV(file);

        DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
        int count = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {

                String[] tokens = line.split(",");
                if (tokens.length != 5) continue;

                ExpenseItem eex = new ExpenseItem(
                        tokens[0],
                        tokens[1],
                        tokens[2],
                        tokens[3],
                        tokens[4],
                        null,null
                );
                dbHelper.insertExpense( tokens[0], tokens[1], tokens[2], tokens[3], tokens[4],null,null, null);

                int val = (int) (((float)count/totalRows)*100);

                // Update progress every 50 records
                if (count % 1 == 0 || count == totalRows) {
                    setProgressAsync(new Data.Builder()
                            .putInt("progress", count)
                            .putInt("total", totalRows)


                            .putInt("per", val)
                            .build());
                }
                Log.i("per", String.valueOf((float) (count) + " / "+((float)count/totalRows)*100 ));
                count++;
            }
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;


            showNotification("CSV Import Complete", count + " entries inserted in "+duration+ " ms");
            return Result.success();

        } catch (Exception e) {
            e.printStackTrace();
            showNotification("CSV Import Failed", "Error: " + e.getMessage());
            return Result.failure();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showNotification(String title, String text) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_notify_more)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
//               public void onRequestPermissionsResult(int requestCode, String[] permissions,int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, getApplicationContext().getPackageName());
                getApplicationContext().startActivity(intent);
            return;
        }
        NotificationManagerCompat.from(getApplicationContext()).notify((int) System.currentTimeMillis(), builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "CSV Import Channel";
            String description = "Notifies about CSV import status";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static int getCsvRowCountWithCommonsCSV(File file) {
        int count = 0;
        try (Reader in = new FileReader(file)) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in);
            for (CSVRecord record : records) {
                count++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return count;
    }
}
