package com.yalin.style.domain.interactor;

import com.yalin.style.domain.observable.WallpaperObservable;

import javax.inject.Inject;

/**
 * @author jinyalin
 * @since 2017/5/23.
 */

public class ObserverSources {

    private WallpaperObservable wallpaperObservable;

    @Inject
    public ObserverSources(WallpaperObservable wallpaperObservable) {
        this.wallpaperObservable = wallpaperObservable;
    }

    public void registerObserver(DefaultObserver<Void> observer) {
        wallpaperObservable.registerObserver(observer);
    }

    public void unregisterObserver(DefaultObserver<Void> observer) {
        wallpaperObservable.unregisterObserver(observer);
    }

}
