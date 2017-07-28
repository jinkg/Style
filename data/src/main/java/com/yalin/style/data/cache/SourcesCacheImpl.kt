package com.yalin.style.data.cache

import android.content.Context
import android.graphics.Color
import com.yalin.style.data.R
import com.yalin.style.data.entity.SourceEntity
import com.yalin.style.data.extensions.DelegateExt
import com.yalin.style.data.repository.datasource.provider.StyleContract
import com.yalin.style.data.repository.datasource.sync.gallery.GalleryScheduleService
import com.yalin.style.domain.repository.SourcesRepository.*
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

    var selectedId: Int by DelegateExt.preferences(ctx, "selected_source_id", SOURCE_ID_STYLE)

    val advanceSource: SourceEntity
    val featureSource: SourceEntity
    val gallerySource: SourceEntity

    init {
        advanceSource = SourceEntity(SOURCE_ID_ADVANCE).apply {
            title = ctx.getString(R.string.advance_source_title)
            iconId = R.drawable.style_ic_source
            description = ctx.getString(R.string.advance_source_description)
            color = Color.WHITE
            isSelected = selectedId == id
            isHasSetting = true
        }
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

        if (getUsedSourceId() == SOURCE_ID_CUSTOM) {
            GalleryScheduleService.startUp(ctx)
        }
    }

    override fun getSources(ctx: Context): Observable<List<SourceEntity>> {
        return Observable.create { emitter ->
            val sources = ArrayList<SourceEntity>()
            sources.add(advanceSource)
            sources.add(featureSource)
            sources.add(gallerySource)
            emitter.onNext(sources)
            emitter.onComplete()
        }
    }

    override fun selectSource(selectSourceId: Int): Boolean {
        var success = false
        if (featureSource.id == selectSourceId) {
            featureSource.isSelected = true
            gallerySource.isSelected = false
            advanceSource.isSelected = false
            selectedId = selectSourceId
            success = true

            GalleryScheduleService.shutDown(ctx)
        } else if (gallerySource.id == selectSourceId) {
            featureSource.isSelected = false
            gallerySource.isSelected = true
            advanceSource.isSelected = false
            selectedId = selectSourceId
            success = true

            GalleryScheduleService.startUp(ctx)
        } else if (advanceSource.id == selectSourceId) {
            featureSource.isSelected = false
            gallerySource.isSelected = false
            advanceSource.isSelected = true
            selectedId = selectSourceId
            success = true

            GalleryScheduleService.shutDown(ctx)
        }
        if (success) {
            notifyChanged()
        }
        return success
    }

    override fun getUsedSourceId(): Int {
        return selectedId
    }

    private fun notifyChanged() {
        ctx.contentResolver.notifyChange(StyleContract.Source.CONTENT_URI, null)
    }
}