package com.yalin.style.data.utils;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;

import com.yalin.style.data.log.LogUtil;
import com.yalin.style.data.repository.datasource.provider.StyleContract.Wallpaper;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * YaLin On 2017/1/2.
 */

public class WallpaperFileHelper {

  private static final String TAG = "WallpaperFileHelper";

  public static final String WALLPAPER_FOLDER = "wallpaper";

  public static final String BOOTSTRAP_FILE_NAME = "starrynight.jpg";

  public static ParcelFileDescriptor openReadFile(Context context, Uri uri, String mode) {
    LogUtil.d(TAG, "Read file Uri=" + (uri == null ? null : uri.toString()));

    String wallpaperId = uri == null ? BOOTSTRAP_FILE_NAME : Wallpaper.getWallpaperId(uri);

    File directory = new File(context.getFilesDir(), WALLPAPER_FOLDER);
    if (!directory.exists() && !directory.mkdir()) {
      return null;
    }

    WallpaperFileHelper.deleteOldFiles(context, wallpaperId);

    File file = new File(directory, wallpaperId);
    if (!file.exists()) {
      LogUtil.d(TAG, "Target file not exist, select bootstrap file");
      file = new File(directory, BOOTSTRAP_FILE_NAME);
    }

    try {
      return ParcelFileDescriptor.open(file, parseMode(mode));
    } catch (FileNotFoundException e) {
      return null;
    }
  }

  public static ParcelFileDescriptor openWriteFile(Context context, Uri uri, String mode) {
    String wallpaperId = Wallpaper.getWallpaperId(uri);
    File directory = new File(context.getFilesDir(), WALLPAPER_FOLDER);
    if (!directory.exists() && !directory.mkdir()) {
      return null;
    }
    File file = new File(directory, wallpaperId);
    try {
      return ParcelFileDescriptor.open(file, parseMode(mode));
    } catch (FileNotFoundException e) {
      return null;
    }
  }

  private static int parseMode(String mode) {
    final int modeBits;
    if ("r".equals(mode)) {
      modeBits = ParcelFileDescriptor.MODE_READ_ONLY;
    } else if ("w".equals(mode) || "wt".equals(mode)) {
      modeBits = ParcelFileDescriptor.MODE_WRITE_ONLY
          | ParcelFileDescriptor.MODE_CREATE
          | ParcelFileDescriptor.MODE_TRUNCATE;
    } else if ("wa".equals(mode)) {
      modeBits = ParcelFileDescriptor.MODE_WRITE_ONLY
          | ParcelFileDescriptor.MODE_CREATE
          | ParcelFileDescriptor.MODE_APPEND;
    } else if ("rw".equals(mode)) {
      modeBits = ParcelFileDescriptor.MODE_READ_WRITE
          | ParcelFileDescriptor.MODE_CREATE;
    } else if ("rwt".equals(mode)) {
      modeBits = ParcelFileDescriptor.MODE_READ_WRITE
          | ParcelFileDescriptor.MODE_CREATE
          | ParcelFileDescriptor.MODE_TRUNCATE;
    } else {
      throw new IllegalArgumentException("Bad mode '" + mode + "'");
    }
    return modeBits;
  }

  public static boolean copyAssets(Context context, String name, File output) {
    InputStream is = null;
    FileOutputStream fos = null;
    try {
      is = context.getAssets().open(name);
      fos = new FileOutputStream(output);
      byte[] buffer = new byte[2048];
      int len;
      while ((len = is.read(buffer)) > 0) {
        fos.write(buffer, 0, len);
      }
      fos.flush();
      return true;
    } catch (IOException e) {
      return false;
    } finally {
      try {
        if (is != null) {
          is.close();
        }
        if (fos != null) {
          fos.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public static void deleteOldFiles(Context context, final String excludeName) {
    File directory = new File(context.getFilesDir(), WALLPAPER_FOLDER);
    if (!directory.exists()) {
      return;
    }
    File[] files = directory.listFiles(new FileFilter() {
      @Override
      public boolean accept(File fileName) {
        return !fileName.getName().contains(BOOTSTRAP_FILE_NAME) &&
            !TextUtils.equals(fileName.getName(), excludeName);
      }
    });
    for (File file : files) {
      //noinspection ResultOfMethodCallIgnored
      file.delete();
    }
  }
}
