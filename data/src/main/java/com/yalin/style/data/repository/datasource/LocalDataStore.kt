package com.yalin.style.data.repository.datasource

import android.content.Context
import com.yalin.style.data.entity.SourceEntity
import com.yalin.style.data.entity.WallpaperEntity
import io.reactivex.Observable
import java.io.InputStream

/**
 * @author jinyalin
 * @since 2017/5/22.
 */
class LocalDataStore(context: Context) : WallpaperDataStore {
    override fun getWallPaperEntity(): Observable<WallpaperEntity> {
        throw IllegalAccessException()
    }

    override fun switchWallPaperEntity(): Observable<WallpaperEntity> {
        throw IllegalAccessException()
    }

    override fun openInputStream(wallpaperId: String?): Observable<InputStream> {
        throw IllegalAccessException()
    }

    override fun getWallpaperCount(): Observable<Int> {
        throw IllegalAccessException()
    }

    override fun likeWallpaper(wallpaperId: String?): Observable<Boolean> {
        throw IllegalAccessException()
    }

    override fun getSources(context: Context?): Observable<MutableList<SourceEntity>> {
        throw IllegalAccessException()
    }

}