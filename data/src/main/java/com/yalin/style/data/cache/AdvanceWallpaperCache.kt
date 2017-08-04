package com.yalin.style.data.cache

import com.yalin.style.data.entity.AdvanceWallpaperEntity

/**
 * @author jinyalin
 * @since 2017/8/4.
 */
interface AdvanceWallpaperCache {
    fun put(wallpapers: List<AdvanceWallpaperEntity>)

    fun getSelectedWallpaper(): AdvanceWallpaperEntity

    fun selectWallpaper(wallpaperId: String)

    fun getWallpapers(): List<AdvanceWallpaperEntity>

    fun evictAll()

    fun isCached(wallpaperId: String): Boolean

    fun isDirty(): Boolean

    fun makeDirty()
}