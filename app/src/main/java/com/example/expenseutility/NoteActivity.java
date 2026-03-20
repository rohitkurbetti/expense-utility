package com.example.expenseutility;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expenseutility.dto.Note;
import com.example.expenseutility.dto.NoteDAO;
import com.example.expenseutility.entityadapter.NotesAdapter;
import com.example.expenseutility.utility.ThemeHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class NoteActivity extends AppCompatActivity {

    private RecyclerView notesRecyclerView;
    private NotesAdapter notesAdapter;
    private List<Note> notesList;
    private NoteDAO noteDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize database
        noteDAO = new NoteDAO(this);
        noteDAO.open();

        // Initialize views
        notesRecyclerView = findViewById(R.id.notes_recycler_view);
        FloatingActionButton fabAddNote = findViewById(R.id.fab_add_note);

        // Setup RecyclerView
        int spanCount = getResources().getConfiguration().orientation ==
                android.content.res.Configuration.ORIENTATION_LANDSCAPE ? 3 : 2;
        notesRecyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));

        // Load notes
        notesList = new ArrayList<>();
        notesAdapter = new NotesAdapter(this, notesList);
        notesRecyclerView.setAdapter(notesAdapter);

        // Set item click listener
        notesAdapter.setOnItemClickListener(new NotesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Note note = notesList.get(position);
                Intent intent = new Intent(NoteActivity.this, NoteEditorActivity.class);
                intent.putExtra("note_id", note.getId());
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(int position) {
                // Handle long click for selection/multi-select
            }
        });

        // FAB click listener
        fabAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NoteActivity.this, NoteEditorActivity.class);
                startActivity(intent);
            }
        });

        loadNotes();
    }

    private void loadNotes() {
        notesList.clear();
        notesList.addAll(noteDAO.getAllNotes());
        notesAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();
    }

    @Override
    protected void onDestroy() {
        noteDAO.close();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu1, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            // Handle search
            return true;
        } else if (id == R.id.action_settings) {
            // Handle settings
            return true;
        } else if (id == R.id.action_view_type) {
            // Toggle view type
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}