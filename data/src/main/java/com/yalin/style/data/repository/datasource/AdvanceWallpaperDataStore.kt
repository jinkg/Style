package com.yalin.style.data.repository.datasource

import com.yalin.style.data.entity.AdvanceWallpaperEntity
import io.reactivex.Observable

/**
 * @author jinyalin
 * @since 2017/7/28.
 */
interface AdvanceWallpaperDataStore {

    fun getWallPaperEntity(): AdvanceWallpaperEntity

    fun getAdvanceWallpapers(): Observable<List<AdvanceWallpaperEntity>>

    fun selectWallpaper(wallpaperId: String): Observable<Boolean>
}