package com.yalin.style.data.repository.datasource;


import android.content.Context;

import com.yalin.style.data.cache.WallpaperCache;

import com.yalin.style.data.lock.KeepWallpaperLock;
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
    private final OpenInputStreamLock openInputStreamLock;
    private final KeepWallpaperLock keepWallpaperLock;

    @Inject
    WallpaperDataStoreFactory(Context context, WallpaperCache wallpaperCache,
                              OpenInputStreamLock openInputStreamLock,
                              KeepWallpaperLock keepWallpaperLock) {
        this.context = context;
        this.wallpaperCache = wallpaperCache;
        this.openInputStreamLock = openInputStreamLock;
        this.keepWallpaperLock = keepWallpaperLock;
    }

    public WallpaperDataStore create() {
        WallpaperDataStore wallpaperDataStore;
        if (!wallpaperCache.isDirty() && wallpaperCache.isCached()) {
            wallpaperDataStore = new CacheWallpaperDataStore(wallpaperCache, openInputStreamLock);
        } else {
            wallpaperDataStore = createDbDataStore();
        }
        return wallpaperDataStore;
    }

    public WallpaperDataStore createDbDataStore() {
        return new DbWallpaperDataStore(context, wallpaperCache,
                openInputStreamLock, keepWallpaperLock);
    }

    public void onDataRefresh() {
        wallpaperCache.evictAll();
    }
}
