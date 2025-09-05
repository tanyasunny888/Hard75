package com.hard75.hard75.ui;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.hard75.hard75.R;
import com.hard75.hard75.data.db.AppDatabase;
import com.hard75.hard75.data.db.DayTaskDao;
import com.hard75.hard75.data.db.DayTaskEntity;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.Executors;

public class DayTasksSheet {

    public interface OnChanged { void onChanged(); }

    public static void show(Context ctx, long challengeId, int dayIndex, OnChanged cb) {
        // Обязательно activity-context
        final Activity activity = (Activity) ctx;
        final WeakReference<Activity> actRef = new WeakReference<>(activity);

        BottomSheetDialog dlg = new BottomSheetDialog(activity);
        View root = LayoutInflater.from(activity).inflate(R.layout.content_day_tasks, null, false);
        dlg.setContentView(root);

        TextView tvTitle = root.findViewById(R.id.tvTitle);
        LinearProgressIndicator lineProgress = root.findViewById(R.id.lineProgress);
        TextView tvPercent = root.findViewById(R.id.tvPercent);
        RecyclerView rv = root.findViewById(R.id.rvTasks);
        rv.setLayoutManager(new LinearLayoutManager(activity));

        // держим флаг, чтобы не трогать вьюхи после закрытия
        final boolean[] dismissed = { false };
        dlg.setOnDismissListener(d -> {
            dismissed[0] = true;
            if (cb != null) cb.onChanged(); // всегда обновляем доску после закрытия
        });

        final DayTasksAdapter[] adapterHolder = new DayTasksAdapter[1];
        adapterHolder[0] = new DayTasksAdapter((task, checked) -> {
            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase.get(activity).dayTaskDao().setDone(task.id, checked);
                updateUI(actRef, dismissed, challengeId, dayIndex, lineProgress, tvPercent, adapterHolder[0], dlg);
            });
        });
        rv.setAdapter(adapterHolder[0]);

        tvTitle.setText("День " + dayIndex + " — чек-лист");
        updateUI(actRef, dismissed, challengeId, dayIndex, lineProgress, tvPercent, adapterHolder[0], dlg);

        dlg.show();
    }

    private static void updateUI(WeakReference<Activity> actRef,
                                 boolean[] dismissed,
                                 long cid, int day,
                                 LinearProgressIndicator lineProgress, TextView tvPercent,
                                 DayTasksAdapter adapter,
                                 BottomSheetDialog dlg) {

        Executors.newSingleThreadExecutor().execute(() -> {
            Activity act = actRef.get();
            if (act == null) return;

            DayTaskDao tdao = AppDatabase.get(act).dayTaskDao();
            List<DayTaskEntity> list = tdao.getByDay(cid, day);
            int total = list.size();
            int done = 0;
            for (DayTaskEntity e : list) if (e.isDone) done++;
            int percent = total == 0 ? 0 : Math.round(done * 100f / total);
            boolean completed = total > 0 && done == total;

            // Если закрыто — не делаем никаких UI-апдейтов
            if (dismissed[0]) {
                if (completed) {
                    // на всякий случай отметим победу даже если закрыли быстро
                    AppDatabase.get(act).dayProgressDao().markCompleted(cid, day);
                }
                return;
            }

            act.runOnUiThread(() -> {
                // Activity все ещё жива?
                if (act.isFinishing() || (android.os.Build.VERSION.SDK_INT >= 17 && act.isDestroyed()))
                    return;

                // Диалог уже не показывается — ничего не обновляем
                if (!dlg.isShowing()) return;

                // Обновляем список и прогресс
                adapter.submit(list);
                lineProgress.setProgress(percent);
                tvPercent.setText(percent + "%");

                if (completed) {
                    // Отмечаем победу в БД и закрываем лист.
                    Executors.newSingleThreadExecutor().execute(() ->
                            AppDatabase.get(act).dayProgressDao().markCompleted(cid, day));

                    // Закрываем лист (onDismiss вызовет cb.onChanged() и обновит доску)
                    try {
                        dlg.dismiss();
                    } catch (Exception ignore) { }
                }
            });
        });
    }
}
