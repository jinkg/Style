package com.yalin.style.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import com.yalin.style.util.SettingsUtil;
import com.yalin.style.util.WallpaperFileHelper;
import java.io.File;

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

    if (WallpaperFileHelper.copyAssets(this, "starrynight.jpg", file)) {
      SettingsUtil.markBootstrapDone(getApplicationContext());
    }
  }
}
