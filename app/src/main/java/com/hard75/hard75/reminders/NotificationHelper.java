package com.hard75.hard75.reminders;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public class NotificationHelper {
    public static final String CHANNEL_ID = "hard75_reminders";

    public static void ensureChannel(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = ctx.getSystemService(NotificationManager.class);
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel ch = new NotificationChannel(
                        CHANNEL_ID, "Напоминания Hard 75",
                        NotificationManager.IMPORTANCE_DEFAULT
                );
                ch.setDescription("Утренние и вечерние напоминания челленджа");
                nm.createNotificationChannel(ch);
            }
        }
    }
}
