package com.example.expenseutility;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.example.expenseutility.database.DatabaseHelper;
import com.example.expenseutility.emailutility.EmailSender;
import com.example.expenseutility.emailutility.NotificationUtils;
import com.example.expenseutility.entityadapter.CustomListAdapter;
import com.example.expenseutility.entityadapter.ExpenseItem;
import com.example.expenseutility.entityadapter.Expenses;
import com.example.expenseutility.entityadapter.FirebaseExpenseAdapter;
import com.example.expenseutility.notification.DismissNotificationReceiver;
import com.example.expenseutility.notification.NotificationReceiver;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.expenseutility.databinding.ActivityMainBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.Blob;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;

    private DatabaseReference database;

    private ActivityMainBinding binding;
    private static final int REQUEST_MANAGE_STORAGE = 123;
    private static final int REQUEST_WRITE_STORAGE = 112;
    SharedPreferences sharedPreferences;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
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
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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

        if (id == R.id.action_setGlobalIncome) {
            setGlobalIncome();
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
            restoreExpenses();
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

        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendBackupCsvEmail() {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String csvFileName = "BackupExpensesData.csv";
        File csvFile = new File(downloadsDir, csvFileName);

        LocalDateTime localDateTime = LocalDateTime.now();

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss a");



        // Call EmailSender to send the file in the background
        new EmailSender("rohitbackup47@gmail.com", "Expense backup csv "+dateTimeFormatter.format(localDateTime),
                "Backing up expenses data", csvFile).execute();
        NotificationUtils.showNotification(this, "Email Sent", "The CSV file was successfully sent to backup email accounts.");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendEmailWithCSVAttachment() {
        // Get the Downloads directory
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        // Specify the name of the CSV file
        String csvFileName = "BackupExpensesData.csv";
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
    private void restoreExpenses() {
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
            int rowsCount =0;
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

                if(!db.checkIfExists(expCategory, expPart, expAmt, expDateTime, expDate)) {
                    db.insertExpense(expCategory, expPart, expAmt, expDateTime, expDate, null, null, null);
                    rowsCount++;
                }

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("expenses"); // Replace 'items' with your specific path

                String childPath = "";

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate date = LocalDate.parse(expDate, formatter);

                int year = date.getYear();

                DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM"); // "MMM" gives abbreviated month
                String month = monthFormatter.format(date);


                childPath = "/"+year +"/"+(month+"-"+year)+"/"+expDate;
                databaseReference.child(childPath).addListenerForSingleValueEvent(new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                            for (DataSnapshot d1 : dataSnapshot.getChildren()) {
//                                for (DataSnapshot d2 : d1.getChildren()) {
                        if(snapshot.hasChildren()){
                            boolean dataExists = false;
                            for (DataSnapshot d3 : snapshot.getChildren()) {
                                ExpenseItem expenseItem = d3.getValue(ExpenseItem.class);
                                if(expenseItem.getExpenseParticulars().equalsIgnoreCase(expPart) &&
                                        expenseItem.getExpenseCategory().equalsIgnoreCase(expCategory) &&
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
                            if(!dataExists) {
                                try {
                                    FirstFragment.saveToFirebase(expCategory, expPart, expAmt, expDateTime, expDate, null, null);
                                } catch (ParseException e) {
                                    Toast.makeText(MainActivity.this, "Error while storing on cloud", Toast.LENGTH_SHORT).show();
                                    throw new RuntimeException(e);
                                }
                            }
                        } else {
                            try {
                                FirstFragment.saveToFirebase(expCategory, expPart, expAmt, expDateTime, expDate, null, null);
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
            Toast.makeText(this, "Restored rows "+rowsCount, Toast.LENGTH_SHORT).show();

        } catch (Exception ex) {
            System.err.println(ex.fillInStackTrace());
        }
    }

    private void loadParticularsFileFromDownloads() {
        // Specify the file name and path in the Downloads directory
        File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(downloadsFolder, "data.txt");

        if (!file.exists()) {
            Toast.makeText(this, "File not found in Downloads folder", Toast.LENGTH_SHORT).show();
            return;
        }

        // Read and parse the file into a String array
        List<String> itemsArray = parseTextFileToArray(file);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet("partSuggestionsList", new HashSet<>(itemsArray));
        editor.apply();
        Toast.makeText(this, "SuggesstionsList preferred "+itemsArray.size(), Toast.LENGTH_SHORT).show();
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
                if(!blinkTime.isEmpty() && !intervalTime.isEmpty()) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    int blinkTimeInt = Integer.parseInt(blinkTime);
                    int intervalTimeInt = Integer.parseInt(intervalTime);
                    editor.putInt("blinkTime",blinkTimeInt);
                    editor.putInt("intervalTime",intervalTimeInt);
                    editor.apply();
                    Toast.makeText(MainActivity.this, "BlinkTime: "+blinkTimeInt +" IntervalTime: "+intervalTimeInt, Toast.LENGTH_LONG).show();
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

        String dateFrmt = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        String dataTxt = "Total Expense: \u20B9" + totalExpense+"\n" +
                "Average spent (day income) "+String.format("%.2f",(totalExpense/(60000/30))*100)+"%\n"+
                "Average spent (monthly income) "+String.format("%.2f",(totalExpense/60000)*100)+"%";
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

        if(sqlRows.getCount() > 0) {

            String fileName = "BackupExpensesData.csv";

            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File csvFile = new File(downloadsDir, fileName);

            FileOutputStream fileOutputStream = new FileOutputStream(csvFile);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);

            outputStreamWriter.write("ID,ExpCat,Pert,Amt,DateTime,Date\n");

            while(sqlRows.moveToNext()) {
                int id = sqlRows.getInt(0);
                String expCat = sqlRows.getString(1);
                String pert = sqlRows.getString(2);
                int amt = sqlRows.getInt(3);
                String dtm = sqlRows.getString(4);
                String dt = sqlRows.getString(5);
                String flnm = sqlRows.getString(6);
//                byte[] file = sqlRows.getBlob(7);
//                String encodedString = "";
//                if(file!=null) {
//                    encodedString = Base64.getEncoder().encodeToString(file);
//                }
                String row = id + "," + expCat + "," + pert + "," + amt + "," + dtm + "," + dt + "\n";
                outputStreamWriter.write(row);

            }
            outputStreamWriter.close();
            sqlRows.close();
            fileOutputStream.close();
            Toast.makeText(this, "File saved "+csvFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();

        } else {
            //no sql rows returned
        }

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
        Float monthlyIncome = sharedPreferences.getFloat("monthlyIncome", 0f);
        editText.setText(String.valueOf(monthlyIncome));
        // Create the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setTitle("Set income")
                .setPositiveButton("Save", (dialog, which) -> {
                    // Handle the input text
                    String inputText = editText.getText().toString();

                    if(inputText != null && inputText != "" && inputText != "0"){
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
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}