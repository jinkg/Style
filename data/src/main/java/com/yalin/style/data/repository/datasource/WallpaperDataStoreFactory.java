package com.yalin.style.data.repository.datasource;


import android.content.Context;

import com.yalin.style.data.cache.WallpaperCache;

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

    @Inject
    WallpaperDataStoreFactory(Context context, WallpaperCache wallpaperCache) {
        this.context = context;
        this.wallpaperCache = wallpaperCache;
    }

    public WallpaperDataStore create() {
        WallpaperDataStore wallpaperDataStore;
        if (!wallpaperCache.isDirty() && wallpaperCache.isCached()) {
            wallpaperDataStore = new CacheWallpaperDataStore(wallpaperCache);
        } else {
            wallpaperDataStore = createDbDataStore();
        }
        return wallpaperDataStore;
    }

    public WallpaperDataStore createDbDataStore() {
        return new DbWallpaperDataStore(context, wallpaperCache);
    }

    public WallpaperDataStore createCloudDataStore() {
        return new CloudWallpaperDataStore(context, wallpaperCache);
    }

    public void onDataRefresh() {
        wallpaperCache.evictAll();
    }
}
