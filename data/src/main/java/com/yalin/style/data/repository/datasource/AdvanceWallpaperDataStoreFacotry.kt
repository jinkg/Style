package com.yalin.style.data.repository.datasource

import android.content.Context
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author jinyalin
 * @since 2017/7/28.
 */
@Singleton
class AdvanceWallpaperDataStoreFacotry @Inject
constructor(val context: Context) {

    fun create(): AdvanceWallpaperDataStore {
        return AdvanceWallpaperDataStoreImpl(context)
    }

    fun createRemoteDataStore(): AdvanceWallpaperDataStore {
        return RemoteAdvanceWallpaperDataStore(context)
    }
}