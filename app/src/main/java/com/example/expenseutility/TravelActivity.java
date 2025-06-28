package com.example.expenseutility;

import android.os.Bundle;
import android.os.Environment;
import android.widget.GridLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expenseutility.databinding.ActivityMainBinding;
import com.example.expenseutility.databinding.ActivityTravelBinding;
import com.example.expenseutility.entityadapter.Location;
import com.example.expenseutility.entityadapter.LocationAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TravelActivity extends AppCompatActivity {

    ActivityTravelBinding binding;
    RecyclerView recyclerView;
    List<Location> locations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTravelBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());








        recyclerView = findViewById(R.id.recyclerView);
        locations = new ArrayList<>();


        try {
            readLocationsFromCsv(locations);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


//        locations.add(new Location("India", "Kashmir", "https://upload.wikimedia.org/wikipedia/commons/e/e0/A_view_of_Pari_Mahal_Jammu_and_Kashmir_India.jpg"));
//        locations.add(new Location("Turkey", "Istanbul", "https://www.atlys.com/_next/image?url=https%3A%2F%2Fimagedelivery.net%2FW3Iz4WACAy2J0qT0cCT3xA%2Fdidi%2Farticles%2Fgmnvl5g7bl0t7cc0haybuxag%2Fpublic&w=750&q=75"));
//        locations.add(new Location("France", "Paris", "https://upload.wikimedia.org/wikipedia/commons/a/a8/Paris_Ma_ville_Tour_Eiffel_%2823212002682%29.jpg"));
//        locations.add(new Location("Indonesia", "Bali", "https://upload.wikimedia.org/wikipedia/commons/9/9e/Bali%2C_Indonesia_%2850368691146%29.jpg"));
//        locations.add(new Location("Japan", "Kyoto", "https://trawelmart.com/_next/image?url=https%3A%2F%2Ftrawel.blob.core.windows.net%2Ftrawel%2Fcontent_files%2Fjapan%2F67f3bd0c-efb.jpg&w=1920&q=75"));



        LocationAdapter adapter = new LocationAdapter(this, locations);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);






    }

    private void readLocationsFromCsv(List<Location> locations) throws IOException {

        File destCsv = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "destinations.csv");
        BufferedReader reader = new BufferedReader(new FileReader(destCsv));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split(",", -1); // allows empty fields
            if (!tokens[0].equals("Country")) {
                String country = tokens[0];
                String city = tokens[1];
                String description = tokens[2];
                String imageUrl = tokens[3];
                locations.add(new Location(country, city, description, imageUrl));
            }
        }


    }
}