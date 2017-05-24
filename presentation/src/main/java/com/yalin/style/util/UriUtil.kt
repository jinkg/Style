package com.yalin.style.util

import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.text.TextUtils

/**
 * @author jinyalin
 * @since 2017/5/24.
 */
class UriUtil {
    companion object {
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
    }
}