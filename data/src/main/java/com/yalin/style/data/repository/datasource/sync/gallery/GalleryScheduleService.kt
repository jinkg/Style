package com.yalin.style.data.repository.datasource.sync.gallery

import android.app.AlarmManager
import android.app.IntentService
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.database.Cursor
import com.yalin.style.data.entity.GalleryWallpaperEntity
import com.yalin.style.data.extensions.DelegateExt
import com.yalin.style.data.log.LogUtil
import com.yalin.style.data.repository.datasource.provider.StyleContract
import java.util.*

/**
 * @author jinyalin
 * @since 2017/6/9.
 */
class GalleryScheduleService : IntentService(TAG) {
    companion object {
        val TAG = "GalleryScheduleService"

        val PREF_ROTATE_INTERVAL_MIN = "rotate_interval_min"
        val PREF_CURRENT_SHOW_WALLPAPER_ID = "current_gallery_wallpaper_id"

        val DEFAULT_ROTATE_INTERVAL_MIN = 1L

        val ACTION_START_UP = "com.yalin.style.ACTION_START_UP"
        val ACTION_SHUT_DOWN = "com.yalin.style.ACTION_SHUT_DOWN"
        val ACTION_SCHEDULE = "com.yalin.style.ACTION_SCHEDULE"

        fun startUp(context: Context) {
            val intent = Intent(ACTION_START_UP).setComponent(ComponentName(context,
                    GalleryScheduleService::class.java))
            context.startService(intent)
        }

        fun shutDown(context: Context) {
            val intent = Intent(ACTION_SHUT_DOWN).setComponent(ComponentName(context,
                    GalleryScheduleService::class.java))
            context.startService(intent)
        }

        fun publish(context: Context) {
            val intent = Intent(ACTION_SCHEDULE).setComponent(ComponentName(context,
                    GalleryScheduleService::class.java))
            context.startService(intent)
        }
    }

    var rotateIntervalMin: Long by DelegateExt.preferences(this,
            PREF_ROTATE_INTERVAL_MIN, DEFAULT_ROTATE_INTERVAL_MIN)

    var currentShowWallpaperId: Long by DelegateExt.preferences(this,
            PREF_CURRENT_SHOW_WALLPAPER_ID, -1)

    override fun onCreate() {
        super.onCreate()
    }

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            handleCommand(intent.action)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun handleCommand(action: String) {
        when (action) {
            ACTION_START_UP -> startUp()
            ACTION_SCHEDULE -> scheduleNext()
            ACTION_SHUT_DOWN -> shutDown()
        }
    }

    private fun startUp() {
        LogUtil.D(TAG, "Start up gallery schedule service.")
        setNextAlarm()
    }

    private fun scheduleNext() {
        LogUtil.D(TAG, "Schedule next gallery wallpaper.")
        publicNextWallpaper()
        setNextAlarm()
    }

    private fun shutDown() {
        LogUtil.D(TAG, "Shut down gallery schedule service.")
        cancelAlarm()
        stopSelf()
    }

    private fun setNextAlarm() {
        if (rotateIntervalMin > 0) {
            setUpdateAlarm(System.currentTimeMillis() + rotateIntervalMin * 60 * 1000)
        }
    }

    private fun setUpdateAlarm(nextTimeMillis: Long) {
        if (nextTimeMillis < System.currentTimeMillis()) {
            LogUtil.D(TAG, "Refusing to schedule next artwork in the past")
            return
        }

        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.set(AlarmManager.RTC, nextTimeMillis, getHandleNextCommandPendingIntent(this))
        LogUtil.D(TAG, "Scheduling next gallery at " + Date(nextTimeMillis))
    }

    private fun cancelAlarm() {
        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(getHandleNextCommandPendingIntent(this))
    }

    private fun getHandleNextCommandPendingIntent(context: Context): PendingIntent {
        return PendingIntent.getService(context, 0,
                Intent(ACTION_SCHEDULE).setComponent(ComponentName(context, javaClass)),
                PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun publicNextWallpaper() {
        var cursor: Cursor? = null
        val validWallpapers = ArrayList<GalleryWallpaperEntity>()
        try {
            cursor = contentResolver.query(StyleContract.GalleryWallpaper.CONTENT_URI,
                    null, null, null, null)
            validWallpapers.addAll(GalleryWallpaperEntity.readCursor(this, cursor))
        } finally {
            if (cursor != null) {
                cursor.close()
            }
        }

        var notify = false
        if (validWallpapers.size > 1) {
            val random = Random()
            while (true) {
                val selectedEntity = validWallpapers[random.nextInt(validWallpapers.size)]
                if (selectedEntity.id != currentShowWallpaperId) {
                    currentShowWallpaperId = selectedEntity.id
                    break
                }
            }
            notify = true
        } else if (validWallpapers.size == 1) {
            if (currentShowWallpaperId != validWallpapers[0].id) {
                currentShowWallpaperId = validWallpapers[0].id
                notify = true
            }
        } else {
            if (currentShowWallpaperId != -1L) {
                currentShowWallpaperId = -1
                notify = true
            }
        }

        LogUtil.D(TAG, "Current select wallpaper id : $currentShowWallpaperId notify =$notify")
        if (notify) {
            notifyChanged()
        }
    }

    private fun notifyChanged() {
        contentResolver.notifyChange(StyleContract.GalleryWallpaper.CONTENT_URI, null)
    }
}