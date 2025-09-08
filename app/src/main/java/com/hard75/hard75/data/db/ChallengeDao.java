package com.hard75.hard75.data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface ChallengeDao {

    @Insert
    long insert(ChallengeEntity entity);

    @Query("SELECT * FROM challenges WHERE status='ACTIVE' LIMIT 1")
    ChallengeEntity getActive();

    @Query("SELECT * FROM challenges WHERE id=:id LIMIT 1")
    ChallengeEntity getById(long id);

    @Query("UPDATE challenges SET status=:status WHERE id=:id")
    void updateStatus(long id, String status);

    @Query("SELECT COUNT(*) FROM challenges")
    int countAll();
}

