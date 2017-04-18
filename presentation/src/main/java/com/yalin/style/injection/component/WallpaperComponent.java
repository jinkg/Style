package com.yalin.style.injection.component;

import com.yalin.style.injection.modules.WallpaperModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * @author jinyalin
 * @since 2017/4/18.
 */
@Singleton
@Component(modules = WallpaperModule.class)
public interface WallpaperComponent {

}
