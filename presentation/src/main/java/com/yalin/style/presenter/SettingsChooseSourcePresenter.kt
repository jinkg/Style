package com.yalin.style.presenter

import android.os.Bundle
import com.yalin.style.data.log.LogUtil
import com.yalin.style.domain.Source
import com.yalin.style.domain.interactor.DefaultObserver
import com.yalin.style.domain.interactor.GetSources
import com.yalin.style.domain.interactor.SelectSource
import com.yalin.style.domain.repository.SourcesRepository
import com.yalin.style.event.SwitchWallpaperServiceEvent
import com.yalin.style.event.WallpaperActivateEvent
import com.yalin.style.mapper.WallpaperItemMapper
import com.yalin.style.model.SourceItem
import com.yalin.style.view.SourceChooseView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.ArrayList
import javax.inject.Inject

/**
 * @author jinyalin
 * @since 2017/5/23.
 */
class SettingsChooseSourcePresenter @Inject
constructor(val getSourcesUseCase: GetSources,
            val selectSourceUseCae: SelectSource,
            val wallpaperMapper: WallpaperItemMapper) : Presenter {
    companion object {
        val TAG = "SettingsChooseSource"

        var LAST_SELECTED_ID = "last_selected_id"
        var SELECTING = "selecting"
    }

    private val mSources = ArrayList<SourceItem>()

    private var mSourceChooseView: SourceChooseView? = null

    private var mSelectedSource: SourceItem? = null

    private var mLastSelectedItemId = -1

    private var selecting = false

    fun setView(sourceChooseView: SourceChooseView) {
        mSourceChooseView = sourceChooseView
    }

    fun initialize() {
        updateSources()

        EventBus.getDefault().register(this)
    }

    fun selectSource(sourceId: Int) {
        selectSource(sourceId, false)
    }

    private fun selectSource(sourceId: Int, force: Boolean) {
        selecting = false
        mLastSelectedItemId = mSelectedSource!!.id

        var tempSelect = false
        if (!force && needSwitchWallpaper(sourceId)) {
            selecting = true
            tempSelect = true
            LogUtil.D(TAG, "Select source Last Selected $mLastSelectedItemId")
        }
        selectSourceUseCae.executeSerial(object : DefaultObserver<Boolean>() {
            override fun onNext(success: Boolean) {
                LogUtil.D(TAG, "onNext " + success)
                if (success) {
                    for (source in mSources) {
                        source.selected = source.id == sourceId
                        if (source.selected) {
                            mSelectedSource = source
                        }
                    }
                    LogUtil.D(TAG, "selected wallpaper id ${mSelectedSource!!.id}")
                    mSourceChooseView?.sourceSelected(mSources, mSelectedSource!!)
                    if (!force && needSwitchWallpaper(sourceId)) {
                        EventBus.getDefault().post(SwitchWallpaperServiceEvent())
                    }
                }
            }
        }, SelectSource.Params.selectSource(sourceId, tempSelect))
    }

    private fun needSwitchWallpaper(sourceId: Int): Boolean {
        return mLastSelectedItemId == SourcesRepository.SOURCE_ID_ADVANCE ||
                sourceId == SourcesRepository.SOURCE_ID_ADVANCE
    }

    override fun resume() {
        maybeResetSource()
    }

    private fun maybeResetSource() {
        if (selecting) {
            LogUtil.D(TAG, "restore wallpaper to $mLastSelectedItemId")
            selectSource(mLastSelectedItemId, true)
        }
    }

    override fun pause() {
    }

    override fun destroy() {
        EventBus.getDefault().unregister(this)
        getSourcesUseCase.dispose()
        selectSourceUseCae.dispose()
        mSourceChooseView = null
    }

    fun restoreInstanceState(instanceState: Bundle?) {
        if (instanceState != null && instanceState.containsKey(LAST_SELECTED_ID)) {
            mLastSelectedItemId = instanceState.getInt(LAST_SELECTED_ID)
            selecting = instanceState.getBoolean(SELECTING)
        }
    }

    fun saveInstanceState(outState: Bundle) {
        outState.putInt(LAST_SELECTED_ID, mLastSelectedItemId)
        outState.putBoolean(SELECTING, selecting)
    }

    fun updateSources() {
        getSourcesUseCase.executeSerial(object : DefaultObserver<List<Source>>() {
            override fun onNext(sources: List<Source>) {
                super.onNext(sources)
                mSources.clear()
                mSources.addAll(wallpaperMapper.transformSources(sources))
                for (source in mSources) {
                    if (source.selected) {
                        mSelectedSource = source
                        break
                    }
                }
                mSourceChooseView?.renderSources(mSources)
                mSourceChooseView?.sourceSelected(mSources, mSelectedSource!!)
            }
        }, null)
    }

    @Subscribe
    fun onEventMainThread(e: WallpaperActivateEvent) {
        // we cannot known if the user cancel the wallpaper set
        // so we need listen wallpaper be reactivated
        if (!e.isWallpaperActivate) {
            selecting = false
        }
    }

}