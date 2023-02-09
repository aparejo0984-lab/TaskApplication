package com.example.taskapp.helper;

import com.example.taskapp.model.Note;

public interface NoteListener {
    void onNoteClicked(Note note, int position);
}
