package com.yalin.style.data.observable

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import com.yalin.style.data.cache.SourcesCache
import com.yalin.style.data.log.LogUtil
import com.yalin.style.data.repository.datasource.provider.StyleContract
import com.yalin.style.domain.interactor.DefaultObserver
import com.yalin.style.domain.observable.SourcesObservable
import java.util.HashSet
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author jinyalin
 * @since 2017/5/23.
 */
@Singleton
class SourcesObservableImpl @Inject
constructor(val context: Context,
            val sourcesCache: SourcesCache) : SourcesObservable {

    companion object {
        val TAG = "WallpaperObservableImpl"
    }

    private val mObserverSet = HashSet<DefaultObserver<Void>>()
    private val mContentObserver = object : ContentObserver(Handler()) {
        override fun onChange(selfChange: Boolean, uri: Uri) {
            LogUtil.D(TAG, "Source changed notify observer to reload.")
            notifyObserver()
        }
    }

    private var observerRegistered = false

    override fun registerObserver(observer: DefaultObserver<Void>) {
        synchronized(mObserverSet) {
            mObserverSet.add(observer)
            if (!observerRegistered) {
                context.contentResolver
                        .registerContentObserver(StyleContract.Source.CONTENT_URI,
                                true, mContentObserver)
                observerRegistered = true
            }
        }
    }

    override fun unregisterObserver(observer: DefaultObserver<Void>) {
        synchronized(mObserverSet) {
            mObserverSet.remove(observer)
            if (mObserverSet.isEmpty()) {
                context.contentResolver.unregisterContentObserver(mContentObserver)
                observerRegistered = false
            }
        }
    }


    private fun notifyObserver() {
        for (observer in mObserverSet) {
            observer.onNext(null)
            observer.onComplete()
        }
    }

}