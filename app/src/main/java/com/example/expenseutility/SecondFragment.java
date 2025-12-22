package com.example.expenseutility;

import static android.app.Activity.RESULT_OK;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.CursorWindow;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expenseutility.database.DatabaseHelper;
import com.example.expenseutility.databinding.FragmentSecondBinding;
import com.example.expenseutility.dto.ExpenseMonth;
import com.example.expenseutility.entityadapter.ExpenseDetailsAdapter;
import com.example.expenseutility.entityadapter.ExpenseItem;
import com.example.expenseutility.entityadapter.ExpenseMonthAdapter;
import com.example.expenseutility.python.TFLiteModel;
import com.example.expenseutility.utility.Commons;
import com.example.expenseutility.utility.CustomSpinnerAdapter;
import com.example.expenseutility.utility.ExpenseGroupingUtility;
import com.example.expenseutility.utility.SpinnerItem;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class SecondFragment extends Fragment {

    private static final int PICK_PDF_REQUEST = 312;
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB in bytes
    private static DatabaseHelper db;
    private static CopyOnWriteArrayList<ExpenseItem> expenseItems = new CopyOnWriteArrayList<>();
    private static ExpenseDetailsAdapter expenseDetailsAdapter;
    ProgressBar progressBar;
    float total = 0;
    private byte[] pdfBytes;
    private String fileName;
    private FragmentSecondBinding binding;
    private TFLiteModel tfliteModel;
    private float[] binaryArray;
    private float[] binaryArrayForCityType;
    private ArrayList<TableRow> tableRows = new ArrayList<>(); // To keep track of the rows
    private ConstraintLayout layout;
    private LinearLayout linearLayout;
    private TableLayout expenseDetailsLayout;
    private String dateVal;
    private String dateTimeVal;
    private EditText chooseFileTextEdit;
    private ListView expenseDetailsListView;
    private ListView listViewTest;
    private ImageButton expMonthtoggle;
    private RecyclerView recyclerView;
    private ExpenseMonthAdapter adapter;
    private List<ExpenseMonth> expenseList;
    private LinearLayout expenseMonthLayout;

    public static void callOnClickListener(Context context, String delId, int position, ExpenseItem expenseItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete " + delId);
        builder.setMessage("Proceed for deletion ? ");
        builder.setIcon(R.drawable.file_delete_svgrepo_com);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int count = db.deleteRow(Integer.parseInt("" + delId));
                if (count > 0) {
                    Toast.makeText(context, "Deleted ID " + delId, Toast.LENGTH_SHORT).show();

                    deleteRecordFromCloud(expenseItem);

                    expenseItems.remove(position);
                    expenseDetailsAdapter.notifyDataSetChanged();


                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void deleteRecordFromCloud(ExpenseItem expenseItemObj) {

        // Reference to the Firebase Realtime Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference(Build.MODEL + "/" + "expenses");

        String childPath = "";
        String expDate = expenseItemObj.getExpenseDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(expDate, formatter);

        int year = date.getYear();

        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM"); // "MMM" gives abbreviated month
        String month = monthFormatter.format(date);

        childPath = "/" + year + "/" + (month + "-" + year) + "/" + expDate;


        String finalChildPath = childPath;
        databaseReference.child(childPath).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChildren()) {
                    boolean dataExists = false;
                    for (DataSnapshot d3 : snapshot.getChildren()) {
                        ExpenseItem expenseItem = d3.getValue(ExpenseItem.class);
                        if (expenseItem.getExpenseParticulars().equalsIgnoreCase(expenseItemObj.getExpenseParticulars()) &&
                                expenseItem.getExpenseCategory().equalsIgnoreCase(expenseItemObj.getExpenseCategory()) &&
                                expenseItem.getExpenseAmount().toString().equals(expenseItemObj.getExpenseAmount().toString()) &&
                                expenseItem.getExpenseDateTime().equalsIgnoreCase(expenseItemObj.getExpenseDateTime()) &&
                                expenseItem.getExpenseDate().equalsIgnoreCase(expenseItemObj.getExpenseDate())
                        ) {
                            dataExists = true;
                            databaseReference.child(finalChildPath + "/" + d3.getKey()).removeValue();
//                            Toast.makeText(this, "Deleted "+i.getExpenseParticulars() +" "+i.getExpenseDateTime()+" "+i.getExpenseAmount(),
//                                    Toast.LENGTH_LONG).show();


                            break;
                        } else {
                            dataExists = false;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = new DatabaseHelper(getContext());
        layout = view.findViewById(R.id.layout);
        linearLayout = view.findViewById(R.id.linearLayout);
        expenseDetailsLayout = view.findViewById(R.id.expenseDetailsLayout);
        expenseDetailsListView = view.findViewById(R.id.expenseDetailsListView);
        expMonthtoggle = view.findViewById(R.id.expMonthtoggle);
//        expenseMonthLayout = view.findViewById(R.id.expenseMonthLayout);
        AtomicBoolean isEnabled = new AtomicBoolean(false);


        expMonthtoggle.setOnClickListener(v -> {
//            if (!isEnabled.get()) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Expand Month");
            builder.setIcon(R.drawable.calendar_svgrepo_com);
            builder.setCancelable(false);
            View view1 = getLayoutInflater().inflate(R.layout.alert_diag_month_expense_layout, null, false);
            builder.setView(view1);


            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });


            // Initialize RecyclerView
            recyclerView = view1.findViewById(R.id.recyclerViewMonth);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

            // Create sample data
            expenseList = new ArrayList<>();

            // Group expenses by month using the utility
            expenseList = ExpenseGroupingUtility.groupExpensesByMonth(expenseItems);

            expenseList.stream().findFirst().ifPresent(expenseMonth -> {
                expenseMonth.setExpanded(true);
            });

            // Setup adapter
            adapter = new ExpenseMonthAdapter(getContext(), expenseList);
            recyclerView.setAdapter(adapter);


//            isEnabled.set(true);
//            } else {
//                isEnabled.set(false);
//                expenseMonthLayout.setVisibility(View.GONE);
//            }
            AlertDialog dialog = builder.create();
            dialog.show();
        });


        progressBar = new ProgressBar(getContext());
        try {
            populateTable1();
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        if (getArguments() != null) {
            String date = getArguments().getString("date");

            List<ExpenseItem> filteredList = expenseItems.stream().filter(expenseItem -> expenseItem.getExpenseDate().contains(date)).collect(Collectors.toList());

            long monthExpSum = filteredList.stream().mapToLong(ExpenseItem::getExpenseAmount).sum();
            NumberFormat formatter = NumberFormat.getInstance(new Locale("en", "IN"));
            int roundedAmount = (int) monthExpSum;

            String formattedAmount = "₹" + formatter.format(roundedAmount);
            binding.tvHeading1.setText("Total  " + formattedAmount);
            expenseDetailsAdapter.filterMonthlyList(getContext(), filteredList);

        }

        binding.btnPredict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setCancelable(false);

                View predictorView = getLayoutInflater().inflate(R.layout.model_predictor_view, null, false);

                EditText etArea = predictorView.findViewById(R.id.etArea);

                builder.setView(predictorView);
                builder.setTitle("Real estate prices prediction");
                Spinner locationSpinner = predictorView.findViewById(R.id.locationSpinner);
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                        R.array.location_array, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                locationSpinner.setAdapter(adapter);
                locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String selectedLocation = parent.getItemAtPosition(position).toString();
                        binaryArray = convertToBinaryArray(selectedLocation);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // Do nothing
                    }
                });


                Spinner cityTypeSpinner = predictorView.findViewById(R.id.cityTypeSpinner);
                ArrayAdapter<CharSequence> cityTypeSpinnerAdapter = ArrayAdapter.createFromResource(getContext(),
                        R.array.location_array_city_spinner, android.R.layout.simple_spinner_item);
                cityTypeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                cityTypeSpinner.setAdapter(cityTypeSpinnerAdapter);
                cityTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String selectedLocation = parent.getItemAtPosition(position).toString();
                        binaryArrayForCityType = convertToBinaryArrayForCityType(selectedLocation);
//                        Toast.makeText(getContext(), "Selected: " + selectedLocation, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // Do nothing
                    }
                });


