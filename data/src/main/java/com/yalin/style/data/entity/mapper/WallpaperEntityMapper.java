package com.yalin.style.data.entity.mapper;

import com.fernandocejas.arrow.checks.Preconditions;
import com.yalin.style.data.entity.WallpaperEntity;
import com.yalin.style.domain.Wallpaper;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author jinyalin
 * @since 2017/4/18.
 */
@Singleton
public class WallpaperEntityMapper {

  @Inject
  public WallpaperEntityMapper() {
  }

  public Wallpaper transform(WallpaperEntity wallpaperEntity) {
    Preconditions.checkNotNull(wallpaperEntity, "Wallpaper can not be null.");
    Wallpaper wallpaper = new Wallpaper();
    wallpaper.title = wallpaperEntity.title;
    wallpaper.attribution = wallpaperEntity.attribution;
    wallpaper.byline = wallpaperEntity.byline;
    wallpaper.imageUri = wallpaperEntity.imageUri;
    wallpaper.wallpaperId = wallpaperEntity.wallpaperId;
    wallpaper.liked = wallpaperEntity.liked;
    wallpaper.isDefault = wallpaperEntity.isDefault;
    return wallpaper;
  }
}
