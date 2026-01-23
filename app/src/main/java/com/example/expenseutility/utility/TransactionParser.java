package com.example.expenseutility.utility;

public class TransactionParser {

    /**
     * Parse a single transaction string
     *
     * @param transaction The transaction string to parse
     * @return The extracted text according to the rules
     */
    public static String parseTransaction(String transaction) {
        if (transaction == null || transaction.isEmpty()) {
            return "";
        }

        // If string contains slashes (/)
        if (transaction.contains("/")) {
            return extractAfterThirdSlash(transaction);
        }
        // If string contains dashes (-) but no slashes
        else if (transaction.contains("-")) {
            return extractFirstFourWords(transaction);
        }
        // If neither, return original
        else {
            return transaction;
        }
    }

    /**
     * Extract text after the third slash
     * Example: "UPI/P2M/401191960122/CHANDRAMMA/UPI/Paytm Payments Bank"
     * Returns: "CHANDRAMMA/UPI/Paytm Payments Bank"
     */
    private static String extractAfterThirdSlash(String text) {
        int slashCount = 0;
        int index = -1;

        // Find the position after the 3rd slash
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '/') {
                slashCount++;
                if (slashCount == 3) {
                    index = i;
                    break;
                }
            }
        }

        // If we found the 3rd slash, return everything after it
        if (index != -1 && index < text.length() - 1) {
            return text.substring(index + 1);
        }

        // If not enough slashes, return original
        return text;
    }

    /**
     * Extract first four words separated by dashes
     * Example: "ACH-DR-Groww-POXYHNNB369O- UTIB7021505220008570"
     * Returns: "ACH-DR-Groww-POXYHNNB369O"
     */
    private static String extractFirstFourWords(String text) {
        String[] parts = text.split("-");

        // If we have at least 4 parts
        if (parts.length >= 4) {
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                if (i > 0) {
                    result.append("-");
                }
                result.append(parts[i]);
            }
            return result.toString();
        }
        // If less than 4 parts, return everything joined with dashes
        else {
            return String.join("-", parts);
        }
    }

    public static String parseTransactions(String transaction) {
        return parseTransaction(transaction);
    }
}