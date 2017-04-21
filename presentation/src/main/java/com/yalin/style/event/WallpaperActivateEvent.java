package com.yalin.style.event;

/**
 * @author jinyalin
 * @since 2017/4/21.
 */

public class WallpaperActivateEvent {
    private final boolean wallpaperActivate;

    public WallpaperActivateEvent(boolean wallpaperActivate) {
        this.wallpaperActivate = wallpaperActivate;
    }

    public boolean isWallpaperActivate() {
        return wallpaperActivate;
    }
}
