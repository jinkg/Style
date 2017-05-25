package com.yalin.style.data.utils

import android.annotation.TargetApi
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.text.TextUtils
import com.yalin.style.data.log.LogUtil
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