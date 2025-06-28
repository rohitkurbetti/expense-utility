package com.example.expenseutility;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

public class LocationDetailsActivity extends AppCompatActivity {

    ImageView imageView;
    TextView countryText, cityText, descriptionText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_details);

        imageView = findViewById(R.id.imageView);
        countryText = findViewById(R.id.countryText);
        cityText = findViewById(R.id.cityText);
        descriptionText = findViewById(R.id.descriptionText);

        String country = getIntent().getStringExtra("country");
        String city = getIntent().getStringExtra("city");
        String description = getIntent().getStringExtra("description");
        String imageUrl = getIntent().getStringExtra("imageUrl");

        countryText.setText(country);
        cityText.setText(city);
        descriptionText.setText(description);

        Glide.with(this).load(imageUrl).into(imageView);




    }
}