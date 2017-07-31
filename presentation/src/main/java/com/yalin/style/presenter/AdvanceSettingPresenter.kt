package com.yalin.style.presenter

import android.text.TextUtils
import com.yalin.style.data.log.LogUtil
import com.yalin.style.domain.AdvanceWallpaper
import com.yalin.style.domain.interactor.*
import com.yalin.style.event.SwitchWallpaperServiceEvent
import com.yalin.style.event.WallpaperActivateEvent
import com.yalin.style.exception.ErrorMessageFactory
import com.yalin.style.mapper.AdvanceWallpaperItemMapper
import com.yalin.style.view.AdvanceSettingView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.lang.Exception
import javax.inject.Inject

/**
 * @author jinyalin
 * @since 2017/7/28.
 */
class AdvanceSettingPresenter
@Inject constructor(val getAdvanceWallpapers: GetAdvanceWallpapers,
                    val loadAdvanceWallpaper: LoadAdvanceWallpaper,
                    val selectAdvanceWallpaper: SelectAdvanceWallpaper,
                    val getSelectedAdvanceWallpaper: GetSelectedAdvanceWallpaper,
                    val advanceWallpaperItemMapper: AdvanceWallpaperItemMapper)
    : Presenter {

    private val wallpaperObserver = WallpapersObserver()
    private var view: AdvanceSettingView? = null

    private var mLastSelectedItemId: String? = null

    private var selecting = false

    fun setView(view: AdvanceSettingView) {
        this.view = view
    }

    fun initialize() {
        getAdvanceWallpapers.execute(wallpaperObserver, null)
        mLastSelectedItemId = getSelectedAdvanceWallpaper.selected.wallpaperId
        EventBus.getDefault().register(this)
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

    fun selectAdvanceWallpaper(wallpaperId: String, rollback: Boolean) {
        if (!rollback && TextUtils.equals(wallpaperId, mLastSelectedItemId)) {
            view?.complete()
            return
        }
        selecting = false
        if (!rollback) {
            selecting = true
        }
        selectAdvanceWallpaper.execute(object : DefaultObserver<Boolean>() {
            override fun onNext(success: Boolean) {
                if (!rollback && success) {
                    EventBus.getDefault().post(SwitchWallpaperServiceEvent())
                }
            }

            override fun onComplete() {
                view?.wallpaperSelected(wallpaperId)
            }
        }, SelectAdvanceWallpaper.Params.selectWallpaper(wallpaperId))
    }

    override fun resume() {
        maybeResetWallpaper()
    }

    private fun maybeResetWallpaper() {
        if (selecting && !TextUtils.isEmpty(mLastSelectedItemId)) {
            view?.executeDelay(Runnable {
                LogUtil.D(SettingsChooseSourcePresenter.TAG,
                        "restore wallpaper to $mLastSelectedItemId")
                selectAdvanceWallpaper(mLastSelectedItemId!!, true)
            }, 300)
        }
    }

    override fun pause() {

    }

    override fun destroy() {
        EventBus.getDefault().unregister(this)
        getAdvanceWallpapers.dispose()
        loadAdvanceWallpaper.dispose()
        selectAdvanceWallpaper.dispose()
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

    @Subscribe
    fun onEventMainThread(e: WallpaperActivateEvent) {
        // we cannot known if the user cancel the wallpaper set
        // so we need listen wallpaper be reactivated
        if (!e.isWallpaperActivate) {
            selecting = false
        } else {
            mLastSelectedItemId = getSelectedAdvanceWallpaper.selected.wallpaperId
        }
    }
}