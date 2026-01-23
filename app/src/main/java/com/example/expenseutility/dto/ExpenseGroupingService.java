package com.example.expenseutility.dto;

import static com.example.expenseutility.constants.ExpenseConstants.EXPENSE_CATEGORY_ICONS;

import com.example.expenseutility.entityadapter.ExpenseItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpenseGroupingService {

    // Main grouping method - returns List<YearData>
    public static List<YearData> groupExpensesByYearMonthCategory(List<ExpenseItem> expenseItems) {
        // First, group by year and month
        Map<String, Map<String, Map<String, List<ExpenseItem>>>> groupedData = new HashMap<>();

        for (ExpenseItem item : expenseItems) {
            // Parse date to get year and month
            String date = item.getExpenseDate(); // Assuming format "yyyy-MM-dd"
            if (date == null || date.isEmpty()) {
                continue;
            }

            String[] dateParts = date.split("-");

            if (dateParts.length >= 2) {
                String year = dateParts[0];
                String month = dateParts[1];
                String monthYear = getMonthName(Integer.parseInt(month)) + " " + year;
                String category = item.getExpenseCategory() != null ? item.getExpenseCategory() : "Uncategorized";

                // Initialize nested maps if needed
                groupedData.putIfAbsent(year, new HashMap<>());
                groupedData.get(year).putIfAbsent(monthYear, new HashMap<>());
                groupedData.get(year).get(monthYear).putIfAbsent(category, new ArrayList<>());

                // Add item to appropriate group
                groupedData.get(year).get(monthYear).get(category).add(item);
            }
        }

        // Convert to YearData structure
        List<YearData> yearDataList = new ArrayList<>();

        // Sort years in descending order
        List<String> years = new ArrayList<>(groupedData.keySet());
        years.sort(Collections.reverseOrder());

        for (String year : years) {
            Map<String, Map<String, List<ExpenseItem>>> monthData = groupedData.get(year);
            List<MonthData> monthDataList = new ArrayList<>();
            double yearlyTotal = 0.0;

            // Sort months in descending order
            List<String> monthYears = new ArrayList<>(monthData.keySet());
            monthYears.sort((m1, m2) -> {
                // Parse monthYear strings for proper sorting (e.g., "December 2025")
                String[] parts1 = m1.split(" ");
                String[] parts2 = m2.split(" ");
                if (parts1.length >= 2 && parts2.length >= 2) {
                    int year1 = Integer.parseInt(parts1[1]);
                    int year2 = Integer.parseInt(parts2[1]);
                    int month1 = getMonthNumber(parts1[0]);
                    int month2 = getMonthNumber(parts2[0]);

                    if (year1 != year2) {
                        return Integer.compare(year2, year1); // Descending year
                    }
                    return Integer.compare(month2, month1); // Descending month
                }
                return 0;
            });

            for (String monthYear : monthYears) {
                Map<String, List<ExpenseItem>> categoryData = monthData.get(monthYear);
                List<CategoryData> categoryDataList = new ArrayList<>();
                double monthTotal = 0.0;

                // Sort categories by name for consistency
                List<String> categories = new ArrayList<>(categoryData.keySet());
                Collections.sort(categories);

                for (String category : categories) {
                    List<ExpenseItem> items = categoryData.get(category);
                    List<ExpenseItem1> expenseItem1List = new ArrayList<>();
                    double categoryTotal = 0.0;

                    // Convert ExpenseItem to ExpenseItem1
                    for (ExpenseItem item : items) {
                        ExpenseItem1 item1 = new ExpenseItem1(
                                category,
                                item.getExpenseAmount() != null ? item.getExpenseAmount().doubleValue() : 0.0,
                                item.getExpenseDate(),
                                item.getExpenseParticulars() != null ? item.getExpenseParticulars() : ""
                        );
                        expenseItem1List.add(item1);
                        categoryTotal += item.getExpenseAmount() != null ? item.getExpenseAmount().doubleValue() : 0.0;
                    }

                    monthTotal += categoryTotal;
                    CategoryData catData = new CategoryData(category, categoryTotal, EXPENSE_CATEGORY_ICONS.get(category), expenseItem1List);
                    categoryDataList.add(catData);
                }

                yearlyTotal += monthTotal;
                MonthData month = new MonthData(monthYear, monthTotal, categoryDataList);
                monthDataList.add(month);
            }

            YearData yearData = new YearData(year, yearlyTotal, monthDataList);
            yearDataList.add(yearData);
        }

        return yearDataList;
    }

    private static String getMonthName(int month) {
        String[] monthNames = {
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };
        return (month >= 1 && month <= 12) ? monthNames[month - 1] : "Unknown";
    }

    private static int getMonthNumber(String monthName) {
        Map<String, Integer> monthMap = new HashMap<>();
        monthMap.put("January", 1);
        monthMap.put("February", 2);
        monthMap.put("March", 3);
        monthMap.put("April", 4);
        monthMap.put("May", 5);
        monthMap.put("June", 6);
        monthMap.put("July", 7);
        monthMap.put("August", 8);
        monthMap.put("September", 9);
        monthMap.put("October", 10);
        monthMap.put("November", 11);
        monthMap.put("December", 12);
        return monthMap.getOrDefault(monthName, 0);
    }

    // Alternative method if you need to process the data and get specific format
    public static List<YearData> processExpenseData(List<ExpenseItem> expenseItems) {
        return groupExpensesByYearMonthCategory(expenseItems);
    }

}