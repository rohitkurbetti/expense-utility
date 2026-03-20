package com.example.expenseutility;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.example.expenseutility.entityadapter.ImagePreviewPagerAdapter;

import java.util.ArrayList;

public class ImagePreviewActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_IDS = "image_ids";
    public static final String EXTRA_SELECTED_STATES = "selected_states";
    public static final String EXTRA_CURRENT_POSITION = "current_position";
    public static final String EXTRA_RESULT_SELECTED_STATES = "result_selected_states";

    private long[] imageIds;

    private ViewPager2 viewPager;
    private ImagePreviewPagerAdapter adapter;
    private ArrayList<String> imagePaths;
    private boolean[] selectedStates;
    private int currentPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview_slideshow);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager = findViewById(R.id.viewPager);

        imageIds = getIntent().getLongArrayExtra(EXTRA_IMAGE_IDS);
        selectedStates = getIntent().getBooleanArrayExtra(EXTRA_SELECTED_STATES);
        currentPosition = getIntent().getIntExtra(EXTRA_CURRENT_POSITION, 0);

        if (imageIds == null) imageIds = new long[0];
        if (selectedStates == null || selectedStates.length != imageIds.length) {
            selectedStates = new boolean[imageIds.length];
        }

        adapter = new ImagePreviewPagerAdapter(this, imageIds, selectedStates, (position, isSelected) -> {
            // Callback when selection changes – update array
            selectedStates[position] = isSelected;
        });
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(currentPosition, false);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentPosition = position;
                getSupportActionBar().setTitle("Image " + (position + 1) + " of " + imageIds.length);
            }
        });

        getSupportActionBar().setTitle("Image " + (currentPosition + 1) + " of " + imageIds.length);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finishWithResult();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finishWithResult();
        super.onBackPressed();
    }

    private void finishWithResult() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_RESULT_SELECTED_STATES, selectedStates);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}