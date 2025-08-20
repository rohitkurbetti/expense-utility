package com.example.expenseutility;

import static com.example.expenseutility.ExpenseInputActivity.getFormatted;
import static com.example.expenseutility.FirstFragment.saveToFirebase;
import static com.example.expenseutility.utility.SmsNotificationUtils.parseAmount;
import static com.example.expenseutility.utility.SmsNotificationUtils.parseDateTime;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import com.example.expenseutility.dto.Transaction;
import com.example.expenseutility.emailutility.EmailSender;
import com.example.expenseutility.emailutility.NotificationUtils;
import com.example.expenseutility.entityadapter.ExpenseItem;
import com.example.expenseutility.entityadapter.TransactionAdapter;
import com.example.expenseutility.notification.DismissNotificationReceiver;
import com.example.expenseutility.notification.NotificationReceiver;
import com.example.expenseutility.utility.SmsNotificationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.MenuItemCompat;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;

    private DatabaseReference database;

    private ActivityMainBinding binding;
    private static final int REQUEST_MANAGE_STORAGE = 123;
    private static final int REQUEST_WRITE_STORAGE = 112;
    SharedPreferences sharedPreferences;
    private static final int REQUEST_CODE_NOTIFICATION = 2001;
    private static final int SMS_PERMISSION_CODE = 101;
    private DatabaseHelper db;
    private List<Transaction> transactionList;

    Spinner themeSpinner;
    String[] themes = {
            "Red", "Blue", "Green", "Purple", "Orange",
            "Teal", "Pink", "Cyan", "Lime", "Brown",
            "Mint", "Coral", "Steel", "Lavender", "Mustard"
    };

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
        String[] selectionArgs = { String.valueOf(threeDaysAgoMillis) };
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

                    if(amount>0.0d && !dateTime.isEmpty()) {

                        if(db==null) {
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

                            String dateStr = parsedDateTime.getYear()+"-"+month+"-"+day;



                            //check if record exists in db
                            boolean isExists = db.checkIfExistsMinPossibleParams((int) amount, dateTimeStr.toString(), dateStr);

                            if(!isExists) {
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
    protected void onCreate(Bundle savedInstanceState) {
        applyUserTheme();  // Apply before setContentView
        super.onCreate(savedInstanceState);
        requestSmsPermission();
         sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        db = new DatabaseHelper(this);
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


        float income = sharedPreferences.getFloat("monthlyIncome", 87000.0f);

        if(income==0.0f) {
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
            case "Default": setTheme(R.style.Base_Theme_ExpenseUtility); break;
            case "Red": setTheme(R.style.AppTheme_Red); break;
            case "Blue": setTheme(R.style.AppTheme_Blue); break;
            case "Green": setTheme(R.style.AppTheme_Green); break;
            case "Purple": setTheme(R.style.AppTheme_Purple); break;
            case "Orange": setTheme(R.style.AppTheme_Orange); break;
            case "Teal": setTheme(R.style.AppTheme_Teal); break;
            case "Pink": setTheme(R.style.AppTheme_Pink); break;
            case "Cyan": setTheme(R.style.AppTheme_Cyan); break;
            case "Lime": setTheme(R.style.AppTheme_Lime); break;
            case "Brown": setTheme(R.style.AppTheme_Brown); break;
            case "Mint": setTheme(R.style.AppTheme_Mint); break;
            case "Coral": setTheme(R.style.AppTheme_Coral); break;
            case "Steel": setTheme(R.style.AppTheme_Steel); break;
            case "Lavender": setTheme(R.style.AppTheme_Lavender); break;
            case "Mustard": setTheme(R.style.AppTheme_Mustard); break;
            default: setTheme(R.style.Base_Theme_ExpenseUtility); break;
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





        return super.onOptionsItemSelected(item);
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
                if (isHeader) { isHeader = false; continue; }
                String[] columns = line.split(",", -1);
                if (columns.length >= 3) {
                    String date = columns[0].trim();
                    String particulars = columns[1].trim();
                    String debit = columns[2].trim();
                    if(debit.isEmpty()) {
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

                if(selectedTtems>0) {
                    transactionList.stream().filter(Transaction::isSelected).filter(t -> {
                        return !t.getCategory().equalsIgnoreCase("Select Options");
                    }).forEach(item -> {
                        Log.i("Item >> ", item.getCategory()+" "+item.getParticulars()+" "+item.getDate()+" "+item.getDebitAmount());

                        String category = item.getCategory();
                        String particulars = item.getParticulars()!=null&&!item.getParticulars().isBlank()?
                                item.getParticulars().substring(0,Math.min(15,item.getParticulars().length()))
                                :"Txn";
                        double amount = Double.parseDouble(item.getDebitAmount());

                        String dateStr = item.getDate();

                        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

                        LocalDate localDate = LocalDate.parse(dateStr, dateTimeFormatter);
                        LocalDateTime localDateTime = localDate.atTime(0,0,0);

                        String dateTimeStr = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

//                        localDateTime.toString();


                        try {
                            boolean res = db.insertExpense(category, particulars, String.valueOf((int) amount), dateTimeStr, String.valueOf(localDate), null, null, null);


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

                if(!db.checkIfExists(expCategory, expAmt, expDateTime, expDate)) {
                    db.insertExpense(expCategory, expPart, expAmt, expDateTime, expDate, null, null, null);
                    rowsCount++;
                }

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(Build.MODEL+"/"+"expenses"); // Replace 'items' with your specific path

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
                                if(expenseItem.getExpenseCategory().equalsIgnoreCase(expCategory) &&
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
                                    saveToFirebase(expCategory, expPart, expAmt, expDateTime, expDate, null, null);
                                } catch (ParseException e) {
                                    Toast.makeText(MainActivity.this, "Error while storing on cloud", Toast.LENGTH_SHORT).show();
                                    throw new RuntimeException(e);
                                }
                            }
                        } else {
                            try {
                                saveToFirebase(expCategory, expPart, expAmt, expDateTime, expDate, null, null);
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
        float income = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).getFloat("monthlyIncome", 87000.0f);

        String dateFrmt = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        String dataTxt = "Total Expense: \u20B9" + totalExpense+"\n" +
                "Average spent (day income) "+String.format("%.2f",(totalExpense/(income/30))*100)+"%\n"+
                "Average spent (monthly income) "+String.format("%.2f",(totalExpense/income)*100)+"%";
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
        Float monthlyIncome = sharedPreferences.getFloat("monthlyIncome", 87000f);
        editText.setText(String.valueOf(monthlyIncome));
        // Create the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.calculator_finance_income_svgrepo_com);
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