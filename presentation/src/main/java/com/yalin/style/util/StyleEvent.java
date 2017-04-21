package com.yalin.style.util;

import com.yalin.style.event.WallpaperActivateEvent;
import com.yalin.style.event.WallpaperDetailOpenedEvent;
import com.yalin.style.register.EventObservable;

/**
 * @author jinyalin
 * @since 2017/4/19.
 */

public class StyleEvent {

    private static boolean mStyleActive = false;

    private static EventObservable<WallpaperActivateEvent> activateEventObservable
            = new EventObservable<>();
    private static EventObservable<WallpaperDetailOpenedEvent> detailObservable
            = new EventObservable<>();

    public static boolean isStyleActive() {
        return mStyleActive;
    }

    public static void notifyStyleActive(boolean active) {
        mStyleActive = active;
        WallpaperActivateEvent activateEvent = new WallpaperActivateEvent(active);
        activateEventObservable.notify(activateEvent);
    }

    public static EventObservable<WallpaperActivateEvent> getActivateEventObservable() {
        return activateEventObservable;
    }

    public static EventObservable<WallpaperDetailOpenedEvent> getDetailObservable() {
        return detailObservable;
    }
}
