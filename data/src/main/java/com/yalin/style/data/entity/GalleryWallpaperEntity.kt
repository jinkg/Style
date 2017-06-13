package com.yalin.style.data.entity

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import com.yalin.style.data.log.LogUtil
import com.yalin.style.data.repository.datasource.provider.StyleContract
import com.yalin.style.data.utils.getCacheFileForUri
import com.yalin.style.data.utils.getImagesFromTreeUri
import java.io.InputStream
import java.util.ArrayList

/**
 * @author jinyalin
 * @since 2017/5/24.
 */
class GalleryWallpaperEntity {
    var id: Long = 0
    var uri: String? = null
    var isTreeUri: Boolean = false
    var dateTime: Long = 0
    var location: String? = null
    var hasMetadata: Boolean = false

    companion object {
        private val TAG = "GalleryWallpaperEntity"

        fun readCursor(context: Context, cursor: Cursor?): ArrayList<GalleryWallpaperEntity> {
            val validWallpapers = ArrayList<GalleryWallpaperEntity>(5)
            while (cursor != null && cursor.moveToNext()) {
                val entity = readEntityFromCursor(cursor)
                var inputStream: InputStream? = null
                try {
                    // valid input stream
                    if (!entity.isTreeUri) {
                        inputStream = context.contentResolver.openInputStream(
                                Uri.parse(entity.uri))
                    }
                    validWallpapers.add(entity)
                } catch (e: Exception) {
                    LogUtil.D(TAG, "Cannot open inputStream for uri : "
                            + entity.uri)
                    val cacheFile = getCacheFileForUri(context, entity.uri!!)
                    if ((cacheFile != null && cacheFile.exists())) {
                        // has cache file
                        validWallpapers.add(entity)
                    }
                } finally {
                    inputStream?.close()
                }
            }
            return validWallpapers;
        }

        fun readEntityFromCursor(cursor: Cursor): GalleryWallpaperEntity {
            val entity = GalleryWallpaperEntity()

            entity.id = cursor.getLong(cursor.getColumnIndex(
                    StyleContract.GalleryWallpaper._ID))
            entity.uri = cursor.getString(cursor.getColumnIndex(
                    StyleContract.GalleryWallpaper.COLUMN_NAME_CUSTOM_URI))
            entity.isTreeUri = cursor.getInt(cursor.getColumnIndex(
                    StyleContract.GalleryWallpaper.COLUMN_NAME_IS_TREE_URI)) == 1
            entity.dateTime = cursor.getLong(cursor.getColumnIndex(
                    StyleContract.GalleryWallpaper.COLUMN_NAME_DATE_TIME))
            entity.location = cursor.getString(cursor.getColumnIndex(
                    StyleContract.GalleryWallpaper.COLUMN_NAME_LOCATION))
            entity.hasMetadata = cursor.getInt(cursor.getColumnIndex(
                    StyleContract.GalleryWallpaper.COLUMN_NAME_HAS_METADATA)) == 1
            return entity
        }
    }
}