package com.yalin.style.data.repository

import com.yalin.style.data.entity.mapper.WallpaperEntityMapper
import com.yalin.style.data.repository.datasource.SourcesDataStoreFactory
import com.yalin.style.domain.Source
import com.yalin.style.domain.repository.SourcesRepository
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author jinyalin
 * @since 2017/5/23.
 */
@Singleton
class SourcesDataRepository @Inject
constructor(val sourcesDataStoreFactory: SourcesDataStoreFactory,
            val wallpaperEntityMapper: WallpaperEntityMapper) : SourcesRepository {

    override fun getSources(): Observable<List<Source>> {
        val sourcesDataStore = sourcesDataStoreFactory.create()
        return sourcesDataStore.getSources().map(wallpaperEntityMapper::transformSources)
    }

    override fun selectSource(sourceId: Int): Observable<Boolean> {
        val sourcesDataStore = sourcesDataStoreFactory.create()
        return sourcesDataStore.selectSource(sourceId)
    }

}