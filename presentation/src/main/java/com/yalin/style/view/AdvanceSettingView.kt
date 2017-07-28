package com.yalin.style.view

import com.yalin.style.model.AdvanceWallpaperItem

/**
 * @author jinyalin
 * @since 2017/7/28.
 */
interface AdvanceSettingView : LoadingDataView {
    fun renderWallpapers(wallpapers: List<AdvanceWallpaperItem>)

    fun showDownloading()

    fun showEmpty()
}