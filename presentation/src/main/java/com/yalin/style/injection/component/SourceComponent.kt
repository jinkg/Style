package com.yalin.style.injection.component

import com.yalin.style.injection.PerActivity
import com.yalin.style.injection.modules.SourceModule
import com.yalin.style.view.fragment.SettingsChooseSourceFragment

import dagger.Component

/**
 * @author jinyalin
 * *
 * @since 2017/4/18.
 */
@PerActivity
@Component(dependencies = arrayOf(ApplicationComponent::class),
        modules = arrayOf(SourceModule::class))
interface SourceComponent {
    fun inject(settingsChooseSourceFragment: SettingsChooseSourceFragment)
}
