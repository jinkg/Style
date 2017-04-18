package com.yalin.style.render;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.support.media.ExifInterface;
import android.util.Log;
import com.yalin.style.data.repository.datasource.provider.StyleContract;
import java.io.IOException;
import java.io.InputStream;

/**
 * YaLin 2016/12/30.
 */

public class RealRenderController extends RenderController {

  private static final String TAG = "RealRenderController";

  private ContentObserver mContentObserver;

  public RealRenderController(Context context, StyleBlurRenderer renderer,
      Callbacks callbacks) {
    super(context, renderer, callbacks);
    mContentObserver = new ContentObserver(new Handler()) {
      @Override
      public void onChange(boolean selfChange, Uri uri) {
        reloadCurrentArtwork(false);
      }
    };
    context.getContentResolver().registerContentObserver(StyleContract.Wallpaper.CONTENT_URI,
        true, mContentObserver);
    reloadCurrentArtwork(false);
  }

  @Override
  public void destroy() {
    super.destroy();
    mContext.getContentResolver().unregisterContentObserver(mContentObserver);
  }

  @Override
  protected BitmapRegionLoader openDownloadedCurrentArtwork(boolean forceReload) {
    // Load the stream
    try {
      // Check if there's rotation
      int rotation = 0;
      InputStream in = null;
      try {
        in = mContext.getContentResolver()
            .openInputStream(StyleContract.Wallpaper.CONTENT_URI);
        if (in == null) {
          return null;
        }
        ExifInterface exifInterface = new ExifInterface(in);
        int orientation = exifInterface.getAttributeInt(
            ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        switch (orientation) {
          case ExifInterface.ORIENTATION_ROTATE_90:
            rotation = 90;
            break;
          case ExifInterface.ORIENTATION_ROTATE_180:
            rotation = 180;
            break;
          case ExifInterface.ORIENTATION_ROTATE_270:
            rotation = 270;
            break;
        }
      } catch (IOException e) {
        Log.w(TAG, "Couldn't open EXIF interface on artwork", e);
      } finally {
        if (in != null) {
          //noinspection ThrowFromFinallyBlock
          in.close();
        }
      }
      return BitmapRegionLoader.newInstance(
          mContext.getContentResolver().openInputStream(StyleContract.Wallpaper.CONTENT_URI),
          rotation);
    } catch (IOException e) {
      Log.e(TAG, "Error loading image", e);
      return null;
    }
  }
}
