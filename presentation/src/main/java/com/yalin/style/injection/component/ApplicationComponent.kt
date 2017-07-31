package com.yalin.style.injection.component

import android.content.Context
import com.yalin.style.StyleWallpaperService
import com.yalin.style.StyleWallpaperServiceMirror

import com.yalin.style.domain.executor.PostExecutionThread
import com.yalin.style.domain.executor.SerialThreadExecutor
import com.yalin.style.domain.executor.ThreadExecutor
import com.yalin.style.domain.observable.SourcesObservable
import com.yalin.style.domain.repository.SourcesRepository
import com.yalin.style.domain.observable.WallpaperObservable
import com.yalin.style.domain.repository.WallpaperRepository
import com.yalin.style.engine.StyleWallpaperProxy
import com.yalin.style.injection.modules.ApplicationModule
import com.yalin.style.view.activity.AdvanceSettingActivity
import com.yalin.style.view.activity.GallerySettingActivity
import com.yalin.style.view.fragment.StyleRenderFragment

import javax.inject.Singleton

import dagger.Component

/**
 * @author jinyalin
 * *
 * @since 2017/4/18.
 */
@Singleton
@Component(modules = arrayOf(ApplicationModule::class))
interface ApplicationComponent {
    fun inject(styleStyleWallpaperEngine: StyleWallpaperProxy.StyleWallpaperEngine)

    fun inject(styleView: StyleRenderFragment.StyleView)

    fun inject(gallerySettingActivity: GallerySettingActivity)
    fun inject(advanceSettingActivity: AdvanceSettingActivity)

    fun inject(styleWallpaperService: StyleWallpaperService)
    fun inject(styleWallpaperService: StyleWallpaperServiceMirror)

    //Exposed to sub-graphs.
    fun context(): Context

    fun threadExecutor(): ThreadExecutor
    fun serialThreadExecutor(): SerialThreadExecutor
    fun postExecutionThread(): PostExecutionThread
    fun wallpaperRepository(): WallpaperRepository
    fun sourcesRepository(): SourcesRepository
    fun wallpaperObservable(): WallpaperObservable
    fun sourceObservable(): SourcesObservable
}