//                builder.setView();
                builder.setPositiveButton("Predict", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            if (etArea.getText().toString().isEmpty()) {
                                Toast.makeText(getContext(), "Please provide valid input", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            tfliteModel = new TFLiteModel(getContext());

                            // Prepare input data
                            float[][] input = new float[1][4]; // Change inputSize to match your model's input shape
                            input[0][0] = Float.parseFloat(etArea.getText().toString()); // Example input, replace with actual input
                            input[0][1] = binaryArray[0];
                            input[0][2] = binaryArray[1];
                            input[0][3] = binaryArrayForCityType[0];
                            // Run inference
                            float[][] output = tfliteModel.runInference(input);


                            // Use the output
//                            Toast.makeText(getContext(), "Model Output: " + output[0][0], Toast.LENGTH_SHORT).show();

                            StringBuilder stringBuilder = new StringBuilder();
                            StringBuilder stringBuilder1 = new StringBuilder();

                            stringBuilder.append("Area (In Sq.ft): ");
                            stringBuilder.append("\n");

                            stringBuilder1.append("" + etArea.getText());
                            stringBuilder1.append("\n");

                            stringBuilder.append("Locality : ");
                            stringBuilder.append("\n");

                            stringBuilder1.append(locationSpinner.getSelectedItem());
                            stringBuilder1.append("\n");

                            stringBuilder.append("City Type : ");
                            stringBuilder.append("\n");

                            stringBuilder1.append(cityTypeSpinner.getSelectedItem());
                            stringBuilder1.append("\n");


                            stringBuilder.append("\n\n" + "Predicted price (INR) :");
                            stringBuilder.append("\n");

                            stringBuilder1.append("\n\n" + output[0][0]);
                            stringBuilder1.append("\n");


                            binding.tvPredResult.setText(stringBuilder);
                            binding.tvPredResult1.setText(stringBuilder1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();


            }
        });


        binding.pieChartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String date = getArguments() == null ? "" : getArguments().getString("date");
                int[] collect;
                if (!date.isEmpty()) {

                    List<ExpenseItem> filteredList = expenseItems.stream().filter(expenseItem -> expenseItem.getExpenseDate().contains(date)).collect(Collectors.toList());
                    collect = filteredList.stream().mapToInt(ExpenseItem::getId).toArray();
                } else {
                    collect = expenseItems.stream().mapToInt(ExpenseItem::getId).toArray();

                }

                Intent intent = new Intent(getContext(), ChartActivity.class);
                intent.putExtra("filteredByDate", true);
                intent.putExtra("filteredList", (Serializable) collect);
                startActivity(intent);
            }
        });

        binding.barChartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), BarChartActivity.class);
                startActivity(intent);
            }
        });

