package com.yalin.style.domain.interactor;

import com.yalin.style.domain.repository.WallpaperObservable;

import javax.inject.Inject;

/**
 * @author jinyalin
 * @since 2017/5/23.
 */

public class ObserverWallpaper {

    private WallpaperObservable wallpaperObservable;

    @Inject
    public ObserverWallpaper(WallpaperObservable wallpaperObservable) {
        this.wallpaperObservable = wallpaperObservable;
    }

    public void registerObserver(DefaultObserver<Void> observer) {
        wallpaperObservable.registerObserver(observer);
    }

    public void unregisterObserver(DefaultObserver<Void> observer) {
        wallpaperObservable.unregisterObserver(observer);
    }

}
