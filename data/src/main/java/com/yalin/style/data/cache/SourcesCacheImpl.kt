package com.yalin.style.data.cache

import android.content.Context
import android.graphics.Color
import com.yalin.style.data.R
import com.yalin.style.data.entity.SourceEntity
import com.yalin.style.data.extensions.DelegateExt
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author jinyalin
 * @since 2017/5/22.
 */
@Singleton
class SourcesCacheImpl @Inject
constructor(ctx: Context) : SourcesCache {

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
            iconId = R.drawable.featuredart_ic_source
            description = ctx.getString(R.string.featuredart_source_description)
            color = Color.WHITE
            selected = selectedId == id
            hasSetting = false
        }

        gallerySource = SourceEntity(SOURCE_ID_CUSTOM).apply {
            title = ctx.getString(R.string.gallery_title)
            iconId = R.drawable.gallery_ic_source
            description = ctx.getString(R.string.gallery_description)
            color = 0x4db6ac
            selected = selectedId == id
            hasSetting = true
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

    override fun selectSource(selectedEntity: SourceEntity) {
        if (featureSource.id == selectedEntity.id) {
            featureSource.selected = true
            gallerySource.selected = false
            selectedId = selectedEntity.id
        } else if (gallerySource.id == selectedEntity.id) {
            featureSource.selected = false
            gallerySource.selected = true
            selectedId = selectedEntity.id
        }
    }

    override fun useCustomSource(): Boolean {
        return gallerySource.selected
    }
}