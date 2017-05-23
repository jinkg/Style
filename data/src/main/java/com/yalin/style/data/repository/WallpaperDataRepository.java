package com.yalin.style.data.repository;

import android.content.Context;
import android.text.TextUtils;

import com.fernandocejas.arrow.checks.Preconditions;
import com.yalin.style.data.entity.mapper.WallpaperEntityMapper;
import com.yalin.style.data.repository.datasource.WallpaperDataStore;
import com.yalin.style.data.repository.datasource.WallpaperDataStoreFactory;
import com.yalin.style.data.repository.datasource.sync.SyncHelper;
import com.yalin.style.data.repository.datasource.sync.account.Account;
import com.yalin.style.domain.Wallpaper;
import com.yalin.style.domain.repository.WallpaperRepository;


import java.io.InputStream;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;

/**
 * @author jinyalin
 * @since 2017/4/18.
 */
@Singleton
public class WallpaperDataRepository implements WallpaperRepository {

    private final Context context;
    private final WallpaperDataStoreFactory wallpaperDataStoreFactory;
    private final WallpaperEntityMapper wallpaperEntityMapper;

    @Inject
    WallpaperDataRepository(Context context,
                            WallpaperDataStoreFactory wallpaperDataStoreFactory,
                            WallpaperEntityMapper wallpaperEntityMapper) {
        this.context = context;
        this.wallpaperDataStoreFactory = wallpaperDataStoreFactory;
        this.wallpaperEntityMapper = wallpaperEntityMapper;

        Account.createSyncAccount(context);
        SyncHelper.updateSyncInterval(context);
    }


    @Override
    public Observable<Wallpaper> getWallpaper() {
        final WallpaperDataStore dataStore = wallpaperDataStoreFactory.create();
        return dataStore.getWallPaperEntity().map(wallpaperEntityMapper::transform);
    }

    @Override
    public Observable<Wallpaper> switchWallpaper() {
        final WallpaperDataStore dataStore = wallpaperDataStoreFactory.create();
        return dataStore.switchWallPaperEntity().map(wallpaperEntityMapper::transform);
    }

    @Override
    public Observable<InputStream> openInputStream(String wallpaperId) {
        Preconditions.checkArgument(!TextUtils.isEmpty(wallpaperId), "WallpaperId cannot be null");
        final WallpaperDataStore dataStore = wallpaperDataStoreFactory.createDbDataStore();
        return dataStore.openInputStream(wallpaperId);
    }

    @Override
    public Observable<Integer> getWallpaperCount() {
        final WallpaperDataStore dataStore = wallpaperDataStoreFactory.create();
        return dataStore.getWallpaperCount();
    }

    @Override
    public Observable<Boolean> likeWallpaper(String wallpaperId) {
        Preconditions.checkArgument(!TextUtils.isEmpty(wallpaperId), "WallpaperId cannot be null");
        final WallpaperDataStore dataStore = wallpaperDataStoreFactory.createDbDataStore();
        return dataStore.likeWallpaper(wallpaperId);
    }

}
