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

    Observable<InputStream> openInputStream(String wallpaperId);

    void registerObserver(DefaultObserver<Void> observer);

    void unregisterObserver(DefaultObserver<Void> observer);
}
