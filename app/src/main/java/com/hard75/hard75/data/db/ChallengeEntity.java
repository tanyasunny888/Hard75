package com.hard75.hard75.data.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "challenges")
public class ChallengeEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String level;
    public int durationDays;
    public long startDate;
    public String status;

    public ChallengeEntity(String level, int durationDays, long startDate, String status) {
        this.level = level;
        this.durationDays = durationDays;
        this.startDate = startDate;
        this.status = status;
    }
}

