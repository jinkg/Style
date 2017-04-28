package com.yalin.style.data.cache;

import com.fernandocejas.arrow.checks.Preconditions;
import com.yalin.style.data.entity.WallpaperEntity;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;

/**
 * @author jinyalin
 * @since 2017/4/20.
 */
@Singleton
public class WallpaperCacheImpl implements WallpaperCache {
    private WallpaperEntity wallpaperEntity;

    @Inject
    public WallpaperCacheImpl() {
    }

    @Override
    public Observable<WallpaperEntity> get() {
        Preconditions.checkNotNull(wallpaperEntity, "There is not cached wallpaper.");
        return Observable.create(emitter -> {
            emitter.onNext(new WallpaperEntity(wallpaperEntity));
            emitter.onComplete();
        });
    }

    @Override
    public int getCachedId() {
        Preconditions.checkNotNull(wallpaperEntity, "There is not cached wallpaper.");
        return wallpaperEntity.id;
    }

    @Override
    public void put(WallpaperEntity wallpaperEntity) {
        this.wallpaperEntity = wallpaperEntity;
    }

    @Override
    public boolean isCached() {
        return wallpaperEntity != null;
    }

    @Override
    public boolean isDirty() {
        return wallpaperEntity == null;
    }

    @Override
    public void evictAll() {
        wallpaperEntity = null;
    }
}
