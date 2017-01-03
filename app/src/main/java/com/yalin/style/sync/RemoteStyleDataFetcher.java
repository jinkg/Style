package com.yalin.style.sync;

import android.content.Context;
import android.text.TextUtils;
import com.yalin.style.BuildConfig;
import com.yalin.style.Config;
import com.yalin.style.util.LogUtil;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * YaLin 2017/1/3.
 */

public class RemoteStyleDataFetcher {

  private static final String TAG = "RemoteConferenceDataFet";

  private final Context mContext;

  private final String mWallpaperUrl;

  public RemoteStyleDataFetcher(Context context) {
    mContext = context;
    mWallpaperUrl = getWallpaperUrl();
  }

  public String fetchStyleDataIfNewer() throws IOException {
    OkHttpClient httpClient = new OkHttpClient.Builder()
        .connectTimeout(Config.DEFAULT_CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(Config.DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS)
        .build();

    Request request = new Request.Builder().url(new URL(mWallpaperUrl)).build();
    Response response = httpClient.newCall(request).execute();
    if (response == null) {
      LogUtil.f(TAG, "Request for wallpaper returned null response.");
      throw new IOException("Request for data wallpaper returned null response.");
    }
    int status = response.code();
    if (status == HttpURLConnection.HTTP_OK) {
      LogUtil.d(TAG, "Server return HTTP_OK, so new data is available.");
      String body = response.body().string();
      if (TextUtils.isEmpty(body)) {
        LogUtil.f(TAG, "Request for wallpaper returned empty data.");
        throw new IOException("Error fetching wallpaper data : no data.");
      }
      LogUtil.d(TAG, "Wallpaper " + mWallpaperUrl + " read, contents: " + body);
      return body;
    } else if (status == HttpURLConnection.HTTP_NOT_MODIFIED) {
      LogUtil.d(TAG, "HTTP_NOT_MODIFIED: data has not changed since");
      return null;
    } else {
      LogUtil.d(TAG, "Error fetching conference data: HTTP status " + status + " and manifest " +
          mWallpaperUrl);
      throw new IOException("Error fetching conference data: HTTP status " + status);
    }
  }

  private String getWallpaperUrl() {
    return BuildConfig.SERVER_WALLPAPER_ENDPOINT;

  }
}
