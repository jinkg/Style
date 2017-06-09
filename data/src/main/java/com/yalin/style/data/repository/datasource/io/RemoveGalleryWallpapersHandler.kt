package com.yalin.style.data.repository.datasource.io

import android.content.ContentProviderOperation
import android.content.Context
import android.text.TextUtils
import com.google.gson.JsonElement
import com.yalin.style.data.repository.datasource.provider.StyleContract
import com.yalin.style.domain.GalleryWallpaper
import java.util.ArrayList

/**
 * YaLin
 * On 2017/5/26.
 */

class RemoveGalleryWallpapersHandler(val context: Context,
                                     val uris: List<GalleryWallpaper>) : JSONHandler(context) {

    override fun makeContentProviderOperations(list: ArrayList<ContentProviderOperation>) {
        uris.filterNot { TextUtils.isEmpty(it.uri) }
                .map {
                    StyleContract.GalleryWallpaper
                            .buildGalleryWallpaperDeleteUri(it.uri)
                }
                .map { ContentProviderOperation.newDelete(it) }
                .mapTo(list) { it.build() }
    }

    override fun process(element: JsonElement) {

    }
}
