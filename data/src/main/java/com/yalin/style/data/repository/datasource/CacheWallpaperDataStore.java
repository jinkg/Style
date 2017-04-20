package com.yalin.style.data.repository.datasource;

import com.yalin.style.data.cache.WallpaperCache;
import com.yalin.style.data.entity.WallpaperEntity;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * @author jinyalin
 * @since 2017/4/20.
 */

public class CacheWallpaperDataStore implements WallpaperDataStore {
    private final WallpaperCache wallpaperCache;

    @Inject
    public CacheWallpaperDataStore(WallpaperCache wallpaperCache) {
        this.wallpaperCache = wallpaperCache;
    }

    @Override
    public Observable<WallpaperEntity> getWallPaperEntity() {
        return wallpaperCache.get();
    }
}
