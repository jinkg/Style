package com.yalin.style.engine

import android.content.Context
import android.service.wallpaper.WallpaperService
import com.yalin.style.domain.interactor.GetSelectedAdvanceWallpaper
import com.yalin.style.domain.interactor.GetSelectedSource
import com.yalin.style.domain.repository.SourcesRepository
import com.yalin.style.engine.advance.BokehRainbowWallpaper
import javax.inject.Inject

/**
 * @author jinyalin
 * *
 * @since 2017/7/27.
 */

class ProxyProvider @Inject constructor(val getSelectedSourceUseCase: GetSelectedSource,
                                        val getAdvanceWallpaper: GetSelectedAdvanceWallpaper) {
    val NORMAL_PROXY_CLASS = "com.yalin.style.engine.StyleWallpaperProxy"

    fun provideProxy(host: Context): WallpaperService {
        if (getSelectedSourceUseCase.selectedSourceId == SourcesRepository.SOURCE_ID_ADVANCE) {
            val selected = getAdvanceWallpaper.selected
            if (selected.isDefault) {
                return BokehRainbowWallpaper(host)
            } else {
                val proxy = ProxyApi.getProxy(host, selected.storePath, selected.providerName)
                if (proxy != null) {
                    return proxy
                }
            }
        }

        val constructor = Class.forName(NORMAL_PROXY_CLASS).getConstructor(Context::class.java)
        return constructor.newInstance(host) as WallpaperService
    }
}
