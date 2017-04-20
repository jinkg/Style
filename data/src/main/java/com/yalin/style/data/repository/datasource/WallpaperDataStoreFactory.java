package com.yalin.style.data.repository.datasource;


import android.content.Context;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author jinyalin
 * @since 2017/4/18.
 */

@Singleton
public class WallpaperDataStoreFactory {
    private Context context;

    @Inject
    DbWallpaperDataStore dbWallpaperDataStore;
    @Inject
    CacheWallpaperDataStore cacheWallpaperDataStore;

    @Inject
    WallpaperDataStoreFactory(Context context) {
        this.context = context;
    }

    public WallpaperDataStore create() {
        return dbWallpaperDataStore;
    }

    public WallpaperDataStore createDbDataStore() {
        return dbWallpaperDataStore;
    }

    public void invalidCache() {
        cacheWallpaperDataStore.clearCache();
    }
}
