package com.yalin.style.data.repository

import android.content.Context
import com.yalin.style.data.entity.mapper.AdvanceWallpaperEntityMapper
import com.yalin.style.data.extensions.DelegateExt
import com.yalin.style.data.repository.datasource.AdvanceWallpaperDataStoreFactory
import com.yalin.style.domain.AdvanceWallpaper
import com.yalin.style.domain.GalleryWallpaper
import com.yalin.style.domain.Wallpaper
import com.yalin.style.domain.repository.WallpaperRepository
import io.reactivex.Observable
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author jinyalin
 * @since 2017/7/27.
 */
@Singleton
class AdvanceWallpaperDataRepository
@Inject constructor(val context: Context,
                    val factory: AdvanceWallpaperDataStoreFactory,
                    val wallpaperMapper: AdvanceWallpaperEntityMapper,
                    val styleRepository: StyleWallpaperDataRepository)
    : WallpaperRepository {
    var needRollback: Boolean by DelegateExt.preferences(context, "is_need_rollback",
            false)

    override fun getWallpaper(): Observable<Wallpaper> {
        return Observable.create<Wallpaper> { emitter ->
            emitter.onNext(wallpaperMapper.mapToWallpaper(factory.create().getWallpaperEntity()))
            emitter.onComplete()
        }
    }

    override fun switchWallpaper(): Observable<Wallpaper> {
        return Observable.create<Wallpaper> { emitter ->
            emitter.onError(IllegalStateException(
                    "StyleWallpaperRepository have not gallery wallpapers."))
        }
    }

    override fun openInputStream(wallpaperId: String?): Observable<InputStream> {
        var id: String? = null
        styleRepository.wallpaper.subscribe(
                { wallpaper -> id = wallpaper.wallpaperId })

        return styleRepository.openInputStream(id)
    }

    override fun getWallpaperCount(): Observable<Int> {
        return Observable.create<Int> { emitter ->
            emitter.onNext(1)
            emitter.onComplete()
        }
    }

    override fun likeWallpaper(wallpaperId: String?): Observable<Boolean> {
        return Observable.create<Boolean> { emitter ->
            emitter.onError(IllegalStateException(
                    "StyleWallpaperRepository have not gallery wallpapers."))
        }
    }

    override fun addGalleryWallpaperUris(uris: MutableList<GalleryWallpaper>?): Observable<Boolean> {
        return Observable.create<Boolean> { emitter ->
            emitter.onError(IllegalStateException(
                    "StyleWallpaperRepository have not gallery wallpapers."))
        }
    }

    override fun removeGalleryWallpaperUris(uris: MutableList<GalleryWallpaper>?): Observable<Boolean> {
        return Observable.create<Boolean> { emitter ->
            emitter.onError(IllegalStateException(
                    "StyleWallpaperRepository have not gallery wallpapers."))
        }
    }

    override fun getGalleryWallpapers(): Observable<List<GalleryWallpaper>> {
        return Observable.create<List<GalleryWallpaper>> { emitter ->
            emitter.onError(IllegalStateException(
                    "StyleWallpaperRepository have not gallery wallpapers."))
        }
    }

    override fun getAdvanceWallpapers(): Observable<List<AdvanceWallpaper>> {
        return factory.create().getAdvanceWallpapers().map(wallpaperMapper::transformList)
    }

    override fun loadAdvanceWallpapers(): Observable<List<AdvanceWallpaper>> {
        return factory.createRemoteDataStore().getAdvanceWallpapers()
                .map(wallpaperMapper::transformList)
    }

    override fun downloadAdvanceWallpaper(wallpaperId: String): Observable<Long> {
        return factory.createRemoteDataStore().downloadWallpaper(wallpaperId)
    }

    override fun selectAdvanceWallpaper(wallpaperId: String, tempSelect: Boolean):
            Observable<Boolean> {
        return factory.create().selectWallpaper(wallpaperId, tempSelect)
    }

    override fun getAdvanceWallpaper(): AdvanceWallpaper {
        return wallpaperMapper.transform(factory.create().getWallpaperEntity())
    }

    override fun readAdvanceAd(wallpaperId: String): Observable<Boolean> {
        return factory.create().readAd(wallpaperId)
    }

    override fun foreNow(wallpaperUri: String?): Observable<Boolean> {
        return Observable.create<Boolean> { emitter ->
            emitter.onError(IllegalStateException(
                    "StyleWallpaperRepository have not gallery wallpapers."))
        }
    }

    override fun setGalleryUpdateInterval(intervalMin: Int): Observable<Boolean> {
        return Observable.create<Boolean> { emitter ->
            emitter.onError(IllegalStateException(
                    "StyleWallpaperRepository have not gallery wallpapers."))
        }
    }

    override fun getGalleryUpdateInterval(): Observable<Int> {
        return Observable.create<Int> { emitter ->
            emitter.onError(IllegalStateException(
                    "StyleWallpaperRepository have not gallery wallpapers."))
        }
    }

    fun markRollback() {
        needRollback = true
    }

    fun maybeRollback() {
        if (needRollback) {
            factory.create().rollback()
        }
    }
}