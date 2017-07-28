package com.yalin.style.presenter

import com.yalin.style.domain.AdvanceWallpaper
import com.yalin.style.domain.interactor.DefaultObserver
import com.yalin.style.domain.interactor.GetAdvanceWallpapers
import com.yalin.style.view.AdvanceSettingView
import javax.inject.Inject

/**
 * @author jinyalin
 * @since 2017/7/28.
 */
class AdvanceSettingPresenter @Inject constructor(val getAdvanceWallpapers: GetAdvanceWallpapers)
    : Presenter {

    private val wallpaperObserver = WallpapersObserver()
    private var view: AdvanceSettingView? = null

    fun setView(view: AdvanceSettingView) {
        this.view = view
    }

    fun initialize() {
        getAdvanceWallpapers.execute(wallpaperObserver, null)
    }

    override fun resume() {

    }

    override fun pause() {

    }

    override fun destroy() {
        getAdvanceWallpapers.dispose()
    }

    private class WallpapersObserver : DefaultObserver<List<AdvanceWallpaper>>() {
        override fun onNext(t: List<AdvanceWallpaper>) {
        }

        override fun onComplete() {
        }

        override fun onError(exception: Throwable?) {
        }
    }
}