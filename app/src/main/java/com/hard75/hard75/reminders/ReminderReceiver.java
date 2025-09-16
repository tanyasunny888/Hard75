package com.hard75.hard75.reminders;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.hard75.hard75.ChallengeBoardActivity;
import com.hard75.hard75.R;
import com.hard75.hard75.data.db.AppDatabase;
import com.hard75.hard75.data.db.ChallengeDao;
import com.hard75.hard75.data.db.ChallengeEntity;
import com.hard75.hard75.data.db.DayProgressAgg;
import com.hard75.hard75.data.db.DayTaskDao;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReminderReceiver extends BroadcastReceiver {

    private static final String[] MOTIVATION = new String[]{
            "У тебя всё получится!",
            "Сделай ещё один шаг к цели!",
            "Сегодня новый день — день твоих побед!",
            "Сделаем это!",
            "Докажи, что ты можешь!"
    };

    // единый лёгкий пул для ресивера
    private static final ExecutorService EXEC = Executors.newSingleThreadExecutor();

    @Override
    public void onReceive(Context context, Intent intent) {
        // быстрый выход/перепланировка, если выключено
        if (!com.hard75.hard75.util.Prefs.isRemindersEnabled(context)) {
            ReminderScheduler.onAlarmFired(context, intent != null ? intent.getAction() : null);
            return;
        }

        final String action = (intent != null) ? intent.getAction() : null;
        if (action == null) return;

        // переводим работу в фон, чтобы не трогать main thread
        final PendingResult pr = goAsync();
        final Context appCtx = context.getApplicationContext();

        EXEC.execute(() -> {
            try {
                NotificationHelper.ensureChannel(appCtx);

                AppDatabase db = AppDatabase.get(appCtx);
                ChallengeDao cdao = db.challengeDao();
                ChallengeEntity ch = cdao.getActive();
                if (ch == null) {
                    ReminderScheduler.onAlarmFired(appCtx, action);
                    return;
                }

                int todayIndex = computeTodayIndex(ch.startDate);
                int totalDays = ch.durationDays;

                if (ReminderScheduler.ACTION_MORNING.equals(action)) {
                    String extra = MOTIVATION[(Math.max(1, todayIndex) - 1) % MOTIVATION.length];
                    String text = String.format(Locale.getDefault(),
                            "Сегодня %d-й из %d дней челленджа. %s",
                            todayIndex, totalDays, extra);
                    showNotification(appCtx, 201, "Hard 75", text);
                    ReminderScheduler.onAlarmFired(appCtx, action);

                } else if (ReminderScheduler.ACTION_EVENING.equals(action)) {
                    DayTaskDao tdao = db.dayTaskDao();
                    List<DayProgressAgg> aggs = tdao.getAggForBoard(ch.id);
                    int done = 0, total = 0;
                    for (DayProgressAgg a : aggs) {
                        if (a.dayIndex == todayIndex) {
                            done = a.done;
                            total = a.total;
                            break;
                        }
                    }
                    if (total > 0 && done < total) {
                        String text = "Время отметить выполненные задачи. Задачи можно отмечать только в текущий день.";
                        showNotification(appCtx, 202, "Hard 75", text);
                    }
                    ReminderScheduler.onAlarmFired(appCtx, action);
                }

                else if (ReminderScheduler.ACTION_FAILURE.equals(action)) {
                    // Одноразовое уведомление о провале
                    String title = "Hard 75";
                    String text  = "Челлендж провален, попробуйте снова.";
                    showNotification(appCtx, 203, title, text);

                    // очистим флажок, чтобы не повторялось
                    com.hard75.hard75.util.Prefs.clearFailureAlarm(appCtx);
                    // одноразовый будильник рескейдлить не нужно
                }

        } catch (Throwable t) {
                // проглатываем, чтобы ресивер не крэшил процесс
            } finally {
                pr.finish();
            }
        });
    }

    private void showNotification(Context ctx, int id, String title, String text) {
        Intent open = new Intent(ctx, ChallengeBoardActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent content = PendingIntent.getActivity(
                ctx, id, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, NotificationHelper.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(content);

        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(id, b.build());
    }

    private static int computeTodayIndex(long startDateMs) {
        Calendar start = Calendar.getInstance();
        start.setTimeInMillis(startDateMs);
        zeroTime(start);
        Calendar today = Calendar.getInstance();
        zeroTime(today);
        long days = (today.getTimeInMillis() - start.getTimeInMillis()) / (24L * 60 * 60 * 1000);
        return (int) days + 1;
    }

    private static void zeroTime(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
    }
}
