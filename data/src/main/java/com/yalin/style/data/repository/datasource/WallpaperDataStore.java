package com.yalin.style.data.repository.datasource;

import com.yalin.style.data.entity.WallpaperEntity;

import io.reactivex.Observable;

/**
 * @author jinyalin
 * @since 2017/4/18.
 */

public interface WallpaperDataStore {
    Observable<WallpaperEntity> getWallPaperEntity();
}
