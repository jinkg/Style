package com.yalin.style.util;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import com.yalin.style.provider.StyleContract.Wallpaper;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * YaLin On 2017/1/2.
 */

public class WallpaperFileHelper {

  public static final String WALLPAPER_FOLDER = "wallpaper";

  public static final String BOOTSTRAP_FILE_NAME = "starrynight.jpg";

  public static final String CURRENT_FILE_NAME = "current";

  public static ParcelFileDescriptor openReadFile(Context context, Uri uri, String mode) {
    String wallpaperId = uri == null ? BOOTSTRAP_FILE_NAME : Wallpaper.getWallpaperId(uri);
    File directory = new File(context.getFilesDir(), WALLPAPER_FOLDER);
    if (!directory.exists() && !directory.mkdir()) {
      return null;
    }
    File file = new File(directory, wallpaperId);
    if (!file.exists()) {
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

}
