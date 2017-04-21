package com.yalin.style.event;

/**
 * @author jinyalin
 * @since 2017/4/21.
 */

public class WallpaperDetailOpenedEvent {
    private final boolean wallpaperDetailOpened;

    public WallpaperDetailOpenedEvent(boolean wallpaperDetailOpened) {
        this.wallpaperDetailOpened = wallpaperDetailOpened;
    }

    public boolean isWallpaperDetailOpened() {
        return wallpaperDetailOpened;
    }
}
