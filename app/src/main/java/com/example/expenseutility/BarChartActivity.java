package com.example.expenseutility;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Bar;
import com.example.expenseutility.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BarChartActivity extends AppCompatActivity {


    AnyChartView barChartView;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_chart);
        db = new DatabaseHelper(this);

        barChartView = findViewById(R.id.barChartView);


        Cartesian horizontalBarChart = AnyChart.bar();




        Cursor res =  db.getExpenseGroupByDate();
        // Sample data for the horizontal bar chart
        List<DataEntry> data = new ArrayList<>();
        if(res.getCount()>0) {
            while(res.moveToNext()) {
                String cate=  res.getString(0);
                Long amount = res.getLong(1);
                data.add(new ValueDataEntry(cate, amount));
            }
        } else {
            Toast.makeText(this, "No data to show", Toast.LENGTH_SHORT).show();
            finish();
        }

        Bar bar = horizontalBarChart.bar(data);
        bar.color(getRandomDescentHexColors(10)[0]);

        // Set chart title and axis labels
        horizontalBarChart.title("Expense Data");
        horizontalBarChart.yAxis(0).title("Expenses");
        horizontalBarChart.xAxis(0).title("Date");
        horizontalBarChart.fullScreen();
        // Enable chart to be horizontal
        horizontalBarChart.isVertical(false);
        horizontalBarChart.animation(true, 2500);
        barChartView.setChart(horizontalBarChart);

    }

    // Generate random descent colors
    private String[] getRandomDescentHexColors(int count) {
        String[] hexColors = new String[count];
        Random random = new Random();

        for (int i = 0; i < count; i++) {
            float hue = random.nextFloat() * 360;  // Random hue between 0 and 360 degrees
            float saturation = 0.7f;               // Maintain saturation at a descent level (70%)
            float lightness = 0.65f;               // Lightness for good visibility (65%)

            // Convert HSV to ARGB color
            int color = Color.HSVToColor(new float[]{hue, saturation, lightness});

            // Convert the color to hex string
            hexColors[i] = String.format("#%06X", (0xFFFFFF & color));  // Extract RGB and convert to hex
        }
        return hexColors;
    }
}