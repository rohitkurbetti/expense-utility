package com.example.expenseutility;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.expenseutility.database.DatabaseHelper;
import com.example.expenseutility.entityadapter.Suggestion;
import com.example.expenseutility.entityadapter.SuggestionAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class SuggestionActivity extends AppCompatActivity {

    private ListView listView;
    private DatabaseHelper db;
    private Button btnAddSugg,delSelectedBtn;
    private EditText etAddSugg;
    public static List<Suggestion> suggestionsList;
    private SuggestionAdapter adapter;
    private SharedPreferences sharedPreferences;

    private DatabaseReference database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestion);

        // Find the toolbar and set it as the ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Optional: Set up other toolbar settings
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Suggestion Configurations");  // Set toolbar title
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);  // Show the Up button

        }

        database = FirebaseDatabase.getInstance().getReference();


        listView = findViewById(R.id.suggestionsListView);
        etAddSugg = findViewById(R.id.etAddSuggestion);
        btnAddSugg = findViewById(R.id.btnAddSuggestion);
        delSelectedBtn = findViewById(R.id.delSelectedBtn);
        db = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);



        // Retrieve data from database and display it in ListView
        suggestionsList = db.getAllSuggestions();

        if(suggestionsList.isEmpty()) {
            Set<String> suggList = sharedPreferences.getStringSet("partSuggestionsList", new HashSet<>());

            suggList.forEach(s -> {
                db.addSuggestion(s);
            });
            suggestionsList = db.getAllSuggestions();
        }

        adapter = new SuggestionAdapter(this, suggestionsList);
        listView.setAdapter(adapter);



        btnAddSugg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String addSuggestionVal = etAddSugg.getText().toString();
                if(addSuggestionVal !=null && !addSuggestionVal.isEmpty() && addSuggestionVal != "" && addSuggestionVal.length() > 0) {
                    db.addSuggestion(addSuggestionVal);
                    suggestionsList = db.getAllSuggestions();
                    adapter.refreshList(adapter,suggestionsList);
                    etAddSugg.setText("");
                    FirstFragment.refreshSuggList();
                }
                listView.post(() -> listView.setSelection(adapter.getCount() - 1));
            }
        });







    }

}