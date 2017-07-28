package com.yalin.style.data.repository.datasource

import android.content.Context
import android.database.Cursor
import com.yalin.style.data.entity.AdvanceWallpaperEntity
import com.yalin.style.data.repository.datasource.provider.StyleContract
import io.reactivex.Observable

/**
 * @author jinyalin
 * @since 2017/7/28.
 */
class AdvanceWallpaperDataStoreImpl(val context: Context) : AdvanceWallpaperDataStore {
    companion object {
        val TAG = "AdvanceDataStore"
    }

    override fun getWallPaperEntity(): AdvanceWallpaperEntity {
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
        return validWallpapers[0]
    }

    override fun getAdvanceWallpapers(): Observable<List<AdvanceWallpaperEntity>> {
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

}