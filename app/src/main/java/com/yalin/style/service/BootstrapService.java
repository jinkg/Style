package com.yalin.style.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import com.yalin.style.util.SettingsUtil;
import com.yalin.style.util.WallpaperFileHelper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * YaLin On 2017/1/2.
 */

public class BootstrapService extends IntentService {

  private static final String TAG = "BootstrapService";

  public BootstrapService() {
    super(TAG);
  }

  public static void startBootstrapIfNecessary(Context context) {
    if (!SettingsUtil.isBootstrapDone(context)) {
      context.startService(new Intent(context, BootstrapService.class));
    }
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    File directory = new File(getFilesDir(), WallpaperFileHelper.WALLPAPER_FOLDER);
    if (!directory.exists() && !directory.mkdir()) {
      return;
    }
    File file = new File(directory, WallpaperFileHelper.BOOTSTRAP_FILE_NAME);
    if (file.exists()) {
      file.delete();
    }
    InputStream is = null;
    FileOutputStream fos = null;
    try {
      is = getAssets().open("starrynight.jpg");
      fos = new FileOutputStream(file);
      byte[] buffer = new byte[2048];
      int len;
      while ((len = is.read(buffer)) > 0) {
        fos.write(buffer, 0, len);
      }
      SettingsUtil.markBootstrapDone(getApplicationContext());
    } catch (IOException e) {
      e.printStackTrace();
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
}
