package com.yalin.style.data.cache

import com.yalin.style.data.entity.GalleryWallpaperEntity
import javax.inject.Inject
import javax.inject.Singleton


/**
 * @author jinyalin
 * @since 2017/5/26.
 */
@Singleton
class GalleryWallpaperCache @Inject constructor() {
    private var wallpaperEntities: List<GalleryWallpaperEntity>? = null

    fun put(wallpaperEntities: List<GalleryWallpaperEntity>) {
        this.wallpaperEntities = wallpaperEntities
    }

    fun get() = wallpaperEntities

    fun isCached() = wallpaperEntities != null

    fun isDirty() = wallpaperEntities != null

    fun evictAll() {
        wallpaperEntities = null
    }
}
