package com.yalin.style.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;

/**
 * @author jinyalin
 * @since 2017/5/2.
 */

public class Prefs {
    public static final String PREF_GREY_AMOUNT = "grey_amount";
    public static final String PREF_DIM_AMOUNT = "dim_amount";
    public static final String PREF_BLUR_AMOUNT = "blur_amount";
    public static final String PREF_DISABLE_BLUR_WHEN_LOCKED
            = "disable_blur_when_screen_locked_enabled";

    private static final String WALLPAPER_PREFERENCES_NAME = "wallpaper_preferences";
    private static final String PREF_MIGRATED = "migrated_from_default";

    public synchronized static SharedPreferences getSharedPreferences(Context context) {
        Context deviceProtectedContext =
                ContextCompat.createDeviceProtectedStorageContext(context);

        Context contextToUse = deviceProtectedContext != null ? deviceProtectedContext : context;
        return contextToUse.getSharedPreferences(WALLPAPER_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    private Prefs() {
    }
}
