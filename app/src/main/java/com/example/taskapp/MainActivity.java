package com.example.taskapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.taskapp.database.NoteDatabase;
import com.example.taskapp.helper.NoteAdapter;
import com.example.taskapp.helper.NoteListener;
import com.example.taskapp.model.Note;
import com.example.taskapp.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements NoteListener {

    private RecyclerView recyclerView;
    private List<Note> noteList;
    private NoteAdapter noteAdapter;

    private int noteClickedPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView imageAddNoteMain = findViewById(R.id.imageAddNoteMain);
        imageAddNoteMain.setOnClickListener(v -> startActivityForResult(
                new Intent(getApplicationContext(), CreateNoteActivity.class), Constants.REQUEST_CODE_ADD_NOTE
        ));

        recyclerView = findViewById(R.id.notesRecyclerView);
        recyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        );

        noteList = new ArrayList<>();
        noteAdapter = new NoteAdapter(noteList, this);
        recyclerView.setAdapter(noteAdapter);

        getNotes(Constants.REQUEST_CODE_SHOW_NOTES, false);

        EditText inputSearch = findViewById(R.id.inputSearch);
        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                noteAdapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(noteList.size() != 0) {
                    noteAdapter.searchNote(s.toString());
                }
            }
        });


        findViewById(R.id.imageSortUP).setOnClickListener(v -> {
            recyclerView = findViewById(R.id.notesRecyclerView);
            recyclerView.setLayoutManager(
                    new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            );

            noteList = new ArrayList<>();
            noteAdapter = new NoteAdapter(noteList, this);
            recyclerView.setAdapter(noteAdapter);

            getNotes(Constants.REQUEST_CODE_SHOW_NOTES, false);
        });

        findViewById(R.id.imageSortDown).setOnClickListener(v -> {
            recyclerView = findViewById(R.id.notesRecyclerView);
            recyclerView.setLayoutManager(
                    new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            );

            noteList = new ArrayList<>();
            noteAdapter = new NoteAdapter(noteList, this);
            recyclerView.setAdapter(noteAdapter);

            getNotesASC(Constants.REQUEST_CODE_SHOW_NOTES, false);
        });
    }

    private void getNotes(final int requestNote, final boolean isNoteDeleted) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executorService.execute(() -> {
            List<Note> allNotes = NoteDatabase.getNotesDatabase(getApplicationContext())
                    .noteDao().getAllNotes();

            handler.post(() -> {
                if(requestNote == Constants.REQUEST_CODE_SHOW_NOTES) {
                    noteList.addAll(allNotes);
                    noteAdapter.notifyDataSetChanged();
                } else if(requestNote == Constants.REQUEST_CODE_ADD_NOTE) {
                    noteList.add(0, allNotes.get(0));
                    noteAdapter.notifyItemInserted(0);
                    recyclerView.smoothScrollToPosition(0);
                } else if(requestNote == Constants.REQUEST_CODE_UPDATE_NOTE) {
                    noteList.remove(noteClickedPosition);
                    if(isNoteDeleted){
                        noteAdapter.notifyItemRemoved(noteClickedPosition);
                    } else {
                        noteList.add(noteClickedPosition, allNotes.get(noteClickedPosition));
                        noteAdapter.notifyItemChanged(noteClickedPosition);
                    }
                }
            });
        });
    }

    private void getNotesASC(final int requestNote, final boolean isNoteDeleted) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executorService.execute(() -> {
            List<Note> allNotes = NoteDatabase.getNotesDatabase(getApplicationContext())
                    .noteDao().getAllNotesASC();

            handler.post(() -> {
                if(requestNote == Constants.REQUEST_CODE_SHOW_NOTES) {
                    noteList.addAll(allNotes);
                    noteAdapter.notifyDataSetChanged();
                } else if(requestNote == Constants.REQUEST_CODE_ADD_NOTE) {
                    noteList.add(0, allNotes.get(0));
                    noteAdapter.notifyItemInserted(0);
                    recyclerView.smoothScrollToPosition(0);
                } else if(requestNote == Constants.REQUEST_CODE_UPDATE_NOTE) {
                    noteList.remove(noteClickedPosition);
                    if(isNoteDeleted){
                        noteAdapter.notifyItemRemoved(noteClickedPosition);
                    } else {
                        noteList.add(noteClickedPosition, allNotes.get(noteClickedPosition));
                        noteAdapter.notifyItemChanged(noteClickedPosition);
                    }
                }
            });
        });
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if(intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, Constants.REQUEST_CODE_SELECT_IMAGE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Constants.REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK) {
            getNotes(Constants.REQUEST_CODE_ADD_NOTE, false);
        } else if( requestCode == Constants.REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK) {
            if(data != null) {
                getNotes(Constants.REQUEST_CODE_UPDATE_NOTE, data.getBooleanExtra("isNoteDeleted", false));
            }
        } else if( requestCode == Constants.REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if(data != null) {
                Uri selectedImageUri = data.getData();
                if(selectedImageUri != null) {
                    try {
                        String selectedImagePath = getPathFromUri(selectedImageUri);
                        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                        intent.putExtra("isFromQuickActions", true);
                        intent.putExtra("quickActionType", "image");
                        intent.putExtra("imagePath", selectedImagePath);
                        startActivityForResult(intent, Constants.REQUEST_CODE_ADD_NOTE);
                    }catch(Exception e) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private String getPathFromUri(Uri contentUri) {
        String filePath;
        Cursor cursor = getContentResolver().query(contentUri, null,null,null,null);
        if(cursor == null) {
            filePath = contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }

    @Override
    public void onNoteClicked(Note note, int position) {
        noteClickedPosition = position;
        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
        intent.putExtra("isViewOrUpdate", true);
        intent.putExtra("note",note);
        startActivityForResult(intent, Constants.REQUEST_CODE_UPDATE_NOTE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == Constants.REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else {
                Toast.makeText(this,"Permission Denied",Toast.LENGTH_SHORT).show();
            }
        }
    }
}