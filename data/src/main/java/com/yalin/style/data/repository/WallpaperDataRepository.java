package com.yalin.style.data.repository;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

import com.yalin.style.data.entity.mapper.WallpaperEntityMapper;
import com.yalin.style.data.log.LogUtil;
import com.yalin.style.data.repository.datasource.WallpaperDataStore;
import com.yalin.style.data.repository.datasource.WallpaperDataStoreFactory;
import com.yalin.style.data.repository.datasource.provider.StyleContract;
import com.yalin.style.data.repository.datasource.sync.SyncHelper;
import com.yalin.style.data.repository.datasource.sync.account.Account;
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
    private static final String TAG = "WallpaperDataRepository";

    private final Set<DefaultObserver<Void>> mObserverSet = new HashSet<>();
    private final ContentObserver mContentObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            LogUtil.D(TAG, "Wallpaper data changed notify observer to reload.");
            wallpaperDataStoreFactory.onDataRefresh();
            notifyObserver();
        }
    };

    private final Context context;
    private final WallpaperDataStoreFactory wallpaperDataStoreFactory;
    private final WallpaperEntityMapper wallpaperEntityMapper;

    private boolean observerRegistered = false;

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

    private void notifyObserver() {
        for (DefaultObserver<Void> observer : mObserverSet) {
            observer.onNext(null);
            observer.onComplete();
        }
    }

    @Override
    public Observable<Wallpaper> getWallpaper() {
        final WallpaperDataStore dataStore = wallpaperDataStoreFactory.create();
        return dataStore.getWallPaperEntity().map(wallpaperEntityMapper::transform);
    }

    @Override
    public void registerObserver(DefaultObserver<Void> observer) {
        synchronized (mObserverSet) {
            mObserverSet.add(observer);
            if (!observerRegistered) {
                context.getContentResolver()
                        .registerContentObserver(StyleContract.Wallpaper.CONTENT_URI,
                                true, mContentObserver);
                observerRegistered = true;
            }
        }
    }

    @Override
    public void unregisterObserver(DefaultObserver<Void> observer) {
        synchronized (mObserverSet) {
            mObserverSet.remove(observer);
            if (mObserverSet.isEmpty()) {
                context.getContentResolver().unregisterContentObserver(mContentObserver);
                observerRegistered = false;
            }
        }
    }

}
