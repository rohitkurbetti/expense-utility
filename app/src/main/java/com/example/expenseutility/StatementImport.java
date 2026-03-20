package com.example.expenseutility;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.expenseutility.database.DatabaseHelper;
import com.example.expenseutility.dto.Transaction1;
import com.example.expenseutility.entityadapter.TransactionAdapter1;
import com.example.expenseutility.utility.ThemeHelper;
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
import java.util.Locale;
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
    private ProgressBar progressBar;
    private TextView tvProgressText;
    private TextView tvInsertionSummary;
    private View progressSection; // Add reference to parent layout

    private InsertTransactionsTask insertTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statement_import);

        // Initialize views
        listView = findViewById(R.id.list_view);
        previewTitle = findViewById(R.id.tv_preview_title);
        btnInsert = findViewById(R.id.btn_insert);
        btnSelectFile = findViewById(R.id.btn_select_file);
        headerCheckBox = findViewById(R.id.cb_select_all);
        tvTotalDebit = findViewById(R.id.tv_total_debit);
        progressBar = findViewById(R.id.progress_bar);
        tvProgressText = findViewById(R.id.tv_progress_text);
        tvInsertionSummary = findViewById(R.id.tv_insertion_summary);
        progressSection = findViewById(R.id.progress_section); // Get the parent layout

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
                insertSelectedTransactionsInBackground();
            }
        });

        // Initially hide insert button and progress views
        btnInsert.setVisibility(View.GONE);
        previewTitle.setVisibility(View.GONE);
        headerCheckBox.setVisibility(View.GONE);
        tvTotalDebit.setVisibility(View.GONE);
        progressSection.setVisibility(View.GONE); // Hide the entire progress section
        tvInsertionSummary.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel any running tasks
        if (insertTask != null && !insertTask.isCancelled()) {
            insertTask.cancel(true);
        }
    }

    private void requestStoragePermission() {
        // For Android 11+, use new permission model
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            openFilePicker();
        } else {
            openFilePicker();
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/*"); // For CSV/text files
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        String[] mimeTypes = {"text/csv", "text/comma-separated-values", "text/plain"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(Intent.createChooser(intent, "Select CSV File"), PICK_CSV_FILE);
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

                        // Create transaction with category
                        Transaction1 transaction = new Transaction1(serialNo, date, particulars,
                                String.valueOf(debitAmount.intValue()), category, parsedParticulars);
                        transactionList.add(transaction);
                        serialNo++;
                    } catch (NumberFormatException e) {
                        // Skip invalid amounts
                        Log.e("CSV Parse", "Invalid debit amount: " + debit);
                    }
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
            updateTotalDebitDisplay();

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
    private void updateTotalDebitDisplay() {
        double totalDebit = 0.0;
        for (Transaction1 transaction : transactionList) {
            try {
                totalDebit += Double.parseDouble(transaction.getDebit());
            } catch (NumberFormatException e) {
                // Skip invalid amounts
            }
        }

        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
        String formattedTotal = decimalFormat.format(totalDebit);
        tvTotalDebit.setText("Total Debit: " + formattedTotal);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void insertSelectedTransactionsInBackground() {
        // Get selected transactions
        List<Transaction1> selectedTransactions = new ArrayList<>();
        for (Transaction1 transaction : transactionList) {
            if (transaction.isChecked()) {
                selectedTransactions.add(transaction);
            }
        }

        if (selectedTransactions.isEmpty()) {
            Toast.makeText(this, "No transactions selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Start AsyncTask for background processing
        insertTask = new InsertTransactionsTask();
        insertTask.execute(selectedTransactions);
    }

    private void refreshTransactionList() {
        // Remove successfully inserted transactions from the list
        List<Transaction1> toRemove = new ArrayList<>();
        for (Transaction1 transaction : transactionList) {
            if (transaction.isChecked()) {
                toRemove.add(transaction);
            }
        }
        transactionList.removeAll(toRemove);

        // Update serial numbers using the new setter
        for (int i = 0; i < transactionList.size(); i++) {
            transactionList.get(i).setSerialNo(i + 1);
        }

        // Update adapter
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        // Update total debit
        updateTotalDebitDisplay();

        // If no transactions left, hide UI elements
        if (transactionList.isEmpty()) {
            previewTitle.setVisibility(View.GONE);
            btnInsert.setVisibility(View.GONE);
            headerCheckBox.setVisibility(View.GONE);
            tvTotalDebit.setVisibility(View.GONE);
            progressSection.setVisibility(View.GONE); // Hide progress section
            Toast.makeText(this, "All transactions processed!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * AsyncTask for background processing of transactions
     */
    private class InsertTransactionsTask extends AsyncTask<List<Transaction1>, InsertProgress, InsertResult> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Show progress UI - set the entire progress section visible
            btnInsert.setEnabled(false);
            btnInsert.setText("Processing...");
            progressSection.setVisibility(View.VISIBLE); // Show parent layout
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setIndeterminate(false); // Switch to determinate mode
            tvProgressText.setVisibility(View.VISIBLE);
            tvInsertionSummary.setVisibility(View.VISIBLE);
            tvInsertionSummary.setText("Starting transaction insertion...");
            progressBar.setMax(0); // Will be set when we know the count
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected InsertResult doInBackground(List<Transaction1>... params) {
            List<Transaction1> selectedTransactions = params[0];
            InsertResult result = new InsertResult();
            result.totalTransactions = selectedTransactions.size();

            // Publish initial progress
            InsertProgress progress = new InsertProgress();
            progress.current = 0;
            progress.total = result.totalTransactions;
            progress.message = "Starting...";
            publishProgress(progress);

            for (int i = 0; i < selectedTransactions.size(); i++) {
                // Check if task is cancelled
                if (isCancelled()) {
                    result.cancelled = true;
                    break;
                }

                Transaction1 transaction = selectedTransactions.get(i);

                // Update progress
                progress.current = i + 1;
                progress.total = result.totalTransactions;
                progress.message = "Processing: " + transaction.getParticulars();
                publishProgress(progress);

                try {
                    // Calculate total amount
                    double debitAmount = Double.parseDouble(transaction.getDebit());

                    DatabaseHelper db = DatabaseHelper.getInstance(StatementImport.this);

                    // Check if transaction already exists
                    if (db.checkifExistsFromStatement(transaction.getCategory(),
                            transaction.getParticulars(),
                            transaction.getDebit(),
                            transaction.getFormattedDate())) {
                        result.duplicateCount++;

                        // Update progress for duplicate
                        progress.message = "Duplicate: " + transaction.getParticulars();
                        publishProgress(progress);

                        Log.i("TRANSACTION", "Duplicate: " + transaction);
                    } else {
                        // Insert new transaction
                        boolean insertSuccess = db.insertExpense(
                                transaction.getCategory(),
                                transaction.getParticulars(),
                                transaction.getDebit(),
                                null,
                                transaction.getFormattedDate(),
                                null, null, null, null, false);

                        if (insertSuccess) {
                            result.successCount++;
                            result.totalAmount += debitAmount;

                            // Update category totals
                            String category = transaction.getCategory();
                            result.categoryTotals.put(category,
                                    result.categoryTotals.getOrDefault(category, 0.0) + debitAmount);

                            // Update Firebase in background
                            try {
                                FirstFragment.saveToFirebase(
                                        transaction.getCategory(),
                                        transaction.getParticulars(),
                                        transaction.getDebit(),
                                        null,
                                        transaction.getFormattedDate(),
                                        null, null, null, false);
                            } catch (ParseException e) {
                                Log.e("FIREBASE", "Error saving to Firebase", e);
                            }

                            // Update progress for success
                            progress.message = "Inserted: " + transaction.getParticulars();
                            publishProgress(progress);

                            Log.i("INSERT", "Success: " + transaction);
                        } else {
                            result.failedCount++;

                            // Update progress for failure
                            progress.message = "Failed: " + transaction.getParticulars();
                            publishProgress(progress);

                            Log.i("INSERT", "Failed: " + transaction);
                        }
                    }

                    // Optional: Small delay to show progress clearly
//                    Thread.sleep(10);

                } catch (Exception e) {
                    result.failedCount++;
                    Log.e("PROCESSING", "Error processing transaction: " + transaction, e);
                }
            }

            return result;
        }

        @Override
        protected void onProgressUpdate(InsertProgress... values) {
            super.onProgressUpdate(values);
            InsertProgress progress = values[0];

            // Update progress bar
            progressBar.setMax(progress.total);
            progressBar.setProgress(progress.current);

            // Calculate percentage
            int percentage = 0;
            if (progress.total > 0) {
                percentage = (progress.current * 100) / progress.total;
            }

            // Update progress text with both count and percentage
            tvProgressText.setText(String.format(Locale.getDefault(),
                    "Processing %d of %d (%d%%)", progress.current, progress.total, percentage));

            // Update summary with current activity
            tvInsertionSummary.setText(progress.message);
        }

        @Override
        protected void onPostExecute(InsertResult result) {
            super.onPostExecute(result);

            // Hide progress bar and text, but keep the section visible for summary
            progressBar.setVisibility(View.GONE);
            tvProgressText.setVisibility(View.GONE);

            // Re-enable insert button
            btnInsert.setEnabled(true);
            btnInsert.setText("Insert Transactions");

            // Show detailed summary
            if (result.cancelled) {
                tvInsertionSummary.setText("Processing cancelled by user.");
                Toast.makeText(StatementImport.this, "Processing cancelled", Toast.LENGTH_SHORT).show();
            } else {
                showInsertionSummary(result);

                // Refresh the transaction list to remove processed items
                if (result.successCount > 0) {
                    refreshTransactionList();
                }
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            btnInsert.setEnabled(true);
            btnInsert.setText("Insert Transactions");
            progressSection.setVisibility(View.GONE); // Hide entire section
            tvInsertionSummary.setText("Processing cancelled.");
        }

        private void showInsertionSummary(InsertResult result) {
            DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
            StringBuilder summary = new StringBuilder();

            summary.append("✓ Processing Complete!\n\n");
            summary.append("Successful: ").append(result.successCount).append("\n");
            summary.append("Failed: ").append(result.failedCount).append("\n");
            summary.append("Duplicates: ").append(result.duplicateCount).append("\n");
            summary.append("Total Amount: ").append(decimalFormat.format(result.totalAmount)).append("\n");

            // Add category breakdown if available
            if (!result.categoryTotals.isEmpty()) {
                summary.append("\nCategory Breakdown:\n");
                for (Map.Entry<String, Double> entry : result.categoryTotals.entrySet()) {
                    String category = entry.getKey();
                    if (category == null || category.isEmpty()) {
                        category = "Uncategorized";
                    }
                    summary.append("• ").append(category).append(": ")
                            .append(decimalFormat.format(entry.getValue())).append("\n");
                }
            }

            tvInsertionSummary.setText(summary.toString());

            // Show toast with basic info
            String toastMessage = "Inserted " + result.successCount + " transactions";
            if (result.failedCount > 0) {
                toastMessage += " (" + result.failedCount + " failed)";
            }
            Toast.makeText(StatementImport.this, toastMessage, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Class to hold progress update information
     */
    private class InsertProgress {
        int current;
        int total;
        String message;
    }

    /**
     * Class to hold insertion result
     */
    private class InsertResult {
        int totalTransactions = 0;
        int successCount = 0;
        int failedCount = 0;
        int duplicateCount = 0;
        double totalAmount = 0.0;
        boolean cancelled = false;
        Map<String, Double> categoryTotals = new HashMap<>();
    }
}