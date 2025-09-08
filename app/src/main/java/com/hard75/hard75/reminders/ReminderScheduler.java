package com.hard75.hard75.reminders;

import android.app.AlarmManager;
import android.app.PendingIntent;
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
}
