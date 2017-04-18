package com.yalin.style.injection.modules;

import android.content.Context;

import com.yalin.style.UIThread;
import com.yalin.style.data.executor.JobExecutor;
import com.yalin.style.data.repository.WallpaperDataRepository;
import com.yalin.style.domain.executor.PostExecutionThread;
import com.yalin.style.domain.executor.ThreadExecutor;
import com.yalin.style.domain.repository.WallpaperRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * @author jinyalin
 * @since 2017/4/18.
 */
@Module
public class ApplicationModule {
    private Context applicationContext;

    public ApplicationModule(Context context) {
        applicationContext = context.getApplicationContext();
    }

    @Provides
    @Singleton
    Context provideApplicationContext() {
        return applicationContext;
    }

    @Provides
    @Singleton
    ThreadExecutor provideThreadExecutor(JobExecutor jobExecutor) {
        return jobExecutor;
    }

    @Provides
    @Singleton
    PostExecutionThread providePostExecutionThread(UIThread uiThread) {
        return uiThread;
    }

    @Provides
    @Singleton
    WallpaperRepository provideUserRepository(WallpaperDataRepository wallpaperDataRepository) {
        return wallpaperDataRepository;
    }
}
