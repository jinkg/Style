package com.yalin.style.presenter

import android.net.Uri
import com.yalin.style.domain.GalleryWallpaper
import com.yalin.style.domain.interactor.AddGalleryWallpaper
import com.yalin.style.domain.interactor.DefaultObserver
import com.yalin.style.domain.interactor.GetGalleryWallpaper
import com.yalin.style.mapper.WallpaperItemMapper
import com.yalin.style.model.GalleryWallpaperItem
import com.yalin.style.view.GallerySettingView
import javax.inject.Inject

/**
 * @author jinyalin
 * @since 2017/5/25.
 */
class GallerySettingPresenter @Inject
constructor(val wallpaperItemMapper: WallpaperItemMapper,
            val addGalleryWallpaperUseCase: AddGalleryWallpaper,
            val getGalleryWallpaperUseCase: GetGalleryWallpaper) : Presenter {

    var gallerySettingView: GallerySettingView? = null

    val mWallpapers = ArrayList<GalleryWallpaperItem>()

    fun setView(gallerySettingView: GallerySettingView) {
        this.gallerySettingView = gallerySettingView
    }

    fun initialize() {
        refreshGalleryWallpaper()
    }

    fun addGalleryWallpaper(uris: Set<Uri>) {

        val galleryWallpapers = java.util.ArrayList<GalleryWallpaper>()
        for (uri in uris) {
            val wallpaper = GalleryWallpaper()
            wallpaper.uri = uri.toString()
            galleryWallpapers.add(wallpaper)
        }

        addGalleryWallpaperUseCase.execute(
                AddCustomWallpaperObserver(),
                AddGalleryWallpaper.Params.addCustomWallpaperUris(galleryWallpapers))
    }

    override fun resume() {
    }

    override fun pause() {
    }

    override fun destroy() {
        addGalleryWallpaperUseCase.dispose()
        getGalleryWallpaperUseCase.dispose()
        gallerySettingView = null
    }


    private fun refreshGalleryWallpaper() {
        getGalleryWallpaperUseCase.execute(object : DefaultObserver<List<GalleryWallpaper>>() {
            override fun onNext(wallpapers: List<GalleryWallpaper>) {
                val itemSet = wallpaperItemMapper.transformGalleryWallpaper(wallpapers)

                mWallpapers.clear()
                mWallpapers.addAll(itemSet)

                gallerySettingView?.renderGalleryWallpapers(mWallpapers)
            }
        }, null)
    }


    private inner class AddCustomWallpaperObserver : DefaultObserver<Boolean>() {
        override fun onNext(success: Boolean) {
            super.onNext(success)
            if (success) {
                refreshGalleryWallpaper()
            }
        }
    }
}