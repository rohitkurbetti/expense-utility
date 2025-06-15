package com.example.expenseutility;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Pie;
import com.example.expenseutility.database.DatabaseHelper;
import com.example.expenseutility.databinding.ActivityCaptureBinding;
import com.example.expenseutility.entityadapter.ExpenseItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChartActivity extends AppCompatActivity {

    ActivityCaptureBinding binding;

    AnyChartView anyChartView;

    TextView filteredTextView,filterValuesTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        DatabaseHelper db = new DatabaseHelper(getApplicationContext());
        anyChartView = findViewById(R.id.any_chart_view);
        anyChartView.setZoomEnabled(true);
        anyChartView.setElevation(4f);
        anyChartView.setHovered(true);
        filteredTextView = findViewById(R.id.filterTextView);
        filterValuesTextView = findViewById(R.id.filterValuesTextView);
        boolean isFilterByDate = getIntent().getBooleanExtra("filteredByDate", false);

        if(isFilterByDate) {
            int[] items =  (int[]) getIntent().getSerializableExtra("filteredList");
            Cursor itemsList = db.getExpenseByIds(items);
            setupPieChart(itemsList);
        } else {
            Cursor cursor = db.getAllExpenseDataForChart();
            if(cursor.getCount() > 0){
                setupPieChart(cursor);
            } else {
                Toast.makeText(this, "No data to show", Toast.LENGTH_SHORT).show();
                finish();
            }
        }



    }

    private void inflateFilteredDataChart(List<ExpenseItem> filteredList) {
        Pie pie = AnyChart.pie();
        List<DataEntry> dataEntryList = new ArrayList<>();
        pie.animation().duration(700).enabled(true);

        for (ExpenseItem item: filteredList) {
            dataEntryList.add(new ValueDataEntry(item.getExpenseCategory(), item.getExpenseAmount()));
        }
        pie.data(dataEntryList);
        anyChartView.setChart(pie);
    }


    public void setupPieChart(Cursor cursor) {
        Pie pie = AnyChart.pie();
        List<DataEntry> dataEntryList = new ArrayList<>();

        pie.animation().duration(700).enabled(true);
        Map<String, Long> cateValMap = new HashMap<>();
        Long totalAmount = 0L;
        StringBuilder stringBuilder = new StringBuilder();

        StringBuilder stringBuilderVal = new StringBuilder();
        int counter=0;
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

cateValMap.forEach((k,v) -> {

        dataEntryList.add(new ValueDataEntry(k, v));
    stringBuilder.append("\u2022  "+k);
//            stringBuilder.append("  ");
//            stringBuilder.append("\u20B9"+expenseCategoryAmount);
    stringBuilder.append("\n");

    stringBuilderVal.append("\n");
//            stringBuilderVal.append("  ");
    stringBuilderVal.append("\u20B9"+v);
//            stringBuilderVal.append("\n");

});


        float income = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).getFloat("monthlyIncome", 87000.0f);


        pie.data(dataEntryList);
        anyChartView.setChart(pie);

        double perc = ((double) totalAmount / income) * 100;
        perc = Double.parseDouble(String.format("%.2f",perc));
        double dailyAvg = ((double)totalAmount / (income/30)) * 100;
        dailyAvg = Double.parseDouble(String.format("%.2f",dailyAvg));
        String finalSb1 = "Total amount: \n\n\u2022 Avg(Daily): \n\u2022 Avg(Monthly):  \n\n\nTotal Drilldown: \n\n"+stringBuilder;
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(finalSb1);
        spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, finalSb1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        filteredTextView.setText(spannableStringBuilder);
        filterValuesTextView.setText("\u20B9"+totalAmount+"\n\n"+dailyAvg+"%" +"\n"+perc+"% \n\n\n\n"+stringBuilderVal);

    }

}