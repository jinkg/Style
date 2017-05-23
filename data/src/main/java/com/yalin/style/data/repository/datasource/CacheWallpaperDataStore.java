package com.yalin.style.data.repository.datasource;

import com.yalin.style.data.cache.WallpaperCache;
import com.yalin.style.data.entity.WallpaperEntity;

import com.yalin.style.data.exception.ReswitchException;
import com.yalin.style.data.lock.OpenInputStreamLock;

import java.io.InputStream;

import io.reactivex.Observable;

/**
 * @author jinyalin
 * @since 2017/4/20.
 */

public class CacheWallpaperDataStore implements WallpaperDataStore {

    private final WallpaperCache wallpaperCache;
    private final OpenInputStreamLock openInputStreamLock;

    public CacheWallpaperDataStore(WallpaperCache wallpaperCache,
                                   OpenInputStreamLock openInputStreamLock) {
        this.wallpaperCache = wallpaperCache;
        this.openInputStreamLock = openInputStreamLock;
    }

    @Override
    public Observable<WallpaperEntity> getWallPaperEntity() {
        return wallpaperCache.get();
    }

    @Override
    public Observable<WallpaperEntity> switchWallPaperEntity() {
        if (openInputStreamLock.obtain()) {
            openInputStreamLock.release();
            return wallpaperCache.getNext();
        } else {
            return Observable.create(emitter ->
                    emitter.onError(new ReswitchException()));
        }
    }

    @Override
    public Observable<InputStream> openInputStream(String wallpaperId) {
        throw new UnsupportedOperationException("Cache data store not support open input stream.");
    }

    @Override
    public Observable<Integer> getWallpaperCount() {
        return wallpaperCache.getWallpaperCount();
    }

    @Override
    public Observable<Boolean> likeWallpaper(String wallpaperId) {
        throw new UnsupportedOperationException("Cache data store not support open input stream.");
    }
}
