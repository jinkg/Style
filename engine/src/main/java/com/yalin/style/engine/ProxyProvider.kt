package com.yalin.style.engine

import android.content.Context
import com.yalin.style.domain.interactor.GetSelectedSource
import com.yalin.style.domain.repository.SourcesRepository
import com.yalin.style.engine.advance.AdvanceGLWallpaperService
import javax.inject.Inject

/**
 * @author jinyalin
 * *
 * @since 2017/7/27.
 */

class ProxyProvider @Inject constructor(val getSelectedSourceUseCase: GetSelectedSource) {
    val NORMAL_PROXY_CLASS = "com.yalin.style.engine.StyleWallpaperProxy"

    fun provideProxy(host: Context): WallpaperServiceProxy {
        if (getSelectedSourceUseCase.selectedSourceId == SourcesRepository.SOURCE_ID_ADVANCE) {
            return AdvanceGLWallpaperService(host)
        } else {
            val constructor = Class.forName(NORMAL_PROXY_CLASS).getConstructor(Context::class.java)
            return constructor.newInstance(host) as WallpaperServiceProxy
        }

    }
}
