package com.hard75.hard75;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hard75.hard75.data.db.AppDatabase;
import com.hard75.hard75.data.db.ChallengeDao;
import com.hard75.hard75.data.db.ChallengeEntity;
import com.hard75.hard75.data.db.DayProgressDao;
import com.hard75.hard75.data.db.DayProgressEntity;
import com.hard75.hard75.data.db.DayTaskDao;
import com.hard75.hard75.data.db.DayTaskEntity;
import com.hard75.hard75.domain.ChallengeTemplates;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


/**
 * Мастер создания челленджа:
 * - выбор уровня (soft / medium / hard)
 * - выбор длительности (21..75)
 * - редактирование шаблона задач (мультистрочный ввод)
 * - генерация задач DayTaskEntity и доски DayProgressEntity
 * - переход на ChallengeBoardActivity
 *
 * Разметка: res/layout/activity_create_challenge.xml
 * Диалог редактирования: res/layout/dialog_edit_tasks_multiline.xml
 */
public class CreateChallengeActivity extends AppCompatActivity {

    // UI
    private RadioGroup rgLevel;
    private SeekBar seekDuration;
    private TextView tvDurationVal;
    private Button btnCustomize, btnStart;
    private LinearLayout llPreview;
    private TextView tvPreviewEmpty;

    private RecyclerView rvEditableTasks;
    private FloatingActionButton fabAddTask;
    private com.hard75.hard75.ui.CreateTasksAdapter editAdapter;


    // Текущий отредактированный список задач (если null — берём шаблон по уровню)
    @Nullable
    private List<String> customTasks = null;

