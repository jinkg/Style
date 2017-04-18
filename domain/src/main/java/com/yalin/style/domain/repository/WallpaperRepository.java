package com.yalin.style.domain.repository;

import com.yalin.style.domain.Wallpaper;
import com.yalin.style.domain.interactor.DefaultObserver;

import io.reactivex.Observable;

/**
 * @author jinyalin
 * @since 2017/4/18.
 */

public interface WallpaperRepository {
    Observable<Wallpaper> getWallpaper();

    void registerObserver(DefaultObserver<Wallpaper> observer);

    void unregisterObserver(DefaultObserver<Wallpaper> observer);
}
