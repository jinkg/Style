package com.yalin.style.mapper

import com.fernandocejas.arrow.checks.Preconditions
import com.yalin.style.domain.Source
import com.yalin.style.domain.Wallpaper
import com.yalin.style.model.SourceItem
import com.yalin.style.model.WallpaperItem
import java.util.ArrayList

import javax.inject.Inject

/**
 * @author jinyalin
 * *
 * @since 2017/4/18.
 */
class WallpaperItemMapper @Inject
constructor() {

    fun transform(wallpaper: Wallpaper): WallpaperItem {
        Preconditions.checkNotNull(wallpaper, "Wallpaper can not be null.")
        val wallpaperItem = WallpaperItem()
        wallpaperItem.title = wallpaper.title
        wallpaperItem.attribution = wallpaper.attribution
        wallpaperItem.byline = wallpaper.byline
        wallpaperItem.imageUri = wallpaper.imageUri
        wallpaperItem.wallpaperId = wallpaper.wallpaperId
        wallpaperItem.liked = wallpaper.liked
        wallpaperItem.isDefault = wallpaper.isDefault
        return wallpaperItem
    }

    fun transformSources(sourceEntities: List<Source>): List<SourceItem> {
        Preconditions.checkNotNull(sourceEntities, "SourceEntity can not be null.")
        val sources = ArrayList<SourceItem>()
        for (entity in sourceEntities) {
            sources.add(transformSource(entity))
        }
        return sources
    }

    fun transformSource(source: Source): SourceItem {
        Preconditions.checkNotNull(source, "SourceEntity can not be null.")
        val sourceItem = SourceItem()
        sourceItem.id = source.id
        sourceItem.title = source.title
        sourceItem.description = source.description
        sourceItem.iconId = source.iconId
        sourceItem.selected = source.selected
        sourceItem.hasSetting = source.hasSetting
        sourceItem.color = source.color
        return sourceItem
    }
}
