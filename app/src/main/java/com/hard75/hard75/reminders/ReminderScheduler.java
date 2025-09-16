package com.hard75.hard75.reminders;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class ReminderScheduler {

    public static final String ACTION_MORNING = "com.hard75.hard75.ACTION_MORNING";
    public static final String ACTION_EVENING = "com.hard75.hard75.ACTION_EVENING";

    public static void scheduleAll(Context ctx) {
        int mh = com.hard75.hard75.util.Prefs.getMorningHour(ctx);
        int mm = com.hard75.hard75.util.Prefs.getMorningMinute(ctx);
        int eh = com.hard75.hard75.util.Prefs.getEveningHour(ctx);
        int em = com.hard75.hard75.util.Prefs.getEveningMinute(ctx);
        scheduleDaily(ctx, mh, mm, ACTION_MORNING, 1001);
        scheduleDaily(ctx, eh, em, ACTION_EVENING, 1002);
    }

    public static void scheduleDefault(Context ctx) {
        scheduleAll(ctx);
    }

    public static void cancelAll(Context ctx) {
        cancel(ctx, ACTION_MORNING, 1001);
        cancel(ctx, ACTION_EVENING, 1002);
    }

    public static void rescheduleMorning(Context ctx) {
        int h = com.hard75.hard75.util.Prefs.getMorningHour(ctx);
        int m = com.hard75.hard75.util.Prefs.getMorningMinute(ctx);
        scheduleDaily(ctx, h, m, ACTION_MORNING, 1001);
    }

    public static void rescheduleEvening(Context ctx) {
        int h = com.hard75.hard75.util.Prefs.getEveningHour(ctx);
        int m = com.hard75.hard75.util.Prefs.getEveningMinute(ctx);
        scheduleDaily(ctx, h, m, ACTION_EVENING, 1002);
    }

    private static void scheduleDaily(Context ctx, int hour, int minute, String action, int reqCode) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = pending(ctx, action, reqCode);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        if (cal.getTimeInMillis() <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        try {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
        } catch (SecurityException ignored) {
            // если нет разрешения — просто выходим
        }
    }

    private static void cancel(Context ctx, String action, int reqCode) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pending(ctx, action, reqCode));
    }

    static void onAlarmFired(Context ctx, String action) {
        if (ACTION_MORNING.equals(action)) {
            rescheduleMorning(ctx);
        } else if (ACTION_EVENING.equals(action)) {
            rescheduleEvening(ctx);
        }
    }

    private static PendingIntent pending(Context ctx, String action, int reqCode) {
        Intent i = new Intent(ctx, ReminderReceiver.class).setAction(action);
        return PendingIntent.getBroadcast(
                ctx, reqCode, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    // добавьте новый action:
    public static final String ACTION_FAILURE = "com.hard75.hard75.ACTION_FAILURE";

    // публичный метод для назначения на следующее утро в 9:00
    public static void scheduleFailureNextMorning(Context ctx) {
        long when = computeNextMorning9();
        // сохраним момент, чтобы пережить перезагрузку
        com.hard75.hard75.util.Prefs.setFailureAlarmAt(ctx, when);

        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = pending(ctx, ACTION_FAILURE, 300);
        am.cancel(pi); // на всякий случай
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, when, pi);
    }

    /** вызывать из BootReceiver, чтобы восстановить после ребута */
    @SuppressLint("ScheduleExactAlarm")
    public static void rescheduleFailureIfNeeded(Context ctx) {
        long when = com.hard75.hard75.util.Prefs.getFailureAlarmAt(ctx);
        if (when <= 0) return;

        // если время уже в прошлом — покажем сразу и очистим
        long now = System.currentTimeMillis();
        if (when <= now) {
            try {
                pending(ctx, ACTION_FAILURE, 300).send();
            } catch (CanceledException e) {
                // фолбэк: напрямую дергаем ресивер
                Intent i = new Intent(ctx, ReminderReceiver.class).setAction(ACTION_FAILURE);
                ctx.sendBroadcast(i);
            }
            com.hard75.hard75.util.Prefs.clearFailureAlarm(ctx);
            return;
        }

        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        am.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                when,
                pending(ctx, ACTION_FAILURE, 300)
        );
    }

    // вспомогательное:
    private static long computeNextMorning9() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DAY_OF_YEAR, 1);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 9);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

}
