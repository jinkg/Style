package com.yalin.style.data.repository.datasource;

import android.content.Context;

import com.yalin.style.data.cache.SourcesCache;
import com.yalin.style.data.cache.WallpaperCache;
import com.yalin.style.data.entity.SourceEntity;
import com.yalin.style.data.entity.WallpaperEntity;

import com.yalin.style.data.exception.ReswitchException;
import com.yalin.style.data.lock.OpenInputStreamLock;

import java.io.InputStream;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * @author jinyalin
 * @since 2017/4/20.
 */

public class CacheWallpaperDataStore implements WallpaperDataStore {

    private final WallpaperCache wallpaperCache;
    private final SourcesCache sourcesCache;
    private final OpenInputStreamLock openInputStreamLock;

    @Inject
    public CacheWallpaperDataStore(WallpaperCache wallpaperCache,
                                   SourcesCache sourcesCache,
                                   OpenInputStreamLock openInputStreamLock) {
        this.wallpaperCache = wallpaperCache;
        this.sourcesCache = sourcesCache;
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

    @Override
    public Observable<List<SourceEntity>> getSources(Context context) {
        return sourcesCache.getSources(context);
    }
}
