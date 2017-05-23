package com.yalin.style.injection.modules

import android.content.Context

import com.yalin.style.UIThread
import com.yalin.style.data.cache.*
import com.yalin.style.data.executor.JobExecutor
import com.yalin.style.data.repository.SourcesDataRepository
import com.yalin.style.data.repository.WallpaperDataRepository
import com.yalin.style.domain.executor.PostExecutionThread
import com.yalin.style.domain.executor.ThreadExecutor
import com.yalin.style.domain.repository.SourcesRepository
import com.yalin.style.domain.repository.WallpaperRepository

import javax.inject.Singleton

import dagger.Module
import dagger.Provides

/**
 * @author jinyalin
 * *
 * @since 2017/4/18.
 */
@Module
class ApplicationModule(context: Context) {
    private val applicationContext: Context = context.applicationContext

    @Provides
    @Singleton
    internal fun provideApplicationContext(): Context {
        return applicationContext
    }

    @Provides
    @Singleton
    internal fun provideThreadExecutor(jobExecutor: JobExecutor): ThreadExecutor {
        return jobExecutor
    }

    @Provides
    @Singleton
    internal fun providePostExecutionThread(uiThread: UIThread): PostExecutionThread {
        return uiThread
    }

    @Provides
    @Singleton
    internal fun provideWallpaperRepository(wallpaperDataRepository: WallpaperDataRepository):
            WallpaperRepository {
        return wallpaperDataRepository
    }

    @Provides
    @Singleton
    internal fun provideSourcesRepository(sourcesDataRepository: SourcesDataRepository):
            SourcesRepository {
        return sourcesDataRepository
    }

    @Provides
    @Singleton
    internal fun provideWallpaperCache(wallpaperCache: WallpaperCacheImpl): WallpaperCache {
        return wallpaperCache
    }

    @Provides
    @Singleton
    internal fun provideSourceCache(sourceCache: SourcesCacheImpl): SourcesCache {
        return sourceCache
    }
}
