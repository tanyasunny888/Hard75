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

    @Query("UPDATE day_tasks SET isDone=:isDone WHERE id=:taskId")
    void setDone(long taskId, boolean isDone);

    // агрегаты для процентов
    @Query("SELECT dayIndex AS dayIndex, " +
            "SUM(CASE WHEN isDone THEN 1 ELSE 0 END) AS done, " +
            "COUNT(*) AS total " +
            "FROM day_tasks " +
            "WHERE challengeId=:cid " +
            "GROUP BY dayIndex " +
            "ORDER BY dayIndex ASC")
    List<DayProgressAgg> getAggForBoard(long cid);


    // удалить задачи, начиная с конкретного дня (включительно)
    @Query("DELETE FROM day_tasks WHERE challengeId=:cid AND dayIndex>=:fromDay")
    void deleteFromDay(long cid, int fromDay);
}

