package com.yalin.style.data.repository

import com.yalin.style.data.entity.mapper.WallpaperEntityMapper
import com.yalin.style.data.repository.datasource.GalleryWallpaperDataStoreFactory
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
class GalleryWallpaperDataRepository @Inject
constructor(val galleryWallpaperDataStoreFactory: GalleryWallpaperDataStoreFactory,
            val wallpaperEntityMapper: WallpaperEntityMapper) :
        WallpaperRepository {

    override fun getWallpaper(): Observable<Wallpaper> =
            galleryWallpaperDataStoreFactory.create()
                    .wallPaperEntity.map(wallpaperEntityMapper::transform)

    override fun switchWallpaper(): Observable<Wallpaper> =
            galleryWallpaperDataStoreFactory.create()
                    .switchWallPaperEntity().map(wallpaperEntityMapper::transform)

    override fun openInputStream(wallpaperId: String?): Observable<InputStream> =
            galleryWallpaperDataStoreFactory.create()
                    .openInputStream(wallpaperId)

    override fun getWallpaperCount(): Observable<Int> =
            galleryWallpaperDataStoreFactory.create()
                    .wallpaperCount

    override fun likeWallpaper(wallpaperId: String): Observable<Boolean> =
            galleryWallpaperDataStoreFactory.create()
                    .likeWallpaper(wallpaperId)

    override fun addGalleryWallpaperUris(uris: List<GalleryWallpaper>): Observable<Boolean> =
            galleryWallpaperDataStoreFactory.create()
                    .addGalleryWallpaperUris(uris)

    override fun removeGalleryWallpaperUris(uris: List<GalleryWallpaper>): Observable<Boolean> =
            galleryWallpaperDataStoreFactory.create()
                    .removeGalleryWallpaperUris(uris)

    override fun getGalleryWallpapers(): Observable<List<GalleryWallpaper>> =
            galleryWallpaperDataStoreFactory.create()
                    .getGalleryWallpaperUris().map(wallpaperEntityMapper::transformGalleryWallpaper)
}