//        new LoadDataTask().execute();


        binding.seeAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_thirdFragment);
            }
        });

    }

    private void populateTable1() throws NoSuchFieldException, IllegalAccessException {
        Cursor data = db.getAllExpenseData();
        Field field = CursorWindow.class.getDeclaredField("sCursorWindowSize");
        field.setAccessible(true);
        field.set(null, 100 * 1024 * 1024); //100 MB is the new size
        if (data.getCount() > 0) {
            expenseItems.clear();
            total = 0;
            while (data.moveToNext()) {
                Integer id = data.getInt(0);
                String date = data.getString(5);
                String particulars = data.getString(2);
                Float amount = data.getFloat(3);
                String expDateTime = data.getString(4);
                String category = data.getString(1);
                String partDetails = data.getString(8);
                int isHomeExpense = data.getInt(9);

                ExpenseItem expenseItem = new ExpenseItem();
                expenseItem.setId(id);
                expenseItem.setExpenseAmount((long) Double.parseDouble(String.valueOf(amount)));
                expenseItem.setExpenseDate(date);
                expenseItem.setExpenseParticulars(particulars);
                expenseItem.setExpenseDateTime(expDateTime);
                expenseItem.setExpenseCategory(category);
                expenseItem.setPartDetails(partDetails);
                expenseItem.setHomeExpense(isHomeExpense == 1);
                expenseItems.add(expenseItem);
                total += amount;

            }
            int roundedAmount = (int) total;

            String formattedAmount = Commons.getFormattedCurrency(roundedAmount);

            binding.tvHeading1.setText("Total  " + formattedAmount);

//            Map<String, List<ExpenseItem>> map = processExpenseItems(expenseItems);
//
//            List<Object> groupedTransactions = new ArrayList<>();
//            for(Map.Entry<String, List<ExpenseItem>> entry : map.entrySet()) {
//                groupedTransactions.add(entry.getKey());
//                groupedTransactions.addAll(entry.getValue());
//            }


            // Set the ListView adapter
            expenseDetailsAdapter = new ExpenseDetailsAdapter(getContext(), expenseItems);
            expenseDetailsListView.setAdapter(expenseDetailsAdapter);

        }
    }

