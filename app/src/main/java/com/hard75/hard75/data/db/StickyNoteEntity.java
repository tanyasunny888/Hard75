package com.hard75.hard75.data.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sticky_notes")
public class StickyNoteEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String title;
    public String body;
    public int colorRes;     // R.color.sticker_yellow и т.п.
    public long createdAt;   // System.currentTimeMillis()

    public StickyNoteEntity(String title, String body, int colorRes, long createdAt) {
        this.title = title;
        this.body = body;
        this.colorRes = colorRes;
        this.createdAt = createdAt;
    }
}
