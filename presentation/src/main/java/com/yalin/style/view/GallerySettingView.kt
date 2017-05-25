package com.yalin.style.view

import com.yalin.style.model.GalleryWallpaperItem

/**
 * @author jinyalin
 * @since 2017/5/25.
 */
interface GallerySettingView : LoadingDataView {
    fun renderGalleryWallpapers(wallpaperItems: List<GalleryWallpaperItem>)
}