package com.yalin.style.data.repository.datasource

import android.content.Context
import com.yalin.style.data.cache.SourcesCache
import com.yalin.style.data.cache.WallpaperCache
import com.yalin.style.data.entity.SourceEntity
import com.yalin.style.data.lock.LikeWallpaperLock
import com.yalin.style.data.lock.OpenInputStreamLock
import io.reactivex.Observable

/**
 * @author jinyalin
 * @since 2017/5/23.
 */
class SourcesDataStoreImpl(val context: Context,
                           val sourcesCache: SourcesCache,
                           val wallpaperCache: WallpaperCache,
                           val openInputStreamLock: OpenInputStreamLock,
                           val likeWallpaperLock: LikeWallpaperLock) : SourcesDataStore {

    override fun selectSource(sourceId: Int): Observable<Boolean> {
        return Observable.create { emitter ->
            emitter.onNext(sourcesCache.selectSource(sourceId))
        }
    }

    override fun getSources(): Observable<List<SourceEntity>> {
        return sourcesCache.getSources(context)
    }

    override fun getWallpaperDataStore(): WallpaperDataStore {
        if (sourcesCache.useCustomSource()) {
            return CustomWallpaperDataStore(context)
        } else {
            val wallpaperDataStore: WallpaperDataStore
            if (!wallpaperCache.isDirty && wallpaperCache.isCached) {
                wallpaperDataStore = CacheWallpaperDataStore(wallpaperCache, openInputStreamLock)
            } else {
                wallpaperDataStore = createDbDataStore()
            }
            return wallpaperDataStore
        }
    }

    override fun getDbWallpaperDataStore(): WallpaperDataStore {
        return createDbDataStore()
    }

    private fun createDbDataStore(): WallpaperDataStore {
        if (sourcesCache.useCustomSource()) {
            return CustomWallpaperDataStore(context)
        } else {
            return DbWallpaperDataStore(context, wallpaperCache,
                    openInputStreamLock, likeWallpaperLock)
        }
    }
}
