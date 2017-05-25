package com.yalin.style.data.repository;

import android.content.Context;
import android.text.TextUtils;

import com.fernandocejas.arrow.checks.Preconditions;
import com.yalin.style.data.entity.mapper.WallpaperEntityMapper;
import com.yalin.style.data.repository.datasource.WallpaperDataStore;
import com.yalin.style.data.repository.datasource.StyleWallpaperDataStoreFactory;
import com.yalin.style.data.repository.datasource.sync.SyncHelper;
import com.yalin.style.data.repository.datasource.sync.account.Account;
import com.yalin.style.domain.GalleryWallpaper;
import com.yalin.style.domain.Wallpaper;
import com.yalin.style.domain.repository.WallpaperRepository;


import java.io.InputStream;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;

/**
 * @author jinyalin
 * @since 2017/4/18.
 */
@Singleton
public class StyleWallpaperDataRepository implements WallpaperRepository {

    private final Context context;
    private final StyleWallpaperDataStoreFactory styleWallpaperDataStoreFactory;
    private final WallpaperEntityMapper wallpaperEntityMapper;

    @Inject
    StyleWallpaperDataRepository(Context context,
                                 StyleWallpaperDataStoreFactory styleWallpaperDataStoreFactory,
                                 WallpaperEntityMapper wallpaperEntityMapper) {
        this.context = context;
        this.styleWallpaperDataStoreFactory = styleWallpaperDataStoreFactory;
        this.wallpaperEntityMapper = wallpaperEntityMapper;

        Account.createSyncAccount(context);
        SyncHelper.updateSyncInterval(context);
    }

    @Override
    public Observable<Wallpaper> getWallpaper() {
        final WallpaperDataStore dataStore = styleWallpaperDataStoreFactory.create();
        return dataStore.getWallPaperEntity().map(wallpaperEntityMapper::transform);
    }

    @Override
    public Observable<Wallpaper> switchWallpaper() {
        final WallpaperDataStore dataStore = styleWallpaperDataStoreFactory.create();
        return dataStore.switchWallPaperEntity().map(wallpaperEntityMapper::transform);
    }

    @Override
    public Observable<InputStream> openInputStream(String wallpaperId) {
        Preconditions.checkArgument(!TextUtils.isEmpty(wallpaperId), "WallpaperId cannot be null");
        final WallpaperDataStore dataStore = styleWallpaperDataStoreFactory.createDbDataStore();
        return dataStore.openInputStream(wallpaperId);
    }

    @Override
    public Observable<Integer> getWallpaperCount() {
        final WallpaperDataStore dataStore = styleWallpaperDataStoreFactory.create();
        return dataStore.getWallpaperCount();
    }

    @Override
    public Observable<Boolean> likeWallpaper(String wallpaperId) {
        Preconditions.checkArgument(!TextUtils.isEmpty(wallpaperId), "WallpaperId cannot be null");
        final WallpaperDataStore dataStore = styleWallpaperDataStoreFactory.createDbDataStore();
        return dataStore.likeWallpaper(wallpaperId);
    }

    @Override
    public Observable<Boolean> addCustomWallpaperUris(List<GalleryWallpaper> uris) {
        return Observable.create(emitter ->
                emitter.onError(new IllegalStateException(
                        "StyleWallpaperRepository can not add custom wallpaper."))
        );
    }

    @Override
    public Observable<List<GalleryWallpaper>> getGalleryWallpapers() {
        return Observable.create(emitter ->
                emitter.onError(new IllegalStateException(
                        "StyleWallpaperRepository have not custom wallpapers."))
        );
    }

}
