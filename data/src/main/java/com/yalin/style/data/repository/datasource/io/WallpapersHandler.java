package com.yalin.style.data.repository.datasource.io;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.yalin.style.data.SyncConfig;
import com.yalin.style.data.entity.WallpaperEntity;
import com.yalin.style.data.log.LogUtil;
import com.yalin.style.data.repository.datasource.provider.StyleContract;
import com.yalin.style.data.repository.datasource.provider.StyleContract.Wallpaper;
import com.yalin.style.data.repository.datasource.provider.StyleContractHelper;
import com.yalin.style.data.utils.TimeUtil;
import com.yalin.style.data.utils.WallpaperFileHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * YaLin 2017/1/3.
 */

public class WallpapersHandler extends JSONHandler {

  private static final String TAG = "WallpapersHandler";

  private ArrayList<WallpaperEntity> mWallpapers = new ArrayList<>();

  public WallpapersHandler(Context context) {
    super(context);
  }

  @Override
  public void makeContentProviderOperations(ArrayList<ContentProviderOperation> list) {
    Uri uri = StyleContractHelper.setUriAsCalledFromSyncAdapter(Wallpaper.CONTENT_URI);
    list.add(ContentProviderOperation.newDelete(uri).build());

    Set<String> validFiles = new HashSet<>();
    validFiles.addAll(queryKeepedWallpapers());
    for (WallpaperEntity wallpaper : mWallpapers) {
      Uri wallpaperUri = Wallpaper.buildWallpaperSaveUri(wallpaper.wallpaperId);
      if (downloadWallpaper(wallpaper, wallpaperUri)
          && WallpaperFileHelper.ensureChecksumValid(mContext,
          wallpaper.checksum, wallpaper.wallpaperId)) {
        LogUtil.D(TAG, "download wallpaper " + wallpaperUri
            + " success, do output wallpaper.");
        outputWallpaper(wallpaper, list, wallpaperUri.toString());
        validFiles.add(wallpaper.wallpaperId);
      }
    }
    // delete old wallpapers
    WallpaperFileHelper.deleteOldFiles(mContext, validFiles);
  }

  @Override
  public void process(JsonElement element) {
    WallpaperEntity[] wallpapers = new Gson().fromJson(element, WallpaperEntity[].class);
    mWallpapers.ensureCapacity(wallpapers.length);
    Collections.addAll(mWallpapers, wallpapers);
  }

  private Set<String> queryKeepedWallpapers() {
    Cursor cursor = null;
    try {
      cursor = mContext.getContentResolver().query(Wallpaper.CONTENT_KEEPED_URI,
          null, null, null, null);
      return readCursor(cursor);
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
  }

  public Set<String> readCursor(Cursor cursor) {
    Set<String> validWallpapers = new HashSet<>();
    while (cursor != null && cursor.moveToNext()) {
      WallpaperEntity wallpaperEntity = WallpaperEntity.readEntityFromCursor(cursor);
      if (!wallpaperEntity.keep) {
        continue;
      }
      try {
        // valid input stream
        InputStream is = mContext.getContentResolver().openInputStream(
            StyleContract.Wallpaper.buildWallpaperUri(
                wallpaperEntity.wallpaperId));
        validWallpapers.add(wallpaperEntity.wallpaperId);
        if (is != null) {
          is.close();
        }
      } catch (Exception e) {
        LogUtil.D(TAG, "File not found with wallpaper id : "
            + wallpaperEntity.wallpaperId);
      }
    }
    return validWallpapers;
  }

  private void outputWallpaper(WallpaperEntity wallpaper,
      ArrayList<ContentProviderOperation> list, String uriString) {
    Uri uri = StyleContractHelper.setUriAsCalledFromSyncAdapter(Wallpaper.CONTENT_URI);
    ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(uri);
    builder.withValue(Wallpaper.COLUMN_NAME_WALLPAPER_ID, wallpaper.wallpaperId);
    builder.withValue(Wallpaper.COLUMN_NAME_TITLE, wallpaper.title);
    builder.withValue(Wallpaper.COLUMN_NAME_IMAGE_URI, uriString);
    builder.withValue(Wallpaper.COLUMN_NAME_BYLINE, wallpaper.byline);
    builder.withValue(Wallpaper.COLUMN_NAME_ATTRIBUTION, wallpaper.attribution);
    builder.withValue(Wallpaper.COLUMN_NAME_ADD_DATE, TimeUtil.getCurrentTime(mContext));
    builder.withValue(Wallpaper.COLUMN_NAME_KEEP, wallpaper.keep ? 1 : 0);

    list.add(builder.build());
  }

  private boolean downloadWallpaper(WallpaperEntity wallpaper, Uri uri) {
    LogUtil.D(TAG, "Start download wallpaper uri = " + uri);
    OutputStream os = null;
    InputStream is = null;
    try {
      os = mContext.getContentResolver().openOutputStream(uri);
      if (os == null) {
        return false;
      }
      OkHttpClient httpClient = new OkHttpClient.Builder()
          .connectTimeout(SyncConfig.DEFAULT_CONNECT_TIMEOUT, TimeUnit.SECONDS)
          .readTimeout(SyncConfig.DEFAULT_DOWNLOAD_TIMEOUT, TimeUnit.SECONDS)
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
        LogUtil.E(TAG, "Download wallpaper " + wallpaper.title + " failed.");
        return false;
      }
    } catch (IOException e) {
      e.printStackTrace();
      LogUtil.E(TAG, "Download wallpaper " + wallpaper.title + " failed.", e);
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
