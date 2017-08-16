package com.yalin.style.view

import com.yalin.style.model.AdvanceWallpaperItem

/**
 * @author jinyalin
 * @since 2017/7/28.
 */
interface AdvanceSettingView : LoadingDataView {
    fun renderWallpapers(wallpapers: List<AdvanceWallpaperItem>)

    fun showEmpty()

    fun complete()

    fun wallpaperSelected(wallpaperId: String)

    fun showDownloadHintDialog(item: AdvanceWallpaperItem)

    fun showDownloadingDialog(item: AdvanceWallpaperItem)

    fun updateDownloadingProgress(downloaded: Long)

    fun downloadComplete(item: AdvanceWallpaperItem)

    fun showDownloadError(item: AdvanceWallpaperItem, e: Exception)

    fun showAd(item: AdvanceWallpaperItem)

    fun adViewed(item: AdvanceWallpaperItem)
}