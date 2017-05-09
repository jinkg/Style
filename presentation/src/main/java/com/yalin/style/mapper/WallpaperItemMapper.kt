package com.yalin.style.mapper

import com.fernandocejas.arrow.checks.Preconditions
import com.yalin.style.domain.Wallpaper
import com.yalin.style.model.WallpaperItem

import javax.inject.Inject

/**
 * @author jinyalin
 * *
 * @since 2017/4/18.
 */
class WallpaperItemMapper @Inject
constructor() {

    fun transform(wallpaper: Wallpaper): WallpaperItem {
        Preconditions.checkNotNull(wallpaper, "Wallpaper can not be null.")
        val wallpaperItem = WallpaperItem()
        wallpaperItem.title = wallpaper.title
        wallpaperItem.attribution = wallpaper.attribution
        wallpaperItem.byline = wallpaper.byline
        wallpaperItem.imageUri = wallpaper.imageUri
        wallpaperItem.wallpaperId = wallpaper.wallpaperId
        wallpaperItem.liked = wallpaper.liked
        wallpaperItem.isDefault = wallpaper.isDefault
        return wallpaperItem
    }
}
