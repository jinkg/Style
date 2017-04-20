package com.yalin.style.data.cache;

import com.yalin.style.data.entity.WallpaperEntity;

import io.reactivex.Observable;

/**
 * @author jinyalin
 * @since 2017/4/20.
 */

public interface WallpaperCache {
    Observable<WallpaperEntity> get();

    void put(WallpaperEntity wallpaperEntity);

    boolean isCached();

    boolean isDirty();

    void evictAll();
}
