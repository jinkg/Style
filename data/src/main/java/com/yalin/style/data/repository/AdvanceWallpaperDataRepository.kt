package com.yalin.style.data.repository

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
@Inject constructor(val styleRepository: StyleWallpaperDataRepository)
    : WallpaperRepository {
    override fun getWallpaper(): Observable<Wallpaper> {
        return styleRepository.wallpaper
    }

    override fun switchWallpaper(): Observable<Wallpaper> {
        return styleRepository.switchWallpaper()
    }

    override fun openInputStream(wallpaperId: String?): Observable<InputStream> {
        return styleRepository.openInputStream(wallpaperId)
    }

    override fun getWallpaperCount(): Observable<Int> {
        return styleRepository.wallpaperCount
    }

    override fun likeWallpaper(wallpaperId: String?): Observable<Boolean> {
        return styleRepository.likeWallpaper(wallpaperId)
    }

    override fun addGalleryWallpaperUris(uris: MutableList<GalleryWallpaper>?): Observable<Boolean> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeGalleryWallpaperUris(uris: MutableList<GalleryWallpaper>?): Observable<Boolean> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getGalleryWallpapers(): Observable<MutableList<GalleryWallpaper>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun foreNow(wallpaperUri: String?): Observable<Boolean> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setGalleryUpdateInterval(intervalMin: Int): Observable<Boolean> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getGalleryUpdateInterval(): Observable<Int> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}