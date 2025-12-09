package com.example.expenseutility;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.expenseutility.database.DatabaseHelper;
import com.example.expenseutility.entityadapter.ExpenseItem;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final String TAG = "ReportActivity";

    private int selectedYear;
    private int selectedMonth;
    private ProgressBar progressBar;
    private TextView statusTextView;
    private Button btnGenerateReport;
    private Button btnViewReport;
    private Button btnShareReport;
    private File generatedPdfFile;

    private DatabaseHelper databaseHelper;
    private String monthFormatted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        databaseHelper = new DatabaseHelper(this);

        initializeViews();
        setupListeners();

        // Check if we have sample data or get from intent
        List<ExpenseItem> expenseItems = getExpenseItems();
        updateStatus("Ready to generate report for " + expenseItems.size() + " items");
    }

    private void initializeViews() {
        progressBar = findViewById(R.id.progressBar);
        statusTextView = findViewById(R.id.statusTextView);
        btnGenerateReport = findViewById(R.id.btnGenerateReport);
        btnViewReport = findViewById(R.id.btnViewReport);
        btnShareReport = findViewById(R.id.btnShareReport);

        // Initially disable view and share buttons
        btnViewReport.setEnabled(false);
        btnShareReport.setEnabled(false);
    }

    private void setupListeners() {
        btnGenerateReport.setOnClickListener(v -> {
//            if (checkStoragePermission()) {

            showYearMonthPickerDialog();


//            generateReport();
//            }
        });

        btnViewReport.setOnClickListener(v -> viewReport());

        btnShareReport.setOnClickListener(v -> shareReport());
    }

    private void showYearMonthPickerDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_year_month_picker);
        dialog.setTitle("Generate Report");
        dialog.setCancelable(true);

        CheckBox cbAllData = dialog.findViewById(R.id.cbAllData);
        LinearLayout layoutYear = dialog.findViewById(R.id.layoutYear);
        LinearLayout layoutMonth = dialog.findViewById(R.id.layoutMonth);
        Spinner spinnerYear = dialog.findViewById(R.id.spinnerYear);
        Spinner spinnerMonth = dialog.findViewById(R.id.spinnerMonth);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirm);

        // Prepare year list
        List<String> years = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        for (int year = 2020; year <= currentYear + 1; year++) {
            years.add(String.valueOf(year));
        }

        // Prepare month list
        String[] months = new String[]{
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };

        // Set up adapters
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);

        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);

        // Set default selections to current month and year
        spinnerYear.setSelection(years.indexOf(String.valueOf(currentYear)));
        spinnerMonth.setSelection(calendar.get(Calendar.MONTH));

        // Checkbox listener to enable/disable date spinners
        cbAllData.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Disable date selection
                    layoutYear.setVisibility(View.GONE);
                    layoutMonth.setVisibility(View.GONE);
                    layoutYear.setAlpha(0.6f);
                    layoutMonth.setAlpha(0.6f);
                } else {
                    // Enable date selection
                    layoutYear.setVisibility(View.VISIBLE);
                    layoutMonth.setVisibility(View.VISIBLE);
                    layoutYear.setAlpha(1.0f);
                    layoutMonth.setAlpha(1.0f);
                }
            }
        });

        // Button click listeners
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cbAllData.isChecked()) {
                    // Generate report for all data
                    generateReport(null, null); // Pass null to indicate all data
                    Toast.makeText(ReportActivity.this, "Generating report for all data", Toast.LENGTH_SHORT).show();
                } else {
                    // Generate report for selected month and year
                    int selectedYear = Integer.parseInt(spinnerYear.getSelectedItem().toString());
                    int selectedMonth = spinnerMonth.getSelectedItemPosition(); // 0-based
                    String monthFormatted = String.format("%02d", selectedMonth + 1);

                    String message = "Generating report for " + months[selectedMonth] + " " + selectedYear;
                    Toast.makeText(ReportActivity.this, message, Toast.LENGTH_SHORT).show();

                    generateReport(selectedYear + "", monthFormatted);
                }
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private List<ExpenseItem> getExpenseItems() {
        // Get expense items from intent or database
        // This is sample data - replace with your actual data source
        List<ExpenseItem> items = new ArrayList<>();

        // Add sample data
        items.add(new ExpenseItem("Office Lunch", 450L, "2024-01-15", "Food"));
        items.add(new ExpenseItem("Taxi to Meeting", 320L, "2024-01-15", "Transport"));
        items.add(new ExpenseItem("Project Supplies", 1250L, "2024-01-14", "Office"));
        items.add(new ExpenseItem("Client Dinner", 1800L, "2024-01-13", "Food"));
        items.add(new ExpenseItem("Internet Bill", 1200L, "2024-01-12", "Utilities"));
        items.add(new ExpenseItem("Team Building", 2500L, "2024-01-11", "Entertainment"));
        items.add(new ExpenseItem("Coffee Supplies", 650L, "2024-01-10", "Office"));
        items.add(new ExpenseItem("Parking Fee", 150L, "2024-01-09", "Transport"));

        return items;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                generateReport(selectedYear + "", monthFormatted);
            } else {
                Toast.makeText(this,
                        "Storage permission is required to save PDF",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void generateReport(String selectedYear, String monthFormatted) {
        String monthFilter = "";
        if (selectedYear != null && monthFormatted != null) {
            monthFilter = selectedYear + "-" + monthFormatted;
        }
        List<ExpenseItem> expenseItems = databaseHelper.getExpenseDataList(monthFilter);
        // Create filename with timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String fileName;
        if (monthFilter.isEmpty()) {
            fileName = "ExpenseReport_" + sdf.format(new Date());
        } else {
            fileName = "ExpenseReport_" + monthFilter + "_" + sdf.format(new Date());
        }

        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        btnGenerateReport.setEnabled(false);
        updateStatus("Generating PDF report...");

        // Create PDF generator
        ExpensePDFGenerator pdfGenerator = new ExpensePDFGenerator(this);

        // Generate report
        pdfGenerator.generateExpenseReport(expenseItems, fileName,
                new ExpensePDFGenerator.PDFGenerationCallback() {
                    @Override
                    public void onSuccess(File pdfFile) {
                        runOnUiThread(() -> {
                            generatedPdfFile = pdfFile;
                            progressBar.setVisibility(View.GONE);
                            btnGenerateReport.setEnabled(true);

                            // Enable view and share buttons
                            btnViewReport.setEnabled(true);
                            btnShareReport.setEnabled(true);

                            updateStatus("Report generated successfully!\n" +
                                    "File saved: " + pdfFile.getName());

                            Toast.makeText(ReportActivity.this,
                                    "PDF generated successfully",
                                    Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onError(String errorMessage) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            btnGenerateReport.setEnabled(true);
                            updateStatus("Error: " + errorMessage);

                            Toast.makeText(ReportActivity.this,
                                    "Failed to generate PDF: " + errorMessage,
                                    Toast.LENGTH_LONG).show();
                        });
                    }

                    @Override
                    public void onProgress(String message) {
                        runOnUiThread(() -> {
                            updateStatus(message);
                        });
                    }
                });
    }

    private void viewReport() {
        if (generatedPdfFile != null && generatedPdfFile.exists()) {
            // Create URI using FileProvider for secure file sharing

            Uri pdfUri = FileProvider.getUriForFile(this, this.getPackageName() + ".fileprovider", generatedPdfFile);

            // Create intent to view PDF
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(pdfUri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Check if there's a PDF viewer app
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this,
                        "No PDF viewer app found. Please install one.",
                        Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this,
                    "PDF file not found. Please generate the report first.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void shareReport() {
        if (generatedPdfFile != null && generatedPdfFile.exists()) {
            // Create URI using FileProvider
            Uri pdfUri = FileProvider.getUriForFile(this, this.getPackageName() + ".fileprovider", generatedPdfFile);

            // Create share intent
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Expense Report");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Here is my expense report.");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Start share activity
            startActivity(Intent.createChooser(shareIntent, "Share Expense Report"));
        } else {
            Toast.makeText(this,
                    "PDF file not found. Please generate the report first.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void updateStatus(String message) {
        statusTextView.setText(message);
    }


}