package com.example.expenseutility;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private Switch switchImageSlider;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);

// Reference the switch
        switchImageSlider = findViewById(R.id.switchImageSlider);

        // Load saved state
        boolean isEnabled = prefs.getBoolean("showImageSliderPanel", false);
        switchImageSlider.setChecked(isEnabled);

        // Listener for toggle
        switchImageSlider.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("showImageSliderPanel", isChecked).apply();
            refreshActivity();
        });

    }

    private void refreshActivity() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setMessage("Refresh activity now?")
                .setIcon(R.drawable.refresh_svgrepo_com)
                .setPositiveButton("Refresh", (dialog, which) -> {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish(); // close SettingsActivity
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }


}