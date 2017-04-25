package com.yalin.style.event;

/**
 * @author jinyalin
 * @since 2017/4/25.
 */

public class SwitchingPhotosStateChangedEvent {
    private boolean mSwitchingPhotos;
    private int mId;

    public SwitchingPhotosStateChangedEvent(int id, boolean switchingPhotos) {
        mId = id;
        mSwitchingPhotos = switchingPhotos;
    }

    public int getCurrentId() {
        return mId;
    }

    public boolean isSwitchingPhotos() {
        return mSwitchingPhotos;
    }
}
