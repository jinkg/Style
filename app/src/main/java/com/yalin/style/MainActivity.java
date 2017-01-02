package com.yalin.style;

import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.yalin.style.provider.StyleContract.Wallpaper;
import com.yalin.style.service.BootstrapService;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
  }

  @Override
  protected void onResume() {
    super.onResume();
    BootstrapService.startBootstrapIfNecessary(this);
  }

  public void setWallpaper(View view) {
    try {
      startActivity(new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
          .putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
              new ComponentName(MainActivity.this, StyleWallpaperService.class))
          .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    } catch (ActivityNotFoundException e) {
      try {
        startActivity(new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
      } catch (ActivityNotFoundException e2) {
        Toast.makeText(MainActivity.this, "xxx", Toast.LENGTH_LONG).show();
      }
    }
  }
}
