package com.example.expenseutility;

import static com.example.expenseutility.ExpenseInputActivity.getFormatted;
import static com.example.expenseutility.FirstFragment.saveToFirebase;
import static com.example.expenseutility.constants.ExpenseConstants.ANN_INCOME;
import static com.example.expenseutility.utility.SmsNotificationUtils.parseAmount;
import static com.example.expenseutility.utility.SmsNotificationUtils.parseDateTime;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.MenuItemCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.expenseutility.constants.ExpenseConstants;
import com.example.expenseutility.database.BudgetHelper;
import com.example.expenseutility.database.DatabaseHelper;
import com.example.expenseutility.database.TaskDatabaseHelper;
import com.example.expenseutility.databinding.ActivityMainBinding;
import com.example.expenseutility.dto.Transaction;
import com.example.expenseutility.dto.TxnIgnoreDto;
import com.example.expenseutility.emailutility.EmailSender;
import com.example.expenseutility.emailutility.NotificationUtils;
import com.example.expenseutility.entityadapter.ExpenseItem;
import com.example.expenseutility.entityadapter.TransactionAdapter;
import com.example.expenseutility.notification.BackupTaskReceiver;
import com.example.expenseutility.notification.DismissNotificationReceiver;
import com.example.expenseutility.notification.NotificationReceiver;
import com.example.expenseutility.utility.SmsNotificationUtils;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {
    private static final int BACKUP_REQUEST_CODE = 9999;
    private static final String BACKUP_ALARM_CHANNEL_ID = "backup_alarm_channel";
    private static final int REQUEST_MANAGE_STORAGE = 123;
    private static final int REQUEST_WRITE_STORAGE = 112;
    private static final int REQUEST_CODE_NOTIFICATION = 2001;
    private static final int SMS_PERMISSION_CODE = 101;
    SharedPreferences sharedPreferences;
    Spinner themeSpinner;
    String[] themes = {
            "Red", "Blue", "Green", "GreenParrot", "Purple", "Orange",
            "Teal", "Pink", "Cyan", "Lime", "Brown",
            "Mint", "Coral", "Steel", "Lavender", "Mustard"
    };
    private AppBarConfiguration appBarConfiguration;
    private DatabaseReference database;
    private ActivityMainBinding binding;
    private DatabaseHelper db;
    private List<Transaction> transactionList;
    private TaskDatabaseHelper taskDbHelper;
    private MenuItem tasksMenuItem;
    private TextView badgeTextView;
    private View badgeActionView;

    public void reCalculateDailyLimit(String fromOnResume) {
        FirstFragment.showDailyLimitPB();
        new Thread(() -> {

            try {
                long startTime = System.nanoTime();

                DatabaseHelper databaseHelper = DatabaseHelper.getInstance(this);
                BudgetHelper budgetHelper = BudgetHelper.getInstance(this);
                int monthExpenses = (int) databaseHelper.getTotalExpenseForCurrentMonth();

                int year = Calendar.getInstance().get(Calendar.YEAR);
                int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
                int budget = budgetHelper.getBudget(year, month);

                int remainingBalance = budget - monthExpenses;

                Log.i("budget", "" + budget);
                Log.i("monthExpenses", "" + monthExpenses);
                Log.i("remainingBalance", "" + remainingBalance);

                int remDays = BudgetActivity.getDaysRemainingInMonth();
                remDays = remDays == 0 ? 1 : remDays;
                Log.i("remainingDays", "" + remDays);

                int dailyLimit = remainingBalance / remDays;

                Log.i("final", "\u20B9" + dailyLimit);

                // Update UI on the main thread
                runOnUiThread(() -> {
                    FirstFragment.updateLimitText(String.valueOf(dailyLimit));
//                    Toast.makeText(this, "Ctx " + this.getApplicationContext().getPackageName(), Toast.LENGTH_SHORT).show();
                });

                long endTime = System.nanoTime();
                long durationNano = endTime - startTime;
                double durationMs = durationNano / 1_000_000.0;
                Log.d("Performance", fromOnResume + " Method execution time: " + durationMs + " ms");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void requestSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS},
                    SMS_PERMISSION_CODE);
        } else {
            readExistingSms();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void readExistingSms() {

        long currentTimeMillis = System.currentTimeMillis();
        long threeDaysAgoMillis = currentTimeMillis - (3L * 24 * 60 * 60 * 1000); // 3 days in millis


        Uri uriSms = Uri.parse("content://sms/inbox");
        String selection = "date >= ?";
        String[] selectionArgs = {String.valueOf(threeDaysAgoMillis)};
        Cursor cursor = getContentResolver().query(uriSms, null, selection, selectionArgs, "date DESC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
//                String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
//                long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("date"));
//                String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
//                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
//                String seen = cursor.getString(cursor.getColumnIndexOrThrow("seen"));
//                String read = cursor.getString(cursor.getColumnIndexOrThrow("read"));
//                String date_sent = cursor.getString(cursor.getColumnIndexOrThrow("date_sent"));
//                String person = cursor.getString(cursor.getColumnIndexOrThrow("creator"));

//                if (body.contains("Debit INR")
//                        || body.contains("Debited INR") || body.contains("XX7794")
//                ) {
//                    if (!TransactionQueue.getInstance(this).getMessages().contains(body)) {
//                        TransactionQueue.getInstance(this).getMessages().add(body);
                // Show/update reminder notification


                double amount = parseAmount(body); // e.g., extract "100.00"
                String dateTime = parseDateTime(body); // extract date and time
                int notificationId = (amount + dateTime).hashCode(); // consistent ID

                if (amount > 0.0d && !dateTime.isEmpty()) {

                    if (db == null) {
                        db = new DatabaseHelper(this);
                    }

                    if (!db.isTransactionIgnored(amount, dateTime)) {
                        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yy HH:mm:ss");
                        DateTimeFormatter outputDateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

                        LocalDateTime parsedDateTime = LocalDateTime.parse(dateTime, dateTimeFormatter);

                        StringBuilder dateTimeStr = new StringBuilder();


                        String month = getFormatted(parsedDateTime.getMonthValue());
                        String day = getFormatted(parsedDateTime.getDayOfMonth());
                        String hour = getFormatted(parsedDateTime.getHour());
                        String minute = getFormatted(parsedDateTime.getMinute());
                        String second = getFormatted(parsedDateTime.getSecond());

                        dateTimeStr.append(parsedDateTime.getYear());
                        dateTimeStr.append("-");
                        dateTimeStr.append(month);
                        dateTimeStr.append("-");
                        dateTimeStr.append(day);
                        dateTimeStr.append(" ");
                        dateTimeStr.append(hour);
                        dateTimeStr.append(":");
                        dateTimeStr.append(minute);

                        String dateStr = parsedDateTime.getYear() + "-" + month + "-" + day;


                        //check if record exists in db
                        boolean isExists = db.checkIfExistsMinPossibleParams((int) amount, dateTimeStr.toString(), dateStr);

                        if (!isExists) {
                            SmsNotificationUtils.showInputNotification(this, body, (int) notificationId);
                        }
                    }


                }
//                    }
//                }

            }
            cursor.close();


        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIncomingIntent(intent);
    }

    private void handleIncomingIntent(Intent intent) {
        if (intent != null) {
            StringBuilder receivedData = new StringBuilder();

            // Check for different types of data
            if (intent.hasExtra("DATA_KEY")) {
                String data = intent.getStringExtra("DATA_KEY");
                receivedData.append("Data: ").append(data).append("\n");
            }

            if (intent.hasExtra("TIMESTAMP")) {
                long timestamp = intent.getLongExtra("TIMESTAMP", 0);
                receivedData.append("Timestamp: ").append(new Timestamp(timestamp)).append("\n");
            }

            if (intent.hasExtra("SENDER_PACKAGE")) {
                String sender = intent.getStringExtra("SENDER_PACKAGE");
                receivedData.append("Sender: ").append(sender).append("\n");
            }

            if (intent.hasExtra(Intent.EXTRA_TEXT)) {
                String extraText = intent.getStringExtra(Intent.EXTRA_TEXT);
                receivedData.append("Extra Text: ").append(extraText).append("\n");
            }

            if (intent.getAction() != null) {
                receivedData.append("Action: ").append(intent.getAction()).append("\n");
            }

            if (intent.getData() != null) {
                receivedData.append("URI: ").append(intent.getData().toString()).append("\n");
            }

            if (receivedData.length() > 0) {

                Toast.makeText(this, "Data received successfully!" + receivedData, Toast.LENGTH_SHORT).show();
                Log.i("DDD", "" + receivedData);


                // Create and show the dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("📬 Data Received from Another App")
                        .setMessage(receivedData.toString())
                        .setPositiveButton("Process Data", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                processReceivedData(intent);
                                dialog.dismiss();
                            }
                        })
//                        .setNegativeButton("View in App", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                // Also update the main text view
////                                updateMainTextView(dialogMessage.toString());
//                                dialog.dismiss();
//                            }
//                        })
                        .setNeutralButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setCancelable(false)
                        .show();


                // Process the received data
                processReceivedData(intent);
            }
        }
    }

    private void processReceivedData(Intent intent) {
        // Add your business logic here to handle the received data
        String action = intent.getAction();

        if ("com.example.expenseutility.ACTION_PROCESS_DATA".equals(action)) {
            // Handle custom action
            String data = intent.getStringExtra("DATA_KEY");
            String priority = intent.getStringExtra("PRIORITY");

            // Perform actions based on the data
            if ("HIGH".equals(priority)) {
                // Handle high priority data
                performHighPriorityTask(data);
            }
        }

        // You can also send a result back if needed
        sendResultBackToSender();
    }

    private void sendResultBackToSender() {
        // If you need to send data back to the sender
        Intent resultIntent = new Intent();
        resultIntent.setAction("com.example.sendingapp.ACTION_RESULT");
        resultIntent.putExtra("RESULT", "Data processed successfully");
        resultIntent.putExtra("PROCESSED_AT", System.currentTimeMillis());

        // Note: This is a broadcast, you might need a different approach
        // for actual two-way communication
        sendBroadcast(resultIntent);
    }

    private void performHighPriorityTask(String data) {
        // Implement your high priority task logic here
        Toast.makeText(this, "Processing high priority: " + data, Toast.LENGTH_LONG).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyUserTheme();  // Apply before setContentView
        super.onCreate(savedInstanceState);
        requestSmsPermission();
// Handle incoming intent
//        handleIncomingIntent(getIntent());
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        db = new DatabaseHelper(this);

        // Initialize database helper
        taskDbHelper = new TaskDatabaseHelper(this);

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().getReference();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            checkManageStoragePermission();
        } else {
            checkStoragePermission();
        }

//        createNotificationChannelBkgrndImport();
        requestNotificationPermission();

        String month = new SimpleDateFormat("yyyy-MM").format(new Date());

        List<ExpenseItem> expenseItemList = db.getMonthData(month);

        Log.i("expenseItemList >> ", expenseItemList.toString());


        float income = sharedPreferences.getFloat("monthlyIncome", ANN_INCOME);

        if (income == 0.0f) {
            setGlobalIncome();
        }


//        Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
//                .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
//        startActivity(intent);


//        scheduleCsvImport(this);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

    }

    private void applyUserTheme() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = prefs.getString("app_theme", "Theme.ExpenseUtility");

        switch (theme) {
            case "Default":
                setTheme(R.style.Base_Theme_ExpenseUtility);
                break;
            case "Red":
                setTheme(R.style.AppTheme_Red);
                break;
            case "Blue":
                setTheme(R.style.AppTheme_Blue);
                break;
            case "Green":
                setTheme(R.style.AppTheme_Green);
                break;
            case "GreenParrot":
                setTheme(R.style.AppTheme_GreenParrot);
                break;
            case "Purple":
                setTheme(R.style.AppTheme_Purple);
                break;
            case "Orange":
                setTheme(R.style.AppTheme_Orange);
                break;
            case "Teal":
                setTheme(R.style.AppTheme_Teal);
                break;
            case "Pink":
                setTheme(R.style.AppTheme_Pink);
                break;
            case "Cyan":
                setTheme(R.style.AppTheme_Cyan);
                break;
            case "Lime":
                setTheme(R.style.AppTheme_Lime);
                break;
            case "Brown":
                setTheme(R.style.AppTheme_Brown);
                break;
            case "Mint":
                setTheme(R.style.AppTheme_Mint);
                break;
            case "Coral":
                setTheme(R.style.AppTheme_Coral);
                break;
            case "Steel":
                setTheme(R.style.AppTheme_Steel);
                break;
            case "Lavender":
                setTheme(R.style.AppTheme_Lavender);
                break;
            case "Mustard":
                setTheme(R.style.AppTheme_Mustard);
                break;
            default:
                setTheme(R.style.Base_Theme_ExpenseUtility);
                break;
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_CODE_NOTIFICATION);
                Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                startActivity(intent);
            }
        }
    }

    private void createNotificationChannelBkgrndImport() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "CSV_IMPORT_CHANNEL";
            CharSequence name = "CSV Import Notifications";
            String description = "Notifies when CSV import starts and finishes";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            readExistingSms();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void checkManageStoragePermission() {
        if (!Environment.isExternalStorageManager()) {
            // Request permission
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_MANAGE_STORAGE);
        }
    }

    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem item = menu.findItem(R.id.action_notifications);
        View actionView = MenuItemCompat.getActionView(item);
        TextView badge = actionView.findViewById(R.id.badge_text_view);

        int count = TransactionQueue.getInstance(this).getCount();
        if (count > 0) {
            badge.setText(String.valueOf(count));
            badge.setVisibility(View.VISIBLE);
        } else {
            badge.setVisibility(View.GONE);
        }

        tasksMenuItem = menu.findItem(R.id.action_tasks_list);
        // Get the custom action view
        if (tasksMenuItem != null && tasksMenuItem.getActionView() != null) {
            badgeActionView = tasksMenuItem.getActionView();

            // Set click listener on the entire action view
            badgeActionView.setOnClickListener(v -> {
                onOptionsItemSelected(tasksMenuItem);
            });

            // Get the badge TextView
            badgeTextView = badgeActionView.findViewById(R.id.badge);
            updateTaskCountBadge();
        }

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
//        reCalculateDailyLimit("from onResume");

        updateTaskCountBadge();

    }

    private void updateTaskCountBadge() {
        if (badgeTextView == null) {
            return;
        }

        TaskDatabaseHelper dbHelper = new TaskDatabaseHelper(this);
        // We can get the count by querying the database for all tasks, but that's inefficient.
        // Instead, we should have a method in TaskDatabaseHelper to get the count.

        // Let's assume we add a method in TaskDatabaseHelper to get the task count.
        int taskCount = dbHelper.getTaskCount();

        if (taskCount > 0) {
            badgeTextView.setText(String.valueOf(taskCount));
            badgeTextView.setVisibility(View.VISIBLE);
        } else {
            badgeTextView.setVisibility(View.GONE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Delete all data?");
            builder.setMessage("Are you sure to delete all data?");
            builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    deleteAllData();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.setCancelable(false);
            AlertDialog dialog = builder.create();
            dialog.show();
            return true;
        }

        if (id == R.id.action_tasks_list) {
            Intent intent = new Intent(MainActivity.this, TaskListActivity.class);
            startActivity(intent);
            return true;
        }


        if (id == R.id.action_setGlobalIncome) {
            setGlobalIncome();
            return true;
        }

        if (id == R.id.report) {
            Intent intent = new Intent(MainActivity.this, ReportActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.dataActivity) {
            Intent intent = new Intent(MainActivity.this, ListDataActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.nestedViewActivity) {
            Intent intent = new Intent(MainActivity.this, NestedViewActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_trig_notif) {
            showNotification();
            return true;
        }
        if (id == R.id.action_exportcsv) {
            try {
                sendBackupCsvEmail();
                exportCSV();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return true;
        }
        if (id == R.id.action_restoreExpenses) {

            // Create and show progress dialog
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Restoring expenses...");
            progressDialog.show();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    restoreExpenses(progressDialog);
                }
            }).start();


            return true;
        }
        if (id == R.id.action_schedule_notif) {
            launchNotification();
            return true;
        }
        if (id == R.id.action_firebaseload) {
            Intent intent = new Intent(this, RealtimeFirebaseActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_flashConfig) {
            initFlashConfig();
            return true;
        }

        if (id == R.id.action_load_particulars) {
            loadParticularsFileFromDownloads();
            return true;
        }

        if (id == R.id.suggestionsConfig) {
            Intent intent = new Intent(this, ConfigGridActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.sendBkpMail) {
//            sendEmailWithCSVAttachment();
            sendBackupCsvEmail();
            return true;
        }

        if (id == R.id.csvStatementImport) {
            importCsvStatement();
            return true;
        }

        if (id == R.id.chooseTheme) {
            showThemeChooserDialog();
            return true;
        }

        if (id == R.id.travelLauncher) {
            travelLauncher();
            return true;
        }

        if (id == R.id.imageDetector) {
            imageDetectionLauncer();
            return true;
        }

        if (id == R.id.settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.qr_code) {
            runQRProcess();
            return true;
        }

        if (id == R.id.criticalFireBaseImport) {
            processCriticalFirebaseImport();
            return true;
        }

        if (id == R.id.newUI) {
            Intent intent = new Intent(this, NewUI.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.budget) {
            Intent intent = new Intent(this, BudgetActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.statementImport) {
            Intent intent = new Intent(this, StatementImport.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_schedule_backup) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                scheduleDailyBackup();
            } else {
                Toast.makeText(this, "Requires Android 8.0 or higher", Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        if (id == R.id.action_cancel_backup) {
            cancelDailyBackup();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void processCriticalFirebaseImport() {
        // First, show confirmation dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Import");
        builder.setMessage("Do you want to import data from Firebase? This process cannot be undone.");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked Yes, proceed with import
                startFirebaseImportProcess();
            }

            private void startFirebaseImportProcess() {
                // Check internet connectivity first
                if (!isNetworkAvailable()) {
                    showSnackbar("No internet connection available");
                    return;
                }

                ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setTitle("Restoring Data");
                progressDialog.setMessage("Please wait...");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setCancelable(false);
                progressDialog.show();

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(Build.MODEL + "/" + "expenses");

                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    private int totalRecords = 0;
                    private int processedRecords = 0;
                    private int importedRecords = 0; // Track only newly imported records

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // First, count all records
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            for (DataSnapshot d1 : dataSnapshot.getChildren()) {
                                for (DataSnapshot d2 : d1.getChildren()) {
                                    for (DataSnapshot d3 : d2.getChildren()) {
                                        totalRecords++;
                                    }
                                }
                            }
                        }

                        if (totalRecords == 0) {
                            progressDialog.dismiss();
                            showSnackbar("No data found to import");
                            return;
                        }

                        progressDialog.setMax(totalRecords);

                        // Process with small delays to allow UI updates
                        processWithDelay(snapshot, progressDialog);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialog.dismiss();

                        // Check if error is due to network issues
                        if (error.getCode() == DatabaseError.DISCONNECTED ||
                                error.getCode() == DatabaseError.NETWORK_ERROR) {
                            showSnackbar("Import failed: " + error.getMessage());
                        } else {
                            showSnackbar("Import failed: " + error.getMessage());
                        }
                    }

                    private void processWithDelay(final DataSnapshot snapshot, final ProgressDialog dialog) {
                        final Handler handler = new Handler();

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    for (DataSnapshot d1 : dataSnapshot.getChildren()) {
                                        for (DataSnapshot d2 : d1.getChildren()) {
                                            for (DataSnapshot d3 : d2.getChildren()) {
                                                // Check if we lost internet during processing
                                                if (!isNetworkAvailable()) {
                                                    handler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            dialog.dismiss();
                                                            showSnackbar("Internet connection lost during import");
                                                        }
                                                    });
                                                    return;
                                                }

                                                // Process record...
                                                ExpenseItem expense = d3.getValue(ExpenseItem.class);

                                                String expCategory = expense.getExpenseCategory();
                                                String expPart = expense.getExpenseParticulars();
                                                String expAmt = String.valueOf(expense.getExpenseAmount());
                                                String expDateTime = expense.getExpenseDateTime();
                                                String expDate = expense.getExpenseDate();
                                                String encodedPartDetails = expense.getPartDetails();
                                                boolean isHomeExpense = expense.isHomeExpense();

                                                // Check if record already exists
                                                boolean exists = db.checkIfExists(expCategory, expAmt, expDateTime, expDate);

                                                if (!exists) {
                                                    try {
                                                        db.insertExpense(expCategory, expPart, expAmt, expDateTime,
                                                                expDate, null, null, null,
                                                                encodedPartDetails, isHomeExpense);
                                                        importedRecords++; // Only increment for newly imported records
                                                    } catch (IllegalAccessException |
                                                             NoSuchFieldException e) {
                                                        Log.e("ImportError", "Error importing record", e);
                                                    }
                                                }

                                                processedRecords++;

                                                // Update UI on main thread - only update progress every 5 records
                                                // or when percentage changes to reduce UI updates
                                                if (processedRecords % 5 == 0 || processedRecords == totalRecords) {
                                                    final int finalProcessed = processedRecords;
                                                    final int finalImported = importedRecords;
                                                    handler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            dialog.setProgress(finalProcessed);
                                                            int percentage = (int) (((float) finalProcessed / totalRecords) * 100);
                                                            dialog.setMessage(percentage + "% complete\nImported: " + finalImported);
                                                        }
                                                    });
                                                }
                                            }
                                        }
                                    }
                                }

                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialog.dismiss();

                                        // Show appropriate message based on import results
                                        // Keeping the EXACT same text content as your original Toast messages
                                        if (importedRecords > 0) {
                                            showSnackbar("Imported " + importedRecords + " new records out of " + totalRecords + " total");
                                        } else {
                                            showSnackbar("All " + totalRecords + " records already exist.\nNo new records imported.");
                                        }
                                    }
                                });
                            }
                        }).start();
                    }
                });

            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked No, do nothing
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    // Helper method to check network connectivity
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    // Helper method to show Snackbar with the same text as Toast
    private void showSnackbar(String message) {
        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            // Use Snackbar instead of Toast, but keep the same message text
            Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);

            // Only add retry action for network-related errors (keep your exact message text)
            if (message.contains("No internet connection") ||
                    message.contains("Internet connection lost") ||
                    message.contains("Import failed")) {
                snackbar.setAction("RETRY", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        processCriticalFirebaseImport();
                    }
                });
            }

            snackbar.show();
        } else {
            // Fallback to Toast if Snackbar can't be shown
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }


