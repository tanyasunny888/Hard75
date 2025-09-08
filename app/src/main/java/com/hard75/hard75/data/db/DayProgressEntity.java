package com.hard75.hard75.data.db;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "day_progress",
        indices = {
                @Index("challengeId"),
                @Index(value = {"challengeId", "dayIndex"}, unique = true)
        })
public class DayProgressEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long challengeId;
    public int dayIndex;
    public boolean completed;

    public DayProgressEntity(long challengeId, int dayIndex, boolean completed) {
        this.challengeId = challengeId;
        this.dayIndex = dayIndex;
        this.completed = completed;
    }
}

