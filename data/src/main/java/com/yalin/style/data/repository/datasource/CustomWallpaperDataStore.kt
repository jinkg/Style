package com.yalin.style.data.repository.datasource

import android.content.Context
import com.yalin.style.data.entity.WallpaperEntity
import com.yalin.style.data.lock.OpenInputStreamLock
import io.reactivex.Observable
import java.io.IOException
import java.io.InputStream

/**
 * @author jinyalin
 * @since 2017/5/22.
 */
class CustomWallpaperDataStore(val context: Context,
                               val openInputStreamLock: OpenInputStreamLock) : WallpaperDataStore {
    override fun getWallPaperEntity(): Observable<WallpaperEntity> {
        return createEntitiesObservable()
    }

    override fun switchWallPaperEntity(): Observable<WallpaperEntity> {
        throw IllegalAccessException("")
    }

    override fun openInputStream(wallpaperId: String?): Observable<InputStream> {
        return Observable.create<InputStream> { emitter ->
            try {
                openInputStreamLock.obtain()
                val inputStream = context.assets.open("painterly-architectonic.jpg")
                emitter.onNext(inputStream)
                emitter.onComplete()
            } catch (e: IOException) {
                emitter.onError(e)
            } finally {
                openInputStreamLock.release()
            }
        }
    }

    override fun getWallpaperCount(): Observable<Int> {
        return Observable.create { emitter ->
            emitter.onNext(1)
            emitter.onComplete()
        }
    }

    override fun likeWallpaper(wallpaperId: String?): Observable<Boolean> {
        throw IllegalAccessException("")
    }

    private fun createEntitiesObservable(): Observable<WallpaperEntity> {
        return Observable.create<WallpaperEntity> { emitter ->
            emitter.onNext(buildDefaultWallpaper())
            emitter.onComplete()
        }
    }

    private fun buildDefaultWallpaper(): WallpaperEntity {
        val wallpaperEntity = WallpaperEntity()
        wallpaperEntity.id = -1
        wallpaperEntity.attribution = "kinglloy.com"
        wallpaperEntity.byline = "Lyubov Popova222, 1918"
        wallpaperEntity.imageUri = "imageUri"
        wallpaperEntity.title = "Painterly Architectonic123"
        wallpaperEntity.wallpaperId = "-1"
        wallpaperEntity.liked = false
        wallpaperEntity.isDefault = true
        return wallpaperEntity
    }
}