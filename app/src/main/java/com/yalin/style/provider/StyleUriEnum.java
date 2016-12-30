package com.yalin.style.provider;

import com.yalin.style.provider.StyleDatabase.Tables;

/**
 * YaLin 2016/12/30.
 */

public enum StyleUriEnum {
  WALLPAPER(100, "wallpaper", Tables.WALLPAPER),
  LAST_WALLPAPER(101, "wallpaper/last", null);

  public int code;
  public String path;
  public String table;

  StyleUriEnum(int code, String path, String table) {
    this.code = code;
    this.path = path;
    this.table = table;
  }
}
