package com.yalin.style.util;

import android.content.Context;
import android.graphics.Typeface;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jinyalin
 * @since 2017/4/21.
 */

public class TypefaceUtil {
    private static final Map<String, Typeface> sTypefaceCache = new HashMap<>();

    public static Typeface getAndCache(Context context, String assetPath) {
        synchronized (sTypefaceCache) {
            if (!sTypefaceCache.containsKey(assetPath)) {
                Typeface tf = Typeface.createFromAsset(
                        context.getApplicationContext().getAssets(), assetPath);
                sTypefaceCache.put(assetPath, tf);
            }
            return sTypefaceCache.get(assetPath);
        }
    }
}
