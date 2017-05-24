package com.yalin.style.data.repository

import com.yalin.style.data.entity.mapper.WallpaperEntityMapper
import com.yalin.style.data.repository.datasource.CustomWallpaperDataStoreFactory
import com.yalin.style.domain.GalleryWallpaper
import com.yalin.style.domain.Wallpaper
import com.yalin.style.domain.repository.WallpaperRepository
import io.reactivex.Observable
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author jinyalin
 * @since 2017/5/24.
 */
@Singleton
class CustomWallpaperDataRepository @Inject
constructor(val customWallpaperDataStoreFactory: CustomWallpaperDataStoreFactory,
            val wallpaperEntityMapper: WallpaperEntityMapper) :
        WallpaperRepository {

    override fun getWallpaper(): Observable<Wallpaper> =
            customWallpaperDataStoreFactory.create()
                    .wallPaperEntity.map(wallpaperEntityMapper::transform)

    override fun switchWallpaper(): Observable<Wallpaper> =
            customWallpaperDataStoreFactory.create()
                    .switchWallPaperEntity().map(wallpaperEntityMapper::transform)

    override fun openInputStream(wallpaperId: String?): Observable<InputStream> =
            customWallpaperDataStoreFactory.create()
                    .openInputStream(wallpaperId)

    override fun getWallpaperCount(): Observable<Int> =
            customWallpaperDataStoreFactory.create()
                    .wallpaperCount

    override fun likeWallpaper(wallpaperId: String): Observable<Boolean> =
            customWallpaperDataStoreFactory.create()
                    .likeWallpaper(wallpaperId)

    override fun addCustomWallpaperUris(uris: Set<GalleryWallpaper>): Observable<Boolean> =
            customWallpaperDataStoreFactory.create()
                    .addCustomWallpaperUris(uris)

    override fun getGalleryWallpapers(): Observable<Set<GalleryWallpaper>> =
            customWallpaperDataStoreFactory.create()
                    .getCustomWallpaperUris().map(wallpaperEntityMapper::transformGalleryWallpaper)
}