package com.yalin.style

//import com.facebook.stetho.Stetho

import android.content.Context
import android.support.multidex.MultiDex
import android.support.multidex.MultiDexApplication
import com.tencent.bugly.crashreport.CrashReport
import com.yalin.style.analytics.Analytics
import com.yalin.style.data.log.LogUtil
import com.yalin.style.data.repository.AdvanceWallpaperDataRepository
import com.yalin.style.injection.component.ApplicationComponent
import com.yalin.style.injection.component.DaggerApplicationComponent
import com.yalin.style.injection.modules.ApplicationModule
import com.yalin.style.extensions.DelegatesExt
import javax.inject.Inject

/**
 * @author jinyalin
 * *
 * @since 2017/4/18.
 */
class StyleApplication : MultiDexApplication() {

    companion object {
        private val TAG = "StyleApplication"

        var instance: StyleApplication by DelegatesExt.notNullSingleValue()
    }

    val applicationComponent: ApplicationComponent by lazy { initializeInjector() }

    @Inject
    lateinit var advanceWallpaperRepository: AdvanceWallpaperDataRepository

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        applicationComponent.inject(this)

        resetExceptionHandler()

        Analytics.init(this)
        CrashReport.initCrashReport(applicationContext,
                BuildConfig.BUGLY_APP_ID, LogUtil.LOG_ENABLE)
        CrashReport.setAppChannel(applicationContext, com.yalin.style.data.BuildConfig.CHANNEL)

//        if (BuildConfig.DEMO_MODE) {
//            Stetho.initialize(
//                    Stetho.newInitializerBuilder(this)
//                            .enableDumpapp(
//                                    Stetho.defaultDumperPluginsProvider(this))
//                            .enableWebKitInspector(
//                                    Stetho.defaultInspectorModulesProvider(this))
//                            .build())
//        }
    }

    private fun initializeInjector() = DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(this))
            .build()


    private fun resetExceptionHandler() {
        val exceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            advanceWallpaperRepository.markRollback()
            LogUtil.F(TAG, "exception", e)
            exceptionHandler.uncaughtException(t, e)
        }
    }
}
