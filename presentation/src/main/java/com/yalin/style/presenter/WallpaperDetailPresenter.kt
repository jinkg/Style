package com.yalin.style.presenter

import android.content.Intent
import android.os.Bundle

import com.yalin.style.analytics.Analytics
import com.yalin.style.analytics.Event
import com.yalin.style.data.exception.ReswitchException
import com.yalin.style.domain.Wallpaper
import com.yalin.style.domain.interactor.DefaultObserver
import com.yalin.style.domain.interactor.GetWallpaper
import com.yalin.style.domain.interactor.GetWallpaperCount
import com.yalin.style.domain.interactor.LikeWallpaper
import com.yalin.style.domain.interactor.SwitchWallpaper
import com.yalin.style.event.WallpaperSwitchEvent
import com.yalin.style.exception.ErrorMessageFactory
import com.yalin.style.injection.PerActivity
import com.yalin.style.mapper.WallpaperItemMapper
import com.yalin.style.model.WallpaperItem
import com.yalin.style.view.WallpaperDetailView

import org.greenrobot.eventbus.EventBus

import javax.inject.Inject

/**
 * @author jinyalin
 * *
 * @since 2017/4/20.
 */
@PerActivity
class WallpaperDetailPresenter @Inject
constructor(private val getWallpaperUseCase: GetWallpaper,
            private val getWallpaperCountUseCase: GetWallpaperCount,
            private val switchWallpaperUseCase: SwitchWallpaper,
            private val likeWallpaperUseCase: LikeWallpaper,
            private val wallpaperItemMapper: WallpaperItemMapper) : Presenter {

    companion object {
        private val CURRENT_ITEM = "current_item"
        private val WALLPAPER_COUNT = "wallpaper_count"
    }

    private var currentShowItem: WallpaperItem? = null
    private var wallpaperCount: Int = 0

    private var wallpaperDetailView: WallpaperDetailView? = null

    private val wallpaperRefreshObserver: WallpaperRefreshObserver

    init {
        wallpaperRefreshObserver = WallpaperRefreshObserver()
        getWallpaperUseCase.registerObserver(wallpaperRefreshObserver)
    }

    fun setView(wallpaperDetailView: WallpaperDetailView) {
        this.wallpaperDetailView = wallpaperDetailView
    }

    fun initialize() {
        getWallpaperUseCase.execute(WallpaperObserver(), null)
        getWallpaperCountUseCase.execute(WallpaperCountObserver(), null)
    }

    fun getNextWallpaper() {
        switchWallpaperUseCase.execute(WallpaperObserver(true), null)
    }

    fun likeWallpaper() {
        if (currentShowItem == null) {
            return
        }
        likeWallpaperUseCase.execute(WallpaperLikeObserver(),
                LikeWallpaper.Params.likeWallpaper(currentShowItem!!.wallpaperId))

        Analytics.logEvent(wallpaperDetailView!!.context(),
                if (!currentShowItem!!.liked) Event.LIKE
                else Event.UN_LIKE, currentShowItem!!.title)
    }

    fun shareWallpaper() {
        if (currentShowItem == null) {
            return
        }
        val detailUrl = "www.kinglloy.com"
        val artist = currentShowItem!!.
                byline.replaceFirst("\\.\\s*($|\\n).*".toRegex(), "").trim { it <= ' ' }

        var shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, "My Android wallpaper today is '"
                + currentShowItem!!.title.trim { it <= ' ' }
                + "' by " + artist
                + ". #StyleWallpaper\n\n"
                + detailUrl)
        shareIntent = Intent.createChooser(shareIntent, "Share wallpaper")
        wallpaperDetailView!!.shareWallpaper(shareIntent)
    }

    fun restoreInstanceState(instanceState: Bundle?) {
        if (instanceState != null && instanceState.containsKey(CURRENT_ITEM)) {
            currentShowItem = instanceState.getParcelable<WallpaperItem>(CURRENT_ITEM)
            wallpaperCount = instanceState.getInt(WALLPAPER_COUNT)
            showWallpaperDetailInView(currentShowItem)
            showOrHideNextView(wallpaperCount)
        } else {
            initialize()
        }
    }

    fun saveInstanceState(outState: Bundle) {
        if (currentShowItem != null) {
            outState.putParcelable(CURRENT_ITEM, currentShowItem)
            outState.putInt(WALLPAPER_COUNT, wallpaperCount)
        }
    }

    override fun resume() {

    }

    override fun pause() {

    }

    override fun destroy() {
        getWallpaperUseCase.dispose()
        getWallpaperUseCase.unregisterObserver(wallpaperRefreshObserver)
        wallpaperDetailView = null
    }

    private fun showWallpaperDetailInView(wallpaperItem: WallpaperItem?) {
        wallpaperDetailView!!.renderWallpaper(wallpaperItem)
        wallpaperDetailView!!.validLikeAction(!wallpaperItem!!.isDefault)
        if (!wallpaperItem.isDefault) {
            wallpaperDetailView!!.updateLikeState(wallpaperItem, wallpaperItem.liked)
        }
    }

    private fun showOrHideNextView(count: Int) {
        wallpaperDetailView!!.showNextButton(count > 1)
    }

    private inner class WallpaperObserver @JvmOverloads
    constructor(private val isSwitch: Boolean = false) : DefaultObserver<Wallpaper>() {

        override fun onNext(wallpaper: Wallpaper) {
            val wallpaperItem = wallpaperItemMapper.transform(wallpaper)
            currentShowItem = wallpaperItem
            showWallpaperDetailInView(wallpaperItem)
        }

        override fun onComplete() {
            if (isSwitch) {
                EventBus.getDefault().post(WallpaperSwitchEvent())
            }
        }

        override fun onError(exception: Throwable) {
            if (exception is ReswitchException) {
                return
            }
            wallpaperDetailView!!
                    .showError(ErrorMessageFactory.create(wallpaperDetailView!!.context(),
                            exception as Exception))
        }
    }

    private inner class WallpaperCountObserver : DefaultObserver<Int>() {

        override fun onNext(count: Int?) {
            wallpaperCount = count!!
            showOrHideNextView(count)
        }

        override fun onComplete() {}

        override fun onError(exception: Throwable) {
            showOrHideNextView(0)
        }
    }

    private inner class WallpaperLikeObserver : DefaultObserver<Boolean>() {

        override fun onNext(liked: Boolean?) {
            wallpaperDetailView!!.updateLikeState(currentShowItem, liked!!)
        }
    }

    private inner class WallpaperRefreshObserver : DefaultObserver<Void>() {
        override fun onComplete() {
            initialize()
        }
    }

}
