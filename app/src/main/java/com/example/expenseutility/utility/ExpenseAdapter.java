package com.example.expenseutility.utility;

import static androidx.appcompat.content.res.AppCompatResources.getDrawable;

import static com.example.expenseutility.constants.ExpenseConstants.ANN_INCOME;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expenseutility.BottomSheetFragment;
import com.example.expenseutility.R;
import com.example.expenseutility.entityadapter.ExpenseItem;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseViewHolder> {

    private Context context;
    private List<ExpenseItem> expenseItems;
    private List<ExpenseItem> expenseItemsBkp = new ArrayList<>();

    public ExpenseAdapter(Context context, List<ExpenseItem> expenseItems) {
        this.context = context;
        this.expenseItems = expenseItems;
        expenseItemsBkp.clear();
        expenseItemsBkp.addAll(expenseItems);
    }

    public void setSearchList(List<ExpenseItem> expenseItems){
        this.expenseItems = expenseItems;
        notifyDataSetChanged();
    }

    public void filterExpensesByCategory(String selectedCategories, TextView bannerTxt) {
        List<ExpenseItem> filteredList = new ArrayList<>();
        if(selectedCategories.equals("")){
            expenseItems.clear();
            expenseItems.addAll(expenseItemsBkp);
            notifyDataSetChanged();
            bannerTxt.setText("");
            bannerTxt.setVisibility(View.GONE);
        } else {
            if (selectedCategories.equalsIgnoreCase("Today's expenses")) {
                String dateFmtted = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                expenseItemsBkp.stream().filter(e -> e.getExpenseDate().equalsIgnoreCase(dateFmtted)).collect(Collectors.toList());
                for (ExpenseItem expense : expenseItemsBkp) {
                    if (dateFmtted.contains(expense.getExpenseDate())) {
                        filteredList.add(expense);
                    }
                }
            } else {
                for (ExpenseItem expense : expenseItemsBkp) {
                    if (selectedCategories.contains(expense.getExpenseCategory())) {
                        filteredList.add(expense);
                    }
                }
            }
            updateRecyclerView(filteredList, bannerTxt);
        }
    }

    public void updateRecyclerView(List<ExpenseItem> filteredExpenses, TextView bannerTxt) {
        int sum = filteredExpenses.stream().mapToInt(i -> i.getExpenseAmount().intValue()).sum();

        float income = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).getFloat("monthlyIncome", ANN_INCOME);


        double val = ((double) sum/income)*100;
        if(sum != 0) {
            bannerTxt.setVisibility(View.VISIBLE);
            bannerTxt.setText("Total: \u20b9" + sum + "              Monthly exp: " + String.format("%.2f",val)+"%");
        } else {
            bannerTxt.setVisibility(View.VISIBLE);
            bannerTxt.setText("No records found");
        }
        expenseItems.clear();
        expenseItems.addAll(filteredExpenses);
        notifyDataSetChanged();
    }



    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.see_all_recyclerview_item, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {

        holder.seeAllImageView.setImageDrawable(context.getDrawable(expenseItems.get(position).getImageDrawableId()));
        holder.particulars.setText(expenseItems.get(position).getExpenseParticulars());
        holder.amount.setText(String.valueOf(expenseItems.get(position).getExpenseAmount()));
        holder.date.setText(expenseItems.get(position).getExpenseDate());
        holder.category.setText(expenseItems.get(position).getExpenseCategory());
        holder.recCard.setOnClickListener(v -> {
            BottomSheetFragment bottomSheetFragment = BottomSheetFragment.newInstance(expenseItems.get(position));
            bottomSheetFragment.show(((AppCompatActivity) v.getContext()).getSupportFragmentManager(), "BottomSheetDialog");
        });


    }

    @Override
    public int getItemCount() {
        return expenseItems.size();
    }
}


class ExpenseViewHolder extends RecyclerView.ViewHolder {

    TextView particulars,amount,date,category;
    ImageView seeAllImageView;
    CardView recCard;



    public ExpenseViewHolder(@NonNull View itemView) {
        super(itemView);

        seeAllImageView = itemView.findViewById(R.id.seeAllImageView);
        recCard = itemView.findViewById(R.id.recCard);
        particulars = itemView.findViewById(R.id.particularsTxt);
        amount = itemView.findViewById(R.id.amountTxt);
        date = itemView.findViewById(R.id.expenseDateTxt);
        category = itemView.findViewById(R.id.expenseCategoryTxt);



    }
}
