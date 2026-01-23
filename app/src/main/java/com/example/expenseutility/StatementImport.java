package com.example.expenseutility;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.expenseutility.database.DatabaseHelper;
import com.example.expenseutility.dto.Transaction1;
import com.example.expenseutility.entityadapter.TransactionAdapter1;
import com.example.expenseutility.utility.TransactionParser;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatementImport extends AppCompatActivity {

    private static final int PICK_CSV_FILE = 1;
    private static final int PERMISSION_REQUEST_STORAGE = 101;

    private ListView listView;
    private TransactionAdapter1 adapter;
    private List<Transaction1> transactionList;
    private TextView previewTitle;
    private Button btnInsert;
    private Button btnSelectFile;
    private CheckBox headerCheckBox;
    private TextView tvTotalDebit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statement_import);

        // Initialize views
        listView = findViewById(R.id.list_view);
        previewTitle = findViewById(R.id.tv_preview_title);
        btnInsert = findViewById(R.id.btn_insert);
        btnSelectFile = findViewById(R.id.btn_select_file);
        headerCheckBox = findViewById(R.id.cb_select_all);
        tvTotalDebit = findViewById(R.id.tv_total_debit);

        transactionList = new ArrayList<>();
        adapter = new TransactionAdapter1(this, transactionList, headerCheckBox);
        listView.setAdapter(adapter);

        // Set header checkbox listener
        headerCheckBox.setChecked(true);
        headerCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (adapter != null) {
                    adapter.selectAll(isChecked);
                }
            }
        });

        // Set button listeners
        btnSelectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestStoragePermission();
            }
        });

        btnInsert.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                
                insertSelectedTransactions();
            }
        });

        // Initially hide insert button
        btnInsert.setVisibility(View.GONE);
        previewTitle.setVisibility(View.GONE);
        headerCheckBox.setVisibility(View.GONE);
        tvTotalDebit.setVisibility(View.GONE);
    }

    private void requestStoragePermission() {
//        if (ContextCompat.checkSelfPermission(this,
//                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//                    PERMISSION_REQUEST_STORAGE);
//        } else {
        openFilePicker();
//        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openFilePicker();
            } else {
                Toast.makeText(this, "Permission denied to read storage", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/*"); // For CSV/text files
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select CSV File"), PICK_CSV_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_CSV_FILE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                parseCSVFile(uri);
            }
        }
    }

    private void parseCSVFile(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            boolean isFirstLine = true;
            transactionList.clear();
            int serialNo = 1;
            double totalDebit = 0.0;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip header
                }

                // Parse CSV line with Category (4 columns)
                String[] parts = line.split(",");

                // Check for both old (3 columns) and new (4 columns) formats
                if (parts.length >= 4) {
                    // New format with Category
                    String date = parts[0].trim();
                    String particulars = parts[1].trim();
                    String debit = parts[2].trim();
                    String category = parts[3].trim();

                    // Calculate total
                    try {
                        Double debitAmount = Double.parseDouble(debit);

                        String parsedParticulars = TransactionParser.parseTransactions(particulars);

                        totalDebit += debitAmount;
                        // Create transaction with category
                        Transaction1 transaction = new Transaction1(serialNo, date, particulars, String.valueOf(debitAmount.intValue()), category, parsedParticulars);
                        transactionList.add(transaction);
                        serialNo++;
                    } catch (NumberFormatException e) {
                        // Skip invalid amounts
                    }

                } else if (parts.length >= 3) {
//                    // Old format without Category
//                    String date = parts[0].trim();
//                    String particulars = parts[1].trim();
//                    String debit = parts[2].trim();
//
//                    // Calculate total
//                    try {
//                        double debitAmount = Double.parseDouble(debit);
//                        totalDebit += debitAmount;
//                    } catch (NumberFormatException e) {
//                        // Skip invalid amounts
//                    }
//
//                    // Create transaction with default category
//                    Transaction1 transaction = new Transaction1(serialNo, date, particulars, debit, "");
//                    transactionList.add(transaction);
//                    serialNo++;
                }
            }

            reader.close();

            // Create adapter with header checkbox reference
            adapter = new TransactionAdapter1(this, transactionList, headerCheckBox);
            listView.setAdapter(adapter);

            // Select all by default
            adapter.selectAll(true);

            // Update UI
            adapter.notifyDataSetChanged();
            previewTitle.setVisibility(View.VISIBLE);
            btnInsert.setVisibility(View.VISIBLE);
            headerCheckBox.setVisibility(View.VISIBLE);
            tvTotalDebit.setVisibility(View.VISIBLE);

            // Update total debit display
            updateTotalDebitDisplay(totalDebit);

            Toast.makeText(this, "Parsed " + transactionList.size() + " transactions",
                    Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error parsing CSV file: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Update the total debit display with formatted amount
     */
    private void updateTotalDebitDisplay(double totalDebit) {
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
        String formattedTotal = decimalFormat.format(totalDebit);
        tvTotalDebit.setText("Total Debit: " + formattedTotal);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void insertSelectedTransactions() {
        List<Transaction1> selectedTransactions = new ArrayList<>();
        Map<String, Double> categoryTotals = new HashMap<>();
        double selectedTotalDebit = 0.0;

        for (int i = 1; i <= 12; i++) {

            String month1 = "2024-" + String.format("%02d", i);
            double sum = transactionList.stream()
                    .filter(t -> t.getFormattedDate() != null && t.getFormattedDate().startsWith(month1))
                    .filter(t -> t.getDebit() != null && !t.getDebit().isEmpty())
                    .mapToDouble(t -> {
                        try {
                            return Double.parseDouble(t.getDebit());
                        } catch (NumberFormatException e) {
                            return 0.0; // Return 0 for invalid values
                        }
                    })
                    .sum();
            Log.i("SUM >>>>> " + month1, "" + sum);
        }

        for (Transaction1 transaction : transactionList) {
            if (transaction.isChecked()) {
                selectedTransactions.add(transaction);

                // Calculate totals
                try {
                    double debitAmount = Double.parseDouble(transaction.getDebit());
                    selectedTotalDebit += debitAmount;

                    // Update category totals
                    String category = transaction.getCategory();
                    categoryTotals.put(category,
                            categoryTotals.getOrDefault(category, 0.0) + debitAmount);

                    DatabaseHelper db = DatabaseHelper.getInstance(this);


                    if (db.checkifExistsFromStatement(transaction.getCategory(), transaction.getParticulars(),
                            transaction.getDebit(), transaction.getFormattedDate())) {
                        Log.i("present ", "y");
                    } else {
                        Log.i("present ", "n");
                        boolean res = db.insertExpense(transaction.getCategory(), transaction.getParticulars(), transaction.getDebit(),
                                null, transaction.getFormattedDate(), null, null, null, null, false);
                        if (res) {
                            Log.i("INSERT", "inserted " + transaction);
                            try {
                                FirstFragment.saveToFirebase(transaction.getCategory(), transaction.getParticulars(), transaction.getDebit(),
                                        null, transaction.getFormattedDate(), null, null, null, false);
                            } catch (ParseException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            Log.i("INSERT", "inserted failed" + transaction);
                        }
                    }


                } catch (NumberFormatException | IllegalAccessException | NoSuchFieldException e) {
                    // Skip invalid amounts
                }
            }
        }

        // Here you would typically insert into database
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
        String message = "Inserting " + selectedTransactions.size() + " transactions:\n";
        message += "Total Amount: " + decimalFormat.format(selectedTotalDebit) + "\n\n";

        // Add category breakdown if available
        if (!categoryTotals.isEmpty()) {
            message += "Category Breakdown:\n";
            for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
                message += "- " + entry.getKey() + ": " + decimalFormat.format(entry.getValue()) + "\n";
            }
            message += "\n";
        }

        message += "Sample Transactions:\n";
        for (int i = 0; i < Math.min(selectedTransactions.size(), 3); i++) {
            Transaction1 t = selectedTransactions.get(i);
            message += t.getSerialNo() + ". " + t.getFormattedDate() + " - " +
                    t.getDebit() + " (" + t.getCategory() + ")\n";
        }

        if (selectedTransactions.size() > 3) {
            message += "... and " + (selectedTransactions.size() - 3) + " more";
        }

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}