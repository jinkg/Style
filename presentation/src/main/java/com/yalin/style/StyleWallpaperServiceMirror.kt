package com.yalin.style

/**
 * YaLin 2016/12/30.
 */
class StyleWallpaperServiceMirror : StyleWallpaperService() {

    override fun getWallpaperTargetClass(): Class<*> {
        return StyleWallpaperService::class.java
    }
}
