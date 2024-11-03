package com.example.expenseutility;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.expenseutility.database.DatabaseHelper;
import com.example.expenseutility.databinding.FragmentSecondBinding;
import com.example.expenseutility.databinding.FragmentThirdBinding;
import com.example.expenseutility.entityadapter.ExpenseItem;
import com.example.expenseutility.utility.ExpenseAdapter;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.CalendarConstraints;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ThirdFragment extends Fragment {
    private FragmentThirdBinding binding;
    private DatabaseHelper db;

    private RecyclerView recyclerView;
    private ExpenseAdapter adapter;
    private List<ExpenseItem> expenseItems;

    private SearchView searchView;

    private List<ExpenseItem> filteredList = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentThirdBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = new DatabaseHelper(getContext());
        recyclerView = view.findViewById(R.id.seeAllRecyclerView);
        expenseItems = new ArrayList<>();
        searchView = view.findViewById(R.id.search);
        loadExpenseList();

        ChipGroup chipGroup = view.findViewById(R.id.chip_group);
        String[] categories = {"Housing Expenses", "Transportation", "Food", "Healthcare", "Fuel", "Debt Payments", "Entertainment"
        ,"Savings and Investments","Grocery","Clothing and Personal Care",
        "Education","Charity and Gifts","Travel","Insurance","Childcare and Education","Miscellaneous"};

        for (String category : categories) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_item, chipGroup, false);
            chip.setText(category);
            chip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    filterExpensesByCategory(); // Call your filtering method
                }
            });
            chipGroup.addView(chip);
        }

        binding.openFilteredChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int[] collect = filteredList.stream().mapToInt(ExpenseItem::getId).toArray();

                Intent intent = new Intent(getContext(), ChartActivity.class);
                intent.putExtra("filteredByDate", (Serializable) true);
                intent.putExtra("filteredList", (Serializable) collect);
                startActivity(intent);

                Toast.makeText(getContext(), "Chart Filtered ", Toast.LENGTH_SHORT).show();
            }
        });


        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchList(newText);
                return true;
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());

        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new ExpenseAdapter(getContext(), expenseItems);

        recyclerView.setAdapter(adapter);



    }

    public void filterExpensesByCategory() {
        String selectedCategories ="";
        for (int i = 0; i < binding.chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) binding.chipGroup.getChildAt(i);
            if (chip.isChecked()) {
                selectedCategories = chip.getText().toString();
            }
        }
        adapter.filterExpensesByCategory(selectedCategories, binding.bannerTxt);
    }



    private void searchList(String newText) {
        List<ExpenseItem> expenseItemsForSearch = new ArrayList<>();
        boolean searchByDate = false;
        if (ThirdFragment.isValidDate(newText)) {
            Log.d("DateValidator", newText + " is valid.");
            searchByDate = true;
        } else {
            Log.d("DateValidator", newText + " is invalid.");
            searchByDate = false;
        }

        for (ExpenseItem item : expenseItems) {
            if(searchByDate){
                if(item.getExpenseDate().toLowerCase().contains(newText.toLowerCase())) {
                    expenseItemsForSearch.add(item);
                }
            } else {
                if(item.getExpenseCategory().toLowerCase().contains(newText.toLowerCase())) {
                    expenseItemsForSearch.add(item);
                }
            }
        }
        if(expenseItemsForSearch.isEmpty()){
//            Toast.makeText(getContext(), "Not found", Toast.LENGTH_SHORT).show();
            binding.openFilteredChart.setVisibility(View.GONE);
        } else {
            adapter.setSearchList(expenseItemsForSearch);
            filteredList.clear();
            filteredList.addAll(expenseItemsForSearch);
            if(expenseItemsForSearch.size() < expenseItems.size()) {
                binding.openFilteredChart.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), "..", Toast.LENGTH_SHORT).show();
            }
        }

    }

    // Regex for multiple date formats including slashes and dashes
    private static final String DATE_PATTERN =
            "^\\d{4}[-/]\\d{2}[-/]\\d{2}$|" +   // yyyy-MM-dd or yyyy/MM/dd (e.g., 2024-09-09 or 2024/09/09)
                    "^\\d{2}[-/]\\d{2}[-/]\\d{4}$|" +   // dd-MM-yyyy or dd/MM/yyyy (e.g., 09-09-2024 or 09/09/2024)
                    "^\\d{2}[-/]\\d{2}[-/]\\d{2}$|" +   // yy-MM-dd or dd-MM-yy, also with slashes (e.g., 24-09-09 or 09/09/24)
                    "^\\d{2}[-/]\\d{2}[-/]\\d{4}$";     // MM-dd-yyyy or MM/dd/yyyy (e.g., 09-24-2024 or 09/24/2024)

    // Method to validate the input text with regex
    public static boolean isValidDate(String inputText) {
        Pattern pattern = Pattern.compile(DATE_PATTERN);
        Matcher matcher = pattern.matcher(inputText);
        return matcher.matches();  // Return true if input matches the regex
    }



    private void loadExpenseList() {
        Cursor expenseData = db.getAllExpenseData();
        if(expenseData.getCount() > 0){
            expenseItems.clear();

            while(expenseData.moveToNext()){
                expenseItems.add(new ExpenseItem(
                        Integer.valueOf(expenseData.getInt(0)),
                        String.valueOf(expenseData.getString(2)),
                        expenseData.getLong(3),
                        String.valueOf(expenseData.getString(5)),
                        String.valueOf(expenseData.getString(1)),
                        String.valueOf(expenseData.getString(6)),
                        expenseData.getBlob(7))
                );
            }
        } else {
            Toast.makeText(getContext(), "Expense List is empty !", Toast.LENGTH_SHORT).show();
        }
    }
}