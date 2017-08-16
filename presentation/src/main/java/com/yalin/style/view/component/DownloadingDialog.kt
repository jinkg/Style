package com.yalin.style.view.component

import android.content.Context
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.yalin.style.R
import com.yalin.style.model.AdvanceWallpaperItem

/**
 * @author jinyalin
 * *
 * @since 2017/8/11.
 */

class DownloadingDialog constructor(context: Context) {
    private val dialog = MaterialDialog.Builder(context)
            .iconRes(R.drawable.advance_downloading)
            .title(R.string.downloading)
            .cancelable(false)
            .customView(R.layout.dialog_downloading, false).build()

    private val progressView = dialog.findViewById(R.id.downloadProgress) as TextView

    fun show() {
        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
    }

    fun updateProgress(progress: Long) {
        progressView.text = formatSize(progress)
    }

    fun showError(item: AdvanceWallpaperItem, e: Exception) {

    }

    companion object {
        val C = 1024
    }

    private fun formatSize(progress: Long): String {
        if (progress < C) {
            return "$progress B"
        }
        if (progress < C * C) {
            return "${progress / C} KB"
        }
        return "%2f MB".format(progress / (C * C).toFloat())
    }
}
