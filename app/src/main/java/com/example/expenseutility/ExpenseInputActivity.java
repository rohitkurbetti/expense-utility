package com.example.expenseutility;

import static com.example.expenseutility.FirstFragment.saveToFirebase;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.ui.AppBarConfiguration;

import com.example.expenseutility.database.DatabaseHelper;
import com.example.expenseutility.databinding.ActivityExpenseInputBinding;
import com.example.expenseutility.utility.Commons;
import com.example.expenseutility.utility.CustomSpinnerAdapter;
import com.example.expenseutility.utility.SpinnerItem;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ExpenseInputActivity extends AppCompatActivity {

    Spinner spinnerCategory;
    EditText particularsInput, particularsDetail;
    TextView amountView, dateTimeView;
    CheckBox homeCheckBox;
    double amount;
    String dateTime;
    private AppBarConfiguration appBarConfiguration;
    private ActivityExpenseInputBinding binding;
    private Button btnCancel;

    public static String getFormatted(int monthValue) {
        return monthValue != 0 ? String.format("%02d", monthValue) : "0";
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityExpenseInputBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        spinnerCategory = findViewById(R.id.spinnerCategory);
        particularsInput = findViewById(R.id.editParticulars);
        particularsDetail = findViewById(R.id.editParticularsDetail);
        amountView = findViewById(R.id.textAmount);
        dateTimeView = findViewById(R.id.textDateTime);
        btnCancel = findViewById(R.id.btnCancel);
        homeCheckBox = findViewById(R.id.homeCheckBox);

        loadSpinner(spinnerCategory);


        amount = getIntent().getDoubleExtra("amount", 0);
        dateTime = getIntent().getStringExtra("dateTime");

        amountView.setText(String.valueOf(amount));
        dateTimeView.setText(dateTime);

        findViewById(R.id.btnSave).setOnClickListener(v -> {
            SpinnerItem spItem = (SpinnerItem) spinnerCategory.getSelectedItem();
            String category = spItem.getText();
            String particulars = particularsInput.getText().toString();
            String partDetails = particularsDetail.getText().toString();
            DatabaseHelper dbHelper = new DatabaseHelper(this);

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
//            dateTimeStr.append(":");
//            dateTimeStr.append(second);

            String dateStr = parsedDateTime.getYear() + "-" + month + "-" + day;

            String encodedPartDetails = Commons.encryptString(partDetails);
            boolean isHomeExpense = homeCheckBox.isChecked();
            try {
                boolean res = dbHelper.insertExpense(category, particulars, String.valueOf((int) amount), dateTimeStr.toString(), dateStr, null, null, null, encodedPartDetails, isHomeExpense);
                Toast.makeText(this, "Saved to db", Toast.LENGTH_SHORT).show();

                if (res) {
                    saveToFirebase(category, particulars, String.valueOf((int) amount), dateTimeStr.toString(), dateStr, null, null, encodedPartDetails, isHomeExpense);

                }
                int notificationId = getIntent().getIntExtra("notificationId", -1);
                if (notificationId != -1) {
                    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancel(notificationId);
                }

            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }


            finish();
        });

        btnCancel.setOnClickListener(v -> {
            this.finish();
        });


//        setSupportActionBar(binding.toolbar);
//
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_expense_input);
//        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

//        binding.fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAnchorView(R.id.fab)
//                        .setAction("Action", null).show();
//            }
//        });
    }

    private void loadSpinner(Spinner spinnerCategory) {
        List<SpinnerItem> items = new ArrayList<>();

        FirstFragment.fetchAllSpinnerOptions(items);

        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(this, items);

        spinnerCategory.setAdapter(adapter);
    }

//    @Override
//    public boolean onSupportNavigateUp() {
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_expense_input);
//        return NavigationUI.navigateUp(navController, appBarConfiguration)
//                || super.onSupportNavigateUp();
//    }
}