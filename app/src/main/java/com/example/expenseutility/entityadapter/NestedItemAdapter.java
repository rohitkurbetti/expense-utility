package com.example.expenseutility.entityadapter;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.expenseutility.R;
import com.example.expenseutility.firebaseview.NestedListAdapter;
import com.example.expenseutility.utility.Commons;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class NestedItemAdapter extends ArrayAdapter<ExpenseItem> {

    private final Context context;
    private final List<ExpenseItem> items;
    private final List<Boolean> checkedStates; // Track the checkbox states


    public NestedItemAdapter(Context context, List<ExpenseItem> items) {
        super(context, R.layout.nested_item_popup_layout, items);
        this.context = context;
        this.items = items;

        this.checkedStates = new ArrayList<>(items.size());

        // Initialize all checkboxes as unchecked
        for (int i = 0; i < items.size(); i++) {
            checkedStates.add(false);
        }

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.nested_item_popup_layout, parent, false);
        }

        ExpenseItem currentItem = items.get(position);

        CheckBox delNestedItemCheckBox = convertView.findViewById(R.id.delNestedItemCheckBox);
        ImageView iconViewNested = convertView.findViewById(R.id.iconViewNested);
        TextView titleTextView = convertView.findViewById(R.id.textViewParticulars);
        TextView amountTextView = convertView.findViewById(R.id.textViewAmount);
        TextView descriptionTextView = convertView.findViewById(R.id.textViewCategory);

        iconViewNested.setImageDrawable(context.getDrawable(currentItem.getId()));
        titleTextView.setText(currentItem.getExpenseParticulars());
        amountTextView.setText(String.valueOf("\u20B9"+currentItem.getExpenseAmount()));
        descriptionTextView.setText(currentItem.getExpenseCategory());

        if(currentItem.getPartDetails() !=null && !currentItem.getPartDetails().isBlank()) {
            titleTextView.setPaintFlags(titleTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

            // Add info icon for items with partDetails
            Drawable infoIcon = ContextCompat.getDrawable(context, R.drawable.sign_info_svgrepo_com);
            infoIcon.setBounds(0, 0, infoIcon.getIntrinsicWidth(), infoIcon.getIntrinsicHeight());
            titleTextView.setCompoundDrawablesRelative(null, null, infoIcon, null);
            titleTextView.setCompoundDrawablePadding(16);


            titleTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setMessage(!currentItem.getPartDetails().isEmpty() ? Commons.decryptString(currentItem.getPartDetails()) : currentItem.getPartDetails())
                            .setPositiveButton("OK", (dialog, id) -> dialog.dismiss());
                    AlertDialog dialog = builder.create();
                    dialog.show();

                    // Optional: Customize dialog text view
                    TextView messageView = dialog.findViewById(android.R.id.message);
                    if (messageView != null) {
                        messageView.setTextSize(14);
                        messageView.setLineSpacing(1.2f, 1.2f);
                    }
                }
            });
        } else {
            titleTextView.setOnClickListener(null);
            titleTextView.setPaintFlags(titleTextView.getPaintFlags() & ~Paint.UNDERLINE_TEXT_FLAG);
            titleTextView.setCompoundDrawablePadding(0);
            titleTextView.setCompoundDrawablesRelative(null, null, null, null);
        }


        delNestedItemCheckBox.setChecked(checkedStates.get(position));

        delNestedItemCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                currentItem.setChecked(isChecked);


                Boolean isChecekd= items.stream().filter(i -> i.getChecked().equals(Boolean.TRUE)).findAny().isPresent();

                if(isChecekd) {
                    NestedListAdapter.btnDeletePopup.setVisibility(View.VISIBLE);
                } else  {
                    NestedListAdapter.btnDeletePopup.setVisibility(View.GONE);
                }

            }
        });


        return convertView;
    }

}
