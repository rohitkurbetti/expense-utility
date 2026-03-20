package com.example.expenseutility;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.expenseutility.dto.Note;
import com.example.expenseutility.dto.NoteContent;
import com.example.expenseutility.dto.NoteDAO;
import com.example.expenseutility.dto.NoteType;
import com.example.expenseutility.utility.DragLayout;
import com.example.expenseutility.utility.ThemeHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NoteEditorActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_FILE_REQUEST = 2;
    private static final int CAPTURE_IMAGE_REQUEST = 3;

    private DragLayout dragLayout;
    private List<NoteContent> canvasItems;
    private NoteDAO noteDAO;
    private Note currentNote;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_editor);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize database
        noteDAO = new NoteDAO(this);
        noteDAO.open();

        // Initialize views
        dragLayout = findViewById(R.id.drag_layout);
        emptyView = findViewById(R.id.empty_view);
        FloatingActionButton fabAddContent = findViewById(R.id.fab_add_content);
        FloatingActionButton fabSave = findViewById(R.id.fab_save);

        // Initialize canvas items
        canvasItems = new ArrayList<>();

        // Set drag listener to save positions
        dragLayout.setOnDragListener(new DragLayout.OnDragListener() {
            @Override
            public void onViewDragged(View view, int x, int y) {
                // Find the corresponding NoteContent and update its position
                Object tag = view.getTag();
                if (tag != null && tag instanceof Integer) {
                    int position = (int) tag;
                    if (position >= 0 && position < canvasItems.size()) {
                        NoteContent content = canvasItems.get(position);
                        content.setPositionX(x);
                        content.setPositionY(y);
                    }
                }
            }
        });

        // Load existing note if editing
        int noteId = getIntent().getIntExtra("note_id", -1);
        if (noteId != -1) {
            loadNote(noteId);
        } else {
            currentNote = new Note();
            currentNote.setTitle("Untitled Note");
        }

        // FAB click listeners
        fabAddContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddContentDialog();
            }
        });

        fabSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
            }
        });

        // Back button in toolbar
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        updateEmptyView();
    }

    private void loadNote(int noteId) {
        // For simplicity, we'll load from database
        List<Note> allNotes = noteDAO.getAllNotes();
        for (Note note : allNotes) {
            if (note.getId() == noteId) {
                currentNote = note;
                break;
            }
        }

        if (currentNote != null) {
            // Set toolbar title
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(currentNote.getTitle());
            }

            // Clear existing views
            dragLayout.removeAllViews();
            canvasItems.clear();

            // Load note contents
            if (currentNote.getContents() != null) {
                canvasItems.addAll(currentNote.getContents());

                // Add all items to drag layout
                for (int i = 0; i < canvasItems.size(); i++) {
                    addContentToCanvas(canvasItems.get(i), i);
                }
            }

            updateEmptyView();
        }
    }

    private void showAddContentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Content");

        String[] contentTypes = {"Text", "Image", "Link", "Document", "Drawing", "Checklist"};
        builder.setItems(contentTypes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: // Text
                        addTextContent();
                        break;
                    case 1: // Image
                        addImageContent();
                        break;
                    case 2: // Link
                        addLinkContent();
                        break;
                    case 3: // Document
                        addDocumentContent();
                        break;
                    case 4: // Drawing
                        addDrawingContent();
                        break;
                    case 5: // Checklist
                        addChecklistContent();
                        break;
                }
            }
        });

        builder.show();
    }

    private void addTextContent() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Text");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setMinLines(3);
        input.setHint("Enter your text here...");
        builder.setView(input);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String text = input.getText().toString().trim();
                if (!text.isEmpty()) {
                    NoteContent content = new NoteContent();
                    content.setType(NoteType.TEXT);
                    content.setContent(text);
                    content.setOrder(canvasItems.size());

                    // Random position
                    int x = 50 + (canvasItems.size() * 10);
                    int y = 50 + (canvasItems.size() * 10);
                    content.setPositionX(x);
                    content.setPositionY(y);

                    // Add to list and canvas
                    canvasItems.add(content);
                    addContentToCanvas(content, canvasItems.size() - 1);
                    updateEmptyView();
                    Toast.makeText(NoteEditorActivity.this, "Text added", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void addImageContent() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Image");

        String[] options = {"Take Photo", "Choose from Gallery"};
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    // Take photo
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(takePictureIntent, CAPTURE_IMAGE_REQUEST);
                    }
                } else {
                    // Choose from gallery
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(intent, PICK_IMAGE_REQUEST);
                }
            }
        });

        builder.show();
    }

    private void addLinkContent() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Link");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        input.setHint("https://example.com");
        builder.setView(input);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String url = input.getText().toString().trim();
                if (!url.isEmpty()) {
                    // Add http:// if not present
                    if (!url.startsWith("http://") && !url.startsWith("https://")) {
                        url = "https://" + url;
                    }

                    NoteContent content = new NoteContent();
                    content.setType(NoteType.LINK);
                    content.setContent(url);
                    content.setOrder(canvasItems.size());

                    // Random position
                    int x = 50 + (canvasItems.size() * 10);
                    int y = 50 + (canvasItems.size() * 10);
                    content.setPositionX(x);
                    content.setPositionY(y);

                    // Add to list and canvas
                    canvasItems.add(content);
                    addContentToCanvas(content, canvasItems.size() - 1);
                    updateEmptyView();
                    Toast.makeText(NoteEditorActivity.this, "Link added", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void addDocumentContent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a file"),
                    PICK_FILE_REQUEST);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Please install a file manager", Toast.LENGTH_SHORT).show();
        }
    }

    private void addDrawingContent() {
        NoteContent content = new NoteContent();
        content.setType(NoteType.DRAWING);
        content.setContent("Drawing");
        content.setOrder(canvasItems.size());

        // Random position
        int x = 50 + (canvasItems.size() * 10);
        int y = 50 + (canvasItems.size() * 10);
        content.setPositionX(x);
        content.setPositionY(y);

        // Add to list and canvas
        canvasItems.add(content);
        addContentToCanvas(content, canvasItems.size() - 1);
        updateEmptyView();
        Toast.makeText(this, "Drawing added", Toast.LENGTH_SHORT).show();
    }

    private void addChecklistContent() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Checklist Item");

        final EditText input = new EditText(this);
        input.setHint("Enter checklist item");
        builder.setView(input);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String item = input.getText().toString().trim();
                if (!item.isEmpty()) {
                    NoteContent content = new NoteContent();
                    content.setType(NoteType.LIST);
                    content.setContent("[ ] " + item);
                    content.setOrder(canvasItems.size());

                    // Random position
                    int x = 50 + (canvasItems.size() * 10);
                    int y = 50 + (canvasItems.size() * 10);
                    content.setPositionX(x);
                    content.setPositionY(y);

                    // Add to list and canvas
                    canvasItems.add(content);
                    addContentToCanvas(content, canvasItems.size() - 1);
                    updateEmptyView();
                    Toast.makeText(NoteEditorActivity.this, "Checklist added", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void addContentToCanvas(NoteContent content, int position) {
        View contentView = createContentView(content, position);

        if (contentView != null) {
            // Get position from content or use default
            int x = content.getPositionX();
            int y = content.getPositionY();

            if (x == 0 && y == 0) {
                // Default position
                x = 50 + (position * 20);
                y = 50 + (position * 20);
            }

            // Add to drag layout
            dragLayout.addDraggableChild(contentView, x, y);

            // Tag the view with its position
            contentView.setTag(position);

            // Set up delete button
            ImageButton btnDelete = contentView.findViewById(R.id.btn_delete);
            if (btnDelete != null) {
                btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removeContent(position);
                    }
                });
            }

            // Set up edit button if exists
            ImageButton btnEdit = contentView.findViewById(R.id.btn_edit);
            if (btnEdit != null) {
                btnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (content.getType() == NoteType.TEXT) {
                            editTextContent(content, position);
                        }
                    }
                });
            }
        }
    }

    private View createContentView(NoteContent content, int position) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View contentView = null;

        switch (content.getType()) {
            case TEXT:
                contentView = inflater.inflate(R.layout.layout_draggable_text, null);
                TextView textView = contentView.findViewById(R.id.text_content);
                if (textView != null && content.getContent() != null) {
                    textView.setText(content.getContent());
                }
                break;

            case IMAGE:
                contentView = inflater.inflate(R.layout.layout_draggable_image, null);
                ImageView imageView = contentView.findViewById(R.id.image_content);
                if (imageView != null && content.getFilePath() != null) {
                    try {
                        Glide.with(this)
                                .load(Uri.parse(content.getFilePath()))
                                .placeholder(android.R.drawable.ic_menu_camera)
                                .into(imageView);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;

            case LINK:
                contentView = inflater.inflate(R.layout.layout_draggable_link, null);
                TextView linkView = contentView.findViewById(R.id.link_text);
                if (linkView != null && content.getContent() != null) {
                    linkView.setText(content.getContent());
                    // Make clickable
                    contentView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(content.getContent()));
                                startActivity(intent);
                            } catch (Exception e) {
                                Toast.makeText(NoteEditorActivity.this, "Invalid URL", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                break;

            case DOCUMENT:
                contentView = inflater.inflate(R.layout.layout_draggable_document, null);
                TextView docView = contentView.findViewById(R.id.document_name);
                if (docView != null && content.getContent() != null) {
                    docView.setText(content.getContent());
                    // Make clickable
                    contentView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (content.getFilePath() != null) {
                                try {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setDataAndType(Uri.parse(content.getFilePath()), "application/*");
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    startActivity(intent);
                                } catch (Exception e) {
                                    Toast.makeText(NoteEditorActivity.this, "Cannot open file", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }
                break;

            case LIST:
                contentView = inflater.inflate(R.layout.layout_draggable_checklist, null);
                TextView checklistView = contentView.findViewById(R.id.checklist_text);
                if (checklistView != null && content.getContent() != null) {
                    checklistView.setText(content.getContent());
                }
                break;

            case DRAWING:
                contentView = inflater.inflate(R.layout.layout_draggable_drawing, null);
                TextView drawingView = contentView.findViewById(R.id.drawing_text);
                if (drawingView != null && content.getContent() != null) {
                    drawingView.setText(content.getContent());
                }
                break;
        }

        return contentView;
    }

    private void editTextContent(final NoteContent content, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Text");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setMinLines(3);
        input.setText(content.getContent());
        builder.setView(input);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newText = input.getText().toString().trim();
                if (!newText.isEmpty()) {
                    content.setContent(newText);

                    // Update the view
                    View oldView = dragLayout.findViewWithTag(position);
                    if (oldView != null) {
                        dragLayout.removeView(oldView);
                    }
                    addContentToCanvas(content, position);

                    Toast.makeText(NoteEditorActivity.this, "Text updated", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void removeContent(int position) {
        if (position >= 0 && position < canvasItems.size()) {
            // Remove from list
            canvasItems.remove(position);

            // Remove from layout
            dragLayout.removeView(dragLayout.findViewWithTag(position));

            // Update tags for remaining views
            for (int i = position; i < canvasItems.size(); i++) {
                View view = dragLayout.findViewWithTag(i + 1);
                if (view != null) {
                    view.setTag(i);
                }
            }

            updateEmptyView();
            Toast.makeText(this, "Content removed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST || requestCode == CAPTURE_IMAGE_REQUEST) {
                Uri imageUri = null;

                if (requestCode == CAPTURE_IMAGE_REQUEST) {
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    // Save bitmap to file
                    imageUri = saveBitmapToFile(imageBitmap);
                } else {
                    imageUri = data.getData();
                }

                if (imageUri != null) {
                    NoteContent content = new NoteContent();
                    content.setType(NoteType.IMAGE);
                    content.setContent("Image");
                    content.setFilePath(imageUri.toString());
                    content.setOrder(canvasItems.size());

                    // Random position
                    int x = 50 + (canvasItems.size() * 10);
                    int y = 50 + (canvasItems.size() * 10);
                    content.setPositionX(x);
                    content.setPositionY(y);

                    // Add to list and canvas
                    canvasItems.add(content);
                    addContentToCanvas(content, canvasItems.size() - 1);
                    updateEmptyView();
                    Toast.makeText(this, "Image added", Toast.LENGTH_SHORT).show();
                }

            } else if (requestCode == PICK_FILE_REQUEST) {
                Uri fileUri = data.getData();
                if (fileUri != null) {
                    String fileName = getFileName(fileUri);

                    NoteContent content = new NoteContent();
                    content.setType(NoteType.DOCUMENT);
                    content.setContent(fileName);
                    content.setFilePath(fileUri.toString());
                    content.setOrder(canvasItems.size());

                    // Random position
                    int x = 50 + (canvasItems.size() * 10);
                    int y = 50 + (canvasItems.size() * 10);
                    content.setPositionX(x);
                    content.setPositionY(y);

                    // Add to list and canvas
                    canvasItems.add(content);
                    addContentToCanvas(content, canvasItems.size() - 1);
                    updateEmptyView();
                    Toast.makeText(this, "Document added: " + fileName, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private Uri saveBitmapToFile(Bitmap bitmap) {
        try {
            File imagesFolder = new File(getFilesDir(), "images");
            if (!imagesFolder.exists()) {
                imagesFolder.mkdirs();
            }

            String fileName = "img_" + System.currentTimeMillis() + ".jpg";
            File imageFile = new File(imagesFolder, fileName);

            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();

            return Uri.fromFile(imageFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(android.provider.OpenableColumns.DISPLAY_NAME));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }

        return result;
    }

    private void saveNote() {
        // Update note contents
        currentNote.setContents(canvasItems);
        currentNote.setModifiedDate(new Date());

        // Save to database
        if (currentNote.getId() == 0) {
            // New note
            long id = noteDAO.insertNote(currentNote);
            if (id != -1) {
                currentNote.setId((int) id);
                Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to save note", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            // Update existing note
            int rowsAffected = noteDAO.updateNote(currentNote);
            if (rowsAffected > 0) {
                Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to update note", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Go back to main activity
        finish();
    }

    private void updateEmptyView() {
        if (canvasItems == null || canvasItems.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        // Ask for confirmation if there are unsaved changes
        if (!canvasItems.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Save Note")
                    .setMessage("Do you want to save changes before exiting?")
                    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            saveNote();
                        }
                    })
                    .setNegativeButton("Discard", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNeutralButton("Cancel", null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (noteDAO != null) {
            noteDAO.close();
        }
        super.onDestroy();
    }
}