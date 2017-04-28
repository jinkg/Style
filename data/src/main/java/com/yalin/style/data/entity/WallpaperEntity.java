package com.yalin.style.data.entity;

/**
 * @author jinyalin
 * @since 2017/4/18.
 */

public class WallpaperEntity {
    public int id;
    public String wallpaperId;
    public String imageUri;
    public String title;
    public String byline;
    public String attribution;

    public String checksum;

    public WallpaperEntity() {
    }

    public WallpaperEntity(WallpaperEntity entity) {
        this.id = entity.id;
        this.wallpaperId = entity.wallpaperId;
        this.imageUri = entity.imageUri;
        this.title = entity.title;
        this.byline = entity.byline;
        this.attribution = entity.attribution;

        this.checksum = entity.checksum;
    }
}
