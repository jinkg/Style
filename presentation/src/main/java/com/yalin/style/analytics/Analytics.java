package com.yalin.style.analytics;

import android.content.Context;

import com.flurry.android.FlurryAgent;
import com.yalin.style.BuildConfig;
import com.yalin.style.data.log.LogUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jinyalin
 * @since 2017/5/3.
 */

public class Analytics {
    public static void init(Context context) {
        new FlurryAgent.Builder().withLogEnabled(LogUtil.LOG_ENABLE)
                .build(context, BuildConfig.FLURRY_API_KEY);
    }


    public static void setUserProperty(Context context, String key, String value) {

    }

    public static void logEvent(Context context, String event) {
        FlurryAgent.logEvent(event);
    }

    public static void logEvent(Context context, String event, String... params) {
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put(event, params[0]);
        FlurryAgent.logEvent(event, paramsMap);
    }
}
