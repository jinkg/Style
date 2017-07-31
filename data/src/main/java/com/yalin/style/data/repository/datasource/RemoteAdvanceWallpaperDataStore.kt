package com.yalin.style.data.repository.datasource

import android.content.ContentProviderOperation
import android.content.Context
import android.content.OperationApplicationException
import android.database.Cursor
import android.os.RemoteException
import com.google.gson.JsonParser
import com.yalin.style.data.entity.AdvanceWallpaperEntity
import com.yalin.style.data.exception.NoContentException
import com.yalin.style.data.log.LogUtil
import com.yalin.style.data.repository.datasource.io.AdvanceWallpaperHandler
import com.yalin.style.data.repository.datasource.net.RemoteAdvanceWallpaperFetcher
import com.yalin.style.data.repository.datasource.provider.StyleContract
import io.reactivex.Observable

/**
 * @author jinyalin
 * @since 2017/7/31.
 */
class RemoteAdvanceWallpaperDataStore(val context: Context) : AdvanceWallpaperDataStore {
    companion object {
        val TAG = "RemoteAdvanceWallpaper"
    }

    override fun getWallPaperEntity(): AdvanceWallpaperEntity? {
        throw UnsupportedOperationException("Cache data store not support open input stream.")
    }

    override fun getAdvanceWallpapers(): Observable<List<AdvanceWallpaperEntity>> {
        return Observable.create { emitter ->
            val wallpapers = RemoteAdvanceWallpaperFetcher(context).fetchDataIfNewer()
            val parser = JsonParser()
            val handler = AdvanceWallpaperHandler(context)
            handler.process(parser.parse(wallpapers))
            val batch = ArrayList<ContentProviderOperation>()
            handler.makeContentProviderOperations(batch)

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
}