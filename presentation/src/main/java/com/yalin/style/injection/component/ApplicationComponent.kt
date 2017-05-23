package com.yalin.style.injection.component

import android.content.Context

import com.yalin.style.StyleWallpaperService
import com.yalin.style.domain.executor.PostExecutionThread
import com.yalin.style.domain.executor.ThreadExecutor
import com.yalin.style.domain.repository.SourcesRepository
import com.yalin.style.domain.repository.WallpaperRepository
import com.yalin.style.injection.modules.ApplicationModule
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
    fun inject(styleWallpaperEngine: StyleWallpaperService.StyleWallpaperEngine)

    fun inject(styleView: StyleRenderFragment.StyleView)

    //Exposed to sub-graphs.
    fun context(): Context

    fun threadExecutor(): ThreadExecutor
    fun postExecutionThread(): PostExecutionThread
    fun wallpaperRepository(): WallpaperRepository
    fun sourcesRepository(): SourcesRepository
}
