package com.yalin.style.data.repository.datasource.io

import android.content.ContentProviderOperation
import android.content.Context
import android.database.Cursor
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.yalin.style.data.SyncConfig
import com.yalin.style.data.entity.AdvanceWallpaperEntity
import com.yalin.style.data.exception.RemoteServerException
import com.yalin.style.data.log.LogUtil
import com.yalin.style.data.repository.datasource.provider.StyleContract
import com.yalin.style.data.repository.datasource.provider.StyleContractHelper
import com.yalin.style.data.utils.WallpaperFileHelper
import com.yalin.style.domain.interactor.DefaultObserver
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.*
import java.net.URL
import java.util.ArrayList
import java.util.HashSet
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

/**
 * @author jinyalin
 * @since 2017/7/28.
 */
class AdvanceWallpaperHandler(context: Context) : JSONHandler(context) {
    companion object {
        val TAG = "AdvanceWallpaperHandler"
        val downloadLock = ReentrantLock()
    }

    private var wallpapers: ArrayList<AdvanceWallpaperEntity> = ArrayList()

    override fun makeContentProviderOperations(list: ArrayList<ContentProviderOperation>) {
        val uri = StyleContractHelper.setUriAsCalledFromSyncAdapter(
                StyleContract.AdvanceWallpaper.CONTENT_URI)
        list.add(ContentProviderOperation.newDelete(uri).build())

        val validFiles = HashSet<String>()
        val selectedEntities = querySelectedWallpapers()
        validFiles.addAll(getWallpaperNameSet(selectedEntities))
        for (wallpaper in this.wallpapers) {
            wallpaper.storePath = makeStorePath(wallpaper)
            if (!selectedEntities.contains(wallpaper)) {
                if (wallpaper.lazyDownload || (downloadWallpaperComponent(wallpaper)
                        && WallpaperFileHelper.ensureChecksumValid(mContext,
                        wallpaper.checkSum, wallpaper.storePath))) {
                    LogUtil.D(TAG, "download wallpaper component "
                            + " success, do output wallpaper.")
                    outputWallpaper(wallpaper, list)
                    validFiles.add(makeFilename(wallpaper))
                }
            }
        }
        // delete old wallpapers
        WallpaperFileHelper.deleteOldComponent(mContext, validFiles)
    }

    override fun process(element: JsonElement) {
        val wallpapers = Gson().fromJson(element, Array<AdvanceWallpaperEntity>::class.java)
        this.wallpapers.ensureCapacity(wallpapers.size)
        this.wallpapers.addAll(wallpapers)
    }

    private fun makeFilename(wallpaper: AdvanceWallpaperEntity): String {
        return wallpaper.hashCode().toString() + "_component.apk"
    }

    private fun makeStorePath(wallpaper: AdvanceWallpaperEntity): String {
        val outputDir = WallpaperFileHelper.getAdvanceWallpaperDir(mContext)
        return File(outputDir, makeFilename(wallpaper)).absolutePath
    }

    private fun getWallpaperNameSet(entities: List<AdvanceWallpaperEntity>): Set<String> {
        val ids = HashSet<String>()
        for (entity in entities) {
            ids.add(makeFilename(entity))
        }
        return ids
    }

