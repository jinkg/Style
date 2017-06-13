package com.yalin.style.data.repository.datasource.io

import android.annotation.SuppressLint
import android.content.ContentProviderOperation
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.support.media.ExifInterface
import android.text.TextUtils
import com.google.gson.JsonElement
import com.yalin.style.data.log.LogUtil
import com.yalin.style.data.repository.datasource.provider.StyleContract
import com.yalin.style.data.utils.isTreeUri
import com.yalin.style.data.utils.processUriPermission
import com.yalin.style.domain.GalleryWallpaper
import java.text.SimpleDateFormat
import java.util.ArrayList

/**
 * @author jinyalin
 * @since 2017/5/24.
 */
class GalleryWallpapersHandler(val context: Context,
                               val uris: List<GalleryWallpaper>) : JSONHandler(context) {

    companion object {
        private val TAG = "GalleryWallpapersHandler"

        @SuppressLint("SimpleDateFormat")
        private val sExifDateFormat = SimpleDateFormat("yyyy:MM:dd HH:mm:ss")
    }

    private var mGeocoder: Geocoder = Geocoder(context)

    override fun makeContentProviderOperations(list: ArrayList<ContentProviderOperation>) {
        val uri = StyleContract.GalleryWallpaper.CONTENT_URI
        for (wallpaperEntity in uris) {
            if (TextUtils.isEmpty(wallpaperEntity.uri)) {
                continue
            }
            wallpaperEntity.isTreeUri = isTreeUri(Uri.parse(wallpaperEntity.uri))
            processUriPermission(context, wallpaperEntity)

            val builder = ContentProviderOperation.newInsert(uri)
            builder.withValue(StyleContract.GalleryWallpaper.COLUMN_NAME_CUSTOM_URI,
                    wallpaperEntity.uri)
            builder.withValue(StyleContract.GalleryWallpaper.COLUMN_NAME_IS_TREE_URI,
                    if (wallpaperEntity.isTreeUri) 1 else 0)

            readMetaData(wallpaperEntity.uri, builder)
            list.add(builder.build())
        }
    }

    override fun process(element: JsonElement) {

    }

    private fun readMetaData(uriString: String, builder: ContentProviderOperation.Builder) {
        try {
            val uri = Uri.parse(uriString)
            var hasMetadata = false
            context.contentResolver.openInputStream(uri).use({ `in` ->
                if (`in` == null) {
                    return
                }
                val exifInterface = ExifInterface(`in`)
                val dateString = exifInterface.getAttribute(ExifInterface.TAG_DATETIME)
                if (!TextUtils.isEmpty(dateString)) {
                    val date = sExifDateFormat.parse(dateString)
                    builder.withValue(StyleContract.GalleryWallpaper.COLUMN_NAME_DATE_TIME,
                            date.time)
                    hasMetadata = true
                }

                val latlong = exifInterface.latLong
                if (latlong != null) {
                    // Reverse geocode
                    var addresses: List<Address>? = null
                    try {
                        addresses = mGeocoder.getFromLocation(latlong[0], latlong[1], 1)
                    } catch (e: IllegalArgumentException) {
                        LogUtil.E(TAG, "Invalid latitude/longitude, skipping location metadata", e)
                    }

                    if (addresses != null && addresses.isNotEmpty()) {
                        val addr = addresses[0]
                        val locality = addr.locality
                        val adminArea = addr.adminArea
                        val countryCode = addr.countryCode
                        val sb = StringBuilder()
                        if (!TextUtils.isEmpty(locality)) {
                            sb.append(locality)
                        }
                        if (!TextUtils.isEmpty(adminArea)) {
                            if (sb.isNotEmpty()) {
                                sb.append(", ")
                            }
                            sb.append(adminArea)
                        }
                        if (!TextUtils.isEmpty(countryCode)) {
                            if (sb.isNotEmpty()) {
                                sb.append(", ")
                            }
                            sb.append(countryCode)
                        }
                        builder.withValue(StyleContract.GalleryWallpaper.COLUMN_NAME_LOCATION,
                                sb.toString())
                        hasMetadata = true
                    }
                }
            })
            if (hasMetadata) {
                builder.withValue(StyleContract.GalleryWallpaper.COLUMN_NAME_HAS_METADATA,
                        if (hasMetadata) 1 else 0)
            }
        } catch (e: Exception) {
            LogUtil.E(TAG, "Couldn't read image metadata.", e)
        }
    }
}