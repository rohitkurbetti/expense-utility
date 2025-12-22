package com.example.expenseutility.utility;

import com.example.expenseutility.dto.CategoryItem;
import com.example.expenseutility.dto.ExpenseMonth;
import com.example.expenseutility.entityadapter.ExpenseItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class ExpenseGroupingUtility {

    // Method to group expenses by month and create ExpenseMonth objects
    public static List<ExpenseMonth> groupExpensesByMonth(List<ExpenseItem> expenseItems) {
        if (expenseItems == null || expenseItems.isEmpty()) {
            return new ArrayList<>();
        }

        // Map to store monthly data: Key = "MMM-yyyy", Value = list of expenses for that month
        Map<String, List<ExpenseItem>> monthlyExpenses = new TreeMap<>(new MonthYearComparator());

        // Group expenses by month
        for (ExpenseItem item : expenseItems) {
            String expenseDate = item.getExpenseDate();
            if (expenseDate == null || expenseDate.isEmpty()) {
                continue;
            }

            // Parse date to get month-year
            String monthYear = getMonthYearFromDate(expenseDate);
            if (monthYear == null) {
                continue;
            }

            // Add to monthly grouping
            if (!monthlyExpenses.containsKey(monthYear)) {
                monthlyExpenses.put(monthYear, new ArrayList<>());
            }
            monthlyExpenses.get(monthYear).add(item);
        }

        // Convert to ExpenseMonth objects
        List<ExpenseMonth> result = new ArrayList<>();
        for (Map.Entry<String, List<ExpenseItem>> entry : monthlyExpenses.entrySet()) {
            String monthYear = entry.getKey();
            List<ExpenseItem> monthlyItems = entry.getValue();

            // Calculate total expense for the month
            double totalExpense = calculateTotalExpense(monthlyItems);

            // Create details string
            List<CategoryItem> details = createExpenseDetails(totalExpense, monthlyItems);

            // Create ExpenseMonth object
            ExpenseMonth expenseMonth = new ExpenseMonth(monthYear, totalExpense, details);
            result.add(expenseMonth);
        }

        // Sort in reverse chronological order (newest first)
        Collections.sort(result, new Comparator<ExpenseMonth>() {
            @Override
            public int compare(ExpenseMonth o1, ExpenseMonth o2) {
                return compareMonthYears(o2.getMonthYear(), o1.getMonthYear());
            }
        });

        return result;
    }

    // Extract month-year from date string (format: "yyyy-MM-dd" or "dd-MM-yyyy")
    private static String getMonthYearFromDate(String dateStr) {
        try {
            SimpleDateFormat inputFormat;

            // Try different date formats
            if (dateStr.contains("-")) {
                String[] parts = dateStr.split("-");
                if (parts.length == 3) {
                    if (parts[0].length() == 4) { // yyyy-MM-dd format
                        inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    } else { // dd-MM-yyyy format
                        inputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    }
                } else {
                    return null;
                }
            } else if (dateStr.contains("/")) {
                String[] parts = dateStr.split("/");
                if (parts.length == 3) {
                    if (parts[2].length() == 4) { // dd/MM/yyyy format
                        inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    } else { // MM/dd/yyyy format
                        inputFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }

            Date date = inputFormat.parse(dateStr);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            // Format as "MMM-yyyy" (e.g., "Dec-2025")
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM-yyyy", Locale.getDefault());
            return outputFormat.format(date).toUpperCase();

        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Calculate total expense for a list of items
    private static double calculateTotalExpense(List<ExpenseItem> items) {
        double total = 0.0;
        for (ExpenseItem item : items) {
            if (item.getExpenseAmount() != null) {
                total += item.getExpenseAmount();
            }
        }
        return total;
    }

    // Create formatted details string from monthly expenses
    private static List<CategoryItem> createExpenseDetails(double totalExpense, List<ExpenseItem> items) {
        List<CategoryItem> categoryItems = new ArrayList<>();
        if (items == null || items.isEmpty()) {
            categoryItems.add(new CategoryItem("No expenses recorded for this month.", 0, 0));
            return categoryItems;
        }

        // Group expenses by category and sum amounts
        Map<String, Double> categoryTotals = new HashMap<>();
        for (ExpenseItem item : items) {
            String category = item.getExpenseCategory();
            if (category == null || category.isEmpty()) {
                category = "Uncategorized";
            }

            double amount = item.getExpenseAmount() != null ? item.getExpenseAmount() : 0.0;
            categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + amount);
        }

        // Sort categories by amount (descending)
        List<Map.Entry<String, Double>> sortedCategories = new ArrayList<>(categoryTotals.entrySet());
        sortedCategories.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        // Build details string
//        StringBuilder details = new StringBuilder();
        categoryItems.clear();
        for (Map.Entry<String, Double> entry : sortedCategories) {
            String category = entry.getKey();
            double amount = entry.getValue();
            double percentage = (amount / totalExpense) * 100.0d;

//            details.append("• ").append(category)
//                    .append(": ₹").append(String.format(Locale.ENGLISH, "%.0f", amount))
//                    .append(" (" + String.format("%.2f", (amount / totalExpense) * 100.0d) + "%)")
//                    .append("\n");

            categoryItems.add(new CategoryItem(category, amount, percentage));


        }


        return categoryItems;
    }

    // Comparator for month-year strings (e.g., "Dec-2025")
    private static int compareMonthYears(String monthYear1, String monthYear2) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("MMM-yyyy", Locale.getDefault());
            Date date1 = format.parse(monthYear1);
            Date date2 = format.parse(monthYear2);
            return date1.compareTo(date2);
        } catch (ParseException e) {
            return monthYear1.compareTo(monthYear2);
        }
    }

    // Additional method: Get expenses by category for a specific month
    public static Map<String, Double> getCategoryWiseBreakdown(List<ExpenseItem> items) {
        Map<String, Double> categoryMap = new HashMap<>();
        for (ExpenseItem item : items) {
            String category = item.getExpenseCategory();
            if (category == null || category.isEmpty()) {
                category = "Uncategorized";
            }

            double amount = item.getExpenseAmount() != null ? item.getExpenseAmount() : 0.0;
            categoryMap.put(category, categoryMap.getOrDefault(category, 0.0) + amount);
        }
        return categoryMap;
    }

    // Additional method: Get month-wise total expenses
    public static Map<String, Double> getMonthlyTotals(List<ExpenseItem> expenseItems) {
        Map<String, Double> monthlyTotals = new HashMap<>();

        for (ExpenseItem item : expenseItems) {
            String monthYear = getMonthYearFromDate(item.getExpenseDate());
            if (monthYear == null) continue;

            double amount = item.getExpenseAmount() != null ? item.getExpenseAmount() : 0.0;
            monthlyTotals.put(monthYear, monthlyTotals.getOrDefault(monthYear, 0.0) + amount);
        }

        return monthlyTotals;
    }

    // Custom comparator for month-year strings in descending order
    private static class MonthYearComparator implements Comparator<String> {
        @Override
        public int compare(String s1, String s2) {
            return compareMonthYears(s1, s2);
        }
    }
}