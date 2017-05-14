package com.yalin.style.view

import android.content.Intent

import com.yalin.style.model.WallpaperItem

/**
 * @author jinyalin
 * *
 * @since 2017/4/20.
 */

interface WallpaperDetailView : LoadingDataView {

    fun renderWallpaper(wallpaperItem: WallpaperItem)

    fun showNextButton(show: Boolean)

    fun shareWallpaper(shareIntent: Intent)

    fun validLikeAction(valid: Boolean)

    fun updateLikeState(item: WallpaperItem, liked: Boolean)
}
