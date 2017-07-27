package com.yalin.style

import android.app.WallpaperManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.service.wallpaper.WallpaperService
import com.yalin.style.analytics.Analytics
import com.yalin.style.analytics.Event
import com.yalin.style.domain.interactor.DefaultObserver
import com.yalin.style.domain.interactor.GetSelectedSource
import com.yalin.style.domain.interactor.ObserverSources
import com.yalin.style.domain.repository.SourcesRepository
import com.yalin.style.engine.ProxyProvider
import com.yalin.style.engine.WallpaperServiceProxy
import com.yalin.style.event.WallpaperActivateEvent

import net.rbgrn.android.glwallpaperservice.GLWallpaperService
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.toast
import javax.inject.Inject


/**
 * YaLin 2016/12/30.
 */
open class StyleWallpaperService : GLWallpaperService(), WallpaperServiceProxy.WallpaperActiveCallback {
    @Inject lateinit var proxyProvider: ProxyProvider
    @Inject lateinit var sourcesObserverUseCase: ObserverSources
    @Inject lateinit var getSelectedSourceUseCase: GetSelectedSource

    private var proxy: WallpaperServiceProxy
    private val sourcesObserver = SourcesRefreshObserver()

    private var currentSelectedSource: Int

    init {
        StyleApplication.instance.applicationComponent.inject(this)
        proxy = proxyProvider.provideProxy(this)
        currentSelectedSource = getSelectedSourceUseCase.selectedSourceId
    }

    override fun onCreateEngine(): WallpaperService.Engine {
        return proxy.onCreateEngine()
    }

    override fun onCreate() {
        super.onCreate()
        proxy.onCreate()

        sourcesObserverUseCase.registerObserver(sourcesObserver)
    }

    override fun onDestroy() {
        super.onDestroy()
        proxy.onDestroy()

        sourcesObserverUseCase.unregisterObserver(sourcesObserver)
    }


    override fun onWallpaperActivate() {
        Analytics.logEvent(this, Event.WALLPAPER_CREATED)
        EventBus.getDefault().postSticky(WallpaperActivateEvent(true))
    }

    override fun onWallpaperDeactivate() {
        Analytics.logEvent(this, Event.WALLPAPER_DESTROYED)
        EventBus.getDefault().postSticky(WallpaperActivateEvent(false))
    }

    private inner class SourcesRefreshObserver : DefaultObserver<Void>() {
        override fun onComplete() {
            if ((getSelectedSourceUseCase.selectedSourceId xor currentSelectedSource)
                    >= SourcesRepository.SOURCE_ID_ADVANCE) {
                pickWallpaper()
            }
        }
    }

    open fun getWallpaperTargetClass(): Class<*> {
        return StyleWallpaperServiceMirror::class.java
    }

    private fun pickWallpaper() {
        try {
            startActivity(Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
                    .putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                            ComponentName(this, getWallpaperTargetClass()))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: ActivityNotFoundException) {
            try {
                startActivity(Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            } catch (e2: ActivityNotFoundException) {
                toast(R.string.exception_message_device_unsupported)
                Analytics.logEvent(this, Event.DEVICE_UNSUPPORTED)
            }
        }
    }
}
