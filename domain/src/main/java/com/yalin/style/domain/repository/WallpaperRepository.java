package com.yalin.style.domain.repository;

import com.yalin.style.domain.Wallpaper;
import com.yalin.style.domain.interactor.DefaultObserver;

import java.io.InputStream;

import io.reactivex.Observable;

/**
 * @author jinyalin
 * @since 2017/4/18.
 */

public interface WallpaperRepository {
    Observable<Wallpaper> getWallpaper();

    Observable<Wallpaper> switchWallpaper();

    Observable<InputStream> openInputStream(String wallpaperId);

    Observable<Integer> getWallpaperCount();

    Observable<Void> refreshWallpapers();

    void registerObserver(DefaultObserver<Void> observer);

    void unregisterObserver(DefaultObserver<Void> observer);
}
