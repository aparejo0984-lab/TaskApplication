package com.example.taskapp.model;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface NoteDao {
    @Query("SELECT * FROM note_table ORDER BY id DESC")
    List<Note> getAllNotes();

    @Query("SELECT * FROM note_table ORDER BY id ASC")
    List<Note> getAllNotesASC();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNote(Note note);

    @Delete
    void deleteNote(Note note);
}
