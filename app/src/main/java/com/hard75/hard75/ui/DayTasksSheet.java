package com.hard75.hard75.ui;
import android.widget.ImageButton;
import com.hard75.hard75.data.db.DayProgressDao;

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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import com.hard75.hard75.data.db.ChallengeDao;
import com.hard75.hard75.data.db.ChallengeEntity;
import com.hard75.hard75.data.db.DayProgressDao;

public class DayTasksSheet {

    public interface OnChanged { void onChanged(); }

    public static void show(Context ctx, long challengeId, int dayIndex, boolean editable, OnChanged cb) {
        final Activity activity = (Activity) ctx;
        final WeakReference<Activity> actRef = new WeakReference<>(activity);

        BottomSheetDialog dlg = new BottomSheetDialog(activity);
        View root = LayoutInflater.from(activity).inflate(R.layout.content_day_tasks, null, false);
        dlg.setContentView(root);

        TextView tvTitle = root.findViewById(R.id.tvTitle);
        LinearProgressIndicator lineProgress = root.findViewById(R.id.lineProgress);
        TextView tvPercent = root.findViewById(R.id.tvPercent);
        RecyclerView rv = root.findViewById(R.id.rvTasks);
        ImageButton btnClose = root.findViewById(R.id.btnClose);

        rv.setLayoutManager(new LinearLayoutManager(activity));
        rv.setItemAnimator(null);

        // закрывать только по кнопке/свайпу/тапу мимо
        btnClose.setOnClickListener(v -> dlg.dismiss());

        final boolean[] dismissed = { false };
        dlg.setOnDismissListener(d -> { dismissed[0] = true; if (cb != null) cb.onChanged(); });

        final DayTasksAdapter[] adapterHolder = new DayTasksAdapter[1];
        adapterHolder[0] = new DayTasksAdapter((task, checked) -> {
            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase.get(activity).dayTaskDao().setDone(task.id, checked);
                updateUI(actRef, dismissed, challengeId, dayIndex, lineProgress, tvPercent, adapterHolder[0], dlg);
            });
        });
        adapterHolder[0].setEditable(editable); // ← важное
        rv.setAdapter(adapterHolder[0]);

        // заголовок с датой (как делали ранее)
        Executors.newSingleThreadExecutor().execute(() -> {
            ChallengeEntity ch = AppDatabase.get(activity).challengeDao().getById(challengeId);
            final String dateLabel = (ch == null) ? "" : formatDateForDay(ch.startDate, dayIndex);
            activity.runOnUiThread(() -> tvTitle.setText("День " + dayIndex + (dateLabel.isEmpty() ? "" : " — (" + dateLabel + ")")));
        });

        updateUI(actRef, dismissed, challengeId, dayIndex, lineProgress, tvPercent, adapterHolder[0], dlg);
        dlg.show();
    }

    private static String formatDateForDay(long startMillis, int dayIndex) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(startMillis);
        cal.add(Calendar.DATE, dayIndex - 1);
        return new SimpleDateFormat("dd.MM", Locale.getDefault()).format(cal.getTime());
    }

    private static void updateUI(WeakReference<Activity> actRef,
                                 boolean[] dismissed,
                                 long cid, int day,
                                 LinearProgressIndicator lineProgress, TextView tvPercent,
                                 DayTasksAdapter adapter, BottomSheetDialog dlg) {

        Executors.newSingleThreadExecutor().execute(() -> {
            Activity act = actRef.get(); if (act == null) return;

            List<DayTaskEntity> list = AppDatabase.get(act).dayTaskDao().getByDay(cid, day);
            int total = list.size(), done = 0;
            for (DayTaskEntity e : list) if (e.isDone) done++;
            final int percent = total == 0 ? 0 : Math.round(done * 100f / total);
            final boolean completed = total > 0 && done == total;


            final DayProgressDao pdao = AppDatabase.get(act).dayProgressDao();
            if (completed) {
                pdao.markCompleted(cid, day);
            } else {
                pdao.markNotCompleted(cid, day);
            }

            if (dismissed[0]) return;

            act.runOnUiThread(() -> {
                if (!dlg.isShowing()) return;
                adapter.submit(list);
                lineProgress.setProgress(percent);
                tvPercent.setText(percent + "%");
                // без автозакрытия
            });
        });
    }





}
