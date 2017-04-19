package com.yalin.style.util;

/**
 * @author jinyalin
 * @since 2017/4/19.
 */

public class StyleConfig {
    private static boolean mStyleActive = false;

    public static boolean isStyleActive() {
        return mStyleActive;
    }

    public static void setStyleActive(boolean active) {
        mStyleActive = active;
    }
}
