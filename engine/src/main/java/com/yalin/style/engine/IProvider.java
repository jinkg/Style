package com.yalin.style.engine;

import android.content.Context;
import android.service.wallpaper.WallpaperService;

/**
 * YaLin
 * On 2017/7/27.
 */

public interface IProvider {

    WallpaperService provideProxy(Context host);
}