//    private void processCriticalFirebaseImport() {
//        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(Build.MODEL + "/" + "expenses");
//        databaseReference.addValueEventListener(new ValueEventListener() {
//            private int rowsCount = 0;
//
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                    for (DataSnapshot d1 : dataSnapshot.getChildren()) {
//                        for (DataSnapshot d2 : d1.getChildren()) {
//                            for (DataSnapshot d3 : d2.getChildren()) {
//                                ExpenseItem expense = d3.getValue(ExpenseItem.class);
//
//                                String expCategory = expense.getExpenseCategory();
//                                String expPart = expense.getExpenseParticulars();
//                                String expAmt = String.valueOf(expense.getExpenseAmount());
//                                String expDateTime = expense.getExpenseDateTime();
//                                String expDate = expense.getExpenseDate();
//                                String encodedPartDetails = expense.getPartDetails();
//                                boolean isHomeExpense = expense.isHomeExpense();
//
//
//                                if (!db.checkIfExists(expCategory, expAmt, expDateTime, expDate)) {
//                                    try {
//                                        db.insertExpense(expCategory, expPart, expAmt, expDateTime, expDate, null, null, null, encodedPartDetails, isHomeExpense);
//                                        Log.i("Rows", "Restored rows " + rowsCount);
//
//                                        rowsCount++;
//                                    } catch (IllegalAccessException | NoSuchFieldException e) {
//                                        throw new RuntimeException(e);
//                                    }
//                                }
//
//                            }
//                        }
//                    }
//                }
//                Toast.makeText(MainActivity.this, "Restored records " + rowsCount, Toast.LENGTH_SHORT).show();
//                Log.i("Rows", "Restored rows " + rowsCount);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//
//    }


    // 🔹 Split JSON into chunks (3000 chars each)
    private List<String> splitJsonIntoChunks(String json, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        int length = json.length();
        for (int i = 0; i < length; i += chunkSize) {
            int end = Math.min(length, i + chunkSize);
            chunks.add((i / chunkSize) + "::" + json.substring(i, end));
        }
        return chunks;
    }

    // 🔹 Generate multiple QR codes
    private void generateAndSaveMultiQR(String jsonData) {
        List<String> chunks = splitJsonIntoChunks(jsonData, 2900);
        int qrIndex = 1;

        for (String chunk : chunks) {
            try {
                int size = 1200; // Bigger QR for more data
                QRCodeWriter writer = new QRCodeWriter();
                BitMatrix bitMatrix = writer.encode(chunk, BarcodeFormat.QR_CODE, size, size);

                Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
                for (int x = 0; x < size; x++) {
                    for (int y = 0; y < size; y++) {
                        bitmap.setPixel(x, y, bitMatrix.get(x, y) ?
                                ContextCompat.getColor(this, android.R.color.black) :
                                ContextCompat.getColor(this, android.R.color.white));
                    }
                }
                saveBitmap(bitmap, "sample_json_qr_part_" + qrIndex + ".png");
                qrIndex++;
            } catch (WriterException e) {
                e.printStackTrace();
            }
        }
    }

    private void runQRProcess() {
        // 🔹 Example: Very large JSON (you can expand this more)
        StringBuilder sb = new StringBuilder();
        sb.append("{\n  \"user\": {\n");
        sb.append("    \"id\": \"12345\",\n");
        sb.append("    \"name\": \"Rohit Kurbetti\",\n");
        sb.append("    \"email\": \"rohit@example.com\",\n");
        sb.append("    \"phone\": \"+91-9876543210\",\n");
        sb.append("    \"transactions\": [\n");

        // Add 200 fake transactions for demo
        for (int i = 1; i <= 200; i++) {
            sb.append("      {\"date\": \"2025-09-" + (i % 30 + 1) + "\", \"amount\": \"" + (1000 + i * 10) + "\", \"category\": \"Category" + i + "\"}");
            if (i < 200) sb.append(",\n");
        }

        sb.append("\n    ]\n  }\n}");
        String jsonData = sb.toString();

        // 🔹 Generate QR codes
        generateAndSaveMultiQR(jsonData);

        // 🔹 Collect all saved QR files
        List<File> qrFiles = new ArrayList<>();
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        for (int i = 1; ; i++) {
            File f = new File(path, "sample_json_qr_part_" + i + ".png");
            if (f.exists()) {
                qrFiles.add(f);
            } else break;
        }

        // 🔹 Decode & reconstruct
        String reconstructed = decodeAndReconstruct(qrFiles);
        Toast.makeText(this, "Reconstructed JSON length: " + reconstructed.length(), Toast.LENGTH_LONG).show();

    }

    // 🔹 Decode multiple QR codes & reconstruct JSON
    private String decodeAndReconstruct(List<File> qrFiles) {
        Map<Integer, String> orderedChunks = new TreeMap<>();
        for (File file : qrFiles) {
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                int[] intArray = new int[bitmap.getWidth() * bitmap.getHeight()];
                bitmap.getPixels(intArray, 0, bitmap.getWidth(),
                        0, 0, bitmap.getWidth(), bitmap.getHeight());

                LuminanceSource source = new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), intArray);
                BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
                Result result = new MultiFormatReader().decode(binaryBitmap);

                String text = result.getText();
                String[] parts = text.split("::", 2);
                int index = Integer.parseInt(parts[0]);
                String chunk = parts[1];
                orderedChunks.put(index, chunk);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        StringBuilder fullJson = new StringBuilder();
        for (String chunk : orderedChunks.values()) {
            fullJson.append(chunk);
        }
        Log.i("Reconstructed JSON", fullJson.toString());
        return fullJson.toString();
    }

    // 🔹 Save bitmap to storage
    private void saveBitmap(Bitmap bitmap, String fileName) {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File file = new File(path, fileName);
        try {
            path.mkdirs();
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void imageDetectionLauncer() {

        Intent intent = new Intent(this, ImageDetectionActivity.class);
        startActivity(intent);

    }

    private void travelLauncher() {
        Intent intent = new Intent(this, TravelActivity.class);
        startActivity(intent);
    }

    private void showThemeChooserDialog() {

        String currentTheme = PreferenceManager.getDefaultSharedPreferences(this).getString("app_theme", "Theme.ExpenseUtility");

        int checkedIndex = Arrays.asList(themes).indexOf(currentTheme);

        new AlertDialog.Builder(this)
                .setTitle("Choose Theme")
                .setSingleChoiceItems(themes, checkedIndex, (dialog, which) -> {
                    String selectedTheme = themes[which];

                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
                    editor.putString("app_theme", selectedTheme);
                    editor.apply();
                    dialog.dismiss();

                    Intent intent = getIntent();
                    finish(); // Destroy current activity
                    startActivity(intent); // Start it again
                })
                .setNegativeButton("Cancel", null)
                .show();


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void importCsvStatement() {

        transactionList = new ArrayList<>();
        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Account_stmt.csv");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                String[] columns = line.split(",", -1);
                if (columns.length >= 3) {
                    String date = columns[0].trim();
                    String particulars = columns[1].trim();
                    String debit = columns[2].trim();
                    if (debit.isEmpty()) {
                        continue;
                    } else {
                        Transaction tr = new Transaction(date, particulars, debit);
                        transactionList.add(tr);
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load CSV", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_transaction_list, null);

        ListView listView = dialogView.findViewById(R.id.dialogListView);
        TransactionAdapter adapter = new TransactionAdapter(this, transactionList);
        listView.setAdapter(adapter);

        builder.setView(dialogView)
                .setCancelable(false)
                .setTitle("Transactions List")
                .setPositiveButton("OK", (dialog, which) -> {
                    long selectedTtems = transactionList.stream().filter(Transaction::isSelected).count();

                    if (selectedTtems > 0) {
                        transactionList.stream().filter(Transaction::isSelected).filter(t -> {
                            return !t.getCategory().equalsIgnoreCase("Select Options");
                        }).forEach(item -> {
                            Log.i("Item >> ", item.getCategory() + " " + item.getParticulars() + " " + item.getDate() + " " + item.getDebitAmount());

                            String category = item.getCategory();
                            String particulars = item.getParticulars() != null && !item.getParticulars().isBlank() ?
                                    item.getParticulars().substring(0, Math.min(15, item.getParticulars().length()))
                                    : "Txn";
                            double amount = Double.parseDouble(item.getDebitAmount());

                            String dateStr = item.getDate();

                            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

                            LocalDate localDate = LocalDate.parse(dateStr, dateTimeFormatter);
                            LocalDateTime localDateTime = localDate.atTime(0, 0, 0);

                            String dateTimeStr = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

//                        localDateTime.toString();


                            try {
                                boolean res = db.insertExpense(category, particulars, String.valueOf((int) amount), dateTimeStr, String.valueOf(localDate), null, null, null, null, false);


//                            if(res) {
//                                //save data now into firebase
//
//                                try {
//                                    saveToFirebase(category, particulars, String.valueOf((int) amount), dateTimeStr, String.valueOf(localDate), null, null);
//                                } catch (ParseException e) {
//                                    throw new RuntimeException(e);
//                                }
//                            }

                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            } catch (NoSuchFieldException e) {
                                throw new RuntimeException(e);
                            }


                        });
                    }

                })
                .show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendBackupCsvEmail() {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String csvFileName = ExpenseConstants.BACKUP_EXPENSES_DATA_EXPORT_FILENAME;
        File csvFile = new File(downloadsDir, csvFileName);

        LocalDateTime localDateTime = LocalDateTime.now();

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss a");


        // Call EmailSender to send the file in the background
        new EmailSender("rohitbackup47@gmail.com", "Expense backup csv " + dateTimeFormatter.format(localDateTime),
                "Backing up expenses data", csvFile, this).execute();
        NotificationUtils.showNotification(this, "Email Sent", "The CSV file was successfully sent to backup email accounts.");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendEmailWithCSVAttachment() {
        // Get the Downloads directory
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        // Specify the name of the CSV file
        String csvFileName = ExpenseConstants.BACKUP_EXPENSES_DATA_EXPORT_FILENAME;
        File csvFile = new File(downloadsDir, csvFileName);

        if (!csvFile.exists()) {
            Toast.makeText(this, "CSV file not found", Toast.LENGTH_SHORT).show();
            return;
        }
        // Get the URI for the file
        Uri fileUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Use FileProvider for Android 7.0 (API level 24) and higher
            fileUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", csvFile);
        } else {
            // Direct URI for older versions
            fileUri = Uri.fromFile(csvFile);
        }

        LocalDateTime localDateTime = LocalDateTime.now();

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss a");


        // Create an intent to send the email
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/csv");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{
                "rohitbackup47@gmail.com",
                "kurbettirohit75@gmail.com",
                "rohitbackup0001@gmail.com",
        }); // Replace with recipient's email
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Expense backup csv " + dateTimeFormatter.format(localDateTime));
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Backing up expenses data");
        emailIntent.putExtra(Intent.EXTRA_STREAM, fileUri); // Attach the CSV file

        // Grant read permission for the file URI
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Check if an email app is available
        if (emailIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(Intent.createChooser(emailIntent, "Send email using..."));
        } else {
            Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void restoreExpenses(ProgressDialog progressDialog) {
        DatabaseHelper db = new DatabaseHelper(MainActivity.this);

        File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File backedUpFile = new File(downloadsFolder, "BackupExpensesData.csv");

        if (!backedUpFile.exists()) {
            Log.e("CSVReader", "File does not exist: " + backedUpFile);
            return;
        }

        String line;
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(backedUpFile));
            boolean isFirstLine = true;
            int rowsCount = 0;
            while ((line = br.readLine()) != null) {
                // Skip the header row
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                // Split line by comma
                String[] tokens = line.split(",");

                // Make sure row has the required columns
                if (tokens.length < 4) {
                    continue;
                }

                String expCategory = tokens[1];
                String expPart = tokens[2];
                String expAmt = tokens[3];
                String expDateTime = tokens[4];
                String expDate = tokens[5];
                String encodedPartDetails = "";

                if (tokens.length > 5 && tokens[6].equals("-")) {
                    encodedPartDetails = "";
                } else {
                    encodedPartDetails = tokens[6];
                }
                boolean isHomeExpense = Boolean.parseBoolean(tokens[7]);

                if (!db.checkIfExists(expCategory, expAmt, expDateTime, expDate)) {
                    db.insertExpense(expCategory, expPart, expAmt, expDateTime, expDate, null, null, null, encodedPartDetails, isHomeExpense);
                    rowsCount++;
                }

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(Build.MODEL + "/" + "expenses"); // Replace 'items' with your specific path

                String childPath = "";

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate date = LocalDate.parse(expDate, formatter);

                int year = date.getYear();

                DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM"); // "MMM" gives abbreviated month
                String month = monthFormatter.format(date);


                childPath = "/" + year + "/" + (month + "-" + year) + "/" + expDate;
                String finalEncodedPartDetails = encodedPartDetails;
                databaseReference.child(childPath).addListenerForSingleValueEvent(new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                            for (DataSnapshot d1 : dataSnapshot.getChildren()) {
//                                for (DataSnapshot d2 : d1.getChildren()) {
                        if (snapshot.hasChildren()) {
                            boolean dataExists = false;
                            for (DataSnapshot d3 : snapshot.getChildren()) {
                                ExpenseItem expenseItem = d3.getValue(ExpenseItem.class);
                                if (expenseItem.getExpenseCategory().equalsIgnoreCase(expCategory) &&
                                        expenseItem.getExpenseAmount().toString().equals(expAmt) &&
                                        expenseItem.getExpenseDateTime().equalsIgnoreCase(expDateTime) &&
                                        expenseItem.getExpenseDate().equalsIgnoreCase(expDate)
                                ) {
//                                    Toast.makeText(MainActivity.this, "Entry exists", Toast.LENGTH_SHORT).show();
                                    dataExists = true;
                                    break;
                                } else {
                                    dataExists = false;
                                }
                            }
                            if (!dataExists) {
                                try {
                                    saveToFirebase(expCategory, expPart, expAmt, expDateTime, expDate, null, null, finalEncodedPartDetails, isHomeExpense);
                                } catch (ParseException e) {
                                    Toast.makeText(MainActivity.this, "Error while storing on cloud", Toast.LENGTH_SHORT).show();
                                    throw new RuntimeException(e);
                                }
                            }
                        } else {
                            try {
                                saveToFirebase(expCategory, expPart, expAmt, expDateTime, expDate, null, null, finalEncodedPartDetails, isHomeExpense);
                            } catch (ParseException e) {
                                Toast.makeText(MainActivity.this, "Error while storing on cloud", Toast.LENGTH_SHORT).show();
                                throw new RuntimeException(e);

                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MainActivity.this, "Cloud DB error", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            // Dismiss dialog on UI thread
            int finalRowsCount = rowsCount;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this, "Restored rows " + finalRowsCount, Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception ex) {
            System.err.println(ex.fillInStackTrace());
        }
    }

    private void loadParticularsFileFromDownloads() {
        // Specify the file name and path in the Downloads directory
        File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(downloadsFolder, "particulars_data.txt");

        if (!file.exists()) {
            Toast.makeText(this, "File not found in Downloads folder", Toast.LENGTH_SHORT).show();
            return;
        }

        // Read and parse the file into a String array
        List<String> itemsArray = parseTextFileToArray(file);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet("partSuggestionsList", new HashSet<>(itemsArray));
        editor.apply();
        Toast.makeText(this, "SuggesstionsList preferred " + itemsArray.size(), Toast.LENGTH_SHORT).show();
    }

    private List<String> parseTextFileToArray(File file) {
        List<String> itemList = new ArrayList<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;

            // Read each line and add to the list
            while ((line = reader.readLine()) != null) {
                itemList.add(line.trim()); // Trim to remove any unwanted spaces
            }

            reader.close();
        } catch (IOException e) {
            Log.e("FileReadError", "Error reading file", e);
        }

        // Convert List to Array
        return itemList;
    }


    private void initFlashConfig() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Flashlight config");

        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        View view = inflater.inflate(R.layout.flashlight_config, null);
        EditText etBlinkDuration = view.findViewById(R.id.etblinkDuration);
        EditText etIntervalDuration = view.findViewById(R.id.etIntervalDuration);

        etBlinkDuration.setText(String.valueOf(sharedPreferences.getInt("blinkTime", 50)));
        etIntervalDuration.setText(String.valueOf(sharedPreferences.getInt("intervalTime", 2000)));

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String blinkTime = etBlinkDuration.getText().toString();
                String intervalTime = etIntervalDuration.getText().toString();
                if (!blinkTime.isEmpty() && !intervalTime.isEmpty()) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    int blinkTimeInt = Integer.parseInt(blinkTime);
                    int intervalTimeInt = Integer.parseInt(intervalTime);
                    editor.putInt("blinkTime", blinkTimeInt);
                    editor.putInt("intervalTime", intervalTimeInt);
                    editor.apply();
                    Toast.makeText(MainActivity.this, "BlinkTime: " + blinkTimeInt + " IntervalTime: " + intervalTimeInt, Toast.LENGTH_LONG).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });


        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showNotification() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        double totalExpense = dbHelper.getTotalExpenseForToday();

        // Format the total expense to two decimal places
        String formattedTotalExpense = String.format("%.2f", totalExpense);


        // Create a notification
        NotificationManager notificationManager = (NotificationManager) this.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "expense_channel";
        CharSequence channelName = "Expense Notifications";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        // Create an intent for the dismiss action
        Intent dismissIntent = new Intent(this, DismissNotificationReceiver.class);
        dismissIntent.putExtra("notification_id", 1);
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                dismissIntent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ? PendingIntent.FLAG_IMMUTABLE : 0
        );
        float income = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).getFloat("monthlyIncome", ANN_INCOME);

        String dateFrmt = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        String dataTxt = "Total Expense: \u20B9" + totalExpense + "\n" +
                "Average spent (day income) " + String.format("%.2f", (totalExpense / (income / 30)) * 100) + "%\n" +
                "Average spent (monthly income) " + String.format("%.2f", (totalExpense / income) * 100) + "%";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.money_svgrepo_com__1_) // Add your own icon here
                .setContentTitle("Today's Total Expense")
                .setSubText(dateFrmt)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(dataTxt))
                .addAction(R.drawable.ic_launcher_foreground, "Close", dismissPendingIntent); // Add dismiss action button

        notificationManager.notify(1, builder.build());
    }


    private void launchNotification() {
        setDailyExpenseNotification();
    }

    private void setDailyExpenseNotification() {
        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent;
        pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ? PendingIntent.FLAG_IMMUTABLE : 0
        );


        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 22); // 10 PM
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // Set the alarm to start now and repeat every minute
        long interval = 60 * 1000; // 1 minute in milliseconds
        long startTime = System.currentTimeMillis();

        // Set the alarm to start at 10 PM
        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pendingIntent);
            Toast.makeText(this, "Notification started repeating everyday at 10pm", Toast.LENGTH_SHORT).show();
