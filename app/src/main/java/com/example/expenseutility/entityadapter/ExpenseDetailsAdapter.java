package com.example.expenseutility.entityadapter;

import static com.example.expenseutility.SecondFragment.callOnClickListener;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.example.expenseutility.R;
import com.example.expenseutility.utility.Commons;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

public class ExpenseDetailsAdapter extends BaseAdapter {
    private Context context;
    private List<ExpenseItem> expenseItemList;


    public ExpenseDetailsAdapter(Context context, List<ExpenseItem> expenseItemList) {
        this.context = context;
        this.expenseItemList = expenseItemList;
    }

    public void filterMonthlyList(Context context, List<ExpenseItem> expenseItemList) {
        this.context = context;
        this.expenseItemList = expenseItemList;
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return expenseItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return expenseItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.expense_details_item, parent, false);
        }
        ExpenseItem expenseItem = (ExpenseItem) expenseItemList.get(position);
        TextView expenseIdTxt = convertView.findViewById(R.id.expenseIdTxt);
        TextView expenseParticularsTxt = convertView.findViewById(R.id.expenseParticularsTxt);
        TextView expenseDateTxt = convertView.findViewById(R.id.expenseDateTxt);
        TextView expenseAmountTxt = convertView.findViewById(R.id.expenseAmountTxt);
        ImageView expenseDeleteBtn = convertView.findViewById(R.id.expenseDeleteBtn);

        LinearLayout capsuleLayout = convertView.findViewById(R.id.capsuleLayout);
        TextView initials = convertView.findViewById(R.id.textInitials);

        TextView textLabel = convertView.findViewById(R.id.textLabel);

        if (expenseItem.isHomeExpense()) {
            capsuleLayout.setVisibility(View.VISIBLE);
            textLabel.setText("Home");

            capsuleLayout.setOnClickListener(v -> {
                getExpensesByCategories();
            });

        } else {
            capsuleLayout.setVisibility(View.GONE);
            textLabel.setText("");
        }


        expenseIdTxt.setText(String.valueOf(expenseItem.getId()));
        expenseParticularsTxt.setText(String.valueOf(expenseItem.getExpenseParticulars()));

        if (expenseItem.getPartDetails() != null && !expenseItem.getPartDetails().isEmpty()) {
            expenseParticularsTxt.setPaintFlags(expenseParticularsTxt.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

            expenseParticularsTxt.setOnClickListener(v -> {

                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setMessage(!expenseItem.getPartDetails().isEmpty() ? Commons.decryptString(expenseItem.getPartDetails()) : expenseItem.getPartDetails())
                        .setPositiveButton("OK", (dialog, id) -> dialog.dismiss());
                AlertDialog dialog = builder.create();
                dialog.show();

                // Optional: Customize dialog text view
                TextView messageView = dialog.findViewById(android.R.id.message);
                if (messageView != null) {
                    messageView.setTextSize(14);
                    messageView.setLineSpacing(1.2f, 1.2f);
                }
            });
        } else {
            expenseParticularsTxt.setOnClickListener(null);
            expenseParticularsTxt.setPaintFlags(expenseParticularsTxt.getPaintFlags() & ~Paint.UNDERLINE_TEXT_FLAG);

        }

        expenseDateTxt.setText(String.valueOf(expenseItem.getExpenseDate()));
        expenseAmountTxt.setText(String.valueOf("\u20B9" + expenseItem.getExpenseAmount()));

        // Generate initials
        String[] words = expenseParticularsTxt.getText().toString().trim().split("\\s+");
        StringBuilder init = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) init.append(Character.toUpperCase(word.charAt(0)));
        }
//            initials.setText(init.length() > 2 ? init.substring(0, 2) : init.toString());  // for 2 initials ex. Tea Coffee -> TC
        initials.setText(words.length > 0 && !Objects.equals(words[0], "") ? String.valueOf(Character.toUpperCase(words[0].charAt(0))) : "?");  // for only 1 initials ex. Tea Coffee -> T

        // Create a round shape with random color
        GradientDrawable circleDrawable = new GradientDrawable();
        circleDrawable.setShape(GradientDrawable.OVAL);
        circleDrawable.setColor(getRandomColor1()); // generate random color
        circleDrawable.setSize(100, 100); // optional, for sizing
        initials.setBackground(circleDrawable);


        expenseDeleteBtn.setOnClickListener(v -> {
            callOnClickListener(context, expenseIdTxt.getText().toString(), position, expenseItem);

        });

