package com.yalin.style.presenter

import android.net.Uri
import com.yalin.style.domain.GalleryWallpaper
import com.yalin.style.domain.interactor.*
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
            val removeGalleryWallpaperUseCase: RemoveGalleryWallpaper,
            val getGalleryWallpaperUseCase: GetGalleryWallpaper,
            val forceNowUseCase: ForceNow,
            val setIntervalUseCase: SetGalleryUpdateInterval,
            val getIntervalUseCase: GetGalleryUpdateInterval) : Presenter {

    var gallerySettingView: GallerySettingView? = null

    val mWallpapers = ArrayList<GalleryWallpaperItem>()

    fun setView(gallerySettingView: GallerySettingView) {
        this.gallerySettingView = gallerySettingView
    }

    fun initialize() {
        refreshGalleryWallpaper()
        getUpdateInterval()
    }

    fun addGalleryWallpaper(uris: Set<Uri>) {
        val galleryWallpapers = ArrayList<GalleryWallpaper>()
        for (uri in uris) {
            val wallpaper = GalleryWallpaper()
            wallpaper.uri = uri.toString()
            galleryWallpapers.add(wallpaper)
        }

        addGalleryWallpaperUseCase.execute(
                GalleryWallpaperChangedObserver(),
                AddGalleryWallpaper.Params.addGalleryWallpaperUris(galleryWallpapers))
    }

    fun removeGalleryWallpaper(items: List<GalleryWallpaperItem>) {
        val galleryWallpapers = ArrayList<GalleryWallpaper>()
        for (item in items) {
            val wallpaper = GalleryWallpaper()
            wallpaper.uri = item.uri
            galleryWallpapers.add(wallpaper)
        }

        removeGalleryWallpaperUseCase.execute(
                GalleryWallpaperChangedObserver(),
                RemoveGalleryWallpaper.Params.removeGalleryWallpaperUris(galleryWallpapers))
    }

    fun forceNow(wallpaperUri: String) {
        forceNowUseCase.execute(DefaultObserver<Boolean>(), ForceNow.Params.fromUri(wallpaperUri))
    }

    fun setUpdateInterval(intervalMin: Int) {
        setIntervalUseCase.execute(DefaultObserver<Boolean>(),
                SetGalleryUpdateInterval.Params.interval(intervalMin))
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
            override fun onNext(intervalMin: List<GalleryWallpaper>) {
                val itemSet = wallpaperItemMapper.transformGalleryWallpaper(intervalMin)

                mWallpapers.clear()
                mWallpapers.addAll(itemSet)

                gallerySettingView?.renderGalleryWallpapers(mWallpapers)
            }
        }, null)
    }

    private fun getUpdateInterval() {
        getIntervalUseCase.execute(object : DefaultObserver<Int>() {
            override fun onNext(intervalMin: Int) {
                gallerySettingView?.renderUpdateInterval(intervalMin)
            }
        }, null)
    }


    private inner class GalleryWallpaperChangedObserver : DefaultObserver<Boolean>() {
        override fun onNext(success: Boolean) {
            super.onNext(success)
            if (success) {
                refreshGalleryWallpaper()
            }
        }
    }
}