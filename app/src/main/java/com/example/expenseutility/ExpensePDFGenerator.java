package com.example.expenseutility;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.example.expenseutility.entityadapter.ExpenseItem;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExpensePDFGenerator {

    private static final String TAG = "ExpensePDFGenerator";

    // Colors
    private static final BaseColor COLOR_PRIMARY = new BaseColor(41, 128, 185);
    private static final BaseColor COLOR_SECONDARY = new BaseColor(52, 152, 219);
    private static final BaseColor COLOR_ACCENT = new BaseColor(155, 89, 182);
    private static final BaseColor COLOR_SUCCESS = new BaseColor(39, 174, 96);  // Green for Home
    private static final BaseColor COLOR_WARNING = new BaseColor(241, 196, 15); // Yellow for Personal
    private static final BaseColor COLOR_HOME = new BaseColor(46, 204, 113);    // Green for Home
    private static final BaseColor COLOR_PERSONAL = new BaseColor(230, 126, 34); // Orange for Personal
    private static final BaseColor COLOR_DANGER = new BaseColor(231, 76, 60);
    private static final BaseColor COLOR_LIGHT_GRAY = new BaseColor(245, 245, 245);
    private static final BaseColor COLOR_DARK_GRAY = new BaseColor(51, 51, 51);
    private static final BaseColor COLOR_WHITE = BaseColor.WHITE;

    // Fonts
    private Font titleFont;
    private Font headerFont;
    private Font normalFont;
    private Font boldFont;
    private Font smallFont;
    private Font typeFont;

    // Context
    private Context context;

    public ExpensePDFGenerator(Context context) {
        this.context = context;
        initializeFonts();
    }

    private void initializeFonts() {
        try {
            // Register standard fonts
            FontFactory.registerDirectories();

            // Create fonts
            titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, COLOR_PRIMARY);
            headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, COLOR_SECONDARY);
            normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, COLOR_DARK_GRAY);
            boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, COLOR_DARK_GRAY);
            smallFont = FontFactory.getFont(FontFactory.HELVETICA, 8, COLOR_DARK_GRAY);
            typeFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, COLOR_WHITE);

        } catch (Exception e) {
            Log.e(TAG, "Error initializing fonts", e);
            // Fallback fonts
            titleFont = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, COLOR_PRIMARY);
            headerFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, COLOR_SECONDARY);
            normalFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, COLOR_DARK_GRAY);
            boldFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, COLOR_DARK_GRAY);
            smallFont = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, COLOR_DARK_GRAY);
            typeFont = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, COLOR_WHITE);
        }
    }

    public void generateExpenseReport(List<ExpenseItem> expenseItems, String fileName, PDFGenerationCallback callback) {
        new Thread(() -> {
            try {
                callback.onProgress("Starting PDF generation...");

                // Create directory
                File pdfDir = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOCUMENTS), "ExpenseReports");
                if (!pdfDir.exists()) {
                    if (!pdfDir.mkdirs()) {
                        callback.onError("Failed to create directory");
                        return;
                    }
                }

                // Create PDF file
                File pdfFile = new File(pdfDir, fileName + ".pdf");

                // Create document
                Document document = new Document(PageSize.A4);
                document.setMargins(40, 40, 40, 40);

                callback.onProgress("Creating PDF writer...");
                PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pdfFile));

                document.open();

                // Add content
                callback.onProgress("Adding header...");
                addHeader(document);

                callback.onProgress("Adding summary section...");
                addSummarySection(document, expenseItems);

                callback.onProgress("Adding home vs personal comparison...");
                addHomePersonalComparison(document, expenseItems);

                callback.onProgress("Adding category breakdown...");
                addCategoryBreakdown(document, expenseItems);

                callback.onProgress("Adding expense details...");
                addExpenseDetails(document, expenseItems);

                callback.onProgress("Adding footer...");
                addFooter(document);

                document.close();

                callback.onSuccess(pdfFile);

            } catch (Exception e) {
                Log.e(TAG, "Error generating PDF", e);
                callback.onError("Failed to generate PDF: " + e.getMessage());
            }
        }).start();
    }

    private void addHeader(Document document) throws DocumentException {
        // Title
        Paragraph title = new Paragraph("EXPENSE REPORT", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        document.add(title);

        // Generation date
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
        Paragraph date = new Paragraph("Generated on: " + sdf.format(new Date()), smallFont);
        date.setAlignment(Element.ALIGN_CENTER);
        date.setSpacingAfter(20);
        document.add(date);

        addSeparator(document);
    }

    private void addSummarySection(Document document, List<ExpenseItem> expenseItems) throws DocumentException {
        Paragraph sectionTitle = new Paragraph("SUMMARY", headerFont);
        sectionTitle.setSpacingBefore(10);
        sectionTitle.setSpacingAfter(15);
        document.add(sectionTitle);

        // Calculate statistics including home/personal
        SummaryStats stats = calculateSummaryStats(expenseItems);

        // Create summary table
        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(100);
        summaryTable.setSpacingBefore(10);
        summaryTable.setSpacingAfter(20);

        // Add summary rows
        addSummaryRow(summaryTable, "Total Expenses", formatCurrency(stats.totalAmount));
        addSummaryRow(summaryTable, "Number of Items", String.valueOf(expenseItems.size()));
        addSummaryRow(summaryTable, "Home Expenses", formatCurrency(stats.homeExpenseTotal) +
                " (" + stats.homeExpenseCount + " items)");
        addSummaryRow(summaryTable, "Personal Expenses", formatCurrency(stats.personalExpenseTotal) +
                " (" + stats.personalExpenseCount + " items)");
        addSummaryRow(summaryTable, "Categories", String.valueOf(stats.categoryCount));
        addSummaryRow(summaryTable, "Attachments", String.valueOf(stats.attachmentCount));

        // Average expenses
        double average = expenseItems.isEmpty() ? 0 : (double) stats.totalAmount / expenseItems.size();
        addSummaryRow(summaryTable, "Average per Item", formatCurrency((long) average));

        // Percentage breakdown
        double homePercentage = stats.totalAmount > 0 ?
                (stats.homeExpenseTotal * 100.0 / stats.totalAmount) : 0;
        double personalPercentage = stats.totalAmount > 0 ?
                (stats.personalExpenseTotal * 100.0 / stats.totalAmount) : 0;

        addSummaryRow(summaryTable, "Home %", String.format("%.1f%%", homePercentage));
        addSummaryRow(summaryTable, "Personal %", String.format("%.1f%%", personalPercentage));

        document.add(summaryTable);
        addSeparator(document);
    }

    private void addHomePersonalComparison(Document document, List<ExpenseItem> expenseItems) throws DocumentException {
        Paragraph sectionTitle = new Paragraph("HOME vs PERSONAL EXPENSES", headerFont);
        sectionTitle.setSpacingBefore(10);
        sectionTitle.setSpacingAfter(15);
        document.add(sectionTitle);

        SummaryStats stats = calculateSummaryStats(expenseItems);

        // Create comparison table
        PdfPTable comparisonTable = new PdfPTable(4);
        comparisonTable.setWidthPercentage(100);
        comparisonTable.setWidths(new float[]{2, 2, 2, 2});
        comparisonTable.setSpacingBefore(10);
        comparisonTable.setSpacingAfter(20);

        // Add headers
        addTableHeader(comparisonTable, "Type");
        addTableHeader(comparisonTable, "Amount");
        addTableHeader(comparisonTable, "Count");
        addTableHeader(comparisonTable, "Average");

        // Home expenses row
        double homeAverage = stats.homeExpenseCount > 0 ?
                (double) stats.homeExpenseTotal / stats.homeExpenseCount : 0;

        PdfPCell homeTypeCell = createColoredCell("HOME", COLOR_HOME);
        comparisonTable.addCell(homeTypeCell);
        addTableCell(comparisonTable, formatCurrency(stats.homeExpenseTotal), COLOR_LIGHT_GRAY, Element.ALIGN_RIGHT);
        addTableCell(comparisonTable, String.valueOf(stats.homeExpenseCount), COLOR_LIGHT_GRAY, Element.ALIGN_CENTER);
        addTableCell(comparisonTable, formatCurrency((long) homeAverage), COLOR_LIGHT_GRAY, Element.ALIGN_RIGHT);

        // Personal expenses row
        double personalAverage = stats.personalExpenseCount > 0 ?
                (double) stats.personalExpenseTotal / stats.personalExpenseCount : 0;

        PdfPCell personalTypeCell = createColoredCell("PERSONAL", COLOR_PERSONAL);
        comparisonTable.addCell(personalTypeCell);
        addTableCell(comparisonTable, formatCurrency(stats.personalExpenseTotal), COLOR_LIGHT_GRAY, Element.ALIGN_RIGHT);
        addTableCell(comparisonTable, String.valueOf(stats.personalExpenseCount), COLOR_LIGHT_GRAY, Element.ALIGN_CENTER);
        addTableCell(comparisonTable, formatCurrency((long) personalAverage), COLOR_LIGHT_GRAY, Element.ALIGN_RIGHT);

        // Total row
        double totalAverage = expenseItems.size() > 0 ?
                (double) stats.totalAmount / expenseItems.size() : 0;

        addTableCell(comparisonTable, "TOTAL", COLOR_PRIMARY, Element.ALIGN_CENTER);
        addTableCell(comparisonTable, formatCurrency(stats.totalAmount), COLOR_PRIMARY, Element.ALIGN_RIGHT);
        addTableCell(comparisonTable, String.valueOf(expenseItems.size()), COLOR_PRIMARY, Element.ALIGN_CENTER);
        addTableCell(comparisonTable, formatCurrency((long) totalAverage), COLOR_PRIMARY, Element.ALIGN_RIGHT);

        document.add(comparisonTable);
        addSeparator(document);
    }

    private void addCategoryBreakdown(Document document, List<ExpenseItem> expenseItems) throws DocumentException {
        Paragraph sectionTitle = new Paragraph("CATEGORY BREAKDOWN", headerFont);
        sectionTitle.setSpacingBefore(10);
        sectionTitle.setSpacingAfter(15);
        document.add(sectionTitle);

        // Calculate category stats with home/personal breakdown
        Map<String, CategoryStats> categoryStats = calculateCategoryStats(expenseItems);

        if (categoryStats.isEmpty()) {
            Paragraph noData = new Paragraph("No category data available", normalFont);
            noData.setSpacingAfter(20);
            document.add(noData);
            return;
        }

        // Create category table (6 columns)
        PdfPTable categoryTable = new PdfPTable(6);
        categoryTable.setWidthPercentage(100);
        categoryTable.setWidths(new float[]{3, 2, 2, 2, 2, 2});
        categoryTable.setSpacingBefore(10);
        categoryTable.setSpacingAfter(20);

        // Add headers
        addTableHeader(categoryTable, "Category");
        addTableHeader(categoryTable, "Total Amount");
        addTableHeader(categoryTable, "Home");
        addTableHeader(categoryTable, "Personal");
        addTableHeader(categoryTable, "Count");
        addTableHeader(categoryTable, "Avg/Item");

        // Calculate grand total
        long grandTotal = expenseItems.stream()
                .mapToLong(item -> item.getExpenseAmount() != null ? item.getExpenseAmount() : 0)
                .sum();

        // Add category rows
        boolean alternateRow = false;
        for (Map.Entry<String, CategoryStats> entry : categoryStats.entrySet()) {
            String category = entry.getKey();
            CategoryStats stats = entry.getValue();

            BaseColor rowColor = alternateRow ? COLOR_LIGHT_GRAY : COLOR_WHITE;
            alternateRow = !alternateRow;

            // Calculate average per item
            long avgPerItem = stats.itemCount > 0 ? stats.totalAmount / stats.itemCount : 0;

            // Add cells
            addTableCell(categoryTable, category, rowColor, Element.ALIGN_LEFT);
            addTableCell(categoryTable, formatCurrency(stats.totalAmount), rowColor, Element.ALIGN_RIGHT);
            addTableCell(categoryTable, formatCurrency(stats.homeTotal), rowColor, Element.ALIGN_RIGHT);
            addTableCell(categoryTable, formatCurrency(stats.personalTotal), rowColor, Element.ALIGN_RIGHT);
            addTableCell(categoryTable, String.valueOf(stats.itemCount), rowColor, Element.ALIGN_CENTER);
            addTableCell(categoryTable, formatCurrency(avgPerItem), rowColor, Element.ALIGN_RIGHT);
        }

        // Add total row
        long totalHome = expenseItems.stream()
                .filter(ExpenseItem::isHomeExpense)
                .mapToLong(item -> item.getExpenseAmount() != null ? item.getExpenseAmount() : 0)
                .sum();

        long totalPersonal = expenseItems.stream()
                .filter(item -> !item.isHomeExpense())
                .mapToLong(item -> item.getExpenseAmount() != null ? item.getExpenseAmount() : 0)
                .sum();

        long totalAvg = expenseItems.size() > 0 ? grandTotal / expenseItems.size() : 0;

        addTableCell(categoryTable, "TOTAL", COLOR_PRIMARY, Element.ALIGN_LEFT);
        addTableCell(categoryTable, formatCurrency(grandTotal), COLOR_PRIMARY, Element.ALIGN_RIGHT);
        addTableCell(categoryTable, formatCurrency(totalHome), COLOR_PRIMARY, Element.ALIGN_RIGHT);
        addTableCell(categoryTable, formatCurrency(totalPersonal), COLOR_PRIMARY, Element.ALIGN_RIGHT);
        addTableCell(categoryTable, String.valueOf(expenseItems.size()), COLOR_PRIMARY, Element.ALIGN_CENTER);
        addTableCell(categoryTable, formatCurrency(totalAvg), COLOR_PRIMARY, Element.ALIGN_RIGHT);

        document.add(categoryTable);
        addSeparator(document);
    }

    private void addExpenseDetails(Document document, List<ExpenseItem> expenseItems) throws DocumentException {
        Paragraph sectionTitle = new Paragraph("EXPENSE DETAILS", headerFont);
        sectionTitle.setSpacingBefore(10);
        sectionTitle.setSpacingAfter(15);
        document.add(sectionTitle);

        if (expenseItems.isEmpty()) {
            Paragraph noData = new Paragraph("No expense data available", normalFont);
            noData.setSpacingAfter(20);
            document.add(noData);
            return;
        }

        // Group by category
        Map<String, List<ExpenseItem>> groupedItems = groupByCategory(expenseItems);

        for (Map.Entry<String, List<ExpenseItem>> entry : groupedItems.entrySet()) {
            String category = entry.getKey();
            List<ExpenseItem> categoryItems = entry.getValue();

            // Calculate category totals
            long categoryTotal = categoryItems.stream()
                    .mapToLong(item -> item.getExpenseAmount() != null ? item.getExpenseAmount() : 0)
                    .sum();

            long categoryHomeTotal = categoryItems.stream()
                    .filter(ExpenseItem::isHomeExpense)
                    .mapToLong(item -> item.getExpenseAmount() != null ? item.getExpenseAmount() : 0)
                    .sum();

            long categoryPersonalTotal = categoryItems.stream()
                    .filter(item -> !item.isHomeExpense())
                    .mapToLong(item -> item.getExpenseAmount() != null ? item.getExpenseAmount() : 0)
                    .sum();

            // Category header with breakdown
            String categoryHeaderText = String.format("%s (Total: %s | Home: %s | Personal: %s)",
                    category,
                    formatCurrency(categoryTotal),
                    formatCurrency(categoryHomeTotal),
                    formatCurrency(categoryPersonalTotal));

            Paragraph categoryHeader = new Paragraph(categoryHeaderText,
                    new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, COLOR_SECONDARY));
            categoryHeader.setSpacingBefore(15);
            categoryHeader.setSpacingAfter(10);
            document.add(categoryHeader);

            // Create details table - RETAINED TYPE COLUMN, REMOVED ONLY Home/Personal column
            // Changed from 6 columns to 5 columns: Particulars, Amount, Date, Type, Attachment
            PdfPTable detailsTable = new PdfPTable(5);
            detailsTable.setWidthPercentage(100);
            detailsTable.setWidths(new float[]{4, 2, 2, 2, 2});
            detailsTable.setSpacingBefore(5);
            detailsTable.setSpacingAfter(15);

            // Add headers - RETAINED "Type" header, REMOVED "Home/Personal" header
            addTableHeader(detailsTable, "Particulars");
            addTableHeader(detailsTable, "Amount");
            addTableHeader(detailsTable, "Date");
            addTableHeader(detailsTable, "Type");
            addTableHeader(detailsTable, "Attachment");

            // Add expense items
            boolean alternateRow = false;
            for (ExpenseItem item : categoryItems) {
                BaseColor rowColor = alternateRow ? COLOR_LIGHT_GRAY : COLOR_WHITE;
                alternateRow = !alternateRow;

                // Particulars
                String particulars = item.getExpenseParticulars();
                if (particulars != null && particulars.length() > 30) {
                    particulars = particulars.substring(0, 27) + "...";
                }

                // Amount
                String amount = item.getExpenseAmount() != null ?
                        formatCurrency(item.getExpenseAmount()) : "N/A";

                // Date
                String date = item.getExpenseDate() != null ? item.getExpenseDate() :
                        item.getExpenseDateTime() != null ? item.getExpenseDateTime() : "N/A";

                // Type (Home/Personal) - RETAINED THIS COLUMN
                boolean isHomeExpense = item.isHomeExpense();
                String typeText = isHomeExpense ? "HOME" : "PERSONAL";
                BaseColor typeColor = isHomeExpense ? COLOR_HOME : COLOR_PERSONAL;

                // Attachment
                String attachment = (item.getFileBytes() != null && item.getFileBytes().length > 0) ?
                        "✓" : "";

                // Add cells - RETAINED Type cell, REMOVED Home/Personal description cell
                addTableCell(detailsTable, particulars != null ? particulars : "N/A",
                        rowColor, Element.ALIGN_LEFT);
                addTableCell(detailsTable, amount, rowColor, Element.ALIGN_RIGHT);
                addTableCell(detailsTable, date, rowColor, Element.ALIGN_CENTER);

                // Type cell with color - RETAINED
                PdfPCell typeCell = createColoredCell(typeText, typeColor);
                detailsTable.addCell(typeCell);

                // Attachment
                addTableCell(detailsTable, attachment, rowColor, Element.ALIGN_CENTER);
            }

            document.add(detailsTable);
        }

        addSeparator(document);
    }

    private void addFooter(Document document) throws DocumentException {
        Paragraph footer = new Paragraph("End of Report", smallFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(20);
        document.add(footer);
    }

    private void addSeparator(Document document) throws DocumentException {
        Paragraph separator = new Paragraph();
        separator.add(new Chunk("\n"));
        document.add(separator);
    }

    // Helper methods for table cells
    private void addSummaryRow(PdfPTable table, String label, String value) {
        // Label cell
        PdfPCell labelCell = new PdfPCell(new Phrase(label, boldFont));
        labelCell.setBackgroundColor(COLOR_LIGHT_GRAY);
        labelCell.setPadding(8);
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(labelCell);

        // Value cell
        PdfPCell valueCell = new PdfPCell(new Phrase(value, normalFont));
        valueCell.setBackgroundColor(COLOR_LIGHT_GRAY);
        valueCell.setPadding(8);
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }

    private void addTableHeader(PdfPTable table, String text) {
        PdfPCell headerCell = new PdfPCell(new Phrase(text, boldFont));
        headerCell.setBackgroundColor(COLOR_PRIMARY);
        headerCell.setPadding(8);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(headerCell);
    }

    private void addTableCell(PdfPTable table, String text, BaseColor backgroundColor, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", normalFont));
        cell.setBackgroundColor(backgroundColor);
        cell.setPadding(6);
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    private PdfPCell createColoredCell(String text, BaseColor color) {
        PdfPCell cell = new PdfPCell(new Phrase(text, typeFont));
        cell.setBackgroundColor(color);
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    // Statistics calculation
    private SummaryStats calculateSummaryStats(List<ExpenseItem> expenseItems) {
        SummaryStats stats = new SummaryStats();
        Map<String, Boolean> categories = new HashMap<>();

        for (ExpenseItem item : expenseItems) {
            Long amount = item.getExpenseAmount();
            if (amount != null) {
                stats.totalAmount += amount;

                // Home vs Personal
                if (item.isHomeExpense()) {
                    stats.homeExpenseTotal += amount;
                    stats.homeExpenseCount++;
                } else {
                    stats.personalExpenseTotal += amount;
                    stats.personalExpenseCount++;
                }
            }

            // Count categories
            if (item.getExpenseCategory() != null) {
                categories.put(item.getExpenseCategory(), true);
            }

            // Count attachments
            if (item.getFileBytes() != null && item.getFileBytes().length > 0) {
                stats.attachmentCount++;
            }
        }

        stats.categoryCount = categories.size();
        return stats;
    }

    private Map<String, CategoryStats> calculateCategoryStats(List<ExpenseItem> expenseItems) {
        Map<String, CategoryStats> statsMap = new HashMap<>();

        for (ExpenseItem item : expenseItems) {
            String category = item.getExpenseCategory() != null ?
                    item.getExpenseCategory() : "Uncategorized";

            CategoryStats stats = statsMap.getOrDefault(category, new CategoryStats());

            Long amount = item.getExpenseAmount();
            if (amount != null) {
                stats.totalAmount += amount;

                if (item.isHomeExpense()) {
                    stats.homeTotal += amount;
                } else {
                    stats.personalTotal += amount;
                }
            }
            stats.itemCount++;

            statsMap.put(category, stats);
        }

        return statsMap;
    }

    private Map<String, List<ExpenseItem>> groupByCategory(List<ExpenseItem> expenseItems) {
        Map<String, List<ExpenseItem>> grouped = new HashMap<>();

        for (ExpenseItem item : expenseItems) {
            String category = item.getExpenseCategory() != null ?
                    item.getExpenseCategory() : "Uncategorized";

            if (!grouped.containsKey(category)) {
                grouped.put(category, new ArrayList<>());
            }
            grouped.get(category).add(item);
        }

        return grouped;
    }

    private String formatCurrency(long amount) {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        return "₹" + df.format(amount);
    }

    // Quick method for simple report with Type column
    public void generateQuickReport(List<ExpenseItem> expenseItems, String fileName) throws Exception {
        File pdfDir = new File(context.getExternalFilesDir(null), "ExpenseReports");
        if (!pdfDir.exists()) {
            pdfDir.mkdirs();
        }

        File pdfFile = new File(pdfDir, fileName + ".pdf");

        Document document = new Document(PageSize.A4);
        document.setMargins(40, 40, 40, 40);

        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
        document.open();

        // Title
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, COLOR_PRIMARY);
        Paragraph title = new Paragraph("EXPENSE REPORT", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        document.add(new Paragraph("\n"));

        // Simple table with Type column
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{4, 2, 2, 2, 2});

        // Headers
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, COLOR_WHITE);
        String[] headers = {"Particulars", "Amount", "Date", "Type", "Attachment"};

        for (String header : headers) {
            PdfPCell headerCell = new PdfPCell(new Phrase(header, headerFont));
            headerCell.setBackgroundColor(COLOR_PRIMARY);
            headerCell.setPadding(5);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(headerCell);
        }

        // Rows
        Font rowFont = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, COLOR_DARK_GRAY);
        DecimalFormat df = new DecimalFormat("#,##0.00");

        long total = 0;
        long homeTotal = 0;
        long personalTotal = 0;

        for (ExpenseItem item : expenseItems) {
            // Particulars
            String particulars = item.getExpenseParticulars();
            if (particulars != null && particulars.length() > 25) {
                particulars = particulars.substring(0, 22) + "...";
            }
            table.addCell(new PdfPCell(new Phrase(particulars != null ? particulars : "N/A", rowFont)));

            // Amount
            long amount = item.getExpenseAmount() != null ? item.getExpenseAmount() : 0;
            String amountStr = "₹" + df.format(amount);
            table.addCell(new PdfPCell(new Phrase(amountStr, rowFont)));
            total += amount;

            // Date
            String dateStr = item.getExpenseDate() != null ? item.getExpenseDate() : "N/A";
            table.addCell(new PdfPCell(new Phrase(dateStr, rowFont)));

            // Type with color
            boolean isHome = item.isHomeExpense();
            String typeText = isHome ? "HOME" : "PERSONAL";
            BaseColor typeColor = isHome ? COLOR_HOME : COLOR_PERSONAL;

            PdfPCell typeCell = new PdfPCell(new Phrase(typeText, typeFont));
            typeCell.setBackgroundColor(typeColor);
            typeCell.setPadding(5);
            typeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(typeCell);

            // Attachment
            String attachment = (item.getFileBytes() != null && item.getFileBytes().length > 0) ? "✓" : "";
            table.addCell(new PdfPCell(new Phrase(attachment, rowFont)));

            // Add to totals
            if (isHome) {
                homeTotal += amount;
            } else {
                personalTotal += amount;
            }
        }

        document.add(table);

        // Totals section
        document.add(new Paragraph("\n\n"));

        Font totalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, COLOR_PRIMARY);

        Paragraph homeTotalPara = new Paragraph("Home Expenses: ₹" + df.format(homeTotal), totalFont);
        document.add(homeTotalPara);

        Paragraph personalTotalPara = new Paragraph("Personal Expenses: ₹" + df.format(personalTotal), totalFont);
        document.add(personalTotalPara);

        Paragraph grandTotalPara = new Paragraph("Total: ₹" + df.format(total),
                new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, COLOR_SUCCESS));
        grandTotalPara.setAlignment(Element.ALIGN_RIGHT);
        grandTotalPara.setSpacingBefore(10);
        document.add(grandTotalPara);

        document.close();
    }

    public interface PDFGenerationCallback {
        void onSuccess(File pdfFile);

        void onError(String errorMessage);

        void onProgress(String message);
    }

    // Statistics classes
    private static class SummaryStats {
        long totalAmount;
        long homeExpenseTotal;
        long personalExpenseTotal;
        int homeExpenseCount;
        int personalExpenseCount;
        int categoryCount;
        int attachmentCount;
    }

    private static class CategoryStats {
        long totalAmount;
        long homeTotal;
        long personalTotal;
        int itemCount;
    }
}