package com.yalin.style.data.repository.datasource

import android.content.Context
import com.yalin.style.data.cache.SourcesCache
import com.yalin.style.data.entity.SourceEntity
import io.reactivex.Observable

/**
 * @author jinyalin
 * @since 2017/5/23.
 */
class SourcesDataStoreImpl(val context: Context,
                           val sourcesCache: SourcesCache) : SourcesDataStore {

    override fun selectSource(sourceId: Int): Observable<Boolean> {
        return Observable.create { emitter ->
            emitter.onNext(sourcesCache.selectSource(sourceId))
        }
    }

    override fun getSources(): Observable<List<SourceEntity>> {
        return sourcesCache.getSources(context)
    }

    override fun isUseCustomSource() = sourcesCache.isUseCustomSource()

}
