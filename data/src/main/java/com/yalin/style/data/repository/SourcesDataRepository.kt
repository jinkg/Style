package com.yalin.style.data.repository

import android.content.Context
import com.yalin.style.data.cache.SourcesCacheImpl
import com.yalin.style.data.entity.mapper.WallpaperEntityMapper
import com.yalin.style.data.repository.datasource.SourcesDataStoreFactory
import com.yalin.style.domain.Source
import com.yalin.style.domain.repository.SourcesRepository
import com.yalin.style.domain.repository.SourcesRepository.SOURCE_ID_ADVANCE
import com.yalin.style.domain.repository.SourcesRepository.SOURCE_ID_CUSTOM
import com.yalin.style.domain.repository.WallpaperRepository
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author jinyalin
 * @since 2017/5/23.
 */
@Singleton
class SourcesDataRepository @Inject
constructor(val context: Context,
            val sourcesDataStoreFactory: SourcesDataStoreFactory,
            val wallpaperEntityMapper: WallpaperEntityMapper,
            var styleWallpaperDataRepository: StyleWallpaperDataRepository,
            var customWallpaperDataRepository: GalleryWallpaperDataRepository,
            var advanceWallpaperDataRepository: AdvanceWallpaperDataRepository) : SourcesRepository {
    override fun getSources(): Observable<List<Source>> {
        val sourcesDataStore = sourcesDataStoreFactory.create()
        return sourcesDataStore.getSources().map(wallpaperEntityMapper::transformSources)
    }

    override fun selectSource(sourceId: Int): Observable<Boolean> {
        val sourcesDataStore = sourcesDataStoreFactory.create()
        return sourcesDataStore.selectSource(sourceId)
    }

    override fun getWallpaperRepository(): WallpaperRepository {
        val sourcesDataStore = sourcesDataStoreFactory.create()

        when (sourcesDataStore.getUsedSourceId()) {
            SOURCE_ID_CUSTOM -> return customWallpaperDataRepository
            SOURCE_ID_ADVANCE -> return advanceWallpaperDataRepository
            else -> return styleWallpaperDataRepository
        }
    }

    override fun getSelectedSource(): Int {
        val sourcesDataStore = sourcesDataStoreFactory.create()
        return sourcesDataStore.getUsedSourceId()
    }
}