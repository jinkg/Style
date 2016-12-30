package com.yalin.style.render;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * YaLin 2016/12/30.
 */

public abstract class RenderController {

  protected Context mContext;
  protected StyleBlurRenderer mRenderer;
  protected Callbacks mCallbacks;
  protected boolean mVisible;
  private BitmapRegionLoader mQueuedBitmapRegionLoader;

  public RenderController(Context context, StyleBlurRenderer renderer,
      Callbacks callbacks) {
    mRenderer = renderer;
    mContext = context;
    mCallbacks = callbacks;
  }

  public void destroy() {
    if (mQueuedBitmapRegionLoader != null) {
      mQueuedBitmapRegionLoader.destroy();
    }
  }

  private void throttledForceReloadCurrentArtwork() {
    mThrottledForceReloadHandler.removeMessages(0);
    mThrottledForceReloadHandler.sendEmptyMessageDelayed(0, 250);
  }

  private Handler mThrottledForceReloadHandler = new Handler(Looper.getMainLooper()) {
    @Override
    public void handleMessage(Message msg) {
      reloadCurrentArtwork(true);
    }
  };

  protected abstract BitmapRegionLoader openDownloadedCurrentArtwork(boolean forceReload);

  public void reloadCurrentArtwork(final boolean forceReload) {
    new AsyncTask<Void, Void, BitmapRegionLoader>() {
      @Override
      protected BitmapRegionLoader doInBackground(Void... voids) {
        // openDownloadedCurrentArtwork should be called on a background thread
        return openDownloadedCurrentArtwork(forceReload);
      }

      @Override
      protected void onPostExecute(final BitmapRegionLoader bitmapRegionLoader) {
        if (bitmapRegionLoader == null) {
          return;
        }

        mCallbacks.queueEventOnGlThread(new Runnable() {
          @Override
          public void run() {
            if (mVisible) {
              mRenderer.setAndConsumeBitmapRegionLoader(bitmapRegionLoader);
            } else {
              mQueuedBitmapRegionLoader = bitmapRegionLoader;
            }
          }
        });
      }
    }.execute((Void) null);
  }

  public void setVisible(boolean visible) {
    mVisible = visible;
    if (visible) {
      mCallbacks.queueEventOnGlThread(new Runnable() {
        @Override
        public void run() {
          if (mQueuedBitmapRegionLoader != null) {
            mRenderer.setAndConsumeBitmapRegionLoader(mQueuedBitmapRegionLoader);
            mQueuedBitmapRegionLoader = null;
          }
        }
      });
      mCallbacks.requestRender();
    }
  }

  public interface Callbacks {

    void queueEventOnGlThread(Runnable runnable);

    void requestRender();
  }
}
