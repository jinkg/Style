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

    val sources = ArrayList<SourceEntity>()

    var selectedId: Int by DelegateExt.preferences(ctx, "selected_source_id", SOURCE_ID_STYLE)

    init {
        val featureSource = SourceEntity(SOURCE_ID_STYLE).apply {
            title = ctx.getString(R.string.featuredart_source_title)
            iconId = R.drawable.featuredart_ic_source
            description = ctx.getString(R.string.featuredart_source_description)
            color = Color.WHITE
            selected = selectedId == id
            hasSetting = false
        }
        sources.add(featureSource)

        val gallerySource = SourceEntity(SOURCE_ID_CUSTOM).apply {
            title = ctx.getString(R.string.gallery_title)
            iconId = R.drawable.gallery_ic_source
            description = ctx.getString(R.string.gallery_description)
            color = 0x4db6ac
            selected = selectedId == featureSource.id
            hasSetting = true
        }
        sources.add(gallerySource)
    }

    override fun getSources(ctx: Context): Observable<List<SourceEntity>> {
        return Observable.create { emitter ->
            emitter.onNext(sources)
        }
    }

    override fun selectSource(selectedEntity: SourceEntity) {
        for (source in sources) {
            if (source.id == selectedEntity.id) {
                source.selected = true
                selectedId = selectedEntity.id
                break
            }
        }
    }

    override fun useCustomSource(): Boolean {
        return !sources[0].selected && sources[1].selected
    }
}