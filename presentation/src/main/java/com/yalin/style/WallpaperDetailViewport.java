/*
 * Copyright 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yalin.style;

import android.graphics.RectF;

import com.yalin.style.data.log.LogUtil;
import com.yalin.style.register.EventObservable;

// Singleton that also behaves as an event
public class WallpaperDetailViewport {
    private volatile RectF mViewport0 = new RectF();
    private volatile RectF mViewport1 = new RectF();
    private boolean mFromUser;

    private EventObservable<WallpaperDetailViewport> eventObservable = new EventObservable<>();

    private static WallpaperDetailViewport sInstance = new WallpaperDetailViewport();

    public static WallpaperDetailViewport getInstance() {
        return sInstance;
    }

    public static EventObservable<WallpaperDetailViewport> getEventObservable() {
        return getInstance().eventObservable;
    }

    private WallpaperDetailViewport() {
    }

    public RectF getViewport(int id) {
        return (id == 0) ? mViewport0 : mViewport1;
    }

    public void setViewport(int id, RectF viewport, boolean fromUser) {
        setViewport(id, viewport.left, viewport.top, viewport.right, viewport.bottom,
                fromUser);
    }

    public void setViewport(int id, float left, float top, float right, float bottom,
                            boolean fromUser) {
        mFromUser = fromUser;
        getViewport(id).set(left, top, right, bottom);
        eventObservable.notify(this);
    }

    public boolean isFromUser() {
        return mFromUser;
    }

    public WallpaperDetailViewport setDefaultViewport(int id, float bitmapAspectRatio,
                                                      float screenAspectRatio) {
        mFromUser = false;
        if (bitmapAspectRatio > screenAspectRatio) {
            getViewport(id).set(
                    0.5f - screenAspectRatio / bitmapAspectRatio / 2,
                    0,
                    0.5f + screenAspectRatio / bitmapAspectRatio / 2,
                    1);
        } else {
            getViewport(id).set(
                    0,
                    0.5f - bitmapAspectRatio / screenAspectRatio / 2,
                    1,
                    0.5f + bitmapAspectRatio / screenAspectRatio / 2);
        }
        return this;
    }
}
