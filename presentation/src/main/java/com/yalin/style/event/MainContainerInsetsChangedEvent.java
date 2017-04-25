package com.yalin.style.event;

import android.graphics.Rect;

/**
 * @author jinyalin
 * @since 2017/4/25.
 */

public class MainContainerInsetsChangedEvent {
    private Rect insets;

    public MainContainerInsetsChangedEvent(Rect insets) {
        this.insets = insets;
    }

    public Rect getInsets() {
        return insets;
    }
}
