package com.yalin.style.io;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.yalin.style.Config;
import com.yalin.style.model.WallpaperItem;
import com.yalin.style.provider.StyleContract.Wallpaper;
import com.yalin.style.provider.StyleContractHelper;
import com.yalin.style.util.TimeUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * YaLin 2017/1/3.
 */

public class WallpapersHandler extends JSONHandler {

  private ArrayList<WallpaperItem> mWallpapers = new ArrayList<>();

  public WallpapersHandler(Context context) {
    super(context);
  }

  @Override
  public void makeContentProviderOperations(ArrayList<ContentProviderOperation> list) {
    Uri uri = StyleContractHelper.setUriAsCalledFromSyncAdapter(Wallpaper.CONTENT_URI);
    list.add(ContentProviderOperation.newDelete(uri).build());
    for (WallpaperItem wallpaper : mWallpapers) {
      Uri wallpaperUri = Wallpaper.buildWallpaperUri(wallpaper.wallpaperId);
      if (downloadWallpaper(wallpaper, wallpaperUri)) {
        outputWallpaper(wallpaper, list, wallpaperUri.toString());
      }
    }
  }

  @Override
  public void process(JsonElement element) {
    WallpaperItem[] wallpapers = new Gson().fromJson(element, WallpaperItem[].class);
    mWallpapers.ensureCapacity(wallpapers.length);
    Collections.addAll(mWallpapers, wallpapers);
  }

  private void outputWallpaper(WallpaperItem wallpaper,
      ArrayList<ContentProviderOperation> list, String uriString) {
    Uri uri = StyleContractHelper.setUriAsCalledFromSyncAdapter(Wallpaper.CONTENT_URI);
    ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(uri);
    builder.withValue(Wallpaper.COLUMN_NAME_WALLPAPER_ID, wallpaper.wallpaperId);
    builder.withValue(Wallpaper.COLUMN_NAME_TITLE, wallpaper.title);
    builder.withValue(Wallpaper.COLUMN_NAME_IMAGE_URI, uriString);
    builder.withValue(Wallpaper.COLUMN_NAME_BYLINE, wallpaper.byline);
    builder.withValue(Wallpaper.COLUMN_NAME_ATTRIBUTION, wallpaper.attribution);
    builder.withValue(Wallpaper.COLUMN_NAME_ADD_DATE, TimeUtil.getCurrentTime(mContext));

    list.add(builder.build());
  }

  private boolean downloadWallpaper(WallpaperItem wallpaper, Uri uri) {
    OutputStream os = null;
    InputStream is = null;
    try {
      os = mContext.getContentResolver().openOutputStream(uri);
      if (os == null) {
        return false;
      }
      OkHttpClient httpClient = new OkHttpClient.Builder()
          .connectTimeout(Config.DEFAULT_CONNECT_TIMEOUT, TimeUnit.SECONDS)
          .readTimeout(Config.DEFAULT_DOWNLOAD_TIMEOUT, TimeUnit.SECONDS)
          .build();
      Request request = new Request.Builder().url(new URL(wallpaper.imageUri)).build();

      Response response = httpClient.newCall(request).execute();
      int responseCode = response.code();
      if (responseCode >= 200 && responseCode < 300) {
        is = response.body().byteStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) > 0) {
          os.write(buffer, 0, bytesRead);
        }
        os.flush();
        return true;
      } else {
        return false;
      }
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    } finally {
      try {
        if (os != null) {
          os.close();
        }
        if (is != null) {
          is.close();
        }
      } catch (IOException e) {
        // ignore
      }
    }
  }
}
