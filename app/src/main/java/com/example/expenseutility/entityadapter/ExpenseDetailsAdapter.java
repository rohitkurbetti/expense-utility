package com.example.expenseutility.entityadapter;

import static com.example.expenseutility.SecondFragment.callOnClickListener;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.expenseutility.R;

import java.util.List;
import java.util.Random;

public class ExpenseDetailsAdapter extends BaseAdapter {
    private Context context;
    private List<ExpenseItem> expenseItemList;

    private static final int VIEW_TYPE_MONTH = 0;
    private static final int VIEW_TYPE_TRANSACTION = 1;


    public ExpenseDetailsAdapter(Context context, List<ExpenseItem> expenseItemList) {
        this.context = context;
        this.expenseItemList = expenseItemList;
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

//    @Override
//    public int getItemViewType(int position) {
//        if(expenseItemList.get(position) instanceof String) {
//            return VIEW_TYPE_MONTH;
//        } else {
//            return  VIEW_TYPE_TRANSACTION;
//        }
//    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

//        int viewType = getItemViewType(position);


//        if (viewType == VIEW_TYPE_MONTH) {
//            if (convertView == null) {
//                convertView = LayoutInflater.from(context).inflate(R.layout.expense_details_month_item, parent, false);
//            }
//            TextView monthText = convertView.findViewById(R.id.monthText);
//            monthText.setText((String) expenseItemList.get(position));
//        } else {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.expense_details_item, parent, false);
            }
            ExpenseItem expenseItem = (ExpenseItem) expenseItemList.get(position);
            TextView expenseIdTxt = convertView.findViewById(R.id.expenseIdTxt);
            TextView expenseParticularsTxt = convertView.findViewById(R.id.expenseParticularsTxt);
            TextView expenseDateTxt = convertView.findViewById(R.id.expenseDateTxt);
            TextView expenseAmountTxt = convertView.findViewById(R.id.expenseAmountTxt);
            ImageView expenseDeleteBtn = convertView.findViewById(R.id.expenseDeleteBtn);

            TextView initials = convertView.findViewById(R.id.textInitials);



            expenseIdTxt.setText(String.valueOf(expenseItem.getId()));
            expenseParticularsTxt.setText(String.valueOf(expenseItem.getExpenseParticulars()));
            expenseDateTxt.setText(String.valueOf(expenseItem.getExpenseDate()));
            expenseAmountTxt.setText(String.valueOf(expenseItem.getExpenseAmount()));

            // Generate initials
            String[] words = expenseParticularsTxt.getText().toString().trim().split("\\s+");
            StringBuilder init = new StringBuilder();
            for (String word : words) {
                if (!word.isEmpty()) init.append(Character.toUpperCase(word.charAt(0)));
            }
//            initials.setText(init.length() > 2 ? init.substring(0, 2) : init.toString());  // for 2 initials ex. Tea Coffee -> TC
            initials.setText(words.length > 0 ? String.valueOf(Character.toUpperCase(words[0].charAt(0))) : "?");  // for only 1 initials ex. Tea Coffee -> T

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
