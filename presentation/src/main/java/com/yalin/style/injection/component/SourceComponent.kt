package com.yalin.style.injection.component

import com.yalin.style.injection.PerActivity
import com.yalin.style.injection.modules.WallpaperModule
import com.yalin.style.view.fragment.SettingsChooseSourceFragment
import com.yalin.style.view.fragment.WallpaperDetailFragment

import javax.inject.Singleton

import dagger.Component

/**
 * @author jinyalin
 * *
 * @since 2017/4/18.
 */
@PerActivity
@Component(dependencies = arrayOf(ApplicationComponent::class), modules = arrayOf(WallpaperModule::class))
interface SourceComponent {
    fun inject(settingsChooseSourceFragment: SettingsChooseSourceFragment)
}