//    private class LoadDataTask extends AsyncTask<Void, Void, Void> {
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            // Show the ProgressBar before loading data
//            binding.pbar.setVisibility(View.VISIBLE);
//        }
//
//        @Override
//        protected Void doInBackground(Void... voids) {
//            try {
//                Thread.sleep(3000);
//                int n = 50000;
//                for (int i=0;i<n;i++) {
//                    Logger.getAnonymousLogger().info("BG thread: "+i);
//
//                    double track = ((double)i/n)*100;

    /// /                    if(track > 25f && track <= 35f) {
    /// /                        Logger.getAnonymousLogger().info("BG thread: Reached 25%");
    /// /                    } else if (track > 35f && track <= 50){
    /// /                        Logger.getAnonymousLogger().info("BG thread: Reached 50%");
    /// /                    } else if (track > 50f && track <= 75f){
    /// /                        Logger.getAnonymousLogger().info("BG thread: Reached 75%");
    /// /                    } else if (track > 75f) {
//                        Logger.getAnonymousLogger().info("BG thread: Reached "+String.format("%.2f",track) +"%");
    private Map<String, List<ExpenseItem>> processExpenseItems(CopyOnWriteArrayList<ExpenseItem> expenseItems) {
        Map<String, List<ExpenseItem>> mapItems = new LinkedHashMap<>();
        List<ExpenseItem> expenseItemList = new ArrayList<>();
        int counter = 12;
        for (int i = counter; i > 0; i--) {
            if (expenseItems.size() > 0) {
                for (ExpenseItem ex : expenseItems) {
                    if (ex.getExpenseDate().contains("2024-" + counter)) {
                        expenseItemList.add(ex);
                    }
                }
                mapItems.put("2024-" + counter, expenseItemList);
                expenseItems.removeAll(expenseItemList);
                counter--;
            }
//            expenseItemList.clear();

        }

        return mapItems;


    }

    /// /                    }
