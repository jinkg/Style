package com.yalin.style;

import android.app.Application;

import com.tencent.bugly.crashreport.CrashReport;
import com.yalin.style.data.log.LogUtil;
import com.yalin.style.injection.component.ApplicationComponent;
import com.yalin.style.injection.component.DaggerApplicationComponent;
import com.yalin.style.injection.modules.ApplicationModule;

/**
 * @author jinyalin
 * @since 2017/4/18.
 */
public class StyleApplication extends Application {
    private static final String TAG = "StyleApplication";

    private static StyleApplication INSTANCE;

    public static StyleApplication getInstance() {
        return INSTANCE;
    }

    private ApplicationComponent applicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        initializeInjector();

        resetExceptionHandler();

        CrashReport.initCrashReport(getApplicationContext(), BuildConfig.BUGLY_APP_ID, true);
    }

    private void initializeInjector() {
        applicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
    }

    private void resetExceptionHandler() {
        final Thread.UncaughtExceptionHandler exceptionHandler
                = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                LogUtil.F(TAG, "exception", e);
                exceptionHandler.uncaughtException(t, e);
            }
        });
    }

    public ApplicationComponent getApplicationComponent() {
        return applicationComponent;
    }
}
