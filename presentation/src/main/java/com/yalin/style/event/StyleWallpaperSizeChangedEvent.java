package com.yalin.style.event;

/**
 * @author jinyalin
 * @since 2017/4/25.
 */

public class StyleWallpaperSizeChangedEvent {
    private int mWidth;
    private int mHeight;

    public StyleWallpaperSizeChangedEvent(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }
}