//
//                }
//
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            super.onPostExecute(aVoid);
//            // Hide the ProgressBar after data is loaded
//            binding.pbar.setVisibility(View.GONE);
//            // Show the TableLayout
//            expenseDetailsLayout.setVisibility(View.VISIBLE);
//        }
//    }
    private float[] convertToBinaryArrayForCityType(String cityType) {
        switch (cityType) {
            case "Metro":
                return new float[]{0.0f};
            case "Non-Metro":
                return new float[]{1.0f};
            default:
                return new float[]{0.0f};
        }
    }

    private float[] convertToBinaryArray(String location) {
        switch (location) {
            case "Rural":
                return new float[]{1.0f, 0.0f};
            case "Urban":
                return new float[]{0.0f, 1.0f};
            case "Town":
                return new float[]{0.0f, 0.0f};
            default:
                return new float[]{0.0f, 0.0f};
        }
    }

    @Override
    public void onResume() {
        super.onResume();

//        Cursor data = db.getAllExpenseData();
//        float total = 0;
//        if(data.getCount()>0){
//            StringBuilder stringBuilder = new StringBuilder();
//            while(data.moveToNext()){
//
//                Integer id = data.getInt(0);
//                String date = data.getString(5);
//                String particulars = data.getString(2);
//                Float amount = data.getFloat(3);
//                total += amount;
//                stringBuilder.append(id);
//                stringBuilder.append("  ");
//                stringBuilder.append(date);
//                stringBuilder.append("  ");
//                stringBuilder.append(particulars);
//                stringBuilder.append("  ");
//                stringBuilder.append(amount);
//                stringBuilder.append("\n");
//                createDownloadLink(data.getBlob(6));
//
//            }
//            binding.textViewAllData.setText(stringBuilder);
//            binding.tvHeading.setText("Expense Details (Total "+ total+")");
//        }

//        populateTable();


    }

    private void populateTable() throws NoSuchFieldException, IllegalAccessException {
        Cursor data = db.getAllExpenseData();
        Field field = CursorWindow.class.getDeclaredField("sCursorWindowSize");
        field.setAccessible(true);
        field.set(null, 100 * 1024 * 1024); //100 MB is the new size
        if (data.getCount() > 0) {

            total = 0;
            while (data.moveToNext()) {
                Integer id = data.getInt(0);
                String date = data.getString(5);
                String particulars = data.getString(2);
                Float amount = data.getFloat(3);
                String fileName = data.getString(6);
                total += amount;
                TextView dwnldDocLink = createDownloadLink(data.getBlob(7), fileName);
                dwnldDocLink.setPadding(10, 10, 10, 10);
                dwnldDocLink.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);


                TableRow tableRow = new TableRow(getContext());
                tableRow.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                tableRow.setPadding(0, 5, 0, 5);
                tableRow.setVerticalGravity(Gravity.CENTER_VERTICAL);

//                CheckBox checkBox = crateCheckBox(data.getInt(0));

                TextView srNoView = new TextView(getContext());
                srNoView.setText(String.valueOf(id));
                srNoView.setPadding(10, 10, 10, 10);
                srNoView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                TextView dateView = new TextView(getContext());
                dateView.setText(date);
                dateView.setPadding(10, 10, 10, 10);
                dateView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                TextView particularsView = new TextView(getContext());
                particularsView.setText(particulars);
                particularsView.setPadding(10, 10, 10, 10);
                particularsView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                if (isDarkModeOn()) {
                    particularsView.setTextColor(getResources().getColor(R.color.darkbluecolor));
                } else {
                    particularsView.setTextColor(getResources().getColor(R.color.purple_700));
                }
                particularsView.setClickable(true);
                particularsView.setFocusable(true);
                particularsView.setTypeface(particularsView.getTypeface(), Typeface.BOLD);


                // commented the EDIT functionality on saved expenses
//                        particularsView.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
////                        Toast.makeText(getContext(), "Me dislo "+ particularsView.getText(), Toast.LENGTH_SHORT).show();
//
//                                //show popup menu
//
//                                PopupMenu popupMenu = new PopupMenu(getContext(), particularsView);
//                                popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
////                        MenuItem menuItem = popupMenu.getMenu().getItem(0);
////                        menuItem.setTitle("Edit " + particularsView.getText());
//                                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                                    @Override
//                                    public boolean onMenuItemClick(MenuItem menuItem) {
//
//                                        String menuItemTitle = menuItem.getTitle().toString();
//
//                                        if ("Edit".equalsIgnoreCase(menuItemTitle)) {
//
//                                            openEditAlertDialog(particularsView, id);
//
//                                            return true;
//                                        }
//                                        return false;
//                                    }
//                                });
//
//                                // Show the PopupMenu
//                                popupMenu.show();
//
//
//
//                            }
//                        });


                TextView amountView = new TextView(getContext());
                amountView.setText(String.valueOf(amount));
                amountView.setPadding(10, 10, 10, 10);
                amountView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                ImageButton imageButton = createImageButton(data.getInt(0), tableRow);

//                tableRow.addView(checkBox);
                tableRow.addView(srNoView);
                tableRow.addView(dateView);
                tableRow.addView(particularsView);
                tableRow.addView(amountView);
                tableRow.addView(dwnldDocLink);
                tableRow.addView(imageButton);
//                        tableRow.setBackgroundResource(R.drawable.border_v2);
                expenseDetailsLayout.addView(tableRow);
                tableRows.add(tableRow);


            }
            binding.tvHeading1.setText("Expense Details (Total " + total + ")");


        }


    }

    private void openEditAlertDialog(TextView particularsView, Integer id) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit " + particularsView.getText());
        builder.setMessage("ID " + id + " is now editable ");
        builder.setCancelable(false);

        View view = getLayoutInflater().inflate(R.layout.edit_layout, null, false);

