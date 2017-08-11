package com.yalin.style.data.repository.datasource

import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.content.Context
import android.content.OperationApplicationException
import android.database.Cursor
import android.os.RemoteException
import com.google.gson.JsonParser
import com.yalin.style.data.R
import com.yalin.style.data.entity.AdvanceWallpaperEntity
import com.yalin.style.data.exception.NoContentException
import com.yalin.style.data.exception.RemoteServerException
import com.yalin.style.data.log.LogUtil
import com.yalin.style.data.repository.datasource.io.AdvanceWallpaperHandler
import com.yalin.style.data.repository.datasource.net.RemoteAdvanceWallpaperFetcher
import com.yalin.style.data.repository.datasource.provider.StyleContract
import com.yalin.style.data.repository.datasource.sync.account.Account
import com.yalin.style.domain.interactor.DefaultObserver
import io.reactivex.Observable

/**
 * @author jinyalin
 * @since 2017/7/31.
 */
class RemoteAdvanceWallpaperDataStore(val context: Context,
                                      val localDataStore: AdvanceWallpaperDataStoreImpl)
    : AdvanceWallpaperDataStore {
    companion object {
        val TAG = "RemoteAdvanceWallpaper"
    }

    val wallpaperHandler = AdvanceWallpaperHandler(context)

    override fun getWallpaperEntity(): AdvanceWallpaperEntity {
        throw UnsupportedOperationException("Remote data store not support get wallpaper.")
    }

    override fun getAdvanceWallpapers(): Observable<List<AdvanceWallpaperEntity>> {
        return Observable.create { emitter ->
            val account = Account.getAccount()
            val authority = context.getString(R.string.authority)
            ContentResolver.cancelSync(account, authority)

            val batch = ArrayList<ContentProviderOperation>()
            try {
                val wallpapers = RemoteAdvanceWallpaperFetcher(context).fetchDataIfNewer()
                val parser = JsonParser()
                val handler = AdvanceWallpaperHandler(context)
                handler.process(parser.parse(wallpapers))
                handler.makeContentProviderOperations(batch)
            } catch (e: Exception) {
                emitter.onError(RemoteServerException())
                return@create
            }

            try {
                val operations = batch.size
                if (operations > 0) {
                    context.contentResolver.applyBatch(StyleContract.AUTHORITY, batch)
                }
            } catch (ex: RemoteException) {
                LogUtil.D(TAG, "RemoteException while applying content provider operations.")
                throw RuntimeException("Error executing content provider batch operation", ex)
            } catch (ex: OperationApplicationException) {
                LogUtil.D(TAG, "OperationApplicationException while applying content provider operations.")
                throw RuntimeException("Error executing content provider batch operation", ex)
            }

            var cursor: Cursor? = null
            val validWallpapers = ArrayList<AdvanceWallpaperEntity>()
            try {
                val contentResolver = context.contentResolver
                cursor = contentResolver.query(StyleContract.AdvanceWallpaper.CONTENT_URI,
                        null, null, null, null)
                validWallpapers.addAll(AdvanceWallpaperEntity.readCursor(cursor))
            } finally {
                if (cursor != null) {
                    cursor.close()
                }
            }

            if (validWallpapers.isEmpty()) {
                emitter.onError(NoContentException())
            } else {
                emitter.onNext(validWallpapers)
            }
            emitter.onComplete()
        }
    }

    override fun selectWallpaper(wallpaperId: String, tempSelect: Boolean): Observable<Boolean> {
        throw UnsupportedOperationException("Remote data store not support select wallpaper.")
    }

    override fun downloadWallpaper(wallpaperId: String): Observable<Long> {
        return Observable.create { emitter ->
            val entity = localDataStore.loadWallpaperEntity(wallpaperId)
            wallpaperHandler.downloadWallpaperComponent(entity, object : DefaultObserver<Long>() {
                override fun onNext(downloadedLength: Long) {
                    emitter.onNext(downloadedLength)
                }

                override fun onComplete() {
                    emitter.onComplete()
                }

                override fun onError(exception: Throwable) {
                    emitter.onError(exception)
                }

            })
        }
    }
}