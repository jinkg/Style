package com.yalin.style.presenter

import com.yalin.style.domain.AdvanceWallpaper
import com.yalin.style.domain.interactor.DefaultObserver
import com.yalin.style.domain.interactor.GetAdvanceWallpapers
import com.yalin.style.domain.interactor.LoadAdvanceWallpaper
import com.yalin.style.exception.ErrorMessageFactory
import com.yalin.style.mapper.AdvanceWallpaperItemMapper
import com.yalin.style.view.AdvanceSettingView
import java.lang.Exception
import java.net.SocketTimeoutException
import javax.inject.Inject

/**
 * @author jinyalin
 * @since 2017/7/28.
 */
class AdvanceSettingPresenter
@Inject constructor(val getAdvanceWallpapers: GetAdvanceWallpapers,
                    val loadAdvanceWallpaper: LoadAdvanceWallpaper,
                    val advanceWallpaperItemMapper: AdvanceWallpaperItemMapper)
    : Presenter {

    private val wallpaperObserver = WallpapersObserver()
    private var view: AdvanceSettingView? = null

    fun setView(view: AdvanceSettingView) {
        this.view = view
    }

    fun initialize() {
        getAdvanceWallpapers.execute(wallpaperObserver, null)
    }

    fun loadAdvanceWallpaper() {
        view?.showLoading()
        loadAdvanceWallpaper.execute(object : DefaultObserver<List<AdvanceWallpaper>>() {
            override fun onNext(wallpapers: List<AdvanceWallpaper>) {
                view?.renderWallpapers(advanceWallpaperItemMapper.transformList(wallpapers))
            }

            override fun onComplete() {

            }

            override fun onError(exception: Throwable) {
                view?.showError(
                        ErrorMessageFactory.create(view!!.context(), exception as Exception))
                view?.showRetry()
            }
        }, null)
    }

    override fun resume() {

    }

    override fun pause() {

    }

    override fun destroy() {
        getAdvanceWallpapers.dispose()
        view = null
    }

    private inner class WallpapersObserver : DefaultObserver<List<AdvanceWallpaper>>() {
        override fun onNext(wallpapers: List<AdvanceWallpaper>) {
            if (wallpapers.isEmpty()) {
                view?.showEmpty()
            } else {
                view?.renderWallpapers(advanceWallpaperItemMapper.transformList(wallpapers))
            }
        }

        override fun onComplete() {
        }

        override fun onError(exception: Throwable?) {
        }
    }
}