    // Ограничения длительности
    private static final int MIN_DAYS = 21;
    private static final int MAX_DAYS = 75;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_challenge);

        rgLevel       = findViewById(R.id.rgLevel);
        seekDuration  = findViewById(R.id.seekDuration);
        tvDurationVal = findViewById(R.id.tvDurationVal);
        btnStart      = findViewById(R.id.btnStart);

        // 👇 новые элементы
        rvEditableTasks = findViewById(R.id.rvEditableTasks);
        fabAddTask      = findViewById(R.id.fabAddTask);

        rvEditableTasks.setLayoutManager(new LinearLayoutManager(this));
        editAdapter = new com.hard75.hard75.ui.CreateTasksAdapter(new com.hard75.hard75.ui.CreateTasksAdapter.Listener() {
            @Override public void onEdit(int position, String currentText) { showEditDialog(position, currentText); }
            @Override public void onDelete(int position) {
                editAdapter.remove(position);
                customTasks = editAdapter.data();
            }
        });
        rvEditableTasks.setAdapter(editAdapter);

        // стартовое наполнение
        customTasks = new ArrayList<>(com.hard75.hard75.domain.ChallengeTemplates.baseTasks(currentLevel()));
        editAdapter.submit(customTasks);


        fabAddTask.setOnClickListener(v -> showAddDialog());

        // При смене уровня — подставляем новый шаблон
        rgLevel.setOnCheckedChangeListener((group, checkedId) -> {
            customTasks = new ArrayList<>(com.hard75.hard75.domain.ChallengeTemplates.baseTasks(currentLevel()));
            editAdapter.submit(customTasks);
        });

        // SeekBar: 0..54 + 21 = 21..75
        updateDurationText(MIN_DAYS + seekDuration.getProgress());
        seekDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateDurationText(MIN_DAYS + progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // FAB «добавить пункт»
        fabAddTask.setOnClickListener(v -> showAddDialog());

        // Старт челленджа
        btnStart.setOnClickListener(v -> startChallenge());
    }

    private void showAddDialog() {
        final EditText et = new EditText(this);
        et.setHint("Новый пункт");
        et.setSingleLine(false);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        et.setPadding(pad, pad, pad, pad);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Добавить пункт")
                .setView(et)
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Добавить", (d, w) -> {
                    String t = et.getText().toString().trim();
                    if (t.isEmpty()) return;
                    if (editAdapter != null) {
                        editAdapter.addItem(t);
                        // синхронизируем customTasks с адаптером
                        customTasks = editAdapter.data();
                    }
                })
                .show();
    }

    private void showEditDialog(int position, String current) {
        final EditText et = new EditText(this);
        et.setText(current);
        et.setSelection(current != null ? current.length() : 0);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        et.setPadding(pad, pad, pad, pad);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Редактировать пункт")
                .setView(et)
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Сохранить", (d, w) -> {
                    String t = et.getText().toString().trim();
                    if (t.isEmpty()) return;
                    if (editAdapter != null) {
                        editAdapter.replace(position, t);
                        customTasks = editAdapter.data();
                    }
                })
                .show();
    }



    // ----- Вспомогательные методы UI -----

    private void updateDurationText(int days) {
        tvDurationVal.setText(days + " дней");
    }

    private String currentLevel() {
        int id = rgLevel.getCheckedRadioButtonId();
        RadioButton rb = findViewById(id);
        if (rb == null) return "soft"; // на всякий случай
        String txt = rb.getText().toString().toLowerCase();
        if (txt.contains("soft"))   return "soft";
        if (txt.contains("medium")) return "medium";
        return "hard";
    }

    // Диалог редактирования списка задач (одна строка = один пункт)
    private void showCustomizeDialog(String level) {
        List<String> base = ChallengeTemplates.baseTasks(level);
        if (customTasks == null || customTasks.isEmpty()) customTasks = new ArrayList<>(base);

        StringBuilder sb = new StringBuilder();
        for (String s : customTasks) sb.append(s).append("\n");

        View view = getLayoutInflater().inflate(R.layout.dialog_edit_tasks_multiline, null, false);
        EditText et = view.findViewById(R.id.etTasks);
        et.setText(sb.toString().trim());

        new AlertDialog.Builder(this)
                .setTitle("Настройка чек-листа")
                .setView(view)
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Сохранить", (d, w) -> {
                    String[] lines = et.getText().toString().split("\\r?\\n");
                    List<String> out = new ArrayList<>();
                    for (String line : lines) {
                        String t = line.trim();
                        if (!t.isEmpty()) out.add(t);
                    }
                    if (out.isEmpty()) {
                        Toast.makeText(this, "Список пуст. Использую шаблон уровня.", Toast.LENGTH_SHORT).show();
                        customTasks = new ArrayList<>(base);
                    } else {
                        customTasks = out;
                        Toast.makeText(this, "Сохранено: " + customTasks.size() + " пункт(ов)", Toast.LENGTH_SHORT).show();
                    }
                    updatePreview();
                })
                .show();
    }

    // ----- Основная логика создания челленджа -----

    private void startChallenge() {
        String level = currentLevel();

        int duration = MIN_DAYS + seekDuration.getProgress();
        if (duration < MIN_DAYS) duration = MIN_DAYS;
        if (duration > MAX_DAYS) duration = MAX_DAYS;

        final int finalDuration = duration;

        long start = System.currentTimeMillis();

        // Базовые задачи (с учётом редактирования)
        List<String> baseList = editAdapter != null && !editAdapter.data().isEmpty()
                ? editAdapter.data()
                : com.hard75.hard75.domain.ChallengeTemplates.baseTasks(level);
        final List<String> finalBase = baseList;


        if (finalBase.isEmpty()) {
            Toast.makeText(this, "Выберите хотя бы один пункт чек-листа", Toast.LENGTH_SHORT).show();
            return;
        }

        // Сущность челленджа
        ChallengeEntity ch = new ChallengeEntity(level, duration, start, "ACTIVE");

        btnStart.setEnabled(false);
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                AppDatabase db = AppDatabase.get(this);
                ChallengeDao cdao = db.challengeDao();
                DayTaskDao tdao = db.dayTaskDao();
                DayProgressDao pdao = db.dayProgressDao();

                long cid = cdao.insert(ch);

                List<DayTaskEntity> allTasks = new ArrayList<>();
                for (int day = 1; day <= finalDuration; day++) {
                    int order = 0;
                    for (String title : finalBase) {
                        allTasks.add(new DayTaskEntity(cid, day, title, false, false, order++));
                    }
                }
                tdao.insertAll(allTasks);

                List<DayProgressEntity> board = new ArrayList<>();
                for (int day = 1; day <= finalDuration; day++) {
                    board.add(new DayProgressEntity(cid, day, false));
                }
                pdao.insertAll(board);

                runOnUiThread(() -> {
                    Toast.makeText(this,
                            "Челлендж создан: " + level + " • " + finalDuration + " дней",
                            Toast.LENGTH_LONG).show();
                    startActivity(new Intent(this, ChallengeBoardActivity.class));
                    finish();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnStart.setEnabled(true);
                    Toast.makeText(this, "Ошибка создания челленджа: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });


    }

    /** Текущий список задач для дня (учитывает уровень и кастомные правки) */
    private List<String> currentDailyTasks() {
        String level = currentLevel();
        if (customTasks != null && !customTasks.isEmpty()) {
            return new ArrayList<>(customTasks);
        } else {
            return new ArrayList<>(ChallengeTemplates.baseTasks(level));
        }
    }

    /** Перерисовать превью: очищаем контейнер и добавляем строки */
    private void updatePreview() {
        List<String> tasks = currentDailyTasks();
        llPreview.removeAllViews();

        if (tasks.isEmpty()) {
            tvPreviewEmpty.setVisibility(View.VISIBLE);
            return;
        } else {
            tvPreviewEmpty.setVisibility(View.GONE);
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        for (String t : tasks) {
            View row = inflater.inflate(R.layout.item_preview_task, llPreview, false);
            TextView tv = row.findViewById(R.id.tvTask);
            tv.setText(t);
            llPreview.addView(row);
        }
    }

}
