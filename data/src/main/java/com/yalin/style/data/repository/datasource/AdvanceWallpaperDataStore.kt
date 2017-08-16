package com.yalin.style.data.repository.datasource

import com.yalin.style.data.entity.AdvanceWallpaperEntity
import io.reactivex.Observable

/**
 * @author jinyalin
 * @since 2017/7/28.
 */
interface AdvanceWallpaperDataStore {

    fun getWallpaperEntity(): AdvanceWallpaperEntity

    fun getAdvanceWallpapers(): Observable<List<AdvanceWallpaperEntity>>

    fun selectWallpaper(wallpaperId: String, tempSelect: Boolean): Observable<Boolean>

    fun downloadWallpaper(wallpaperId: String): Observable<Long>

    fun readAd(wallpaperId: String): Observable<Boolean>
}