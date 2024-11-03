package com.example.expenseutility.entityadapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expenseutility.R;
import com.example.expenseutility.SuggestionActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GridAdapter extends RecyclerView.Adapter<GridAdapter.ViewHolder> {

    private List<String> titles;
    private List<Integer> images;
    private Context context;

    public GridAdapter(Context context, List<String> titles, List<Integer> images) {
        this.context = context;
        this.titles = titles;
        this.images = images;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.gridItemTitle.setText(titles.get(position));
        holder.gridItemImage.setImageResource(images.get(position));

        holder.gridItemcardView.setOnClickListener(v -> {
            String itemName = titles.get(position);

            // Attempt to launch the activity dynamically based on the item name
            try {
                // Convert itemName to a fully qualified class name
                String className = context.getPackageName() + "." + itemName + "Activity"; // Example: "com.example.app.Item1Activity"
                Class<?> activityClass = Class.forName(className);

                // Create an Intent to start the resolved activity
                Intent intent = new Intent(context, activityClass);
                context.startActivity(intent);
            } catch (ClassNotFoundException e) {
                // Show an error message if the class is not found
                Toast.makeText(context, "Activity for " + itemName + " not found!", Toast.LENGTH_SHORT).show();
            }
        });

        holder.getGridItemThreeDots.setOnClickListener(v -> showPopupMenu(holder.getGridItemThreeDots, position));



    }

    private void showPopupMenu(ImageView getGridItemThreeDots, int position) {
        // Create a PopupMenu, attach it to the view
        PopupMenu popupMenu = new PopupMenu(context, getGridItemThreeDots);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.config_grid_item_menu, popupMenu.getMenu());

        // Define a Map of Menu IDs to Actions
        Map<Integer, Runnable> menuActions = new HashMap<>();
        menuActions.put(R.id.action_update_firebase, () -> {

            saveSuggestionsToFirebase();

        });

        // Handle menu item clicks using the Map
        popupMenu.setOnMenuItemClickListener(item -> {
            Runnable action = menuActions.get(item.getItemId());
            if (action != null) {
                action.run();  // Execute the action if it exists in the map
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    private void saveSuggestionsToFirebase() {

        List<Suggestion> suggList = SuggestionActivity.suggestionsList;

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        if (suggList != null && suggList.size() > 0) {
            database.child("suggestions").setValue(suggList)
            .addOnSuccessListener(aVoid -> {
                // Success message
                Toast.makeText(context, "Suggestions saved on cloud.", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                // Error handling
                Toast.makeText(context, "Failed to save suggestions on cloud.", Toast.LENGTH_SHORT).show();
            });
        }

    }

    @Override
    public int getItemCount() {
        return titles.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView gridItemTitle;
        ImageView gridItemImage, getGridItemThreeDots;
        CardView gridItemcardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            gridItemTitle = itemView.findViewById(R.id.gridItemTitle);
            gridItemImage = itemView.findViewById(R.id.gridItemImage);
            getGridItemThreeDots = itemView.findViewById(R.id.configThreeDotsBtn);
            gridItemcardView = itemView.findViewById(R.id.configGridItemCardView);
        }
    }
}

