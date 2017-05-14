package com.yalin.style.util

import android.content.Context
import android.content.Intent
import com.yalin.style.R
import com.yalin.style.model.WallpaperItem

/**
 * @author jinyalin
 * @since 2017/5/14.
 */
class ShareUtil {
    companion object {
        fun getShareString(context: Context, wallpaperItem: WallpaperItem) = with(wallpaperItem) {
            val detailUrl = "www.kinglloy.com"
            val artist = byline.replaceFirst("\\.\\s*($|\\n).*".toRegex(), "").trim()

            val result = String.format(
                    context.getString(R.string.share_text), title, artist, detailUrl)
            result
        }

        fun createShareIntent(context: Context, wallpaperItem: WallpaperItem): Intent {
            var shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, ShareUtil.Companion
                    .getShareString(context, wallpaperItem))
            shareIntent = Intent.createChooser(shareIntent,
                    context.getString(R.string.share_title))
            return shareIntent;
        }
    }
}