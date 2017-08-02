package com.yalin.style.data.repository.datasource

import android.content.ContentProviderOperation
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import android.text.format.DateUtils
import com.fernandocejas.arrow.checks.Preconditions
import com.yalin.style.data.R
import com.yalin.style.data.cache.GalleryWallpaperCache
import com.yalin.style.data.entity.GalleryWallpaperEntity
import com.yalin.style.data.entity.WallpaperEntity
import com.yalin.style.data.exception.ReswitchException
import com.yalin.style.data.extensions.DelegateExt
import com.yalin.style.data.lock.OpenInputStreamLock
import com.yalin.style.data.repository.datasource.io.GalleryWallpapersHandler
import com.yalin.style.data.repository.datasource.io.RemoveGalleryWallpapersHandler
import com.yalin.style.data.repository.datasource.provider.StyleContract
import com.yalin.style.data.repository.datasource.sync.gallery.GalleryScheduleService
import com.yalin.style.data.utils.getCacheFileForUri
import com.yalin.style.data.utils.getImagesFromTreeUri
import com.yalin.style.data.utils.notifyChange
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

    var rotateIntervalMin: Int by DelegateExt.preferences(context,
            GalleryScheduleService.PREF_ROTATE_INTERVAL_MIN,
            GalleryScheduleService.DEFAULT_ROTATE_INTERVAL_MIN)

    override fun getWallPaperEntity(): Observable<WallpaperEntity> {
        if (galleryWallpaperCache.isCached()) {
            val entities = galleryWallpaperCache.get()!!
            return Observable.create { emitter ->
                emitter.onNext(peekValid(entities))
                emitter.onComplete()
            }
        } else {
            return createEntitiesObservable().doOnNext(galleryWallpaperCache::put).map { entities ->
                return@map peekValid(entities)
            }
        }
    }

    override fun switchWallPaperEntity(): Observable<WallpaperEntity> {
        if (openInputStreamLock.obtain()) {
            openInputStreamLock.release()
            return doSwitch()
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

                        if (isTreeUri && Build.VERSION.SDK_INT >= 21) {
                            val images = getImagesFromTreeUri(context, Uri.parse(uriString),
                                    Int.MAX_VALUE)
                            inputStream = context.contentResolver.openInputStream(
                                    images[Random().nextInt(images.size)])
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
                emitter.onNext(inputStream!!)
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
            var totalCount: Int = 0
            if (galleryWallpaperCache.isCached()) {
                totalCount = galleryWallpaperCache.get()!!.size
            } else {
                var cursor: Cursor? = null
                try {
                    cursor = context.contentResolver.query(StyleContract.GalleryWallpaper.CONTENT_URI,
                            null, null, null, null)
                    if (cursor != null) {
                        totalCount = cursor.count
                    }
                } finally {
                    cursor?.close()
                }
            }

            emitter.onNext(totalCount)
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

    fun removeGalleryWallpaperUris(uris: List<GalleryWallpaper>): Observable<Boolean> {
        return Observable.create<Boolean> { emitter ->
            galleryWallpaperCache.evictAll()
            var success = true
            try {
                val removeHandler = RemoveGalleryWallpapersHandler(context, uris)
                val contentOperators = ArrayList<ContentProviderOperation>()
                removeHandler.makeContentProviderOperations(contentOperators)
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
        return createEntitiesObservable().doOnNext(galleryWallpaperCache::put)
    }

    fun forceNow(wallpaperUri: String): Observable<Boolean> {
        return Observable.create { emitter ->
            if (galleryWallpaperCache.isCached()) {
                for (entity in galleryWallpaperCache.get()!!) {
                    if (TextUtils.equals(entity.uri, wallpaperUri)) {
                        currentGalleryWallpaperId = entity.id
                        break
                    }
                }
            } else {
                var cursor: Cursor? = null
                try {
                    cursor = context.contentResolver.query(StyleContract.GalleryWallpaper.CONTENT_URI,
                            arrayOf(StyleContract.GalleryWallpaper._ID),
                            StyleContract.GalleryWallpaper.COLUMN_NAME_CUSTOM_URI + " = ? ",
                            arrayOf(wallpaperUri), null)
                    if (cursor != null && cursor.moveToFirst()) {
                        currentGalleryWallpaperId = cursor.getLong(0)
                    }
                } finally {
                    cursor?.close()
                }
            }
            emitter.onNext(true)
            emitter.onComplete()
            notifyChange(context, StyleContract.GalleryWallpaper.CONTENT_URI)
        }
    }

    fun setUpdateIntervalMin(intervalMin: Int): Observable<Boolean> {
        return Observable.create { emitter ->
            GalleryScheduleService.setInterval(context, intervalMin)
            emitter.onNext(true)
            emitter.onComplete()
        }
    }

    fun getUpdateIntervalMin(): Observable<Int> {
        return Observable.create { emitter ->
            emitter.onNext(rotateIntervalMin)
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
                cursor?.close()
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
        wallpaperEntity.title = getTitle(galleryWallpaperEntity.dateTime)
        wallpaperEntity.byline = getByline(galleryWallpaperEntity.location)
        wallpaperEntity.attribution = "kinglloy.com"
        wallpaperEntity.imageUri = galleryWallpaperEntity.uri
        wallpaperEntity.wallpaperId = galleryWallpaperEntity.id.toString()

        return wallpaperEntity
    }

    private fun getTitle(dateTime: Long): String {
        if (dateTime > 0)
            return DateUtils.formatDateTime(context, dateTime,
                    DateUtils.FORMAT_SHOW_DATE
                            or DateUtils.FORMAT_SHOW_YEAR
                            or DateUtils.FORMAT_SHOW_WEEKDAY)
        else return context.getString(R.string.gallery_from_gallery)
    }

    private fun getByline(location: String?): String {
        if (location.isNullOrEmpty())
            return context.getString(R.string.gallery_touch_to_view)
        else return location!!
    }

    private fun doSwitch(): Observable<WallpaperEntity> {
        if (galleryWallpaperCache.isCached()) {
            return Observable.create<WallpaperEntity> { emitter ->
                val entities = galleryWallpaperCache.get()
                emitter.onNext(peekOne(entities!!))
                emitter.onComplete()
            }
        } else {
            return createEntitiesObservable().doOnNext(galleryWallpaperCache::put)
                    .map { entities ->
                        return@map peekOne(entities)
                    }
        }
    }

    private fun peekOne(entities: List<GalleryWallpaperEntity>): WallpaperEntity {
        var selectedEntity: GalleryWallpaperEntity? = null
        if (entities.size > 1) {
            val random = Random()
            while (true) {
                selectedEntity = entities[random.nextInt(entities.size)]
                if (selectedEntity.id != currentGalleryWallpaperId) {
                    currentGalleryWallpaperId = selectedEntity.id
                    break
                }
            }
        } else if (entities.size == 1) {
            if (currentGalleryWallpaperId != entities[0].id) {
                currentGalleryWallpaperId = entities[0].id
            }
            selectedEntity = entities[0]
        } else {
            if (currentGalleryWallpaperId != -1L) {
                currentGalleryWallpaperId = -1
            }
        }
        if (selectedEntity != null) {
            return switchToWallpaperEntity(selectedEntity)
        }
        return DbWallpaperDataStore.buildDefaultWallpaper()
    }
}