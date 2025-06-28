package com.example.expenseutility;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expenseutility.entityadapter.GridAdapter;

import java.util.ArrayList;
import java.util.List;

public class ConfigGridActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    GridAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_grid);

        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // Prepare Data
        List<String> titles = new ArrayList<>();
        titles.add("Suggestion");
        titles.add("Flash");
        titles.add("Data Import");

        List<Integer> images = new ArrayList<>();
        images.add(R.drawable.listing_list_svgrepo_com);
        images.add(R.drawable.baseline_flash_on_24);
        images.add(R.drawable.csv_svgrepo_com);

        // Set Adapter
        adapter = new GridAdapter(this, titles, images);
        recyclerView.setAdapter(adapter);

    }
}