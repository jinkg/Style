package com.yalin.style.data.repository.datasource;


import android.content.Context;

import com.yalin.style.data.cache.SourcesCache;
import com.yalin.style.data.cache.WallpaperCache;

import com.yalin.style.data.lock.LikeWallpaperLock;
import com.yalin.style.data.lock.OpenInputStreamLock;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author jinyalin
 * @since 2017/4/18.
 */

@Singleton
public class WallpaperDataStoreFactory {

    private final Context context;
    private final WallpaperCache wallpaperCache;
    private final SourcesCache sourcesCache;
    private final OpenInputStreamLock openInputStreamLock;
    private final LikeWallpaperLock likeWallpaperLock;

    @Inject
    WallpaperDataStoreFactory(Context context, WallpaperCache wallpaperCache,
                              SourcesCache sourcesCache,
                              OpenInputStreamLock openInputStreamLock,
                              LikeWallpaperLock likeWallpaperLock) {
        this.context = context;
        this.wallpaperCache = wallpaperCache;
        this.sourcesCache = sourcesCache;
        this.openInputStreamLock = openInputStreamLock;
        this.likeWallpaperLock = likeWallpaperLock;
    }

    public WallpaperDataStore create() {
        WallpaperDataStore wallpaperDataStore;
        if (sourcesCache.useCustomSource()) {
            wallpaperDataStore = new LocalDataStore(context);
        } else {
            if (!wallpaperCache.isDirty() && wallpaperCache.isCached()) {
                wallpaperDataStore =
                        new CacheWallpaperDataStore(wallpaperCache, sourcesCache, openInputStreamLock);
            } else {
                wallpaperDataStore = createDbDataStore();
            }
        }
        return wallpaperDataStore;
    }

    public WallpaperDataStore createDbDataStore() {
        if (sourcesCache.useCustomSource()) {
            return new LocalDataStore(context);
        } else {
            return new DbWallpaperDataStore(context, wallpaperCache,
                    openInputStreamLock, likeWallpaperLock);
        }
    }

    public void onDataRefresh() {
        wallpaperCache.evictAll();
    }
}
