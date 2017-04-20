package com.yalin.style.data.repository.datasource;

import com.fernandocejas.arrow.checks.Preconditions;
import com.yalin.style.data.entity.WallpaperEntity;

import java.io.InputStream;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * @author jinyalin
 * @since 2017/4/20.
 */

public class CacheWallpaperDataStore implements WallpaperDataStore {
    private WallpaperEntity cachedEntity;

    private boolean dirty = false;

    @Inject
    public CacheWallpaperDataStore() {
    }

    @Override
    public Observable<WallpaperEntity> getWallPaperEntity() {
        Preconditions.checkNotNull(cachedEntity, "There is no cached entity.");
        return Observable.create(emitter -> {
            emitter.onNext(cachedEntity);
            emitter.onComplete();
        });
    }

    @Override
    public Observable<InputStream> openWallpaperInputStream(String id) {
        throw new UnsupportedOperationException(
                "Cache data store not support this open operation.");
    }

    public void setWallpaperEntity(WallpaperEntity wallpaperEntity) {
        cachedEntity = wallpaperEntity;
        dirty = false;
    }

    public boolean isDirty() {
        return dirty;
    }

    public boolean isCached() {
        return cachedEntity != null;
    }

    public void clearCache() {
        dirty = true;
        cachedEntity = null;
    }
}
