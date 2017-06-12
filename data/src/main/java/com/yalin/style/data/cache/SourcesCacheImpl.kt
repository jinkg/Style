package com.yalin.style.data.cache

import android.content.Context
import android.graphics.Color
import com.yalin.style.data.R
import com.yalin.style.data.entity.SourceEntity
import com.yalin.style.data.extensions.DelegateExt
import com.yalin.style.data.repository.datasource.provider.StyleContract
import com.yalin.style.data.repository.datasource.sync.gallery.GalleryScheduleService
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author jinyalin
 * @since 2017/5/22.
 */
@Singleton
class SourcesCacheImpl @Inject
constructor(val ctx: Context) : SourcesCache {

    companion object {
        val SOURCE_ID_STYLE = 0
        val SOURCE_ID_CUSTOM = 1
    }

    var selectedId: Int by DelegateExt.preferences(ctx, "selected_source_id", SOURCE_ID_STYLE)

    val featureSource: SourceEntity
    val gallerySource: SourceEntity

    init {
        featureSource = SourceEntity(SOURCE_ID_STYLE).apply {
            title = ctx.getString(R.string.featuredart_source_title)
            iconId = R.drawable.style_ic_source
            description = ctx.getString(R.string.featuredart_source_description)
            color = Color.WHITE
            isSelected = selectedId == id
            isHasSetting = false
        }

        gallerySource = SourceEntity(SOURCE_ID_CUSTOM).apply {
            title = ctx.getString(R.string.gallery_title)
            iconId = R.drawable.gallery_ic_source
            description = ctx.getString(R.string.gallery_description)
            color = 0x4db6ac
            isSelected = selectedId == id
            isHasSetting = true
        }

        if (isUseCustomSource()) {
            GalleryScheduleService.startUp(ctx)
        }
    }

    override fun getSources(ctx: Context): Observable<List<SourceEntity>> {
        return Observable.create { emitter ->
            val sources = ArrayList<SourceEntity>()
            sources.add(featureSource)
            sources.add(gallerySource)
            emitter.onNext(sources)
        }
    }

    override fun selectSource(selectSourceId: Int): Boolean {
        var success = false
        if (featureSource.id == selectSourceId) {
            featureSource.isSelected = true
            gallerySource.isSelected = false
            selectedId = selectSourceId
            success = true

            GalleryScheduleService.shutDown(ctx)
        } else if (gallerySource.id == selectSourceId) {
            featureSource.isSelected = false
            gallerySource.isSelected = true
            selectedId = selectSourceId
            success = true

            GalleryScheduleService.startUp(ctx)
        }
        if (success) {
            notifyChanged()
        }
        return success
    }

    override fun isUseCustomSource(): Boolean {
        return gallerySource.isSelected
    }

    private fun notifyChanged() {
        ctx.contentResolver.notifyChange(StyleContract.Source.CONTENT_URI, null)
    }
}