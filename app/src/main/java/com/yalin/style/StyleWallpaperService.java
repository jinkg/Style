package com.yalin.style;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build.VERSION;
import android.support.v4.os.UserManagerCompat;
import android.view.SurfaceHolder;
import com.yalin.style.injection.RenderControllerProvider;
import com.yalin.style.render.RenderController;
import com.yalin.style.render.StyleBlurRenderer;
import com.yalin.style.sync.SyncHelper;
import com.yalin.style.sync.account.Account;
import net.rbgrn.android.glwallpaperservice.GLWallpaperService;

/**
 * YaLin 2016/12/30.
 */

public class StyleWallpaperService extends GLWallpaperService {

  private static final String TAG = "StyleWallpaperService";

  private boolean mInitialized = false;
  private BroadcastReceiver mUnlockReceiver;

  @Override
  public Engine onCreateEngine() {
    return new StyleWallpaperEngine();
  }

  @Override
  public void onCreate() {
    super.onCreate();
    Account.createSyncAccount(this);
    SyncHelper.updateSyncInterval(this);

    if (UserManagerCompat.isUserUnlocked(this)) {
      initialize();
    } else if (VERSION.SDK_INT >= 24) {
      mUnlockReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          initialize();
          unregisterReceiver(this);
        }
      };
      IntentFilter filter = new IntentFilter(Intent.ACTION_USER_UNLOCKED);
      registerReceiver(mUnlockReceiver, filter);
    }
  }

  private void initialize() {

    mInitialized = true;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (mInitialized) {
      //todo
    } else {
      unregisterReceiver(mUnlockReceiver);
    }
  }

  private class StyleWallpaperEngine extends GLEngine implements
      StyleBlurRenderer.Callbacks,
      RenderController.Callbacks {

    private StyleBlurRenderer mRenderer;
    private RenderController mRenderController;

    private boolean mVisible = true;

    @Override
    public void onCreate(SurfaceHolder surfaceHolder) {
      super.onCreate(surfaceHolder);

      mRenderer = new StyleBlurRenderer(StyleWallpaperService.this, this);
      mRenderer.setIsPreview(isPreview());
//      RenderControllerProvider.setStubRenderController(
//          new DemoRenderController(StyleWallpaperService.this, mRenderer, this, true));
      mRenderController = RenderControllerProvider
          .providerRenderController(StyleWallpaperService.this, mRenderer, this);

      setEGLContextClientVersion(2);
      setEGLConfigChooser(8, 8, 8, 0, 0, 0);
      setRenderer(mRenderer);
      setRenderMode(RENDERMODE_WHEN_DIRTY);
      requestRender();
    }

    @Override
    public void onDestroy() {
      super.onDestroy();
      queueEvent(new Runnable() {
        @Override
        public void run() {
          if (mRenderer != null) {
            mRenderer.destroy();
          }
        }
      });
      mRenderController.destroy();
    }

    @Override
    public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
      super.onSurfaceChanged(holder, format, width, height);
      mRenderController.reloadCurrentArtwork(true);
    }

    @Override
    public void onVisibilityChanged(boolean visible) {
      mVisible = visible;
      mRenderController.setVisible(visible);
    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep,
        float yOffsetStep, int xPixelOffset, int yPixelOffset) {
      super.onOffsetsChanged(xOffset, yOffset, xOffsetStep,
          yOffsetStep, xPixelOffset, yPixelOffset);
      mRenderer.setNormalOffsetX(xOffset);
    }

    @Override
    public void queueEventOnGlThread(Runnable runnable) {
      queueEvent(runnable);
    }

    @Override
    public void requestRender() {
      if (mVisible) {
        super.requestRender();
      }
    }
  }
}