//            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, startTime,
//                    interval, pendingIntent);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void exportCSV() throws IOException {
        DatabaseHelper db = new DatabaseHelper(MainActivity.this);
        Cursor sqlRows = db.getAllExpenseData();
        List<ExpenseItem> expenses = new ArrayList<>();

        if (sqlRows.getCount() > 0) {

            String fileName = "BackupExpensesData.csv";

            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File csvFile = new File(downloadsDir, fileName);

            FileOutputStream fileOutputStream = new FileOutputStream(csvFile);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);

            outputStreamWriter.write("ID,ExpCat,Pert,Amt,DateTime,Date,Description,isForHome\n");

            while (sqlRows.moveToNext()) {
                int id = sqlRows.getInt(0);
                String expCat = sqlRows.getString(1);
                String pert = sqlRows.getString(2);
                int amt = sqlRows.getInt(3);
                String dtm = sqlRows.getString(4);
                String dt = sqlRows.getString(5);
                String flnm = sqlRows.getString(6);
                String encodedPartDetails = sqlRows.getString(8);
                encodedPartDetails = encodedPartDetails.isBlank() ? "-" : encodedPartDetails;
                encodedPartDetails = encodedPartDetails.trim();
                boolean isHomeExpense = sqlRows.getInt(9) == 1;

//                byte[] file = sqlRows.getBlob(7);
//                String encodedString = "";
//                if(file!=null) {
//                    encodedString = Base64.getEncoder().encodeToString(file);
//                }
                String row = id + "," + expCat + "," + pert + "," + amt + "," + dtm + "," + dt + "," + encodedPartDetails + "," + isHomeExpense + "\n";
                outputStreamWriter.write(row);
                expenses.add(new ExpenseItem(id, pert, (long) amt, dtm, expCat, null, null, encodedPartDetails, isHomeExpense));


            }
            outputStreamWriter.close();
            sqlRows.close();
            fileOutputStream.close();
            Toast.makeText(this, "File saved " + csvFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();

            // Calculate summary
            Map<String, Double> categorySummary = calculateCategorySummary(expenses);
            double totalAmount = calculateTotalAmount(expenses);

            // Show summary popup
            showSummaryPopup(categorySummary, totalAmount, expenses);

        } else {
            //no sql rows returned
            Toast.makeText(this, "No rows present in database", Toast.LENGTH_LONG).show();
        }

        List<TxnIgnoreDto> txnIgnoreDtoList = db.getAllIgnoredTransactions();

        if (!txnIgnoreDtoList.isEmpty()) {

            String fileName = ExpenseConstants.BACKUP_TXN_IGNORE_EXPORT_FILENAME;
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File csvFile = new File(downloadsDir, fileName);

            FileOutputStream fileOutputStream = new FileOutputStream(csvFile);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);

            outputStreamWriter.write("Id,Amount,DateTime\n");

            txnIgnoreDtoList.forEach(txnIgnoreRow -> {
                String row = txnIgnoreRow.getId() + "," + txnIgnoreRow.getAmount() + "," + txnIgnoreRow.getDateTime() + "\n";
                try {
                    outputStreamWriter.write(row);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            outputStreamWriter.close();
            fileOutputStream.close();
            Toast.makeText(this, "Backup Txn Ignore is completed !", Toast.LENGTH_SHORT).show();
        }
    }

    private void showSummaryPopup(Map<String, Double> categorySummary, double totalAmount, List<ExpenseItem> expenses) {
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");

        Dialog summaryDialog = new Dialog(this);
        summaryDialog.setContentView(R.layout.dialog_summary);
        summaryDialog.setTitle("Expense Summary Report");
        summaryDialog.setCancelable(false);

        TextView tvSummaryContent = summaryDialog.findViewById(R.id.tvSummaryContent);
        Button btnClose = summaryDialog.findViewById(R.id.btnClose);

        StringBuilder summaryBuilder = new StringBuilder();
        summaryBuilder.append("CONSOLIDATED EXPENSE SUMMARY\n\n");
        summaryBuilder.append("CATEGORY WISE BREAKDOWN:\n");
        summaryBuilder.append("------------------------\n");

        // Add each category with its total amount
        for (Map.Entry<String, Double> entry : categorySummary.entrySet()) {
            summaryBuilder.append(entry.getKey())
                    .append(": ₹")
                    .append(decimalFormat.format(entry.getValue()))
                    .append("\n");
        }

        summaryBuilder.append("\n------------------------\n");
        summaryBuilder.append("TOTAL AMOUNT: ₹")
                .append(decimalFormat.format(totalAmount))
                .append("\n");
        summaryBuilder.append("Total Expenses: ")
                .append(expenses.size())
                .append(" transactions");

        tvSummaryContent.setText(summaryBuilder.toString());

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                summaryDialog.dismiss();
            }
        });

        summaryDialog.show();
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

            if (categoryMap.containsKey(category)) {
                categoryMap.put(category, categoryMap.get(category) + amount);
            } else {
                categoryMap.put(category, amount);
            }
        }

        return categoryMap;
    }

    private void showListView() {

        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);

    }

    private void setGlobalIncome() {
        SharedPreferences.Editor editor = sharedPreferences.edit();

// Inflate the custom layout
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.popup_with_edittext, null);

        EditText editText = dialogView.findViewById(R.id.editTextPopup);
        Float monthlyIncome = sharedPreferences.getFloat("monthlyIncome", ANN_INCOME);
        editText.setText(String.valueOf(monthlyIncome));
        // Create the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.calculator_finance_income_svgrepo_com);
        builder.setView(dialogView)
                .setTitle("Set income")
                .setPositiveButton("Save", (dialog, which) -> {
                    // Handle the input text
                    String inputText = editText.getText().toString();

                    if (inputText != null && inputText != "" && inputText != "0") {
                        editor.putFloat("monthlyIncome", Float.valueOf(inputText));
                        editor.apply();
                        Toast.makeText(this, "Income has been set", Toast.LENGTH_SHORT).show();
                    }

                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        // Show the dialog
        builder.create().show();

    }

    private void deleteAllData() {
        DatabaseHelper db = new DatabaseHelper(MainActivity.this);
        db.deleteAllData();
        Toast.makeText(this, "All data deleted !", Toast.LENGTH_SHORT).show();
        db.close();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void scheduleDailyBackup() {
        // Create notification channel for backup alarms
        createBackupNotificationChannel();

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, BackupTaskReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                BACKUP_REQUEST_CODE,
                intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ?
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE :
                        PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Cancel any existing alarm
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }

        // Set time to 10:00 PM IST (16:30 UTC = 22:00 IST)
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata")); // IST timezone

        // Set to 10:00 PM
        calendar.set(Calendar.HOUR_OF_DAY, 22);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // If it's already past 10 PM today, schedule for tomorrow
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Set the alarm
        if (alarmManager != null) {
            // Use setExactAndAllowWhileIdle for precise timing (Android 6.0+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            }

            // Also set a repeating alarm for subsequent days
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
            );

            Log.i("BackupScheduler", "Daily backup scheduled for: " + calendar.getTime());
            Toast.makeText(this, "Daily backup scheduled at 10 PM IST", Toast.LENGTH_SHORT).show();
        }
    }

    private void createBackupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Backup Alarms";
            String description = "Notifications for automated backup tasks";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(
                    BACKUP_ALARM_CHANNEL_ID,
                    name,
                    importance
            );
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void cancelDailyBackup() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, BackupTaskReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                BACKUP_REQUEST_CODE,
                intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ?
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE :
                        PendingIntent.FLAG_UPDATE_CURRENT
        );

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
            Log.i("BackupScheduler", "Daily backup cancelled");
            Toast.makeText(this, "Daily backup cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    // Add a method to trigger backup immediately (for testing)
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void triggerBackupNow() {
        Intent intent = new Intent(this, BackupTaskReceiver.class);
        sendBroadcast(intent);
        Toast.makeText(this, "Backup triggered manually", Toast.LENGTH_SHORT).show();
    }

}