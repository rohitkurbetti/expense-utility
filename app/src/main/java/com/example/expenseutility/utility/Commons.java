package com.example.expenseutility.utility;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.Locale;

public class Commons {


    public static String getFormattedCurrency (long amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        int intAmount = (int) amount;
        formatter.setMaximumFractionDigits(0); // 👈 remove decimals
        return formatter.format(intAmount);
    }


    public static String encryptString(String inputString) {
        String encodedString = null;
        encodedString = Base64.encodeToString(inputString.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);
        return encodedString;
    }

    public static String decryptString(String encodedString) {
        return new String(Base64.decode(encodedString, Base64.DEFAULT), StandardCharsets.UTF_8);
    }

}
