package com.yalin.style.data.repository

import com.yalin.style.data.entity.mapper.AdvanceWallpaperEntityMapper
import com.yalin.style.data.repository.datasource.AdvanceWallpaperDataStoreFacotry
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
@Inject constructor(val factory: AdvanceWallpaperDataStoreFacotry,
                    val wallpaperMapper: AdvanceWallpaperEntityMapper,
                    val styleRepository: StyleWallpaperDataRepository)
    : WallpaperRepository {

    override fun getWallpaper(): Observable<Wallpaper> {
        return styleRepository.wallpaper
    }

    override fun switchWallpaper(): Observable<Wallpaper> {
        return Observable.create<Wallpaper> { emitter ->
            emitter.onError(IllegalStateException(
                    "StyleWallpaperRepository have not gallery wallpapers."))
        }
    }

    override fun openInputStream(wallpaperId: String?): Observable<InputStream> {
        return styleRepository.openInputStream(wallpaperId)
    }

    override fun getWallpaperCount(): Observable<Int> {
        return styleRepository.wallpaperCount
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

    override fun getAdvanceWallpaper(): AdvanceWallpaper {
        return wallpaperMapper.transform(factory.create().getWallPaperEntity())
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

}