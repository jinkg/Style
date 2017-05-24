package com.yalin.style.data.repository.datasource

import android.content.ContentProviderOperation
import android.content.Context
import android.database.Cursor
import com.yalin.style.data.entity.GalleryWallpaperEntity
import com.yalin.style.data.entity.WallpaperEntity
import com.yalin.style.data.lock.OpenInputStreamLock
import com.yalin.style.data.repository.datasource.io.GalleryWallpapersHandler
import com.yalin.style.data.repository.datasource.provider.StyleContract
import com.yalin.style.domain.GalleryWallpaper
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

    fun addCustomWallpaperUris(uris: Set<GalleryWallpaper>): Observable<Boolean> {
        return Observable.create<Boolean> { emitter ->
            var success = true
            try {
                val wallpaperHandler = GalleryWallpapersHandler(context, uris)
                val contentOperators = ArrayList<ContentProviderOperation>()
                wallpaperHandler.makeContentProviderOperations(contentOperators)
                if (contentOperators.size > 0) {
                    context.contentResolver.applyBatch(StyleContract.AUTHORITY, contentOperators)
                }
            } catch (e: Exception) {
                success = false
                emitter.onError(e)
            }
            if (success) {
                emitter.onNext(true)
                emitter.onComplete()
            }
        }
    }

    fun getCustomWallpaperUris(): Observable<Set<GalleryWallpaperEntity>> {
        return Observable.create<Set<GalleryWallpaperEntity>> { emitter ->
            val uris = HashSet<GalleryWallpaperEntity>()
            var cursor: Cursor? = null
            try {
                cursor = context.contentResolver.query(StyleContract.GalleryWallpaper.CONTENT_URI,
                        null, null, null, null)
                while (cursor != null && cursor.moveToNext()) {
                    val item = GalleryWallpaperEntity()
                    item.uri = cursor.getString(
                            cursor.getColumnIndex(
                                    StyleContract.GalleryWallpaper.COLUMN_NAME_CUSTOM_URI))
                    item.isTreeUri = cursor.getInt(
                            cursor.getColumnIndex(
                                    StyleContract.GalleryWallpaper.COLUMN_NAME_IS_TREE_URI)) == 1
                    uris.add(item)
                }
            } finally {
                cursor?.close()
            }

            emitter.onNext(uris)
            emitter.onComplete()
        }
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
        wallpaperEntity.canLike = false
        return wallpaperEntity
    }
}