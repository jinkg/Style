package com.yalin.style.data.repository.datasource

import android.content.Context
import com.yalin.style.data.cache.SourcesCache
import com.yalin.style.data.entity.SourceEntity
import com.yalin.style.data.lock.SelectSourceLock
import io.reactivex.Observable

/**
 * @author jinyalin
 * @since 2017/5/23.
 */
class SourcesDataStoreImpl(val context: Context,
                           val sourcesCache: SourcesCache,
                           val selectSourceLock: SelectSourceLock) : SourcesDataStore {

    override fun selectSource(sourceId: Int, tempSelect: Boolean): Observable<Boolean> {
        return Observable.create { emitter ->
            try {
                if ((!tempSelect && selectSourceLock.obtain()) || tempSelect) {
                    emitter.onNext(sourcesCache.selectSource(sourceId, tempSelect))
                } else {
                    emitter.onNext(false)
                }
                emitter.onComplete()
            } finally {
                selectSourceLock.release()
            }
        }
    }

    override fun getSources(): Observable<List<SourceEntity>> {
        return sourcesCache.getSources(context)
    }

    override fun getUsedSourceId() = sourcesCache.getUsedSourceId()

}
