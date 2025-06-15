package com.example.expenseutility;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.text.TextPaint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.Toast;

import com.example.expenseutility.database.DatabaseHelper;
import com.example.expenseutility.databinding.FragmentThirdBinding;
import com.example.expenseutility.entityadapter.ExpenseItem;
import com.example.expenseutility.utility.ExpenseAdapter;
import com.example.expenseutility.utility.SpinnerItem;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ThirdFragment extends Fragment {
    private FragmentThirdBinding binding;
    private DatabaseHelper db;

    private RecyclerView recyclerView;
    private ExpenseAdapter adapter;
    private List<ExpenseItem> expenseItems;

    private SearchView searchView;

    private List<ExpenseItem> filteredList = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentThirdBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = new DatabaseHelper(getContext());
        recyclerView = view.findViewById(R.id.seeAllRecyclerView);
        expenseItems = new ArrayList<>();
        searchView = view.findViewById(R.id.search);
        loadExpenseList();

        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        searchView.setOnLongClickListener(v ->  {

                // Get the current date to set as the initial selection in the picker
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH); // Month is 0-indexed (0 for January)

                // Create a new DatePickerDialog instance
                // We use our custom theme 'MonthYearPickerDialogTheme' to force spinner mode
                // and pass '1' for dayOfMonth as a placeholder, as it will be hidden.
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        getContext(),
//                        R.style.MonthYearPickerDialogTheme, // Apply our custom theme here
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                // monthOfYear is 0-indexed, so we add 1 to get the actual month number
                                // Format the month to always be two digits (e.g., 01, 02)
                                String formattedMonth = String.format(Locale.getDefault(), "%02d", monthOfYear + 1);
                                String selectedDate = year + "-" + formattedMonth; // Format as YYYY-MM
                                searchView.setQuery(selectedDate, true); // Display the selected date
                            }
                        },
                        year,
                        month,
                        1 // Day of month is not used, but a valid number is required by the constructor
                );
                // This is the hack to hide the day spinner:
                // Set an OnShowListener to manipulate the DatePicker's views after the dialog is shown.
                datePickerDialog.setOnShowListener(dialog -> {
                    // Find the day spinner using its common internal Android resource ID.
                    // This ID can vary slightly between Android versions.
                    int dayId = getResources().getIdentifier("day", "id", "android");
                    if (dayId != 0) {
                        View daySpinner = datePickerDialog.findViewById(dayId);
                        if (daySpinner != null) {
                            daySpinner.setVisibility(View.VISIBLE); // Hide the day spinner
                        }
                    }

                    // Another common ID for the day picker for some Android versions
                    int dayPickerId = getResources().getIdentifier("date_picker_day_picker", "id", "android");
                    if (dayPickerId != 0) {
                        View dayPicker = datePickerDialog.findViewById(dayPickerId);
                        if (dayPicker != null) {
                            dayPicker.setVisibility(View.VISIBLE); // Hide the day picker
                        }
                    }
                });
                // Show the DatePickerDialog
                datePickerDialog.show();
            return false;
        });

        ChipGroup chipGroup = view.findViewById(R.id.chip_group);
        String[] categories = {"Today's expenses","Housing Expenses", "Transportation", "Food", "Healthcare", "Fuel", "Debt Payments", "Entertainment"
        ,"Savings and Investments","Grocery","Clothing and Personal Care",
        "Education","Charity and Gifts","Travel","Insurance","Childcare and Education","Miscellaneous"};

        for (String category : categories) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_item, chipGroup, false);
            chip.setText(category);
            chip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    filterExpensesByCategory(); // Call your filtering method
                }
            });
            chipGroup.addView(chip);
        }

        binding.openFilteredChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int[] collect = filteredList.stream().mapToInt(ExpenseItem::getId).toArray();

                Intent intent = new Intent(getContext(), ChartActivity.class);
                intent.putExtra("filteredByDate", (Serializable) true);
                intent.putExtra("filteredList", (Serializable) collect);
                startActivity(intent);

                Toast.makeText(getContext(), "Chart Filtered ", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnMonthlyPdf.setOnClickListener(new View.OnClickListener() {
            private Paint textPaint = new TextPaint();

            @Override
            public void onClick(View v) {
                int totalAmount=0;
                Map<String, Long> cateValMap = new HashMap<>();

                int[] expIds = filteredList.stream().mapToInt(ExpenseItem::getId).toArray();

                Cursor cursor = db.getExpenseByIds(expIds);

                while(cursor.moveToNext()) {

                    int expCateIdx = cursor.getColumnIndex("expenseCategory");
                    int amountIdx = cursor.getColumnIndex("amount");


                    String expenseCategory = cursor.getString(expCateIdx);
                    Integer expenseCategoryAmount = cursor.getInt(amountIdx);

                    if(cateValMap.containsKey(expenseCategory)) {
                        cateValMap.put(expenseCategory, cateValMap.get(expenseCategory) + expenseCategoryAmount);
                    } else {

                        cateValMap.put(expenseCategory, Long.valueOf(expenseCategoryAmount));
                    }

                    totalAmount += expenseCategoryAmount;

                }

                Log.i("map >> ", cateValMap.toString());
                Log.i("totalAmount >> ", String.valueOf(totalAmount));

                int finalTotalAmount = totalAmount;
                List<ExpenseItem> exList = new ArrayList<ExpenseItem>();
                cateValMap.forEach((key, val) -> {
                    Log.i(" k-v ", key+ " - " + String.format("%.2f",((float)val/ finalTotalAmount)*100) + "% - "+val);

                    exList.add(new ExpenseItem(null,val,null,key));

                });

                generateExpenseReportPdf(getContext(), exList, finalTotalAmount);


            }

            private void generateExpenseReportPdf(Context context, List<ExpenseItem> exList, int finalTotalAmount) {


//                String.format("%.2f",((float)val/ finalTotalAmount)*100)

                int pageWidth = 595;
                int pageHeight = 842;

                PdfDocument pdfDocument = new PdfDocument();
                Paint paint = new Paint();
                Paint headerPaint = new Paint();

                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
                PdfDocument.Page page = pdfDocument.startPage(pageInfo);
                Canvas canvas = page.getCanvas();

                paint.setTextSize(12);
                paint.setColor(Color.BLACK);

                headerPaint.setTextSize(14);
                headerPaint.setColor(Color.parseColor("#1565C0"));
                headerPaint.setFakeBoldText(true);

                int x = 40;
                int y = 50;

                canvas.drawText("Expense Report", x + 200, y, headerPaint);
                y += 30;

                // 📅 Add current date in MMM-YYYY format (e.g., Jun-2025)
                String formattedDate = new SimpleDateFormat("MMM-yyyy").format(new Date());
                headerPaint.setTextSize(12);
                canvas.drawText(formattedDate, pageWidth / 2.5f, y, headerPaint);
                y += 30;

                // ====== Draw Pie Chart ======
                int pieCenterX = pageWidth / 2;
                int pieCenterY = y + 80;
                int pieRadius = 100;
                RectF pieBounds = new RectF(pieCenterX - pieRadius, pieCenterY - pieRadius, pieCenterX + pieRadius, pieCenterY + pieRadius);

// Example colors
                int[] pieColors = {
                        Color.parseColor("#F44336"), // red
                        Color.parseColor("#4CAF50"), // green
                        Color.parseColor("#2196F3"), // blue
                        Color.parseColor("#FF9800"), // orange
                        Color.parseColor("#9C27B0")  // purple
                };

                Paint piePaint = new Paint();
                piePaint.setStyle(Paint.Style.FILL);

                float totalPercent = 0;
                for (ExpenseItem item : exList) {
                    String per = String.format("%.2f",((float)item.getExpenseAmount()/ finalTotalAmount)*100);

                    totalPercent += parsePercentage(per);
                }

                float startAngle = 0;
                int colorIndex = 0;

// Draw slices
                for (ExpenseItem item : exList) {
                    String per = String.format("%.2f",((float)item.getExpenseAmount()/ finalTotalAmount)*100);

                    float percent = parsePercentage(per);
                    float sweepAngle = (percent / totalPercent) * 360f;

                    piePaint.setColor(pieColors[colorIndex % pieColors.length]);
                    canvas.drawArc(pieBounds, startAngle, sweepAngle, true, piePaint);
                    startAngle += sweepAngle;
                    colorIndex++;
                }

                // ====== Draw Legend: 2 items per line ======
                int legendStartX = pageWidth / 6;
                int legendStartY = pieCenterY + pieRadius + 30;
                int legendX = legendStartX;
                int legendY = legendStartY;

                textPaint.setTextAlign(Paint.Align.LEFT);
                textPaint.setTextSize(12);

                int legendSpacing = 200; // space between 2 legends
                int itemsPerLine = 2;
                int itemsOnThisLine = 0;

                colorIndex = 0;

                for (ExpenseItem item : exList) {
                    String per = String.format("%.2f",((float)item.getExpenseAmount()/ finalTotalAmount)*100);

                    piePaint.setColor(pieColors[colorIndex % pieColors.length]);

                    // Draw color box
                    canvas.drawRect(legendX, legendY, legendX + 20, legendY + 20, piePaint);

                    // Draw category text next to box
                    canvas.drawText(item.getExpenseCategory() + " (" + per + "%)", legendX + 30, legendY + 15, textPaint);

                    colorIndex++;
                    itemsOnThisLine++;

                    // Move to next position
                    if (itemsOnThisLine >= itemsPerLine) {
                        // Move to next line
                        legendX = legendStartX;
                        legendY += 30;
                        itemsOnThisLine = 0;
                    } else {
                        // Move to the right
                        legendX += legendSpacing;
                    }
                }

                // Adjust y for table after pie + legend
                y = legendY + 50;

                canvas.drawText("Expense Details", x + 200, y, headerPaint);
                y += 30;


                // Table Headers
                headerPaint.setTextSize(12);
                canvas.drawText("SrNo", x, y, headerPaint);
                canvas.drawText("Expense Category", x + 50, y, headerPaint);
                canvas.drawText("Percentage", x + 200, y, headerPaint);
                canvas.drawText("ExpAmount", x + 300, y, headerPaint);
                canvas.drawText("Comments", x + 400, y, headerPaint);
                y += 15;

                // Divider line
                canvas.drawLine(x, y, pageWidth - x, y, headerPaint);
                y += 20;

                int srNo = 1;
                for (ExpenseItem item : exList) {

                    String per = String.format("%.2f%%",((float)item.getExpenseAmount()/ finalTotalAmount)*100);

                    canvas.drawText(String.valueOf(srNo), x, y, paint);
                    canvas.drawText(item.getExpenseCategory(), x + 50, y, paint);
                    canvas.drawText(per, x + 200, y, paint);
                    canvas.drawText(String.valueOf("\u20B9"+item.getExpenseAmount()), x + 300, y, paint);
                    canvas.drawText("", x + 400, y, paint);
                    y += 20;
                    srNo++;
                }

                pdfDocument.finishPage(page);

                // Save to storage
                File pdfDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "ExpenseReports");
                if (!pdfDir.exists()) pdfDir.mkdirs();

                String fileName = "ExpenseReportMonthly_" + System.currentTimeMillis() + ".pdf";
                File file = new File(pdfDir, fileName);

                try {
                    pdfDocument.writeTo(new FileOutputStream(file));
                    Toast.makeText(context, "PDF Saved: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();


                    // 🔓 Get URI using FileProvider
                    Uri pdfUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);

                    // 📂 Create Intent to view PDF
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(pdfUri, "application/pdf");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    // 🎯 Start activity to view
                    context.startActivity(Intent.createChooser(intent, "Open PDF with"));


                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Failed to save PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                pdfDocument.close();


            }

            private float parsePercentage(String percentStr) {
                try {
                    return Float.parseFloat(percentStr.replace("%", "").trim());
                } catch (NumberFormatException e) {
                    return 0;
                }
            }


        });



        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchList(newText);
                return true;
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());

        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new ExpenseAdapter(getContext(), expenseItems);

        recyclerView.setAdapter(adapter);



    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (DatePicker view, int selectedYear, int selectedMonth, int selectedDay) -> {
                    String selectedDate = selectedYear + "-" + String.format("%02d",(selectedMonth + 1)) + "-" + String.format("%02d",selectedDay);
                    searchView.setQuery(selectedDate, true);
                },
                year, month, day);
        datePickerDialog.show();
    }

    public void filterExpensesByCategory() {
        String selectedCategories ="";
        for (int i = 0; i < binding.chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) binding.chipGroup.getChildAt(i);
            if (chip.isChecked()) {
                selectedCategories = chip.getText().toString();
            }
        }
        adapter.filterExpensesByCategory(selectedCategories, binding.bannerTxt);
    }



    private void searchList(String newText) {
        List<ExpenseItem> expenseItemsForSearch = new ArrayList<>();
        boolean searchByDate = false;
        if (ThirdFragment.isValidDate(newText)) {
            Log.d("DateValidator", newText + " is valid.");
            searchByDate = true;
        } else {
            Log.d("DateValidator", newText + " is invalid.");
            searchByDate = false;
        }
        float income = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).getFloat("monthlyIncome", 87000.0f);

        for (ExpenseItem item : expenseItems) {
            if(searchByDate){

                if(newText.length()==7) {
                    //search by year-month  2025-06

//                    Log.i(" >> text captured: ", newText);
                    if(item.getExpenseDate().toLowerCase().startsWith(newText.toLowerCase())) {
                        expenseItemsForSearch.add(item);
                    }
                    Log.i(" >> date captured: ", item.getExpenseDate());

                } else {
                    if(item.getExpenseDate().toLowerCase().contains(newText.toLowerCase())) {
                        expenseItemsForSearch.add(item);
                    }
                }
            } else {
                if(item.getExpenseCategory().toLowerCase().contains(newText.toLowerCase())) {
                    expenseItemsForSearch.add(item);
                }
                binding.openFilteredChart.setVisibility(View.GONE);
                binding.filteredListExpenseCalcText.setVisibility(View.GONE);
            }
        }
        if(expenseItemsForSearch.isEmpty()){
//            Toast.makeText(getContext(), "Not found", Toast.LENGTH_SHORT).show();
            binding.openFilteredChart.setVisibility(View.GONE);
            binding.filteredListExpenseCalcText.setVisibility(View.GONE);
            adapter.setSearchList(expenseItemsForSearch);

        } else {
            adapter.setSearchList(expenseItemsForSearch);
            filteredList.clear();
            filteredList.addAll(expenseItemsForSearch);
            if(expenseItemsForSearch.size() < expenseItems.size()) {
                binding.openFilteredChart.setVisibility(View.VISIBLE);
                binding.btnMonthlyPdf.setVisibility(View.VISIBLE);
                if(!filteredList.isEmpty()) {
                    long sum = filteredList.stream().mapToLong(expenseItem -> expenseItem.getExpenseAmount()).sum();

                    float monthly = (float) sum/income;
                    float daily = (float) sum / (income/30);
                    monthly = monthly*100;
                    daily = daily*100;



                    binding.filteredListExpenseCalcText.setText("Total  \u20B9"+sum +"  Monthly ("+String.format("%.2f",monthly)+"%)\nDaily("+String.format("%.2f",daily)+"%)");
                }

                binding.filteredListExpenseCalcText.setVisibility(View.VISIBLE);
            }
        }

    }

    // Regex for multiple date formats including slashes and dashes
    private static final String DATE_PATTERN =
            "^\\d{4}[-/]\\d{2}[-/]\\d{2}$|" +   // yyyy-MM-dd or yyyy/MM/dd (e.g., 2024-09-09 or 2024/09/09)
                    "^\\d{2}[-/]\\d{2}[-/]\\d{4}$|" +   // dd-MM-yyyy or dd/MM/yyyy (e.g., 09-09-2024 or 09/09/2024)
                    "^\\d{2}[-/]\\d{2}[-/]\\d{2}$|" +   // yy-MM-dd or dd-MM-yy, also with slashes (e.g., 24-09-09 or 09/09/24)
                    "^\\d{2}[-/]\\d{2}[-/]\\d{4}$|" +     // MM-dd-yyyy or MM/dd/yyyy (e.g., 09-24-2024 or 09/24/2024)
                    "^\\d{4}[-/]\\d{2}$";
    // Method to validate the input text with regex
    public static boolean isValidDate(String inputText) {
        Pattern pattern = Pattern.compile(DATE_PATTERN);
        Matcher matcher = pattern.matcher(inputText);
        return matcher.matches();  // Return true if input matches the regex
    }



    private void loadExpenseList() {




        Cursor expenseData = db.getAllExpenseData();
        if(expenseData.getCount() > 0){
            expenseItems.clear();

            while(expenseData.moveToNext()){
                expenseItems.add(new ExpenseItem(
                        Integer.valueOf(expenseData.getInt(0)),
                        String.valueOf(expenseData.getString(2)),
                        expenseData.getLong(3),
                        String.valueOf(expenseData.getString(5)),
                        String.valueOf(expenseData.getString(1)),
                        String.valueOf(expenseData.getString(6)),
                        expenseData.getBlob(7))
                );
            }

            updateImageDrawableInExpenseItems(expenseItems);

        } else {
            Toast.makeText(getContext(), "Expense List is empty !", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateImageDrawableInExpenseItems(List<ExpenseItem> expenseItems) {

        List<SpinnerItem> items = new ArrayList<>();

        items = FirstFragment.fetchAllSpinnerOptions(items);


        List<SpinnerItem> finalItems = items;
        expenseItems.forEach(e -> {

            if(!finalItems.stream().anyMatch(spinnerItem -> spinnerItem.getText().equalsIgnoreCase(e.getExpenseCategory()))) {
                e.setExpenseCategory("Miscellaneous");
            }

            List<SpinnerItem> items1 = finalItems.stream().filter(i -> i.getText().equalsIgnoreCase(e.getExpenseCategory())).collect(Collectors.toList());
            e.setImageDrawableId(items1.get(0).getImageResourceId());
        });

    }
}