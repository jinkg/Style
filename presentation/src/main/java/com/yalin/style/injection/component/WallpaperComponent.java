package com.yalin.style.injection.component;

import com.yalin.style.injection.PerActivity;
import com.yalin.style.injection.modules.WallpaperModule;
import com.yalin.style.view.fragment.WallpaperDetailFragment;

import javax.inject.Singleton;

import dagger.Component;

/**
 * @author jinyalin
 * @since 2017/4/18.
 */
@PerActivity
@Component(dependencies = ApplicationComponent.class, modules = WallpaperModule.class)
public interface WallpaperComponent {
    void inject(WallpaperDetailFragment wallpaperDetailFragment);
}
