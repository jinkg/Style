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

    private final WallpaperCache wallpaperCache;
    private final SourcesDataStoreImpl sourcesDataStore;

    @Inject
    WallpaperDataStoreFactory(Context context, WallpaperCache wallpaperCache,
                              SourcesCache sourcesCache,
                              OpenInputStreamLock openInputStreamLock,
                              LikeWallpaperLock likeWallpaperLock) {
        this.wallpaperCache = wallpaperCache;
        this.sourcesDataStore = new SourcesDataStoreImpl(context,
                sourcesCache, wallpaperCache, openInputStreamLock, likeWallpaperLock);
    }

    public WallpaperDataStore create() {
        return sourcesDataStore.getWallpaperDataStore();
    }

    public WallpaperDataStore createDbDataStore() {
        return sourcesDataStore.getDbWallpaperDataStore();
    }

    public void onDataRefresh() {
        wallpaperCache.evictAll();
    }
}
