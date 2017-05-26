package com.yalin.style.data.utils

import android.content.Context
import android.net.Uri
import com.yalin.style.data.repository.datasource.provider.StyleContractHelper

/**
 * YaLin
 * On 2017/5/26.
 */
fun notifyChange(context: Context, uri: Uri) {
    if (!StyleContractHelper.isUriCalledFromSyncAdapter(uri)) {
        context.contentResolver.notifyChange(uri, null)
    }
}