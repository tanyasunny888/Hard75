package com.hard75.hard75.util;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;


public class Prefs {
    private static final String NAME = "hard75_prefs";
    private static final String KEY_CONSENT = "consent_accepted";

    public static boolean isConsentAccepted(Context ctx) {
        return ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_CONSENT, false);
    }

    public static void setConsentAccepted(Context ctx, boolean accepted) {
        SharedPreferences.Editor e = ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit();
        e.putBoolean(KEY_CONSENT, accepted);
        e.apply();
    }

    public static boolean isRemindersEnabled(Context c) {
        return c.getSharedPreferences("hard75", Context.MODE_PRIVATE)
                .getBoolean("reminders_enabled", false);
    }
    public static void setRemindersEnabled(Context c, boolean enabled) {
        c.getSharedPreferences("hard75", Context.MODE_PRIVATE)
                .edit().putBoolean("reminders_enabled", enabled).apply();
    }

    public static int getMorningHour(Context c) {
        return c.getSharedPreferences("hard75", Context.MODE_PRIVATE)
                .getInt("rem_m_h", 9);
    }
    public static int getMorningMinute(Context c) {
        return c.getSharedPreferences("hard75", Context.MODE_PRIVATE)
                .getInt("rem_m_m", 0);
    }
    public static void setMorningTime(Context c, int h, int m) {
        c.getSharedPreferences("hard75", Context.MODE_PRIVATE)
                .edit().putInt("rem_m_h", h).putInt("rem_m_m", m).apply();
    }

    public static int getEveningHour(Context c) {
        return c.getSharedPreferences("hard75", Context.MODE_PRIVATE)
                .getInt("rem_e_h", 21);
    }
    public static int getEveningMinute(Context c) {
        return c.getSharedPreferences("hard75", Context.MODE_PRIVATE)
                .getInt("rem_e_m", 0);
    }
    public static void setEveningTime(Context c, int h, int m) {
        c.getSharedPreferences("hard75", Context.MODE_PRIVATE)
                .edit().putInt("rem_e_h", h).putInt("rem_e_m", m).apply();
    }

    // ---- ШАБЛОНЫ ЧЕЛЛЕНДЖА ----
    public static void saveTemplate(Context c, String level, List<String> items) {
        String key = "tpl_" + (level == null ? "soft" : level.toLowerCase(Locale.ROOT));
        try {
            org.json.JSONArray arr = new org.json.JSONArray();
            for (String s : items) if (s != null && !s.trim().isEmpty()) arr.put(s.trim());
            c.getSharedPreferences("hard75", Context.MODE_PRIVATE)
                    .edit().putString(key, arr.toString()).apply();
        } catch (Throwable ignored) {}
    }

    public static List<String> getTemplateOrNull(Context c, String level) {
        String key = "tpl_" + (level == null ? "soft" : level.toLowerCase(Locale.ROOT));
        String json = c.getSharedPreferences("hard75", Context.MODE_PRIVATE).getString(key, null);
        List<String> out = new ArrayList<>();
        if (json == null) return out;
        try {
            org.json.JSONArray arr = new org.json.JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                String s = arr.optString(i, "").trim();
                if (!s.isEmpty()) out.add(s);
            }
        } catch (Throwable ignored) {}
        return out;
    }

    public static List<String> getTemplateOrDefault(Context c, String level) {
        List<String> saved = getTemplateOrNull(c, level);
        if (!saved.isEmpty()) return saved;
        return new ArrayList<>(com.hard75.hard75.domain.ChallengeTemplates.baseTasks(level));
    }


}
