package com.yalin.style.data.observable

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.text.TextUtils
import com.yalin.style.data.cache.SourcesCache
import com.yalin.style.data.log.LogUtil
import com.yalin.style.data.repository.datasource.AdvanceWallpaperDataStoreFactory
import com.yalin.style.data.repository.datasource.StyleWallpaperDataStoreFactory
import com.yalin.style.data.repository.datasource.provider.StyleContract
import com.yalin.style.domain.interactor.DefaultObserver
import com.yalin.style.domain.observable.SourcesObservable
import com.yalin.style.domain.observable.WallpaperObservable
import com.yalin.style.domain.repository.SourcesRepository.SOURCE_ID_CUSTOM
import com.yalin.style.domain.repository.SourcesRepository.SOURCE_ID_STYLE
import com.yalin.style.domain.repository.SourcesRepository.SOURCE_ID_ADVANCE
import java.util.HashSet
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author jinyalin
 * @since 2017/5/23.
 */
@Singleton
class WallpaperObservableImpl @Inject
constructor(val context: Context,
            val sourcesCache: SourcesCache,
            val styleWallpaperDataStoreFactory: StyleWallpaperDataStoreFactory,
            val advanceWallpaperDataStoreFactory: AdvanceWallpaperDataStoreFactory,
            val sourcesObservable: SourcesObservable) :
        WallpaperObservable {

    companion object {
        val TAG = "WallpaperObservableImpl"
    }

    private val mObserverSet = HashSet<DefaultObserver<Void>>()
    private val mWallpaperObserver = object : ContentObserver(Handler()) {
        override fun onChange(selfChange: Boolean, uri: Uri) {
            LogUtil.D(TAG, "Wallpaper data changed notify observer to reload.")
            if (sourcesCache.getUsedSourceId() == SOURCE_ID_CUSTOM
                    && TextUtils.equals(uri.toString(),
                    StyleContract.GalleryWallpaper.CONTENT_URI.toString())) {
                notifyObserver()
            } else if (sourcesCache.getUsedSourceId() == SOURCE_ID_STYLE
                    && TextUtils.equals(uri.toString(),
                    StyleContract.Wallpaper.CONTENT_URI.toString())) {
                notifyObserver()
            } else if (sourcesCache.getUsedSourceId() == SOURCE_ID_ADVANCE
                    && TextUtils.equals(uri.toString(),
                    StyleContract.AdvanceWallpaper.CONTENT_URI.toString())) {
                notifyObserver()
            }
            styleWallpaperDataStoreFactory.onDataRefresh()
            advanceWallpaperDataStoreFactory.onDataRefresh()
        }
    }

    private val sourceObserver = object : DefaultObserver<Void>() {
        override fun onComplete() {
            notifyObserver()
        }
    }

    init {
        context.contentResolver
                .registerContentObserver(StyleContract.Wallpaper.CONTENT_URI,
                        true, mWallpaperObserver)
        context.contentResolver
                .registerContentObserver(StyleContract.GalleryWallpaper.CONTENT_URI,
                        true, mWallpaperObserver)
        context.contentResolver
                .registerContentObserver(StyleContract.AdvanceWallpaper.CONTENT_URI,
                        true, mWallpaperObserver)

        sourcesObservable.registerObserver(sourceObserver)
    }

    override fun registerObserver(observer: DefaultObserver<Void>) {
        synchronized(mObserverSet) {
            mObserverSet.add(observer)
        }
    }

    override fun unregisterObserver(observer: DefaultObserver<Void>) {
        synchronized(mObserverSet) {
            mObserverSet.remove(observer)
        }
    }


    private fun notifyObserver() {
        for (observer in mObserverSet) {
            observer.onNext(null)
            observer.onComplete()
        }
    }

}