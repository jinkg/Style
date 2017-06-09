package com.yalin.style.data.repository.datasource

import android.content.ContentProviderOperation
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.text.TextUtils
import com.fernandocejas.arrow.checks.Preconditions
import com.yalin.style.data.cache.GalleryWallpaperCache
import com.yalin.style.data.entity.GalleryWallpaperEntity
import com.yalin.style.data.entity.WallpaperEntity
import com.yalin.style.data.exception.ReswitchException
import com.yalin.style.data.extensions.DelegateExt
import com.yalin.style.data.lock.OpenInputStreamLock
import com.yalin.style.data.repository.datasource.io.GalleryWallpapersHandler
import com.yalin.style.data.repository.datasource.provider.StyleContract
import com.yalin.style.data.repository.datasource.sync.gallery.GalleryScheduleService
import com.yalin.style.data.utils.getCacheFileForUri
import com.yalin.style.domain.GalleryWallpaper
import io.reactivex.Observable
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author jinyalin
 * @since 2017/5/22.
 */
class GalleryWallpaperDataStore(val context: Context,
                                val openInputStreamLock: OpenInputStreamLock,
                                val galleryWallpaperCache: GalleryWallpaperCache) :
        WallpaperDataStore {

    var currentGalleryWallpaperId: Long by DelegateExt.preferences(context,
            GalleryScheduleService.PREF_CURRENT_SHOW_WALLPAPER_ID, -1)

    override fun getWallPaperEntity(): Observable<WallpaperEntity> {
        return createEntitiesObservable().doOnNext(galleryWallpaperCache::put).map { entities ->
            return@map peekValid(entities)
        }
    }

    override fun switchWallPaperEntity(): Observable<WallpaperEntity> {
        if (openInputStreamLock.obtain()) {
            openInputStreamLock.release()
            return wallPaperEntity
        } else {
            return Observable.create<WallpaperEntity> {
                emitter ->
                emitter.onError(ReswitchException())
            }
        }
    }

    override fun openInputStream(wallpaperId: String?): Observable<InputStream> {
        Preconditions.checkArgument(!TextUtils.isEmpty(wallpaperId))
        return Observable.create<InputStream> { emitter ->
            var cursor: Cursor? = null
            try {
                openInputStreamLock.obtain()
                var inputStream: InputStream? = null
                if (DbWallpaperDataStore.DEFAULT_WALLPAPER_ID == wallpaperId) {
                    inputStream = context.assets.open("painterly-architectonic.jpg")
                } else {
                    cursor = context.contentResolver.query(
                            StyleContract.GalleryWallpaper.buildGalleryWallpaperUri(
                                    wallpaperId!!.toLong()), null, null, null, null)
                    if (cursor != null && cursor.moveToFirst()) {
                        val uriString = cursor.getString(cursor.getColumnIndex(
                                StyleContract.GalleryWallpaper.COLUMN_NAME_CUSTOM_URI))
                        val isTreeUri = cursor.getInt(cursor.getColumnIndex(
                                StyleContract.GalleryWallpaper.COLUMN_NAME_IS_TREE_URI)) == 1
                        if (isTreeUri) {
                            //todo
                        } else {
                            try {
                                inputStream = context.contentResolver.openInputStream(
                                        Uri.parse(uriString))
                            } catch (e: Exception) {
                                // if cached file exist then use cached file
                                val cacheFile = getCacheFileForUri(context, uriString)
                                if ((cacheFile != null && cacheFile.exists())) {
                                    inputStream = FileInputStream(cacheFile)
                                } else {
                                    throw IOException("Cannot open gallery uri : " + uriString)
                                }
                            }
                        }
                    }
                }
                emitter.onNext(inputStream)
                emitter.onComplete()
            } catch (e: IOException) {
                emitter.onError(e)
            } finally {
                cursor?.close()
                openInputStreamLock.release()
            }
        }
    }

    override fun getWallpaperCount(): Observable<Int> {
        return Observable.create { emitter ->
            val cachedCount = if (galleryWallpaperCache.isCached())
                galleryWallpaperCache.get()!!.size else 0

            emitter.onNext(cachedCount)
            emitter.onComplete()
        }
    }

    override fun likeWallpaper(wallpaperId: String?): Observable<Boolean> {
        throw UnsupportedOperationException("Gallery data store not support like.")
    }


    fun addGalleryWallpaperUris(uris: List<GalleryWallpaper>): Observable<Boolean> {
        return Observable.create<Boolean> { emitter ->
            galleryWallpaperCache.evictAll()
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
                GalleryScheduleService.publish(context)
            }
        }
    }

    fun getGalleryWallpaperUris(): Observable<List<GalleryWallpaperEntity>> {
        return Observable.create<List<GalleryWallpaperEntity>> { emitter ->
            val uris = ArrayList<GalleryWallpaperEntity>()
            var cursor: Cursor? = null
            try {
                cursor = context.contentResolver.query(StyleContract.GalleryWallpaper.CONTENT_URI,
                        null, null, null, null)
                while (cursor != null && cursor.moveToNext()) {
                    val item = GalleryWallpaperEntity()
                    item.id = cursor.getLong(cursor.getColumnIndex(
                            StyleContract.GalleryWallpaper._ID))
                    item.uri = cursor.getString(cursor.getColumnIndex(
                            StyleContract.GalleryWallpaper.COLUMN_NAME_CUSTOM_URI))
                    item.isTreeUri = cursor.getInt(cursor.getColumnIndex(
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

    private fun createEntitiesObservable(): Observable<List<GalleryWallpaperEntity>> {
        return Observable.create<List<GalleryWallpaperEntity>> { emitter ->
            var cursor: Cursor? = null
            val validWallpapers = ArrayList<GalleryWallpaperEntity>()
            try {
                val contentResolver = context.contentResolver
                cursor = contentResolver.query(StyleContract.GalleryWallpaper.CONTENT_URI,
                        null, null, null, null)
                validWallpapers.addAll(GalleryWallpaperEntity.readCursor(context, cursor))
            } finally {
                if (cursor != null) {
                    cursor.close()
                }
            }

            emitter.onNext(validWallpapers)
            emitter.onComplete()
        }
    }

    private fun peekValid(entities: List<GalleryWallpaperEntity>): WallpaperEntity {
        if (entities.isNotEmpty()) {
            if (currentGalleryWallpaperId == -1L) {
                val selectedEntity: GalleryWallpaperEntity
                if (entities.size == 1) {
                    selectedEntity = entities[0]
                    currentGalleryWallpaperId = selectedEntity.id
                } else {
                    val random = Random()
                    selectedEntity = entities[random.nextInt(entities.size)]
                    currentGalleryWallpaperId = selectedEntity.id
                }
                return switchToWallpaperEntity(selectedEntity)
            } else {
                entities.filter { it.id == currentGalleryWallpaperId }
                        .forEach { return switchToWallpaperEntity(it) }
            }
        }
        return DbWallpaperDataStore.buildDefaultWallpaper()
    }

    private fun switchToWallpaperEntity(galleryWallpaperEntity: GalleryWallpaperEntity)
            : WallpaperEntity {
        val wallpaperEntity = WallpaperEntity()
        wallpaperEntity.isDefault = false
        wallpaperEntity.canLike = false
        wallpaperEntity.title = "My Photos"
        wallpaperEntity.byline = "Get from your photos"
        wallpaperEntity.attribution = "kinglloy.com"
        wallpaperEntity.imageUri = galleryWallpaperEntity.uri
        wallpaperEntity.wallpaperId = galleryWallpaperEntity.id.toString()

        return wallpaperEntity
    }
}