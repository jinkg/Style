package com.yalin.style.data.repository.datasource.provider;

/**
 * YaLin 2016/12/30.
 */

public enum StyleUriEnum {
    WALLPAPER(100, "wallpaper", StyleDatabase.Tables.WALLPAPER),
    WALLPAPER_SAVE(103, "wallpaper/save/*", null),
    WALLPAPER_LIKE(104, "wallpaper/like/*", null),
    WALLPAPER_LIKED(105, "wallpaper/liked", null),
    WALLPAPER_ID(102, "wallpaper/*", null),

    GALLERY(200, "gallery", StyleDatabase.Tables.GALLERY),
    GALLERY_URI(202, "gallery/uri/*", null),
    GALLERY_ID(201, "gallery/*", null),

    ADVANCE_WALLPAPER(300, "advance_wallpaper", StyleDatabase.Tables.ADVANCE_WALLPAPER),
    ADVANCE_WALLPAPER_SELECTED(302, "advance_wallpaper/selected", null),
    ADVANCE_WALLPAPER_ID(301, "advance_wallpaper/*", null);


    public int code;
    public String path;
    public String table;

    StyleUriEnum(int code, String path, String table) {
        this.code = code;
        this.path = path;
        this.table = table;
    }
}
