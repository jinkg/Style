package com.yalin.style.engine;

import android.content.Context;

/**
 * YaLin
 * On 2017/7/27.
 */

public interface IProvider {

    WallpaperServiceProxy provideProxy(Context host);
}
