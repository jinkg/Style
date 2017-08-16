package com.yalin.style.presenter

import android.os.Bundle
import android.text.TextUtils
import com.yalin.style.data.log.LogUtil
import com.yalin.style.data.utils.WallpaperFileHelper
import com.yalin.style.domain.AdvanceWallpaper
import com.yalin.style.domain.interactor.*
import com.yalin.style.event.SwitchWallpaperServiceEvent
import com.yalin.style.event.WallpaperActivateEvent
import com.yalin.style.exception.ErrorMessageFactory
import com.yalin.style.mapper.AdvanceWallpaperItemMapper
import com.yalin.style.model.AdvanceWallpaperItem
import com.yalin.style.view.AdvanceSettingView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
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
                    val advanceWallpaperItemMapper: AdvanceWallpaperItemMapper,
                    val downloadAdvanceWallpaper: DownloadAdvanceWallpaper,
                    val readAdvanceAd: ReadAdvanceAd)
    : Presenter {

    companion object {
        val DOWNLOAD_STATE = "download_state"
        val DOWNLOADING_ITEM = "download_item"

        val DOWNLOAD_NONE = 0
        val DOWNLOADING = 1
        val DOWNLOAD_ERROR = 2
    }

    private val wallpaperObserver = WallpapersObserver()
    private var view: AdvanceSettingView? = null

    private var mLastSelectedItemId: String? = null

    private var selecting = false

    private var downloadingWallpaper: AdvanceWallpaperItem? = null

    private var downloadState = DOWNLOAD_NONE

    fun setView(view: AdvanceSettingView) {
        this.view = view
    }

    fun initialize() {
        view?.showLoading()
        getAdvanceWallpapers.execute(wallpaperObserver, null)
        mLastSelectedItemId = getSelectedAdvanceWallpaper.selected.wallpaperId
        EventBus.getDefault().register(this)
    }

    fun loadAdvanceWallpaper() {
        view?.showLoading()
        loadAdvanceWallpaper.execute(object : DefaultObserver<List<AdvanceWallpaper>>() {
            override fun onNext(needDownload: List<AdvanceWallpaper>) {
                view?.renderWallpapers(advanceWallpaperItemMapper.transformList(needDownload))
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

    fun selectAdvanceWallpaper(item: AdvanceWallpaperItem) {
        if (WallpaperFileHelper.isNeedDownloadAdvanceComponent(item.lazyDownload,
                item.storePath) || (downloadingWallpaper != null
                && TextUtils.equals(downloadingWallpaper!!.wallpaperId, item.wallpaperId))) {
            view?.showDownloadHintDialog(item)
        } else if (item.needAd) {
            view?.showAd(item)
        } else {
            selectAdvanceWallpaper(item.wallpaperId, false)
        }
    }

    fun requestDownload(item: AdvanceWallpaperItem) {
        view?.showDownloadingDialog(item)
        downloadingWallpaper = item
        downloadState = DOWNLOADING
        downloadAdvanceWallpaper.execute(object : DefaultObserver<Long>() {
            override fun onNext(progress: Long) {
                view?.updateDownloadingProgress(progress)
            }

            override fun onComplete() {
                view?.downloadComplete(item)
                downloadingWallpaper = null
                downloadState = DOWNLOAD_NONE
            }

            override fun onError(exception: Throwable) {
                view?.showDownloadError(item, exception as Exception)
                downloadingWallpaper = null
                downloadState = DOWNLOAD_ERROR
            }
        }, DownloadAdvanceWallpaper.Params.download(item.wallpaperId))
    }

    fun adViewed(item: AdvanceWallpaperItem) {
        readAdvanceAd.execute(object : DefaultObserver<Boolean>() {
            override fun onNext(success: Boolean) {
                if (success) {
                    view?.adViewed(item)
                }
            }
        }, ReadAdvanceAd.Params.read(item.wallpaperId))
    }


    fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(DOWNLOAD_STATE, downloadState)
        if (downloadingWallpaper != null) {
            outState.putParcelable(DOWNLOADING_ITEM, downloadingWallpaper!!)
        }
    }

    fun onRestoreInstanceState(savedInstanceState: Bundle) {
        downloadState = savedInstanceState.getInt(DOWNLOAD_STATE)
        downloadingWallpaper = savedInstanceState.getParcelable(DOWNLOADING_ITEM)

        if (downloadingWallpaper != null) {
            if (downloadState == DOWNLOADING) {
                requestDownload(downloadingWallpaper!!)
            } else if (downloadState == DOWNLOAD_ERROR) {

            }
        }
    }

    fun getDownloadingItem(): AdvanceWallpaperItem? {
        return downloadingWallpaper
    }


    private fun selectAdvanceWallpaper(wallpaperId: String, rollback: Boolean) {
        if (!rollback && TextUtils.equals(wallpaperId, mLastSelectedItemId)) {
            view?.complete()
            return
        }
        selecting = false
        var tempSelect = false
        if (!rollback) {
            selecting = true
            tempSelect = true
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
        }, SelectAdvanceWallpaper.Params.selectWallpaper(wallpaperId, tempSelect))
    }

    override fun resume() {
        maybeResetWallpaper()
    }

    private fun maybeResetWallpaper() {
        if (selecting && !TextUtils.isEmpty(mLastSelectedItemId)) {
            LogUtil.D(SettingsChooseSourcePresenter.TAG,
                    "restore wallpaper to $mLastSelectedItemId")
            selectAdvanceWallpaper(mLastSelectedItemId!!, true)
        }
    }

    override fun pause() {

    }

    override fun destroy() {
        EventBus.getDefault().unregister(this)
        getAdvanceWallpapers.dispose()
        loadAdvanceWallpaper.dispose()
        selectAdvanceWallpaper.dispose()
        downloadAdvanceWallpaper.dispose()
        downloadingWallpaper = null
        view = null
    }

    private inner class WallpapersObserver : DefaultObserver<List<AdvanceWallpaper>>() {
        override fun onNext(needDownload: List<AdvanceWallpaper>) {
            if (needDownload.isEmpty()) {
                view?.showEmpty()
            } else {
                view?.renderWallpapers(advanceWallpaperItemMapper.transformList(needDownload))
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