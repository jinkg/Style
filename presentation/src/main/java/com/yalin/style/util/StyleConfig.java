package com.yalin.style.util;

import java.util.HashSet;
import java.util.Set;

/**
 * @author jinyalin
 * @since 2017/4/19.
 */

public class StyleConfig {

    public interface ActivateListener {
        void onStyleActivate();
    }

    private static boolean mStyleActive = false;
    private static Set<ActivateListener> mActivateListeners = new HashSet<>();

    public static boolean isStyleActive() {
        return mStyleActive;
    }

    public static void setStyleActive(boolean active) {
        mStyleActive = active;
        if (active) {
            for (ActivateListener listener : mActivateListeners) {
                listener.onStyleActivate();
            }
        }
    }

    public static void registerActivateListener(ActivateListener listener) {
        mActivateListeners.add(listener);
    }

    public static void unregisterActivateListener(ActivateListener listener) {
        mActivateListeners.remove(listener);
    }
}
