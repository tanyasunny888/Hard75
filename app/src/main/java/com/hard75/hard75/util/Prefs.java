package com.hard75.hard75.util;

import android.content.Context;
import android.content.SharedPreferences;

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
}
