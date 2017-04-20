package com.yalin.style.injection.component;

import android.content.Context;

import com.yalin.style.StyleWallpaperService;
import com.yalin.style.domain.executor.PostExecutionThread;
import com.yalin.style.domain.executor.ThreadExecutor;
import com.yalin.style.domain.repository.WallpaperRepository;
import com.yalin.style.injection.modules.ApplicationModule;
import com.yalin.style.view.fragment.StyleRenderFragment;

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

    void inject(StyleRenderFragment.StyleView styleView);

    //Exposed to sub-graphs.
    Context context();
    ThreadExecutor threadExecutor();
    PostExecutionThread postExecutionThread();
    WallpaperRepository wallpaperRepository();
}
