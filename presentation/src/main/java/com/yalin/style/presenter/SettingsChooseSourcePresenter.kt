package com.yalin.style.presenter

import com.yalin.style.domain.Source
import com.yalin.style.domain.interactor.DefaultObserver
import com.yalin.style.domain.interactor.GetSources
import com.yalin.style.domain.interactor.SelectSource
import com.yalin.style.injection.PerActivity
import com.yalin.style.mapper.WallpaperItemMapper
import com.yalin.style.model.SourceItem
import com.yalin.style.view.SourceChooseView
import java.util.ArrayList
import javax.inject.Inject

/**
 * @author jinyalin
 * @since 2017/5/23.
 */
@PerActivity
class SettingsChooseSourcePresenter @Inject
constructor(val getSourcesUseCase: GetSources,
            val selectSourceUseCae: SelectSource,
            val wallpaperMapper: WallpaperItemMapper) : Presenter {

    private val mSources = ArrayList<SourceItem>()

    private var mSourceChooseView: SourceChooseView? = null

    private var mSelectedSource: SourceItem? = null

    fun setView(sourceChooseView: SourceChooseView) {
        mSourceChooseView = sourceChooseView
    }

    fun initialize() {
        updateSources()
    }

    fun selectSource(sourceId: Int) {
        selectSourceUseCae.execute(object : DefaultObserver<Boolean>() {
            override fun onNext(success: Boolean) {
                if (success) {
                    for (source in mSources) {
                        source.selected = source.id == sourceId
                        if (source.selected) {
                            mSelectedSource = source
                        }
                    }
                    mSourceChooseView?.sourceSelected(mSources, mSelectedSource!!)
                }
            }
        }, SelectSource.Params.selectSource(sourceId))
    }

    override fun resume() {
    }

    override fun pause() {
    }

    override fun destroy() {
    }

    fun updateSources() {
        getSourcesUseCase.execute(object : DefaultObserver<List<Source>>() {
            override fun onNext(success: List<Source>) {
                super.onNext(success)
                mSources.clear()
                mSources.addAll(wallpaperMapper.transformSources(success))
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

}