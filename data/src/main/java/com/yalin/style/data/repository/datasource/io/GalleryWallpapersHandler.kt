package com.yalin.style.data.repository.datasource.io

import android.content.ContentProviderOperation
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.SQLException
import android.net.Uri
import android.os.Binder
import android.provider.DocumentsContract
import android.text.TextUtils
import com.google.gson.JsonElement
import com.yalin.style.data.log.LogUtil
import com.yalin.style.data.repository.datasource.provider.StyleContract
import com.yalin.style.data.utils.isTreeUri
import com.yalin.style.domain.GalleryWallpaper
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.ArrayList

/**
 * @author jinyalin
 * @since 2017/5/24.
 */
class GalleryWallpapersHandler(val context: Context,
                               val uris: List<GalleryWallpaper>) : JSONHandler(context) {

    companion object {
        private val TAG = "GalleryWallpapersHandler"
    }

    override fun makeContentProviderOperations(list: ArrayList<ContentProviderOperation>) {
        val uri = StyleContract.GalleryWallpaper.CONTENT_URI
        for (wallpaperEntity in uris) {
            if (TextUtils.isEmpty(wallpaperEntity.uri)) {
                continue
            }
            wallpaperEntity.isTreeUri = isTreeUri(uri)
            processUriPermission(wallpaperEntity)

            val builder = ContentProviderOperation.newInsert(uri)
            builder.withValue(StyleContract.GalleryWallpaper.COLUMN_NAME_CUSTOM_URI,
                    wallpaperEntity.uri)
            builder.withValue(StyleContract.GalleryWallpaper.COLUMN_NAME_IS_TREE_URI,
                    if (wallpaperEntity.isTreeUri) 1 else 0)
            list.add(builder.build())
        }
    }

    override fun process(element: JsonElement) {

    }

    private fun processUriPermission(galleryWallpaper: GalleryWallpaper) {
        val uri = Uri.parse(galleryWallpaper.uri)
        if (galleryWallpaper.isTreeUri) {
            try {
                context.contentResolver.takePersistableUriPermission(uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: SecurityException) {
                // You can't persist URI permissions from your own app, so this fails.
                // We'll still have access to it directly
                LogUtil.E(TAG, "processUriPermission exception ", e)
            }
        } else {
            val haveUriPermission = context.checkUriPermission(uri,
                    Binder.getCallingPid(), Binder.getCallingUid(),
                    Intent.FLAG_GRANT_READ_URI_PERMISSION) == PackageManager.PERMISSION_GRANTED
            // If we only have permission to this URI via URI permissions (rather than directly,
            // such as if the URI is from our own app), it is from an external source and we need
            // to make sure to gain persistent access to the URI's content
            if (haveUriPermission) {
                var persistedPermission = false
                // Try to persist access to the URI, saving us from having to store a local copy
                if (DocumentsContract.isDocumentUri(context, uri)) {
                    try {
                        context.contentResolver.takePersistableUriPermission(uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        persistedPermission = true
                        // If we have a persisted URI permission, we don't need a local copy
                        val cachedFile = getCacheFileForUri(context, galleryWallpaper.uri)
                        if (cachedFile != null && cachedFile.exists()) {
                            if (!cachedFile.delete()) {
                                LogUtil.D(TAG, "Unable to delete " + cachedFile)
                            }
                        }
                    } catch (e: SecurityException) {
                        // If we don't have FLAG_GRANT_PERSISTABLE_URI_PERMISSION (such as when using ACTION_GET_CONTENT),
                        // this will fail. We'll need to make a local copy (handled below)
                        LogUtil.E(TAG, "processUriPermission exception ", e)
                    }

                }
                if (!persistedPermission) {
                    // We only need to make a local copy if we weren't able to persist the permission
                    try {
                        writeUriToFile(context, galleryWallpaper.uri,
                                getCacheFileForUri(context, galleryWallpaper.uri))
                    } catch (e: IOException) {
                        LogUtil.E(TAG, "Error downloading gallery image "
                                + galleryWallpaper.uri, e)
                        throw SQLException("Error downloading gallery image "
                                + galleryWallpaper.uri, e)
                    }

                }
            } else {
                // On API 25 and lower, we don't get URI permissions to URIs
                // from our own package so we manage those URI permissions manually
                val resolver = context.contentResolver
                try {
                    resolver.call(uri, "takePersistableUriPermission",
                            uri.toString(), null)
                } catch (e: Exception) {
                    LogUtil.E(TAG, "Unable to manually persist uri permissions to " + uri, e)
                }
            }
        }
    }

    private fun writeUriToFile(context: Context?, uri: String, destFile: File?) {
        if (context == null) {
            return
        }
        if (destFile == null) {
            throw IOException("Invalid destination for " + uri)
        }
        try {
            val input = context.contentResolver.openInputStream(Uri.parse(uri)) ?: return
            val fileOutput = FileOutputStream(destFile)

            val buffer = ByteArray(1024)
            var bytesRead = input.read(buffer)

            while (bytesRead > 0) {
                fileOutput.write(buffer, 0, bytesRead)
                bytesRead = input.read(buffer)
            }
            fileOutput.flush()
        } catch (e: SecurityException) {
            throw IOException("Unable to read Uri: " + uri, e)
        }

    }

    private fun getCacheFileForUri(context: Context, imageUri: String): File? {
        val directory = File(context.getExternalFilesDir(null), "gallery_images")
        if (!directory.exists() && !directory.mkdirs()) {
            return null
        }

        // Create a unique filename based on the imageUri
        val uri = Uri.parse(imageUri)
        val filename = StringBuilder()
        filename.append(uri.scheme).append("_")
                .append(uri.host).append("_")
        var encodedPath = uri.encodedPath
        if (!TextUtils.isEmpty(encodedPath)) {
            val length = encodedPath.length
            if (length > 60) {
                encodedPath = encodedPath.substring(length - 60)
            }
            encodedPath = encodedPath.replace('/', '_')
            filename.append(encodedPath).append("_")
        }
        try {
            val md = MessageDigest.getInstance("MD5")
            md.update(uri.toString().toByteArray(charset("UTF-8")))
            val digest = md.digest()
            for (b in digest) {
                if (0xff and b.toInt() < 0x10) {
                    filename.append("0").append(Integer.toHexString(0xFF and b.toInt()))
                } else {
                    filename.append(Integer.toHexString(0xFF and b.toInt()))
                }
            }
        } catch (e: NoSuchAlgorithmException) {
            filename.append(uri.toString().hashCode())
        } catch (e: UnsupportedEncodingException) {
            filename.append(uri.toString().hashCode())
        }

        return File(directory, filename.toString())
    }
}