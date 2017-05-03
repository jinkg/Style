package com.yalin.style.analytics;

import android.content.Context;

import com.flurry.android.FlurryAgent;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.yalin.style.BuildConfig;

/**
 * @author jinyalin
 * @since 2017/5/3.
 */

public class Analytics {
    public static void init(Context context) {
        new FlurryAgent.Builder().withLogEnabled(true).build(context, BuildConfig.FLURRY_API_KEY);
    }


    public static void setUserProperty(Context context, String key, String value) {
        FirebaseAnalytics.getInstance(context).setUserProperty(key, value);
    }

    public static void logEvent(Context context, String key, String value) {
        FirebaseAnalytics.getInstance(context).logEvent(key, null);
        FlurryAgent.logEvent(key);
    }
}
