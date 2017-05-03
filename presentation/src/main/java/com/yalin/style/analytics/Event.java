package com.yalin.style.analytics;

import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * @author jinyalin
 * @since 2017/5/3.
 */

public class Event extends FirebaseAnalytics.Event {
    public static final String SETTINGS_OPEN = "settings_open";
    public static final String WALLPAPER_CREATED = "wallpaper_created";
    public static final String WALLPAPER_DESTROYED = "wallpaper_destroyed";
    public static final String ACTIVATE = "activate";
    public static final String ABOUT_OPEN = "about_open";
}
