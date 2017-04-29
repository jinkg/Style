package com.yalin.style.data.cache;

import com.fernandocejas.arrow.checks.Preconditions;
import com.yalin.style.data.entity.WallpaperEntity;

import java.util.Queue;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;

/**
 * @author jinyalin
 * @since 2017/4/20.
 */
@Singleton
public class WallpaperCacheImpl implements WallpaperCache {
    private Queue<WallpaperEntity> wallpaperEntities;

    @Inject
    public WallpaperCacheImpl() {
    }

    @Override
    public Observable<WallpaperEntity> get() {
        Preconditions.checkNotNull(wallpaperEntities, "There is not cached wallpaper.");
        Preconditions.checkArgument(!wallpaperEntities.isEmpty(), "There is not cached wallpaper.");
        return Observable.create(emitter -> {
            emitter.onNext(wallpaperEntities.peek());
            emitter.onComplete();
        });
    }

    @Override
    public Observable<WallpaperEntity> getNext() {
        Preconditions.checkNotNull(wallpaperEntities, "There is not cached wallpaper.");
        Preconditions.checkArgument(!wallpaperEntities.isEmpty(), "There is not cached wallpaper.");
        return Observable.create(emitter -> {
            WallpaperEntity entity = wallpaperEntities.poll();
            wallpaperEntities.offer(entity);
            emitter.onNext(wallpaperEntities.peek());
            emitter.onComplete();
        });
    }

    @Override
    public Observable<Integer> getWallpaperCount() {
        Preconditions.checkNotNull(wallpaperEntities, "There is not cached wallpaper.");
        return Observable.create(emitter -> {
            emitter.onNext(wallpaperEntities.size());
            emitter.onComplete();
        });
    }

    @Override
    public void put(Queue<WallpaperEntity> wallpaperEntities) {
        this.wallpaperEntities = wallpaperEntities;
    }

    @Override
    public boolean isCached() {
        return wallpaperEntities != null && !wallpaperEntities.isEmpty();
    }

    @Override
    public boolean isDirty() {
        return wallpaperEntities == null;
    }

    @Override
    public void evictAll() {
        if (wallpaperEntities != null) {
            wallpaperEntities.clear();
        }
        wallpaperEntities = null;
    }
}
