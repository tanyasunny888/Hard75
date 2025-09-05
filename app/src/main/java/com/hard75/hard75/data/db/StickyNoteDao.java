package com.hard75.hard75.data.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface StickyNoteDao {
    @Query("SELECT * FROM sticky_notes ORDER BY createdAt DESC")
    List<StickyNoteEntity> getAllDesc();

    @Insert
    long insert(StickyNoteEntity e);

    @Delete
    void delete(StickyNoteEntity e);
}

