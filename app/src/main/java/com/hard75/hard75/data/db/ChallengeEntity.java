package com.hard75.hard75.data.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "challenges")
public class ChallengeEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String level;       // "soft" | "medium" | "hard"
    public int durationDays;   // 21..75
    public long startDate;     // System.currentTimeMillis()
    public String status;      // "ACTIVE" | "PAUSED" | "COMPLETED"

    public ChallengeEntity(String level, int durationDays, long startDate, String status) {
        this.level = level;
        this.durationDays = durationDays;
        this.startDate = startDate;
        this.status = status;
    }
}
