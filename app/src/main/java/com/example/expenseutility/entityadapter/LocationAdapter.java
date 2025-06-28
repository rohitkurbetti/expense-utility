package com.example.expenseutility.entityadapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.view.menu.MenuView;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.example.expenseutility.LocationDetailsActivity;
import com.example.expenseutility.R;

import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {

    private Context context;
    private List<Location> locationList;

    public LocationAdapter(Context context, List<Location> locationList) {
        this.context = context;
        this.locationList = locationList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleText, subtitleText;
        FrameLayout cardRoot;

        public ViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.imageView);
            titleText = view.findViewById(R.id.titleText);
            subtitleText = view.findViewById(R.id.subtitleText);
            cardRoot = view.findViewById(R.id.cardRoot);
        }
    }

    @Override
    public LocationAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_location, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LocationAdapter.ViewHolder holder, int position) {
        Location loc = locationList.get(position);
        holder.titleText.setText(loc.getCountry());
        holder.subtitleText.setText(loc.getCity());
        Glide.with(context).load(loc.getImageUrl()).into(holder.imageView);


        holder.cardRoot.setOnClickListener(v -> {

            v.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(50)
                    .withEndAction(() -> {
                        v.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(50)
                                .start();
                    }).start();


            Intent intent = new Intent(context, LocationDetailsActivity.class);
            intent.putExtra("country", loc.getCountry());
            intent.putExtra("city", loc.getCity());
            intent.putExtra("description", loc.getDescription());
            intent.putExtra("imageUrl", loc.getImageUrl());
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return locationList.size();
    }
}
