package com.example.expenseutility.utility;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.expenseutility.R;

public class ThemeHelper {

    public static void applyTheme(Activity activity) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        String theme = prefs.getString("app_theme", "Theme.ExpenseUtility");

        switch (theme) {
            case "Default":
                activity.setTheme(R.style.Base_Theme_ExpenseUtility);
                break;
            case "Red":
                activity.setTheme(R.style.AppTheme_Red);
                break;
            case "Blue":
                activity.setTheme(R.style.AppTheme_Blue);
                break;
            case "Green":
                activity.setTheme(R.style.AppTheme_Green);
                break;
            case "GreenParrot":
                activity.setTheme(R.style.AppTheme_GreenParrot);
                break;
            case "Purple":
                activity.setTheme(R.style.AppTheme_Purple);
                break;
            case "Orange":
                activity.setTheme(R.style.AppTheme_Orange);
                break;
            case "Teal":
                activity.setTheme(R.style.AppTheme_Teal);
                break;
            case "Pink":
                activity.setTheme(R.style.AppTheme_Pink);
                break;
            case "Cyan":
                activity.setTheme(R.style.AppTheme_Cyan);
                break;
            case "Lime":
                activity.setTheme(R.style.AppTheme_Lime);
                break;
            case "Brown":
                activity.setTheme(R.style.AppTheme_Brown);
                break;
            case "Mint":
                activity.setTheme(R.style.AppTheme_Mint);
                break;
            case "Coral":
                activity.setTheme(R.style.AppTheme_Coral);
                break;
            case "Steel":
                activity.setTheme(R.style.AppTheme_Steel);
                break;
            case "Lavender":
                activity.setTheme(R.style.AppTheme_Lavender);
                break;
            case "Mustard":
                activity.setTheme(R.style.AppTheme_Mustard);
                break;
            case "Indigo":
                activity.setTheme(R.style.AppTheme_Indigo);
                break;
            case "Olive":
                activity.setTheme(R.style.AppTheme_Olive);
                break;
            case "Maroon":
                activity.setTheme(R.style.AppTheme_Maroon);
                break;
            case "Navy":
                activity.setTheme(R.style.AppTheme_Navy);
                break;
            case "Emerald":
                activity.setTheme(R.style.AppTheme_Emerald);
                break;
            case "Violet":
                activity.setTheme(R.style.AppTheme_Violet);
                break;
            case "Crimson":
                activity.setTheme(R.style.AppTheme_Crimson);
                break;
            case "Charcoal":
                activity.setTheme(R.style.AppTheme_Charcoal);
                break;
            case "Coffee":
                activity.setTheme(R.style.AppTheme_Coffee);
                break;
            case "Plum":
                activity.setTheme(R.style.AppTheme_Plum);
                break;
            case "Sapphire":
                activity.setTheme(R.style.AppTheme_Sapphire);
                break;
            case "Magenta":
                activity.setTheme(R.style.AppTheme_Magenta);
                break;
            case "Pumpkin":
                activity.setTheme(R.style.AppTheme_Pumpkin);
                break;
            case "Ocean":
                activity.setTheme(R.style.AppTheme_Ocean);
                break;
            default:
                activity.setTheme(R.style.Base_Theme_ExpenseUtility);
                break;
        }
    }
}
