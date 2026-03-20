package com.example.expenseutility.utility;

import android.content.Context;
import android.content.SharedPreferences;

public class AppConfig {
    private static final String PREF_NAME = "MyPrefs";
    private static final String KEY_ENABLE_ANIMATIONS = "enable_animations";

    private final SharedPreferences prefs;

    public AppConfig(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isAnimationEnabled() {
        return prefs.getBoolean(KEY_ENABLE_ANIMATIONS, true); // Default to true
    }

    public void setAnimationEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_ENABLE_ANIMATIONS, enabled).apply();
    }
}
