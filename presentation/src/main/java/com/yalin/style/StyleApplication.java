package com.yalin.style;

import android.app.Application;

import com.yalin.style.injection.component.ApplicationComponent;
import com.yalin.style.injection.component.DaggerApplicationComponent;
import com.yalin.style.injection.modules.ApplicationModule;

/**
 * @author jinyalin
 * @since 2017/4/18.
 */

public class StyleApplication extends Application {
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
    }

    private void initializeInjector() {
        applicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
    }

    public ApplicationComponent getApplicationComponent() {
        return applicationComponent;
    }
}
