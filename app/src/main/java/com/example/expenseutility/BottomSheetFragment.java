package com.example.expenseutility;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.example.expenseutility.databinding.BottomSheetLayoutBinding;
import com.example.expenseutility.entityadapter.ExpenseItem;
import com.example.expenseutility.utility.SpinnerItem;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BottomSheetFragment extends BottomSheetDialogFragment {

    private BottomSheetLayoutBinding binding;

    private static final String ARG_OBJECT = "arg_object";
    private SharedPreferences sharedPreferences;

    public static BottomSheetFragment newInstance(ExpenseItem myObject) {
        BottomSheetFragment fragment = new BottomSheetFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_OBJECT, myObject);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = BottomSheetLayoutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sharedPreferences = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        Map<String, Integer> map = new HashMap<>();
        map.put("Housing Expenses", R.drawable.house_to_rent_svgrepo_com);
        map.put("Transportation", R.drawable.ground_transportation_svgrepo_com);
        map.put("Food", R.drawable.meal_easter_svgrepo_com);
        map.put("Healthcare", R.drawable.healthcare_hospital_medical_9_svgrepo_com);
        map.put("Recharge", R.drawable.mobile_phone_recharge_svgrepo_com);
        map.put("Shopping", R.drawable.shopping_cart_svgrepo_com);
        map.put("Subscriptions", R.drawable.youtube_svgrepo_com);
        map.put("Debt Payments", R.drawable.money_svgrepo_com__1_);
        map.put("Entertainment", R.drawable.entertainment_svgrepo_com);
        map.put("Savings and Investments", R.drawable.piggybank_pig_svgrepo_com);
        map.put("Clothing and Personal Care", R.drawable.clothes_clothing_formal_wear_svgrepo_com);
        map.put("Education", R.drawable.education_graduation_learning_school_study_svgrepo_com);
        map.put("Charity and Gifts", R.drawable.loving_charity_svgrepo_com);
        map.put("Travel", R.drawable.travel_svgrepo_com__1_);
        map.put("Insurance", R.drawable.employee_svgrepo_com);
        map.put("Childcare and Education", R.drawable.woman_pushing_stroller_svgrepo_com);
        map.put("Miscellaneous", R.drawable.notebook_miscellaneous_svgrepo_com);
        map.put("Fuel", R.drawable.fuel_station);
        map.put("Grocery", R.drawable.shopping_basket);

        if (getArguments() != null) {
            ExpenseItem expenseItem = (ExpenseItem) getArguments().getSerializable(ARG_OBJECT);
            if (expenseItem != null) {
                binding.expAmountTxt.setText(String.valueOf("\u20B9"+expenseItem.getExpenseAmount()));
                binding.dailyIncomeTxt.setText(String.format("%.2f",((double) expenseItem.getExpenseAmount()/(sharedPreferences.getFloat("monthlyIncome",87000f)/30))*100)+"%");
                binding.monthlyIncomeTxt.setText(String.format("%.2f",((double) expenseItem.getExpenseAmount()/sharedPreferences.getFloat("monthlyIncome",87000f))*100)+"%");
                binding.imageViewCategory.setImageDrawable(getResources().getDrawable(map.get(expenseItem.getExpenseCategory())));

                if(expenseItem.getFileBytes() != null){
                    binding.btnDownloadFile.setVisibility(View.VISIBLE);
                    binding.btnDownloadFile.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String fileName = expenseItem.getFileName();

                            // Assume filePath is the path to the downloaded file
                            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                            File downloadedFile = new File(downloadsDir, fileName);
                            if (downloadedFile.exists()) {
                                openFile(downloadedFile);
                            } else {
                                Toast.makeText(getContext(), "File not found", Toast.LENGTH_SHORT).show();
                            }


                        }
                    });
                } else {
                    binding.btnDownloadFile.setVisibility(View.GONE);
                }


            }
        }





        binding.closeButton.setOnClickListener(v -> dismiss());
    }

    private void openFile(File downloadedFile) {


        Uri fileUri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".fileprovider", downloadedFile);
        String mimeType = getContext().getContentResolver().getType(fileUri);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, mimeType);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Intent chooser = Intent.createChooser(intent, "Open File");
        try {
            startActivity(chooser);
        } catch (ActivityNotFoundException e) {
            // Handle the case where no PDF reader is installed
            Toast.makeText(getContext(), "No application found to open PDF", Toast.LENGTH_SHORT).show();
        }

    }

}
