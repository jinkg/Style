package com.yalin.style.data.repository.datasource.provider;

/**
 * YaLin 2016/12/30.
 */

public enum StyleUriEnum {
  WALLPAPER(100, "wallpaper", StyleDatabase.Tables.WALLPAPER),
  WALLPAPER_SAVE(103, "wallpaper/save/*", null),
  WALLPAPER_KEEP(104, "wallpaper/keep/*", null),
  WALLPAPER_KEEPED(105, "wallpaper/keeped", null),
  WALLPAPER_ID(102, "wallpaper/*", null);

  public int code;
  public String path;
  public String table;

  StyleUriEnum(int code, String path, String table) {
    this.code = code;
    this.path = path;
    this.table = table;
  }
}
