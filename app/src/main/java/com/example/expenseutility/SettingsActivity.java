package com.example.expenseutility;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

import com.example.expenseutility.utility.AppConfig;
import com.example.expenseutility.utility.ThemeHelper;

public class SettingsActivity extends AppCompatActivity {

    private Switch switchImageSlider;
    private Switch switchAnimations;
    private SharedPreferences prefs;
    private AppConfig appConfig;
    private EditText etSmsLookbackDays;
    private Button btnSaveSmsLookbackDays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Settings");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        appConfig = new AppConfig(this);

        // Reference the switches
        switchImageSlider = findViewById(R.id.switchImageSlider);
        switchAnimations = findViewById(R.id.switchAnimations);
        etSmsLookbackDays = findViewById(R.id.etSmsLookbackDays);
        btnSaveSmsLookbackDays = findViewById(R.id.btnSaveSmsLookbackDays);

        // Load saved state
        boolean isImageSliderEnabled = prefs.getBoolean("showImageSliderPanel", false);
        switchImageSlider.setChecked(isImageSliderEnabled);

        int smsLookbackDays = prefs.getInt("smsLookbackDays", 3);
        etSmsLookbackDays.setText(String.valueOf(smsLookbackDays));

        boolean isAnimationsEnabled = appConfig.isAnimationEnabled();
        switchAnimations.setChecked(isAnimationsEnabled);

        // Listener for image slider toggle
        switchImageSlider.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("showImageSliderPanel", isChecked).apply();
            refreshActivity();
        });

        // Listener for animations toggle
        switchAnimations.setOnCheckedChangeListener((buttonView, isChecked) -> {
            appConfig.setAnimationEnabled(isChecked);
        });

        btnSaveSmsLookbackDays.setOnClickListener(v -> {
            String raw = etSmsLookbackDays.getText() != null ? etSmsLookbackDays.getText().toString().trim() : "";
            if (raw.isEmpty()) {
                Toast.makeText(this, "Enter SMS lookback days", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int days = Integer.parseInt(raw);
                if (days < 1) {
                    Toast.makeText(this, "Value must be at least 1 day", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (days > 365) {
                    Toast.makeText(this, "Value should be 365 days or less", Toast.LENGTH_SHORT).show();
                    return;
                }

                prefs.edit().putInt("smsLookbackDays", days).apply();
                Toast.makeText(this, "Saved SMS lookback: " + days + " days", Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid number", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
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