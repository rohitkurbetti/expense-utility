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
import com.example.expenseutility.utility.SpinnerItem;
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

            updateImageDrawableInExpenseItems(expenseItems);

        } else {
            Toast.makeText(getContext(), "Expense List is empty !", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateImageDrawableInExpenseItems(List<ExpenseItem> expenseItems) {

        List<SpinnerItem> items = new ArrayList<>();
        items.add(new SpinnerItem("Select Options", 0));
        items.add(new SpinnerItem("Housing Expenses", R.drawable.house_to_rent_svgrepo_com));
        items.add(new SpinnerItem("Transportation", R.drawable.ground_transportation_svgrepo_com));
        items.add(new SpinnerItem("Food", R.drawable.meal_easter_svgrepo_com));
        items.add(new SpinnerItem("Healthcare", R.drawable.healthcare_hospital_medical_9_svgrepo_com));
        items.add(new SpinnerItem("Fuel", R.drawable.fuel_station));
        items.add(new SpinnerItem("Debt Payments", R.drawable.money_svgrepo_com__1_));
        items.add(new SpinnerItem("Entertainment", R.drawable.entertainment_svgrepo_com));
        items.add(new SpinnerItem("Savings and Investments", R.drawable.piggybank_pig_svgrepo_com));
        items.add(new SpinnerItem("Grocery", R.drawable.shopping_basket));
        items.add(new SpinnerItem("Clothing and Personal Care", R.drawable.clothes_clothing_formal_wear_svgrepo_com));
        items.add(new SpinnerItem("Education", R.drawable.education_graduation_learning_school_study_svgrepo_com));
        items.add(new SpinnerItem("Charity and Gifts", R.drawable.loving_charity_svgrepo_com));
        items.add(new SpinnerItem("Travel", R.drawable.travel_svgrepo_com__1_));
        items.add(new SpinnerItem("Insurance", R.drawable.employee_svgrepo_com));
        items.add(new SpinnerItem("Childcare and Education", R.drawable.woman_pushing_stroller_svgrepo_com));
        items.add(new SpinnerItem("Miscellaneous", R.drawable.healthcare_hospital_medical_9_svgrepo_com));


        expenseItems.forEach(e -> {
            List<SpinnerItem> items1 = items.stream().filter(i -> i.getText().equalsIgnoreCase(e.getExpenseCategory())).collect(Collectors.toList());
            e.setImageDrawableId(items1.get(0).getImageResourceId());
        });

    }
}