package com.yalin.style.data.repository.datasource

import android.content.Context
import com.yalin.style.data.cache.SourcesCache
import com.yalin.style.data.entity.SourceEntity
import com.yalin.style.data.lock.OpenInputStreamLock
import io.reactivex.Observable

/**
 * @author jinyalin
 * @since 2017/5/23.
 */
class SourcesDataStoreImpl(val context: Context,
                           val sourcesCache: SourcesCache,
                           val inputStreamLock: OpenInputStreamLock) : SourcesDataStore {

    override fun selectSource(sourceId: Int): Observable<Boolean> {
        return Observable.create { emitter ->
            try {
                if (inputStreamLock.obtain()) {
                    emitter.onNext(sourcesCache.selectSource(sourceId))
                } else {
                    emitter.onNext(false)
                }
                emitter.onComplete()
            } finally {
                inputStreamLock.release()
            }
        }
    }

    override fun getSources(): Observable<List<SourceEntity>> {
        return sourcesCache.getSources(context)
    }

    override fun isUseCustomSource() = sourcesCache.isUseCustomSource()

}
