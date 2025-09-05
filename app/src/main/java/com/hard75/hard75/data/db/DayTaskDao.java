package com.hard75.hard75.data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DayTaskDao {
    @Insert
    void insertAll(List<DayTaskEntity> tasks);

    @Query("SELECT * FROM day_tasks WHERE challengeId=:cid AND dayIndex=:day ORDER BY sortOrder ASC, id ASC")
    List<DayTaskEntity> getByDay(long cid, int day);
}
