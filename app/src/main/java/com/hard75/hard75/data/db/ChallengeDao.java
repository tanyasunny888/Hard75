package com.hard75.hard75.data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface ChallengeDao {
    @Insert
    long insert(ChallengeEntity e);

    @Query("SELECT * FROM challenges WHERE status='ACTIVE' ORDER BY id DESC LIMIT 1")
    ChallengeEntity getActive();
}
