package com.yalin.style.data.repository.datasource

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.yalin.style.data.cache.AdvanceWallpaperCache
import com.yalin.style.data.entity.AdvanceWallpaperEntity
import com.yalin.style.data.repository.datasource.provider.StyleContract
import com.yalin.style.data.utils.notifyChange
import io.reactivex.Observable

/**
 * @author jinyalin
 * @since 2017/7/28.
 */
class AdvanceWallpaperDataStoreImpl(val context: Context)
    : AdvanceWallpaperDataStore {
    companion object {
        val TAG = "AdvanceDataStore"

        val DEFAULT_WALLPAPER_ID = "-1"
    }

    override fun getWallPaperEntity(): AdvanceWallpaperEntity {
        var cursor: Cursor? = null
        var entity: AdvanceWallpaperEntity? = null
        try {
            val contentResolver = context.contentResolver
            cursor = contentResolver.query(StyleContract.AdvanceWallpaper.CONTENT_SELECTED_URI,
                    null, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                entity = AdvanceWallpaperEntity.readEntityFromCursor(cursor)
            }
        } finally {
            if (cursor != null) {
                cursor.close()
            }
        }
        if (entity == null) {
            entity = buildDefaultWallpaper()
        }
        return entity
    }

    override fun getAdvanceWallpapers(): Observable<List<AdvanceWallpaperEntity>> {
        return createAdvanceWallpapersFromDB()
    }

    override fun selectWallpaper(wallpaperId: String, tempSelect: Boolean): Observable<Boolean> {
        return Observable.create { emitter ->
            val selectValue = ContentValues()
            selectValue.put(StyleContract.AdvanceWallpaper.COLUMN_NAME_SELECTED, 1)
            val unselectedValue = ContentValues()
            unselectedValue.put(StyleContract.AdvanceWallpaper.COLUMN_NAME_SELECTED, 0)
            // unselected old
            context.contentResolver.update(
                    StyleContract.AdvanceWallpaper.CONTENT_SELECTED_URI,
                    unselectedValue, null, null)
            // select new
            val uri = StyleContract.AdvanceWallpaper.buildWallpaperUri(wallpaperId)
            val selectedCount = context.contentResolver.update(uri, selectValue, null, null)
            if (selectedCount > 0) {
                emitter.onNext(true)
            } else {
                emitter.onNext(false)
            }
            emitter.onComplete()
            notifyChange(context, StyleContract.AdvanceWallpaper.CONTENT_URI)
        }
    }

    private fun createAdvanceWallpapersFromDB(): Observable<List<AdvanceWallpaperEntity>> {
        return Observable.create { emitter ->
            var cursor: Cursor? = null
            val validWallpapers = ArrayList<AdvanceWallpaperEntity>()
            try {
                val contentResolver = context.contentResolver
                cursor = contentResolver.query(StyleContract.AdvanceWallpaper.CONTENT_URI,
                        null, null, null, null)
                validWallpapers.addAll(AdvanceWallpaperEntity.readCursor(cursor))
            } finally {
                if (cursor != null) {
                    cursor.close()
                }
            }

            emitter.onNext(validWallpapers)
            emitter.onComplete()
        }
    }

    private fun buildDefaultWallpaper(): AdvanceWallpaperEntity {
        val entity = AdvanceWallpaperEntity()
        entity.isDefault = true
        entity.id = -1
        entity.wallpaperId = DEFAULT_WALLPAPER_ID
        entity.author = "Yalin"
        entity.link = "kinglloy.com"
        entity.name = "Rainbow"

        return entity
    }
}