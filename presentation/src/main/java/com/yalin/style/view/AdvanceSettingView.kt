package com.yalin.style.view

import com.yalin.style.model.AdvanceWallpaperItem

/**
 * @author jinyalin
 * @since 2017/7/28.
 */
interface AdvanceSettingView : LoadingDataView {
    fun renderWallpapers(wallpapers: List<AdvanceWallpaperItem>)

    fun showEmpty()

    fun executeDelay(runnable: Runnable, ms: Long)

    fun complete()

    fun wallpaperSelected(wallpaperId: String)
}