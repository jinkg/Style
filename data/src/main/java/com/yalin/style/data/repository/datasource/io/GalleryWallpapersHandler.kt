package com.yalin.style.data.repository.datasource.io

import android.content.ContentProviderOperation
import android.content.Context
import android.text.TextUtils
import com.google.gson.JsonElement
import com.yalin.style.data.repository.datasource.provider.StyleContract
import com.yalin.style.domain.GalleryWallpaper
import java.util.ArrayList

/**
 * @author jinyalin
 * @since 2017/5/24.
 */
class GalleryWallpapersHandler(val context: Context,
                               val uris: Set<GalleryWallpaper>) : JSONHandler(context) {
    override fun makeContentProviderOperations(list: ArrayList<ContentProviderOperation>) {
        val uri = StyleContract.GalleryWallpaper.CONTENT_URI
        for (wallpaperUri in uris) {
            if (TextUtils.isEmpty(wallpaperUri.uri)) {
                continue
            }
            val builder = ContentProviderOperation.newInsert(uri)
            builder.withValue(StyleContract.GalleryWallpaper.COLUMN_NAME_CUSTOM_URI,
                    wallpaperUri.uri)
            builder.withValue(StyleContract.GalleryWallpaper.COLUMN_NAME_IS_TREE_URI,
                    if (wallpaperUri.isTreeUri) 1 else 0)
            list.add(builder.build())
        }
    }

    override fun process(element: JsonElement) {

    }

}