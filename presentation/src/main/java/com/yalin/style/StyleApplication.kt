package com.yalin.style

import android.app.Application

import com.tencent.bugly.crashreport.CrashReport
import com.yalin.style.analytics.Analytics
import com.yalin.style.data.log.LogUtil
import com.yalin.style.injection.component.ApplicationComponent
import com.yalin.style.injection.component.DaggerApplicationComponent
import com.yalin.style.injection.modules.ApplicationModule
import com.yalin.style.extensions.DelegatesExt

/**
 * @author jinyalin
 * *
 * @since 2017/4/18.
 */
class StyleApplication : Application() {

    companion object {
        private val TAG = "StyleApplication"

        var instance: StyleApplication by DelegatesExt.notNullSingleValue()
    }

    val applicationComponent: ApplicationComponent by lazy { initializeInjector() }

    override fun onCreate() {
        super.onCreate()
        instance = this

        resetExceptionHandler()

        Analytics.init(this)
        CrashReport.initCrashReport(applicationContext,
                BuildConfig.BUGLY_APP_ID, LogUtil.LOG_ENABLE)
        CrashReport.setAppChannel(applicationContext, com.yalin.style.data.BuildConfig.CHANNEL)
    }

    private fun initializeInjector() = DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(this))
            .build()


    private fun resetExceptionHandler() {
        val exceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            LogUtil.F(TAG, "exception", e)
            exceptionHandler.uncaughtException(t, e)
        }
    }
}
