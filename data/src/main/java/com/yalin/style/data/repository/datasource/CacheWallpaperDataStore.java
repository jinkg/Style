package com.yalin.style.data.repository.datasource;

import com.yalin.style.data.cache.WallpaperCache;
import com.yalin.style.data.entity.WallpaperEntity;

import java.io.InputStream;

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

    @Override
    public Observable<WallpaperEntity> switchWallPaperEntity() {
        throw new UnsupportedOperationException("Cache data store not support get entity.");
    }

    @Override
    public Observable<InputStream> openInputStream(String wallpaperId) {
        throw new UnsupportedOperationException("Cache data store not support open input stream.");
    }

    @Override
    public Observable<Integer> getWallpaperCount() {
        throw new UnsupportedOperationException("Cache data store not support get count.");
    }
}
