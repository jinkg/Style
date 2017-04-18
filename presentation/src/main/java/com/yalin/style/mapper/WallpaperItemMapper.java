package com.yalin.style.mapper;

import com.fernandocejas.arrow.checks.Preconditions;
import com.yalin.style.domain.Wallpaper;
import com.yalin.style.model.WallpaperItem;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author jinyalin
 * @since 2017/4/18.
 */
@Singleton
public class WallpaperItemMapper {
    @Inject
    public WallpaperItemMapper() {
    }

    public WallpaperItem transform(Wallpaper wallpaper) {
        Preconditions.checkNotNull(wallpaper, "Wallpaper can not be null.");
        WallpaperItem wallpaperItem = new WallpaperItem();
        wallpaperItem.title = wallpaper.title;
        wallpaperItem.attribution = wallpaper.attribution;
        wallpaperItem.byline = wallpaper.byline;
        wallpaperItem.imageUri = wallpaper.imageUri;
        wallpaperItem.wallpaperId = wallpaper.wallpaperId;
        wallpaperItem.inputStream = wallpaper.inputStream;
        return wallpaperItem;
    }
}
