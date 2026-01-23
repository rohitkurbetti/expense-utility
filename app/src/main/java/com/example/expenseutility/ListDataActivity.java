package com.example.expenseutility;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expenseutility.dto.MainItem1;
import com.example.expenseutility.dto.SubItem1;
import com.example.expenseutility.entityadapter.MainAdapter1;
import com.example.expenseutility.entityadapter.SubAdapter1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListDataActivity extends AppCompatActivity {

    private RecyclerView leftRecyclerView;
    private RecyclerView rightRecyclerView;
    private ConstraintLayout rightPanel;
    private View divider;
    private TextView subListTitle;
    private ImageButton btnClose;

    private MainAdapter1 mainAdapter;
    private SubAdapter1 subAdapter;

    // Sample data
    private List<MainItem1> mainItems = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_data);

        initializeViews();
        setupRecyclerViews();
        createSampleData();
        setupClickListeners();

    }

    private void initializeViews() {
        leftRecyclerView = findViewById(R.id.leftRecyclerView);
        rightRecyclerView = findViewById(R.id.rightRecyclerView);
        rightPanel = findViewById(R.id.rightPanel);
        divider = findViewById(R.id.divider);
        subListTitle = findViewById(R.id.subListTitle);
        btnClose = findViewById(R.id.btnClose);
    }

    private void setupRecyclerViews() {
        // Setup left RecyclerView
        leftRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mainAdapter = new MainAdapter1(mainItems, new MainAdapter1.OnItemClickListener() {
            @Override
            public void onItemClick(MainItem1 item, int position) {
                showRightPanel(item, position);
            }
        });
        leftRecyclerView.setAdapter(mainAdapter);

        // Setup right RecyclerView
        rightRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        subAdapter = new SubAdapter1(new ArrayList<>());
        rightRecyclerView.setAdapter(subAdapter);
    }

    private void createSampleData() {
        // 1. TRAVEL DESTINATIONS
        List<SubItem1> travelItems = Arrays.asList(
                new SubItem1("Paris, France", "City of Love - Eiffel Tower, Louvre Museum"),
                new SubItem1("Tokyo, Japan", "Cherry Blossoms - Shibuya Crossing, Tokyo Tower"),
                new SubItem1("New York, USA", "The Big Apple - Times Square, Central Park"),
                new SubItem1("Sydney, Australia", "Opera House - Bondi Beach, Harbour Bridge"),
                new SubItem1("Cairo, Egypt", "Ancient Pyramids - Sphinx, Nile River")
        );

        // 2. PROGRAMMING LANGUAGES
        List<SubItem1> programmingItems = Arrays.asList(
                new SubItem1("Java", "Object-oriented, platform-independent"),
                new SubItem1("Python", "High-level, interpreted, great for AI/ML"),
                new SubItem1("JavaScript", "Web development, runs in browsers"),
                new SubItem1("Kotlin", "Modern Android development"),
                new SubItem1("Swift", "iOS and macOS development"),
                new SubItem1("C++", "System programming, game development")
        );

        // 3. MOVIE GENRES
        List<SubItem1> movieItems = Arrays.asList(
                new SubItem1("Action", "Fast-paced, thrilling sequences"),
                new SubItem1("Comedy", "Humorous, light-hearted stories"),
                new SubItem1("Drama", "Serious plot, character development"),
                new SubItem1("Sci-Fi", "Futuristic technology, space exploration"),
                new SubItem1("Horror", "Scary, suspenseful content"),
                new SubItem1("Romance", "Love stories, emotional connections")
        );

        // 4. FITNESS ACTIVITIES
        List<SubItem1> fitnessItems = Arrays.asList(
                new SubItem1("Yoga", "Mind-body practice, flexibility"),
                new SubItem1("Running", "Cardiovascular exercise, endurance"),
                new SubItem1("Weight Training", "Strength building, muscle growth"),
                new SubItem1("Swimming", "Full-body workout, low impact"),
                new SubItem1("Cycling", "Leg strength, outdoor/indoor")
        );

        // 5. MUSICAL INSTRUMENTS
        List<SubItem1> musicItems = Arrays.asList(
                new SubItem1("Guitar", "6 strings, acoustic/electric"),
                new SubItem1("Piano", "88 keys, classical/jazz"),
                new SubItem1("Violin", "4 strings, orchestral"),
                new SubItem1("Drums", "Percussion, rhythm section"),
                new SubItem1("Flute", "Woodwind, melodic")
        );

        // 6. COOKING CUISINES
        List<SubItem1> cookingItems = Arrays.asList(
                new SubItem1("Italian", "Pasta, Pizza, Risotto"),
                new SubItem1("Chinese", "Dumplings, Fried Rice, Noodles"),
                new SubItem1("Mexican", "Tacos, Burritos, Guacamole"),
                new SubItem1("Indian", "Curry, Biryani, Naan"),
                new SubItem1("Japanese", "Sushi, Ramen, Tempura")
        );

        // 7. BOOK CATEGORIES
        List<SubItem1> bookItems = Arrays.asList(
                new SubItem1("Fiction", "Imaginary stories, novels"),
                new SubItem1("Non-Fiction", "Real events, biographies"),
                new SubItem1("Mystery", "Crime, detective stories"),
                new SubItem1("Fantasy", "Magic, supernatural elements"),
                new SubItem1("Science", "Facts, research, discoveries")
        );

        // 8. SPORTS
        List<SubItem1> sportsItems = Arrays.asList(
                new SubItem1("Football", "11 players, goal scoring"),
                new SubItem1("Basketball", "5 players, hoop shooting"),
                new SubItem1("Tennis", "Racket sport, singles/doubles"),
                new SubItem1("Cricket", "Bat and ball, wickets"),
                new SubItem1("Badminton", "Shuttlecock, net game")
        );

        // 9. CAR BRANDS
        List<SubItem1> carItems = Arrays.asList(
                new SubItem1("Toyota", "Japanese, reliable sedans"),
                new SubItem1("BMW", "German, luxury performance"),
                new SubItem1("Tesla", "American, electric vehicles"),
                new SubItem1("Mercedes", "German, premium luxury"),
                new SubItem1("Ford", "American, trucks and SUVs")
        );

        // 10. MOBILE PHONES
        List<SubItem1> phoneItems = Arrays.asList(
                new SubItem1("iPhone 15", "Apple, iOS 17, A16 Bionic"),
                new SubItem1("Samsung S24", "Android, Snapdragon 8 Gen 3"),
                new SubItem1("Google Pixel 8", "Pure Android, Tensor G3"),
                new SubItem1("OnePlus 12", "Fast charging, OxygenOS"),
                new SubItem1("Xiaomi 14", "Value for money, MIUI")
        );

        // 11. SOCIAL MEDIA APPS
        List<SubItem1> socialItems = Arrays.asList(
                new SubItem1("Instagram", "Photo/video sharing"),
                new SubItem1("Twitter/X", "Microblogging, news"),
                new SubItem1("Facebook", "Social networking"),
                new SubItem1("TikTok", "Short video content"),
                new SubItem1("LinkedIn", "Professional networking")
        );

        // 12. VIDEO GAMES
        List<SubItem1> gameItems = Arrays.asList(
                new SubItem1("The Legend of Zelda", "Adventure, puzzle solving"),
                new SubItem1("Grand Theft Auto", "Open world, action"),
                new SubItem1("Minecraft", "Sandbox, creativity"),
                new SubItem1("Fortnite", "Battle royale, building"),
                new SubItem1("Call of Duty", "First-person shooter")
        );

        // Create main items with new data
        mainItems.add(new MainItem1("🌍 Travel Destinations", travelItems));
        mainItems.add(new MainItem1("💻 Programming Languages", programmingItems));
        mainItems.add(new MainItem1("🎬 Movie Genres", movieItems));
        mainItems.add(new MainItem1("💪 Fitness Activities", fitnessItems));
        mainItems.add(new MainItem1("🎵 Musical Instruments", musicItems));
        mainItems.add(new MainItem1("🍳 Cooking Cuisines", cookingItems));
        mainItems.add(new MainItem1("📚 Book Categories", bookItems));
        mainItems.add(new MainItem1("⚽ Sports", sportsItems));
        mainItems.add(new MainItem1("🚗 Car Brands", carItems));
        mainItems.add(new MainItem1("📱 Mobile Phones", phoneItems));
        mainItems.add(new MainItem1("📱 Social Media Apps", socialItems));
        mainItems.add(new MainItem1("🎮 Video Games", gameItems));

        mainAdapter.notifyDataSetChanged();
    }


    private void setupClickListeners() {
        btnClose.setOnClickListener(v -> hideRightPanel());
    }


    private void showRightPanel(MainItem1 item, int position) {
        // Update selected position in adapter
        mainAdapter.setSelectedPosition(position);

        // Update right panel content
        subListTitle.setText(item.getTitle());
        subAdapter.updateList(item.getSubItems());

        // Show the panel with animation if it's hidden
        if (rightPanel.getVisibility() == View.GONE) {
            animatePanelShow();
        }
    }

    private void animatePanelShow() {
        // Make panel visible
        rightPanel.setVisibility(View.VISIBLE);
        divider.setVisibility(View.VISIBLE);

        // Get the current width of left RecyclerView
        ViewGroup.LayoutParams leftParams = leftRecyclerView.getLayoutParams();
        ViewGroup.LayoutParams rightParams = rightPanel.getLayoutParams();

        // Animate the constraint width changes
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(300);
        animator.setInterpolator(new DecelerateInterpolator());

        animator.addUpdateListener(animation -> {
            float fraction = animation.getAnimatedFraction();

            // Calculate new widths
            if (leftParams instanceof ConstraintLayout.LayoutParams &&
                    rightParams instanceof ConstraintLayout.LayoutParams) {

                ConstraintLayout.LayoutParams leftLayoutParams = (ConstraintLayout.LayoutParams) leftParams;
                ConstraintLayout.LayoutParams rightLayoutParams = (ConstraintLayout.LayoutParams) rightParams;

                // Left panel goes from 100% to 40%
                leftLayoutParams.matchConstraintPercentWidth = 1f - (0.6f * fraction);

                // Right panel goes from 0% to 60%
                rightLayoutParams.matchConstraintPercentWidth = 0.6f * fraction;

                leftRecyclerView.setLayoutParams(leftLayoutParams);
                rightPanel.setLayoutParams(rightLayoutParams);
            }
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Animation completed
            }
        });

        animator.start();
    }

    private void hideRightPanel() {
        // Clear selection
        mainAdapter.setSelectedPosition(-1);

        // Get the current layout params
        ViewGroup.LayoutParams leftParams = leftRecyclerView.getLayoutParams();
        ViewGroup.LayoutParams rightParams = rightPanel.getLayoutParams();

        // Animate the constraint width changes
        ValueAnimator animator = ValueAnimator.ofFloat(1f, 0f);
        animator.setDuration(300);
        animator.setInterpolator(new DecelerateInterpolator());

        animator.addUpdateListener(animation -> {
            float fraction = animation.getAnimatedFraction();

            if (leftParams instanceof ConstraintLayout.LayoutParams &&
                    rightParams instanceof ConstraintLayout.LayoutParams) {

                ConstraintLayout.LayoutParams leftLayoutParams = (ConstraintLayout.LayoutParams) leftParams;
                ConstraintLayout.LayoutParams rightLayoutParams = (ConstraintLayout.LayoutParams) rightParams;

                // Left panel goes from 40% back to 100%
                leftLayoutParams.matchConstraintPercentWidth = 0.4f + (0.6f * fraction);

                // Right panel goes from 60% back to 0%
                rightLayoutParams.matchConstraintPercentWidth = 0.6f - (0.6f * fraction);

                leftRecyclerView.setLayoutParams(leftLayoutParams);
                rightPanel.setLayoutParams(rightLayoutParams);
            }
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Hide panel after animation
                rightPanel.setVisibility(View.GONE);
                divider.setVisibility(View.GONE);

                // Reset constraints to initial state
                ConstraintLayout.LayoutParams leftLayoutParams =
                        (ConstraintLayout.LayoutParams) leftRecyclerView.getLayoutParams();
                ConstraintLayout.LayoutParams rightLayoutParams =
                        (ConstraintLayout.LayoutParams) rightPanel.getLayoutParams();

                leftLayoutParams.matchConstraintPercentWidth = 1f;
                rightLayoutParams.matchConstraintPercentWidth = 0f;

                leftRecyclerView.setLayoutParams(leftLayoutParams);
                rightPanel.setLayoutParams(rightLayoutParams);
            }
        });

        animator.start();
    }

    @Override
    public void onBackPressed() {
        if (rightPanel.getVisibility() == View.VISIBLE) {
            hideRightPanel();
        } else {
            super.onBackPressed();
        }
    }

}