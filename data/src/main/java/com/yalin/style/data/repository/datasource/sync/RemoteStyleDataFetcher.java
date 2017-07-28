package com.yalin.style.data.repository.datasource.sync;

import android.content.Context;

import com.yalin.style.data.BuildConfig;
import com.yalin.style.data.repository.datasource.net.DataFetcher;

/**
 * YaLin 2017/1/3.
 */

public class RemoteStyleDataFetcher extends DataFetcher {


    public RemoteStyleDataFetcher(Context context) {
        super(context);
    }

    @Override
    protected String getUrl() {
        return BuildConfig.SERVER_WALLPAPER_ENDPOINT;
    }
}
