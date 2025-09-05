package com.hard75.hard75.data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DayProgressDao {
    @Insert
    void insertAll(List<DayProgressEntity> days);

    @Query("SELECT * FROM day_progress WHERE challengeId=:cid ORDER BY dayIndex ASC")
    List<DayProgressEntity> getBoard(long cid);

    @Query("UPDATE day_progress SET completed=1 WHERE challengeId=:cid AND dayIndex=:day")
    void markCompleted(long cid, int day);
}