//        addEventListeners(view, id);

        Spinner spinnerEdit = view.findViewById(R.id.spinnerOptionsEdit);
        EditText etParticularsEdit = view.findViewById(R.id.et_particularsEdit);
        EditText etAmountEdit = view.findViewById(R.id.et_amountEdit);
        EditText etDateTimeEdit = view.findViewById(R.id.et_date_timeEdit);
        chooseFileTextEdit = view.findViewById(R.id.chooseFileTextEdit);

        Cursor cursor = db.getExpenseById(id);

        String pert = "", dateTime = "", spinnerItem = "";
        Long amount = 0L;
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                spinnerItem = cursor.getString(1);
                pert = cursor.getString(2);
                amount = cursor.getLong(3);
                dateTime = cursor.getString(4);
            }
        }

        getAllSpinnerOptions(spinnerEdit, spinnerItem);

        etParticularsEdit.setText(pert);
        etAmountEdit.setText(String.valueOf(amount));
        etDateTimeEdit.setText(dateTime);

        etDateTimeEdit.setOnClickListener(dateTimeView -> showDateTimeDialog(etDateTimeEdit));
        chooseFileTextEdit.setOnClickListener(v -> openFileChooser());


        builder.setView(view);


        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

//                String expenseCategory, String particulars, String amount, String dateTime, String date,String fileName, byte[] fileBytes
                SpinnerItem spItem = (SpinnerItem) spinnerEdit.getSelectedItem();
                boolean res = false;
                try {
                    res = db.insertExpense(String.valueOf(spItem.getText()),
                            etParticularsEdit.getText().toString(),
                            etAmountEdit.getText().toString(),
                            dateTimeVal == null ? etDateTimeEdit.getText().toString() : dateTimeVal,
                            dateVal == null ? etDateTimeEdit.getText().toString().substring(0, 10) : dateVal,
                            fileName, pdfBytes, id, null, false);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }

                if (res) {
//            resetFields();
                    Toast.makeText(getContext(), "Expense updated " + id, Toast.LENGTH_SHORT).show();
//                    tableRows.remove(0);
//                    tableRows.remove(1);
//                    tableRows.remove(2);
//                    tableRows.remove(3);
//                    tableRows.remove(4);
                    expenseDetailsLayout.removeViews(1, expenseDetailsLayout.getChildCount() - 1);
                    try {
                        populateTable1();
                    } catch (NoSuchFieldException e) {
                        throw new RuntimeException(e);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                    int fadeRowIndex = 0;
                    for (int i = 1; i < expenseDetailsLayout.getChildCount(); i++) {
                        TableRow trow = (TableRow) expenseDetailsLayout.getChildAt(i);
                        TextView textView = (TextView) trow.getChildAt(0);
                        Integer match = Integer.parseInt(String.valueOf(textView.getText()));
                        if (id == match) {
                            fadeRowIndex = i;
                        }
                    }
                    TableRow fadeRow = (TableRow) expenseDetailsLayout.getChildAt(fadeRowIndex);
//                    TextView view1 =(TextView) fadeRow.getChildAt(2);
//                    Toast.makeText(getContext(), "dekh > "+view1.getText(), Toast.LENGTH_SHORT).show(); view1.getText();
                    animateBackgroundColor(fadeRow);


//            NavHostFragment.findNavController(FirstFragment.this)
//                    .navigate(R.id.action_FirstFragment_to_SecondFragment);

                } else {
                    Toast.makeText(getContext(), "Expense save failed !", Toast.LENGTH_SHORT).show();
                }


            }

            private void animateBackgroundColor(TableRow fadeRow) {
                int startColor = Color.TRANSPARENT; // Initial color (no color)
                int endColor = Color.YELLOW; // Light yellow color
                endColor = getResources().getColor(R.color.yellow);
                startColor = getResources().getColor(R.color.yellow1);
                int creamWhite = getResources().getColor(android.R.color.system_background_light);

                ValueAnimator colorAnimation = null;
                if (isDarkModeOn()) {

                    int bloueColor = getResources().getColor(R.color.bluecolor);

                    colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), bloueColor, Color.TRANSPARENT);
                } else {
                    colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), endColor, startColor, creamWhite);
                }

                colorAnimation.setDuration(3000);

                // Add update listener to change background color during animation
                colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        fadeRow.setBackgroundColor((int) animator.getAnimatedValue());
                    }
                });

                // Start the animation
                colorAnimation.start();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();


    }

    private boolean isDarkModeOn() {
        return (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_PDF_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PDF_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri pdfUri = data.getData();
            try {

                InputStream inputStream = getContext().getContentResolver().openInputStream(pdfUri);
                if (inputStream != null) {
                    int fileSize = inputStream.available();
                    inputStream.close();
                    if (fileSize <= MAX_FILE_SIZE) {
                        pdfBytes = readPdfFromUri(pdfUri);
                        fileName = getFileName(pdfUri);
                        chooseFileTextEdit.setText(fileName.length() > 25 ? fileName.substring(0, 20) + "..." + fileName.substring(fileName.lastIndexOf(".")) : fileName);
                    } else {
                        Toast.makeText(getContext(), "File size exceeds 2MB limit.", Toast.LENGTH_SHORT).show();
                        pdfBytes = null;
                        fileName = "";
                        chooseFileTextEdit.setText("");
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Failed to read PDF", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getFileName(Uri pdfUri) {
        String result = null;
        if (pdfUri.getScheme().equals("content")) {
            try (Cursor cursor = getContext().getContentResolver().query(pdfUri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = pdfUri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private byte[] readPdfFromUri(Uri uri) throws IOException {
        InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }
        inputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    private void showDateTimeDialog(EditText etDateTimeEdit) {
        final Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    new TimePickerDialog(
                            getContext(),
                            (TimePicker view1, int hourOfDay, int minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);
                                String dateTime = new SimpleDateFormat("dd").format(calendar.getTime()) + "-" + (String.format("%02d", month + 1)) + "-" + year + " " + new SimpleDateFormat("HH").format(calendar.getTime()) + ":" + new SimpleDateFormat("mm").format(calendar.getTime());
                                dateVal = year + "-" + new SimpleDateFormat("MM").format(calendar.getTime()) + "-" + new SimpleDateFormat("dd").format(calendar.getTime());
                                dateTimeVal = year + "-" + (String.format("%02d", month + 1)) + "-" + new SimpleDateFormat("dd").format(calendar.getTime()) + " " + new SimpleDateFormat("HH").format(calendar.getTime()) + ":" + new SimpleDateFormat("mm").format(calendar.getTime());
                                etDateTimeEdit.setText(dateTime);
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                    ).show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void addEventListeners(View view, Integer id) {


    }

    private void getAllSpinnerOptions(Spinner spinnerEdit, String spinnerItem) {

        List<SpinnerItem> items = new ArrayList<>();

        items = FirstFragment.fetchAllSpinnerOptions(items);

        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(getContext(), items);

        spinnerEdit.setAdapter(adapter);


        if (adapter != null) {
            for (int i = 0; i < adapter.getCount(); i++) {
                SpinnerItem option = adapter.getItem(i);
                if (option.getText().equalsIgnoreCase(spinnerItem)) {
                    long ll = spinnerEdit.getItemIdAtPosition(i);
                    spinnerEdit.setSelection((int) ll);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void refreshTable() throws NoSuchFieldException, IllegalAccessException {
        expenseDetailsLayout.removeViews(1, expenseDetailsLayout.getChildCount() - 1);
//        for (TableRow row : tableRows) {
//            expenseDetailsLayout.addView(row);
//        }
        populateTable1();
    }

    private ImageButton createImageButton(int anInt, TableRow tableRow) {
        ContextThemeWrapper themeWrapper = new ContextThemeWrapper(getContext(), android.R.style.Widget_Material_Button_Borderless);
        ImageButton imageButton = new ImageButton(themeWrapper);
        imageButton.setForegroundGravity(Gravity.CENTER);
        imageButton.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        imageButton.setScaleType(ImageView.ScaleType.FIT_CENTER);

        imageButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.baseline_delete_forever_8));
        imageButton.setBackground(null);

        imageButton.setOnClickListener(v -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Delete " + anInt);
            builder.setIcon(R.drawable.file_delete_svgrepo_com);
            builder.setMessage("Proceed for deletion ? ");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int count = db.deleteRow(anInt);
                    if (count > 0) {
                        Toast.makeText(getContext(), "Deleted ID " + anInt, Toast.LENGTH_SHORT).show();
                        deleteRecordFromCloud(null);
                    }
                    expenseDetailsLayout.removeView(tableRow);
                    tableRows.remove(tableRow);
                    try {
                        refreshTable();
                    } catch (NoSuchFieldException e) {
                        throw new RuntimeException(e);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
//                    populateTable();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();

        });
        return imageButton;
    }

    private CheckBox crateCheckBox(int anInt) {
        CheckBox checkBox = new CheckBox(getContext());
        checkBox.setChecked(false);
        checkBox.setGravity(Gravity.CENTER);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                }
            }
        });
        return checkBox;
    }

    private TextView createDownloadLink(byte[] pdfBytes, String fileName) {
        TextView downloadLink = new TextView(getContext());
        if (pdfBytes != null) {
            downloadLink.setText(fileName.length() > 25 ? fileName.substring(0, 20) + "..." + fileName.substring(fileName.lastIndexOf(".")) : fileName);
            downloadLink.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            downloadLink.setClickable(true);
            downloadLink.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            downloadLink.setBackgroundResource(R.drawable.button_link);
            downloadLink.setOnClickListener(v -> {
                try {
                    File file = savePdfToFile(pdfBytes, fileName);
                    Toast.makeText(getContext(), "File downloaded: " + file.getPath(), Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Error saving PDF", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            downloadLink.setText("NA");
        }
        return downloadLink;
    }

    private File savePdfToFile(byte[] pdfBytes, String fileName) throws IOException {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File pdfFile = new File(downloadsDir, fileName);
        FileOutputStream fileOutputStream = new FileOutputStream(pdfFile);
        fileOutputStream.write(pdfBytes);
        fileOutputStream.flush();
        return pdfFile;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}