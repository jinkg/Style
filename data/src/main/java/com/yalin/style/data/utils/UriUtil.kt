package com.yalin.style.data.utils

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.SQLException
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.provider.DocumentsContract
import android.text.TextUtils
import com.yalin.style.data.log.LogUtil
import com.yalin.style.domain.GalleryWallpaper
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

/**
 * @author jinyalin
 * @since 2017/5/25.
 */

private val TAG = "UriUtil"

fun isTreeUri(possibleTreeUri: Uri): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        return DocumentsContract.isTreeUri(possibleTreeUri)
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        // Prior to N we can't directly check if the URI is a tree URI, so we have to just try it
        try {
            val treeDocumentId = DocumentsContract.getTreeDocumentId(possibleTreeUri)
            return !TextUtils.isEmpty(treeDocumentId)
        } catch (e: IllegalArgumentException) {
            // Definitely not a tree URI
            return false
        }

    }
    // No tree URIs prior to Lollipop
    return false
}

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
fun getImagesFromTreeUri(context: Context, treeUri: Uri, maxImages: Int): List<Uri> {
    val images = ArrayList<Uri>()
    val directories = LinkedList<String>()
    directories.add(DocumentsContract.getTreeDocumentId(treeUri))
    while (images.size < maxImages && !directories.isEmpty()) {
        val parentDocumentId = directories.poll()
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri,
                parentDocumentId)
        var children: Cursor?
        try {
            children = context.contentResolver.query(childrenUri,
                    arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                            DocumentsContract.Document.COLUMN_MIME_TYPE), null, null, null)
        } catch (e: SecurityException) {
            // No longer can read this URI, which means no images from this URI
            // This a temporary state as the next onLoadFinished() will remove this item entirely
            children = null
        }

        if (children == null) {
            continue
        }
        while (children.moveToNext()) {
            val documentId = children.getString(
                    children.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID))
            val mimeType = children.getString(
                    children.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE))
            if (DocumentsContract.Document.MIME_TYPE_DIR == mimeType) {
                directories.add(documentId)
            } else if (mimeType != null && mimeType.startsWith("image/")) {
                // Add images to the list
                images.add(DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId))
            }
            if (images.size == maxImages) {
                break
            }
        }
        children.close()
    }
    return images
}

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
fun getDisplayNameForTreeUri(context: Context, treeUri: Uri): String? {
    val documentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri,
            DocumentsContract.getTreeDocumentId(treeUri))
    var data: Cursor? = null
    try {
        data = context.contentResolver.query(documentUri,
                arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME), null, null, null)
    } catch (e: Throwable) {
        LogUtil.E(TAG, "getDisplayNameForTreeUri failed.", e)
    }
    var displayName: String? = null
    if (data != null && data.moveToNext()) {
        displayName = data.getString(
                data.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME))
    }
    data?.close()
    return displayName
}


fun processUriPermission(context: Context, galleryWallpaper: GalleryWallpaper) {
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

fun getCacheFileForUri(context: Context, imageUri: String): File? {
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