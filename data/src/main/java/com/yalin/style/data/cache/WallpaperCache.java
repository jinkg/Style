package com.yalin.style.data.cache;

import com.yalin.style.data.entity.WallpaperEntity;

import java.util.Queue;

import io.reactivex.Observable;

/**
 * @author jinyalin
 * @since 2017/4/20.
 */

public interface WallpaperCache {

  /**
   * return the first
   *
   * @return wallpaperEntity
   */
  Observable<WallpaperEntity> get();

  Observable<WallpaperEntity> getNext();

  Observable<Integer> getWallpaperCount();

  void likeWallpaper(String wallpaperId);

  void put(Queue<WallpaperEntity> wallpaperEntities);

  boolean isCached();

  boolean isCached(String wallpaperId);

  boolean isDirty();

  void evictAll();
}
