package com.hard75.hard75;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hard75.hard75.data.db.AppDatabase;
import com.hard75.hard75.data.db.ChallengeDao;
import com.hard75.hard75.data.db.ChallengeEntity;
import com.hard75.hard75.data.db.DayProgressAgg;
import com.hard75.hard75.data.db.DayProgressDao;
import com.hard75.hard75.data.db.DayProgressEntity;
import com.hard75.hard75.data.db.DayTaskDao;
import com.hard75.hard75.data.db.DayTaskEntity;
import com.hard75.hard75.domain.ChallengeTemplates;
import com.hard75.hard75.ui.DayBoardAdapter;
import com.hard75.hard75.model.DaySticker;
import com.hard75.hard75.ui.DayTasksSheet;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;

public class ChallengeBoardActivity extends AppCompatActivity {

    private RecyclerView rv;
    private DayBoardAdapter adapter;
    private FloatingActionButton fabMenu;
    private long activeChallengeId = -1L;
    private ChallengeEntity activeChallenge = null;
    private int activeTodayIndex = -1; // 1-based
    private boolean isFailed = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_board);

        rv = findViewById(R.id.rvBoard);
        fabMenu = findViewById(R.id.fabMenu);
        fabMenu.setOnClickListener(v -> showSettingsMenu());


        // сетка стикеров
        rv.setLayoutManager(new GridLayoutManager(this, 3));
        rv.setHasFixedSize(true);
        rv.addItemDecoration(new SpacesItemDecoration(getResources().getDimensionPixelSize(R.dimen.board_item_gap)));

        adapter = new DayBoardAdapter(new DayBoardAdapter.Listener() {
            @Override public void onDayClick(DaySticker day) {
                if (activeChallengeId <= 0) return;

                if (isFailed) {
                    showFailedMessageAndOfferRestart();
                    return;
                }

                // редактировать можно только в "свой" календарный день
                boolean editable = (day.dayIndex == activeTodayIndex);
                DayTasksSheet.show(ChallengeBoardActivity.this,
                        activeChallengeId,
                        day.dayIndex,
                        editable,
                        () -> loadBoard());
            }

            @Override public void onDayLongClick(DaySticker day) {
                // лонг-тап отключен по требованиям — ничего не делаем
            }
        });
        rv.setAdapter(adapter);


        loadBoard();
    }

    @Override protected void onResume() {
        super.onResume();
        loadBoard();
    }

    /* =========================  ЗАГРУЗКА/ПОДСЧЁТ  ========================= */

    private void loadBoard() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.get(this);
            ChallengeDao cdao = db.challengeDao();
            DayProgressDao pdao = db.dayProgressDao();
            DayTaskDao tdao = db.dayTaskDao();

            ChallengeEntity active = cdao.getActive();
            if (active == null) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Нет активного челленджа", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(this, CreateChallengeActivity.class));
                    finish();
                });
                return;
            }
            activeChallenge = active;
            activeChallengeId = active.id;
            activeTodayIndex = computeTodayIndex(active.startDate);

            List<DayProgressEntity> progressRows = pdao.getBoard(active.id);
            List<DayProgressAgg> aggs = tdao.getAggForBoard(active.id);

            Map<Integer, DayProgressAgg> map = new HashMap<>();
            for (DayProgressAgg a : aggs) map.put(a.dayIndex, a);

            List<DaySticker> ui = new ArrayList<>();
            for (DayProgressEntity e : progressRows) {
                DayProgressAgg a = map.get(e.dayIndex);
                int percent = (a != null && a.total > 0) ? Math.round(a.done * 100f / a.total) : 0;
                ui.add(new DaySticker(e.dayIndex, e.completed, percent));
            }

            runOnUiThread(() -> adapter.submit(ui));
            // после загрузки — оценить провал и раскрасить кнопку «Сводка»
            evaluateAndUpdateSummaryUI();
        });
    }

    private void evaluateAndUpdateSummaryUI() {
        Executors.newSingleThreadExecutor().execute(() -> {
            if (activeChallenge == null) return;

            AppDatabase db = AppDatabase.get(this);
            ChallengeDao cdao = db.challengeDao();
            DayTaskDao tdao = db.dayTaskDao();

            int today = computeTodayIndex(activeChallenge.startDate);
            int missAllowed = allowedMisses(activeChallenge.level);

            List<DayProgressAgg> aggs = tdao.getAggForBoard(activeChallenge.id);

            boolean failed = false;
            int successDays = 0;

            for (DayProgressAgg a : aggs) {
                // оцениваем только прошедшие полные дни
                if (a.dayIndex < today) {
                    int misses = a.total - a.done;
                    if (misses > missAllowed) { failed = true; break; }
                    successDays++;
                }
            }

            if (failed && !"FAILED".equals(activeChallenge.status)) {
                cdao.updateStatus(activeChallenge.id, "FAILED");
                activeChallenge.status = "FAILED";
            }
            isFailed = failed;
            final int fSuccessDays = successDays;

            runOnUiThread(() -> {
                int color = ContextCompat.getColor(this,
                        isFailed ? R.color.summary_failed : R.color.summary_ok);
                fabMenu.setBackgroundTintList(ColorStateList.valueOf(color));
            });

        });
    }

    private static long floorToMidnight(long ms) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ms);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    private int computeTodayIndex(long startDateMs) {
        long start = floorToMidnight(startDateMs);
        long today = floorToMidnight(System.currentTimeMillis());
        long days = (today - start) / (24L * 60 * 60 * 1000);
        return (int) days + 1; // День 1 = startDate
    }

    private int allowedMisses(String level) {
        if (level == null) return 0;
        switch (level.toLowerCase(Locale.ROOT)) {
            case "soft":   return 2;
            case "medium": return 1;
            default:       return 0; // hard
        }
    }

    /* =========================  СВОДКА / НАСТРОЙКИ  ========================= */

    private void showSummary() {
        if (activeChallenge == null) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.get(this);
            ChallengeDao cdao = db.challengeDao();
            DayTaskDao tdao = db.dayTaskDao();

            // попытка №
            int attempts = cdao.countAll();

            // правила/прогресс: пересчитаем тут, чтобы не полагаться на getTag()
            int today = computeTodayIndex(activeChallenge.startDate);
            int missAllowed = allowedMisses(activeChallenge.level);
            List<DayProgressAgg> aggs = tdao.getAggForBoard(activeChallenge.id);

            boolean failed = false;
            int successDays = 0;
            for (DayProgressAgg a : aggs) {
                if (a.dayIndex < today) {
                    int misses = a.total - a.done;
                    if (misses > missAllowed) { failed = true; break; }
                    successDays++;
                }
            }

            // дата старта
            String started = new java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
                    .format(new java.util.Date(activeChallenge.startDate));

            String level = activeChallenge.level == null ? "" : activeChallenge.level;
            String rule;
            switch (level.toLowerCase(java.util.Locale.ROOT)) {
                case "soft":   rule = "на софте можно пропускать до 2 задач в день"; break;
                case "medium": rule = "на медиум можно пропускать 1 задачу в день";  break;
                default:       rule = "на харде нельзя пропускать задачи";
            }
            String state = (isFailed || failed)
                    ? "Челлендж провален. Попробуйте снова."
                    : "Челлендж в процессе.";

            final boolean finalFailed = (isFailed || failed);
            final String msg =
                    "Челлендж – попытка № " + attempts + "\n" +
                            "Начат: " + started + "\n" +
                            "Режим: " + level + "\n\n" +
                            "Прогресс: успешно " + successDays + " дней из " + activeChallenge.durationDays + ".\n" +
                            "Правило: " + rule + ".\n\n" +
                            state;

            runOnUiThread(() -> {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Сводка")
                        .setMessage(msg)
                        .setPositiveButton(finalFailed ? "Новый челлендж" : "OK",
                                (d, w) -> { if (finalFailed) confirmRestart(); })
                        .setNegativeButton(finalFailed ? "Отмена" : null, null)
                        .show();
            });
        });
    }


    private void showSettingsMenu() {
        String[] items = {"Сводка", "Изменить список задач челленджа", "Начать челлендж заново"};
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Меню")
                .setItems(items, (dialog, which) -> {
                    if (which == 0) {
                        showSummary();
                    } else if (which == 1) {
                        if (isFailed) {
                            showFailedMessageAndOfferRestart();
                        } else {
                            promptEditFutureTasks();
                        }
                    } else if (which == 2) {
                        confirmRestart();
                    }
                })
                .show();
    }


    private void showFailedMessageAndOfferRestart() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Челлендж провален")
                .setMessage("Попробуйте снова. Вы можете начать новый челлендж.")
                .setPositiveButton("Начать заново", (d, w) -> confirmRestart())
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void confirmRestart() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Начать заново?")
                .setMessage("Текущий челлендж будет завершён. Создать новый?")
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Да", (d, w) -> {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        if (activeChallenge != null) {
                            AppDatabase.get(this).challengeDao()
                                    .updateStatus(activeChallenge.id, "FAILED");
                        }
                        runOnUiThread(() -> {
                            startActivity(new Intent(this, CreateChallengeActivity.class));
                            finish();
                        });
                    });
                })
                .show();
    }

    /* =========================  ИЗМЕНЕНИЕ СПИСКА СЕГОДНЯ..КОНЕЦ  ========================= */

    private void promptEditFutureTasks() {
        if (activeChallenge == null) return;

        final android.widget.EditText et = new android.widget.EditText(this);
        et.setHint("Одна строка — один пункт");
        et.setMinLines(6);

        // предложим текущий шаблон уровня как подсказку
        List<String> base = ChallengeTemplates.baseTasks(activeChallenge.level);
        et.setText(android.text.TextUtils.join("\n", base));

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Изменить список задач")
                .setView(et)
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Далее", (d, w) -> {
                    String[] lines = et.getText().toString().split("\\r?\\n");
                    List<String> newList = new ArrayList<>();
                    for (String s : lines) {
                        String t = s.trim();
                        if (!t.isEmpty()) newList.add(t);
                    }
                    if (newList.isEmpty()) {
                        Toast.makeText(this, "Список пуст", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    confirmApplyFutureTasks(newList);
                })
                .show();
    }

    private void confirmApplyFutureTasks(List<String> newList) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Подтвердите изменения")
                .setMessage("Будут изменены все дни, начиная с сегодняшнего. Применить изменение?")
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Применить", (d, w) -> applyFutureTasks(newList))
                .show();
    }

    private void applyFutureTasks(List<String> newList) {
        if (activeChallenge == null) return;
        final int fromDay = activeTodayIndex <= 0 ? 1 : activeTodayIndex;

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.get(this);
            DayTaskDao tdao = db.dayTaskDao();
            DayProgressDao pdao = db.dayProgressDao();

            // удалить старые задачи с сегодняшнего дня
            tdao.deleteFromDay(activeChallenge.id, fromDay);
            // сбросить completion с сегодняшнего дня
            pdao.resetFromDay(activeChallenge.id, fromDay);

            // вставить новые задачи на каждый оставшийся день
            List<DayTaskEntity> all = new ArrayList<>();
            int totalDays = activeChallenge.durationDays;
            for (int day = fromDay; day <= totalDays; day++) {
                int order = 0;
                for (String title : newList) {
                    all.add(new DayTaskEntity(activeChallenge.id, day, title, false, true, order++));
                }
            }
            tdao.insertAll(all);

            runOnUiThread(() -> {
                Toast.makeText(this, "Изменения применены с дня " + fromDay, Toast.LENGTH_SHORT).show();
                loadBoard();
            });
        });
    }

    /* =========================  ВСПОМОГАТЕЛЬНОЕ  ========================= */

    /** простой декоратор отступов между стикерами */
    public static class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private final int space;
        public SpacesItemDecoration(int s) { space = s; }
        @Override public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.set(space, space, space, space);
        }
    }
}
