package com.yalin.style.injection.component;

import com.yalin.style.StyleWallpaperService;
import com.yalin.style.injection.modules.ApplicationModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * @author jinyalin
 * @since 2017/4/18.
 */
@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {
    void inject(StyleWallpaperService.StyleWallpaperEngine styleWallpaperEngine);
}
