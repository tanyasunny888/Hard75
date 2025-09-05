package com.hard75.hard75.data.db;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "day_tasks",
        indices = {@Index("challengeId"), @Index("dayIndex")}
)
public class DayTaskEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long challengeId;
    public int dayIndex;       // 1..N
    public String title;
    public boolean isDone;
    public boolean isUserCreated;
    public int sortOrder;      // для reorder

    public DayTaskEntity(long challengeId, int dayIndex, String title,
                         boolean isDone, boolean isUserCreated, int sortOrder) {
        this.challengeId = challengeId;
        this.dayIndex = dayIndex;
        this.title = title;
        this.isDone = isDone;
        this.isUserCreated = isUserCreated;
        this.sortOrder = sortOrder;
    }
}
