package com.hard75.hard75;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hard75.hard75.data.db.AppDatabase;
import com.hard75.hard75.data.db.ChallengeDao;
import com.hard75.hard75.data.db.ChallengeEntity;
import com.hard75.hard75.data.db.DayProgressDao;
import com.hard75.hard75.data.db.DayProgressEntity;
import com.hard75.hard75.model.DaySticker;
import com.hard75.hard75.ui.DayBoardAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class ChallengeBoardActivity extends AppCompatActivity {

    private DayBoardAdapter adapter;
    private long activeChallengeId = -1L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_board);

        RecyclerView rv = findViewById(R.id.rvBoard);
        rv.setLayoutManager(new GridLayoutManager(this, 3)); // 3 колонки — удобно на доску
        adapter = new DayBoardAdapter(new DayBoardAdapter.Listener() {
            @Override public void onDayClick(DaySticker day) {
                // здесь позже откроем экран чек-листа дня
                Toast.makeText(ChallengeBoardActivity.this, "Открыть день " + day.dayIndex, Toast.LENGTH_SHORT).show();
            }
            @Override public void onDayLongClick(DaySticker day) {
                markWin(day.dayIndex);
            }
        });
        rv.setAdapter(adapter);
    }

    @Override protected void onResume() {
        super.onResume();
        loadBoard();
    }

    private void loadBoard() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.get(this);
            ChallengeDao cdao = db.challengeDao();
            DayProgressDao pdao = db.dayProgressDao();

            ChallengeEntity active = cdao.getActive();
            if (active == null) {
                runOnUiThread(() -> Toast.makeText(this, "Нет активного челленджа", Toast.LENGTH_LONG).show());
                return;
            }
            activeChallengeId = active.id;

            List<DayProgressEntity> src = pdao.getBoard(active.id);
            List<DaySticker> ui = new ArrayList<>();
            for (DayProgressEntity e : src) ui.add(new DaySticker(e.dayIndex, e.completed));

            runOnUiThread(() -> adapter.submit(ui));
        });
    }

    private void markWin(int dayIndex) {
        if (activeChallengeId <= 0) return;
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.get(this).dayProgressDao().markCompleted(activeChallengeId, dayIndex);
            loadBoard();
        });
    }
}
