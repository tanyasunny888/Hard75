package com.hard75.hard75;


import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
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
import com.hard75.hard75.ui.DayTasksSheet;
import com.hard75.hard75.data.db.DayProgressAgg;
import com.hard75.hard75.data.db.DayTaskDao;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import java.util.Map;
import java.util.HashMap;



public class ChallengeBoardActivity extends AppCompatActivity {

    private DayBoardAdapter adapter;
    private long activeChallengeId = -1L;
    private boolean boardLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_board);

        RecyclerView rv = findViewById(R.id.rvBoard);
        rv.setLayoutManager(new GridLayoutManager(this, 3)); // 3 колонки
        rv.setHasFixedSize(true);
        rv.addItemDecoration(new SpacesItemDecoration(getResources().getDimensionPixelSize(R.dimen.board_item_gap)));

        adapter = new DayBoardAdapter(new DayBoardAdapter.Listener() {
            @Override public void onDayClick(DaySticker day) {
                if (!boardLoaded || activeChallengeId <= 0) return; // защита
                DayTasksSheet.show(ChallengeBoardActivity.this, activeChallengeId, day.dayIndex, () -> loadBoard());
            }
            @Override public void onDayLongClick(DaySticker day) {
                if (!boardLoaded || activeChallengeId <= 0) return;
                markWin(day.dayIndex);
            }
        });

        rv.setAdapter(adapter);

        // можно загрузить сразу
        loadBoard();
    }

    @Override protected void onResume() {
        super.onResume();
        // или обновлять при возврате на экран
        loadBoard();
    }

    private void loadBoard() {
        boardLoaded = false;
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.get(this);
            ChallengeDao cdao = db.challengeDao();
            DayProgressDao pdao = db.dayProgressDao();
            DayTaskDao tdao = db.dayTaskDao();

            ChallengeEntity active = cdao.getActive();
            if (active == null) {
                runOnUiThread(() -> Toast.makeText(this, "Нет активного челленджа", Toast.LENGTH_LONG).show());
                return;
            }
            activeChallengeId = active.id;

            List<DayProgressEntity> progressRows = pdao.getBoard(active.id);
            List<DayProgressAgg> aggs = tdao.getAggForBoard(active.id);

            Map<Integer, DayProgressAgg> map = new HashMap<>();
            for (DayProgressAgg a : aggs) map.put(a.dayIndex, a);

            List<DaySticker> ui = new ArrayList<>();
            for (DayProgressEntity e : progressRows) {
                DayProgressAgg a = map.get(e.dayIndex);
                int percent = 0;
                if (a != null && a.total > 0) percent = Math.round(a.done * 100f / a.total);
                ui.add(new DaySticker(e.dayIndex, e.completed, percent));
            }

            runOnUiThread(() -> {
                adapter.submit(ui);
                boardLoaded = true;
            });
        });
    }

    private void markWin(int dayIndex) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.get(this).dayProgressDao().markCompleted(activeChallengeId, dayIndex);
            runOnUiThread(this::loadBoard);
        });
    }

    /** простой декоратор отступов между стикерами */
    static class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private final int space;
        SpacesItemDecoration(int s) { space = s; }
        @Override public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.set(space, space, space, space);
        }
    }
}

