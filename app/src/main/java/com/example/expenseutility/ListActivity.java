package com.example.expenseutility;

import android.os.Bundle;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.expenseutility.entityadapter.CustomListAdapter;

import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity {

    ListView listView;
    CustomListAdapter adapter;
    List<String> items;
    List<String> subItems;
    int[] images;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        listView = findViewById(R.id.listView);
        items = new ArrayList<>();
        subItems = new ArrayList<>();
        images = new int[]{
                R.drawable.clothes_clothing_formal_wear_svgrepo_com,
                R.drawable.education_graduation_learning_school_study_svgrepo_com,
                R.drawable.ground_transportation_svgrepo_com
        };

        // Sample data
        for (int i = 1; i <= 3; i++) {
            items.add("Item " + i);
            subItems.add("Subitem "+ i);
        }

        adapter = new CustomListAdapter(this, items, images, subItems);
        listView.setAdapter(adapter);

        // Optional: Set an item click listener
        listView.setOnItemClickListener((parent, view, position, id) -> {
            // Handle item click
        });



    }
}