    private fun outputWallpaper(wallpaper: AdvanceWallpaperEntity,
                                list: ArrayList<ContentProviderOperation>) {
        val uri = StyleContractHelper.setUriAsCalledFromSyncAdapter(
                StyleContract.AdvanceWallpaper.CONTENT_URI)
        val builder = ContentProviderOperation.newInsert(uri)
        builder.withValue(StyleContract.AdvanceWallpaper.COLUMN_NAME_WALLPAPER_ID, wallpaper.wallpaperId)
        builder.withValue(StyleContract.AdvanceWallpaper.COLUMN_NAME_AUTHOR, wallpaper.author)
        builder.withValue(StyleContract.AdvanceWallpaper.COLUMN_NAME_DOWNLOAD_URL, wallpaper.downloadUrl)
        builder.withValue(StyleContract.AdvanceWallpaper.COLUMN_NAME_ICON_URL, wallpaper.iconUrl)
        builder.withValue(StyleContract.AdvanceWallpaper.COLUMN_NAME_LINK, wallpaper.link)
        builder.withValue(StyleContract.AdvanceWallpaper.COLUMN_NAME_NAME, wallpaper.name)
        builder.withValue(StyleContract.AdvanceWallpaper.COLUMN_NAME_CHECKSUM, wallpaper.checkSum)
        builder.withValue(StyleContract.AdvanceWallpaper.COLUMN_NAME_STORE_PATH, wallpaper.storePath)
        builder.withValue(StyleContract.AdvanceWallpaper.COLUMN_NAME_PROVIDER_NAME, wallpaper.providerName)
        builder.withValue(StyleContract.AdvanceWallpaper.COLUMN_NAME_SELECTED, 0)
        builder.withValue(StyleContract.AdvanceWallpaper.COLUMN_NAME_LAZY_DOWNLOAD,
                if (wallpaper.lazyDownload) 1 else 0)
        builder.withValue(StyleContract.AdvanceWallpaper.COLUMN_NAME_NEED_AD,
                if (wallpaper.needAd) 1 else 0)

        list.add(builder.build())
    }

    private fun querySelectedWallpapers(): List<AdvanceWallpaperEntity> {
        var cursor: Cursor? = null
        try {
            cursor = mContext.contentResolver.query(
                    StyleContract.AdvanceWallpaper.CONTENT_SELECTED_URI, null, null, null, null)
            return AdvanceWallpaperEntity.readCursor(cursor)
        } finally {
            if (cursor != null) {
                cursor.close()
            }
        }
    }

    private fun downloadWallpaperComponent(wallpaper: AdvanceWallpaperEntity): Boolean {
        return downloadWallpaperComponent(wallpaper, null)
    }

    fun downloadWallpaperComponent(wallpaper: AdvanceWallpaperEntity,
                                   observer: DefaultObserver<Long>?): Boolean {
        observer?.onNext(0)
        LogUtil.D(TAG, "Start download wallpaper component to " + wallpaper.storePath)
        val outputFile = File(wallpaper.storePath)
        if (outputFile.exists()) {
            if (WallpaperFileHelper.ensureChecksumValid(mContext,
                    wallpaper.checkSum, wallpaper.storePath)) {
                observer?.onComplete()
                return true
            }
        }
        synchronized(downloadLock) {
            var os: OutputStream? = null
            var _is: InputStream? = null
            try {
                if (outputFile.exists()) {
                    if (WallpaperFileHelper.ensureChecksumValid(mContext,
                            wallpaper.checkSum, wallpaper.storePath)) {
                        observer?.onComplete()
                        return true
                    } else {
                        outputFile.delete()
                    }
                }

                val storePath = outputFile.parentFile
                storePath.mkdirs()
                os = FileOutputStream(outputFile)
                val httpClient = OkHttpClient.Builder()
                        .connectTimeout(SyncConfig.DEFAULT_CONNECT_TIMEOUT.toLong(), TimeUnit.SECONDS)
                        .readTimeout(SyncConfig.DEFAULT_DOWNLOAD_TIMEOUT.toLong(), TimeUnit.SECONDS)
                        .build()
                val request = Request.Builder().url(URL(wallpaper.downloadUrl)).build()

                val response = httpClient.newCall(request).execute()
                val responseCode = response.code()
                if (responseCode in 200..299) {
                    _is = response.body().byteStream()
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    var writeLength = 0L
                    bytesRead = _is.read(buffer)
                    while (bytesRead > 0) {
                        os.write(buffer, 0, bytesRead)
                        writeLength += bytesRead
                        observer?.onNext(writeLength)
                        bytesRead = _is.read(buffer)
                    }
                    os.flush()
                    observer?.onComplete()
                    return true
                } else {
                    LogUtil.E(TAG, "Download wallpaper component " + wallpaper.name + " failed.")
                    observer?.onError(RemoteServerException())
                    return false

                }
            } catch (e: IOException) {
                e.printStackTrace()
                observer?.onError(RemoteServerException())
                LogUtil.E(TAG, "Download wallpaper component" + wallpaper.name + " failed.", e)
                return false
            } finally {
                try {
                    os?.close()
                    _is?.close()
                } catch (e: IOException) {
                    // ignore
                }
            }
        }
    }
}