package com.yalin.style.data.repository;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

import com.yalin.style.data.entity.mapper.WallpaperEntityMapper;
import com.yalin.style.data.repository.datasource.WallpaperDataStore;
import com.yalin.style.data.repository.datasource.WallpaperDataStoreFactory;
import com.yalin.style.data.repository.datasource.provider.StyleContract;
import com.yalin.style.domain.Wallpaper;
import com.yalin.style.domain.interactor.DefaultObserver;
import com.yalin.style.domain.repository.WallpaperRepository;


import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;

/**
 * @author jinyalin
 * @since 2017/4/18.
 */
@Singleton
public class WallpaperDataRepository implements WallpaperRepository {

    private final Set<DefaultObserver<Wallpaper>> mObserverSet = new HashSet<>();
    private final ContentObserver mContentObserver;

    private final WallpaperDataStoreFactory wallpaperDataStoreFactory;
    private final WallpaperEntityMapper wallpaperEntityMapper;

    @Inject
    public WallpaperDataRepository(Context context,
                                   WallpaperDataStoreFactory wallpaperDataStoreFactory,
                                   WallpaperEntityMapper wallpaperEntityMapper) {
        this.wallpaperDataStoreFactory = wallpaperDataStoreFactory;
        this.wallpaperEntityMapper = wallpaperEntityMapper;
        mContentObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                notifyObserver();
            }
        };
        context.getContentResolver().registerContentObserver(StyleContract.Wallpaper.CONTENT_URI,
                true, mContentObserver);
    }

    private void notifyObserver() {
        Wallpaper wallpaper = new Wallpaper();
        for (DefaultObserver<Wallpaper> defaultObserver : mObserverSet) {
            defaultObserver.onNext(wallpaper);
            defaultObserver.onComplete();
        }
    }

    @Override
    public Observable<Wallpaper> getWallpaper() {
        final WallpaperDataStore dataStore = wallpaperDataStoreFactory.create();
        return dataStore.getWallPaperEntity().map(wallpaperEntityMapper::transform);
    }

    @Override
    public void registerObserver(DefaultObserver<Wallpaper> observer) {
        mObserverSet.add(observer);
    }

    @Override
    public void unregisterObserver(DefaultObserver<Wallpaper> observer) {
        mObserverSet.remove(observer);
    }

}
