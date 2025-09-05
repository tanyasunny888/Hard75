package com.hard75.hard75.data.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(
        entities = {
                StickyNoteEntity.class,
                ChallengeEntity.class,
                DayTaskEntity.class,
                DayProgressEntity.class
        },
        version = 3,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract StickyNoteDao stickyNoteDao();
    public abstract ChallengeDao challengeDao();
    public abstract DayTaskDao dayTaskDao();
    public abstract DayProgressDao dayProgressDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase get(Context ctx) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    ctx.getApplicationContext(),
                                    AppDatabase.class,
                                    "hard75.db")
                            // на этапе разработки удобно:
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
