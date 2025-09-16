package com.hard75.hard75.reminders;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!com.hard75.hard75.util.Prefs.isRemindersEnabled(context)) return;

        // восстановим обычные будильники
        ReminderScheduler.scheduleAll(context);

        // если было запланировано одноразовое уведомление о провале — восстановим
        ReminderScheduler.rescheduleFailureIfNeeded(context);
    }

}

