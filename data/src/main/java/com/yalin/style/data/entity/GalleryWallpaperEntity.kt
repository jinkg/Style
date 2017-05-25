package com.yalin.style.data.entity

import android.content.Context
import android.database.Cursor
import android.net.Uri
import com.yalin.style.data.log.LogUtil
import com.yalin.style.data.repository.datasource.provider.StyleContract
import java.util.ArrayList

/**
 * @author jinyalin
 * @since 2017/5/24.
 */
class GalleryWallpaperEntity {
    var id: Long = 0
    var uri: String? = null
    var isTreeUri: Boolean = false

    companion object {
        private val TAG = "GalleryWallpaperEntity"

        fun readCursor(context: Context, cursor: Cursor?): ArrayList<GalleryWallpaperEntity> {
            val validWallpapers = ArrayList<GalleryWallpaperEntity>(5)
            while (cursor != null && cursor.moveToNext()) {
                val entity = readEntityFromCursor(cursor)
                try {
                    // valid input stream
                    val inputStream = context.contentResolver.openInputStream(
                            Uri.parse(entity.uri))

                    validWallpapers.add(entity)
                    inputStream?.close()
                } catch (e: Exception) {
                    LogUtil.D(TAG, "File not found with gallery wallpaper uri : "
                            + entity.uri)
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

            return entity
        }
    }
}