//            // Animate each item (fade + slide)
//            Animation animation = AnimationUtils.loadAnimation(context, R.anim.fade_in_top_to_bottom);
//            convertView.startAnimation(animation);


//        }
        return convertView;
    }

    private void getExpensesByCategories() {

        Map<String, Long> expensesByCategory;
        expensesByCategory = expenseItemList.stream()
                .filter(item -> item.getExpenseAmount() != null && item.getExpenseCategory() != null &&
                        item.isHomeExpense())
                .collect(Collectors.groupingBy(
                        ExpenseItem::getExpenseCategory,
                        Collectors.summingLong(ExpenseItem::getExpenseAmount)
                ));

        if (expensesByCategory.isEmpty()) {
            Toast.makeText(context, "No expenses found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build message with formatting
        SpannableStringBuilder message = new SpannableStringBuilder();
        long totalAllCategories = 0;
        int position = 0;

        // Get current date and time
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy - hh:mm a", Locale.getDefault());
        String currentDateTime = dateFormat.format(new Date());

        // Add date/time at the top
        message.append("Report Date: ").append(currentDateTime).append("\n\n");

        List<Map.Entry<String, Long>> sortedEntries = new ArrayList<>(expensesByCategory.entrySet());
        sortedEntries.sort((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()));

        for (Map.Entry<String, Long> entry : sortedEntries) {
            String category = entry.getKey();
            Long amount = entry.getValue();
            totalAllCategories += amount;

            String line = String.format("%s: ₹%,d\n", category, amount);
            message.append(line);

            // Make category names bold (optional)
            if (position < sortedEntries.size() - 1) {
                int start = message.length() - line.length();
                int end = start + category.length();
                message.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            position++;
        }

        message.append("\n");
        String totalLine = String.format("Grand Total: ₹%,d", totalAllCategories);
        message.append(totalLine);

        // Make total bold
        message.setSpan(new StyleSpan(Typeface.BOLD),
                message.length() - totalLine.length(),
                message.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Create Material AlertDialog
        Map<String, Long> finalExpensesByCategory = expensesByCategory;
        long finalTotalAllCategories = totalAllCategories;
        new MaterialAlertDialogBuilder(context)
                .setTitle("Expense Summary by Category")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .setNeutralButton("Share", (dialog, which) -> shareSummary(finalExpensesByCategory, finalTotalAllCategories, currentDateTime))
                .show();

    }

    private void shareSummary(Map<String, Long> expensesByCategory, long totalAllCategories, String currentDateTime) {
        StringBuilder shareText = new StringBuilder("Expense Summary: " + currentDateTime + "\n\n");

        for (Map.Entry<String, Long> entry : expensesByCategory.entrySet()) {
            shareText.append(entry.getKey())
                    .append(": ₹")
                    .append(entry.getValue())
                    .append("\n");
        }

        shareText.append("\nTotal: ₹").append(totalAllCategories);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
        context.startActivity(Intent.createChooser(shareIntent, "Share Summary"));
    }

    private int getRandomColor() {
        Random random = new Random();
        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);
        return Color.rgb(red, green, blue);
    }

    private int getRandomColor1() {
        int[] colors = {
                Color.parseColor("#F44336"), // Red
                Color.parseColor("#E91E63"), // Pink
                Color.parseColor("#9C27B0"), // Purple
                Color.parseColor("#2196F3"), // Blue
                Color.parseColor("#009688"), // Teal
                Color.parseColor("#4CAF50"), // Green
                Color.parseColor("#FF9800"), // Orange
                Color.parseColor("#795548")  // Brown
        };
        return colors[new Random().nextInt(colors.length)];
    }

}
