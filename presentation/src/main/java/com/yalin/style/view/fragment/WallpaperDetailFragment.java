package com.yalin.style.view.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.yalin.style.WallpaperDetailViewport;
import com.yalin.style.R;
import com.yalin.style.event.MainContainerInsetsChangedEvent;
import com.yalin.style.event.StyleWallpaperSizeChangedEvent;
import com.yalin.style.event.SwitchingPhotosStateChangedEvent;
import com.yalin.style.event.SystemWallpaperSizeChangedEvent;
import com.yalin.style.injection.component.WallpaperComponent;
import com.yalin.style.model.WallpaperItem;
import com.yalin.style.presenter.WallpaperDetailPresenter;
import com.yalin.style.util.ScrimUtil;
import com.yalin.style.util.TypefaceUtil;
import com.yalin.style.view.WallpaperDetailView;
import com.yalin.style.view.component.DrawInsetsFrameLayout;
import com.yalin.style.view.component.PanScaleProxyView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

/**
 * @author jinyalin
 * @since 2017/4/20.
 */

public class WallpaperDetailFragment extends BaseFragment implements WallpaperDetailView,
    View.OnSystemUiVisibilityChangeListener {

  @Inject
  WallpaperDetailPresenter presenter;

  DrawInsetsFrameLayout mainContainer;
  PanScaleProxyView panScaleProxyView;
  View statusBarScrimView;
  View chromeContainer;
  TextView tvAttribution;
  TextView tvTitle;
  TextView tvByline;
  ImageButton btnNext;
  ImageButton btnOverflow;

  PopupMenu overflowMenu;

  private int currentViewportId = 0;
  private float systemWallpaperAspectRatio;
  private float styleWallpaperAspectRatio;
  private boolean deferResetViewport;

  private boolean mGuardViewportChangeListener = false;
  private boolean mOverflowMenuVisible = false;

  public static WallpaperDetailFragment createInstance() {
    return new WallpaperDetailFragment();
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getComponent(WallpaperComponent.class).inject(this);

    EventBus.getDefault().register(this);
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater,
      @Nullable ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.layout_wallpaper_detail, container, false);
    mainContainer = (DrawInsetsFrameLayout) rootView.findViewById(R.id.container);
    panScaleProxyView = (PanScaleProxyView) rootView.findViewById(R.id.pan_scale_proxy);
    statusBarScrimView = rootView.findViewById(R.id.statusbar_scrim);
    chromeContainer = rootView.findViewById(R.id.chrome_container);
    tvAttribution = (TextView) rootView.findViewById(R.id.attribution);
    tvTitle = (TextView) rootView.findViewById(R.id.title);
    tvByline = (TextView) rootView.findViewById(R.id.byline);
    btnNext = (ImageButton) rootView.findViewById(R.id.next_button);
    btnOverflow = (ImageButton) rootView.findViewById(R.id.overflow_button);
    setupDetailViews();
    return rootView;
  }

  private void setupDetailViews() {
    chromeContainer.setBackground(ScrimUtil.makeCubicGradientScrimDrawable(
        0xaa000000, 8, Gravity.BOTTOM));

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
      statusBarScrimView.setVisibility(View.GONE);
      statusBarScrimView = null;
    } else {
      statusBarScrimView.setBackground(ScrimUtil.makeCubicGradientScrimDrawable(
          0x44000000, 8, Gravity.TOP));
    }

    panScaleProxyView.setMaxZoom(5);
    panScaleProxyView.setOnViewportChangedListener(
        new PanScaleProxyView.OnViewportChangedListener() {
          @Override
          public void onViewportChanged() {
            if (mGuardViewportChangeListener) {
              return;
            }
            WallpaperDetailViewport.getInstance().setViewport(
                currentViewportId, panScaleProxyView.getCurrentViewport(), true);
          }
        });
    if (getActivity() instanceof PanScaleProxyView.OnOtherGestureListener) {
      panScaleProxyView.setOnOtherGestureListener(
          (PanScaleProxyView.OnOtherGestureListener) getActivity());
    }

    btnNext.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        presenter.getNextWallpaper();
      }
    });
    setupOverflowButton();
  }

  private void setupOverflowButton() {
    overflowMenu = new PopupMenu(getActivity(), btnOverflow);
    btnOverflow.setOnTouchListener(overflowMenu.getDragToOpenListener());
    btnOverflow.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mOverflowMenuVisible = true;
        overflowMenu.show();
      }
    });
    overflowMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
      @Override
      public void onDismiss(PopupMenu popupMenu) {
        mOverflowMenuVisible = false;
      }
    });
    overflowMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
          case R.id.action_keep:
            presenter.keepWallpaper();
            return true;
          case R.id.action_share:
            presenter.shareWallpaper();
            return true;
          case R.id.action_settings:
            return true;
        }
        return false;
      }
    });
    overflowMenu.getMenu().clear();
    overflowMenu.inflate(R.menu.style_overflow);
    overflowMenu.getMenu().add(0, R.id.action_share, 0, R.string.action_share);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    presenter.setView(this);

    SystemWallpaperSizeChangedEvent syswsce = EventBus.getDefault().getStickyEvent(
        SystemWallpaperSizeChangedEvent.class);
    if (syswsce != null) {
      onEventMainThread(syswsce);
    }

    StyleWallpaperSizeChangedEvent swsce = EventBus.getDefault().getStickyEvent(
        StyleWallpaperSizeChangedEvent.class);
    if (swsce != null) {
      onEventMainThread(swsce);
    }

    WallpaperDetailViewport wdvp = EventBus.getDefault().getStickyEvent(
        WallpaperDetailViewport.class);
    if (wdvp != null) {
      onEventMainThread(wdvp);
    }

    MainContainerInsetsChangedEvent mcisce = EventBus.getDefault().getStickyEvent(
        MainContainerInsetsChangedEvent.class);
    if (mcisce != null) {
      onEventMainThread(mcisce);
    }

    SwitchingPhotosStateChangedEvent spsce = EventBus.getDefault().getStickyEvent(
        SwitchingPhotosStateChangedEvent.class);
    if (spsce != null) {
      onEventMainThread(spsce);
    }

    if (savedInstanceState == null) {
      loadWallpaper();
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    presenter.resume();
  }

  @Override
  public void onPause() {
    super.onPause();
    presenter.pause();
  }

  @Override
  public void onStop() {
    super.onStop();
    overflowMenu.dismiss();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    presenter.destroy();
    EventBus.getDefault().unregister(this);
  }

  private void loadWallpaper() {
    presenter.initialize();
  }

  @Subscribe
  public void onEventMainThread(SystemWallpaperSizeChangedEvent syswsce) {
    if (syswsce.getHeight() > 0) {
      systemWallpaperAspectRatio = syswsce.getWidth() * 1f / syswsce.getHeight();
    } else {
      systemWallpaperAspectRatio = panScaleProxyView.getWidth()
          * 1f / panScaleProxyView.getHeight();
    }
    resetProxyViewport();
  }

  @Subscribe
  public void onEventMainThread(StyleWallpaperSizeChangedEvent swsce) {
    styleWallpaperAspectRatio = swsce.getWidth() * 1f / swsce.getHeight();
    resetProxyViewport();
  }

  @Subscribe
  public void onEventMainThread(WallpaperDetailViewport e) {
    if (!e.isFromUser() && panScaleProxyView != null) {
      mGuardViewportChangeListener = true;
      panScaleProxyView.setViewport(e.getViewport(currentViewportId));
      mGuardViewportChangeListener = false;
    }
  }

  @Subscribe
  public void onEventMainThread(SwitchingPhotosStateChangedEvent spe) {
    currentViewportId = spe.getCurrentId();
    if (panScaleProxyView != null) {
      panScaleProxyView.enablePanScale(!spe.isSwitchingPhotos());
    }
    // Process deferred wallpaper size change when done switching
    if (!spe.isSwitchingPhotos() && deferResetViewport) {
      resetProxyViewport();
    }
  }

  @Subscribe
  public void onEventMainThread(MainContainerInsetsChangedEvent spe) {
    Rect insets = spe.getInsets();
    chromeContainer.setPadding(
        insets.left, insets.top, insets.right, insets.bottom);
  }

  private void resetProxyViewport() {
    if (systemWallpaperAspectRatio == 0 || styleWallpaperAspectRatio == 0) {
      return;
    }

    deferResetViewport = false;
    SwitchingPhotosStateChangedEvent spe = EventBus.getDefault()
        .getStickyEvent(SwitchingPhotosStateChangedEvent.class);
    if (spe != null && spe.isSwitchingPhotos()) {
      deferResetViewport = true;
      return;
    }

    if (panScaleProxyView != null) {
      panScaleProxyView.setRelativeAspectRatio(
          styleWallpaperAspectRatio / systemWallpaperAspectRatio);
    }
  }

  @Override
  public void renderWallpaper(WallpaperItem wallpaperItem, boolean canKeep) {
    String titleFont = "AlegreyaSans-Black.ttf";
    String bylineFont = "AlegreyaSans-Medium.ttf";
    tvTitle.setTypeface(TypefaceUtil.getAndCache(context(), titleFont));
    tvTitle.setText(wallpaperItem.title);
    tvAttribution.setText(wallpaperItem.attribution);
    tvByline.setTypeface(TypefaceUtil.getAndCache(context(), bylineFont));
    tvByline.setText(wallpaperItem.byline);

    if (canKeep) {
      MenuItem keepItem = overflowMenu.getMenu().findItem(R.id.action_keep);
      if (keepItem == null) {
        overflowMenu.getMenu().add(0, R.id.action_keep, 0,
            wallpaperItem.keep ? R.string.action_unkeep : R.string.action_keep);
      } else {
        keepItem.setTitle(wallpaperItem.keep ? R.string.action_unkeep : R.string.action_keep);
      }
    } else {
      overflowMenu.getMenu()
          .removeItem(R.id.action_keep);
    }
  }

  @Override
  public void showNextButton(boolean show) {
    btnNext.setVisibility(show ? View.VISIBLE : View.GONE);
  }

  @Override
  public void shareWallpaper(Intent shareIntent) {
    shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(shareIntent);
  }

  @Override
  public void updateKeepWallpaper(boolean keeped) {
    overflowMenu.getMenu()
        .findItem(R.id.action_keep)
        .setTitle(keeped ? R.string.action_unkeep : R.string.action_keep);
  }

  @Override
  public void showLoading() {

  }

  @Override
  public void hideLoading() {

  }

  @Override
  public void showRetry() {

  }

  @Override
  public void hideRetry() {

  }

  @Override
  public void showError(String message) {
    showToastMessage(message);
  }

  @Override
  public Context context() {
    return getActivity();
  }

  @Override
  public void onSystemUiVisibilityChange(int visibility) {
    final boolean visible = (visibility & View.SYSTEM_UI_FLAG_LOW_PROFILE) == 0;

    final float metadataSlideDistance = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
    chromeContainer.setVisibility(View.VISIBLE);
    chromeContainer.animate()
        .alpha(visible ? 1f : 0f)
        .translationY(visible ? 0 : metadataSlideDistance)
        .setDuration(200)
        .withEndAction(new Runnable() {
          @Override
          public void run() {
            if (!visible) {
              chromeContainer.setVisibility(View.GONE);
            }
          }
        });

    if (statusBarScrimView != null) {
      statusBarScrimView.setVisibility(View.VISIBLE);
      statusBarScrimView.animate()
          .alpha(visible ? 1f : 0f)
          .setDuration(200)
          .withEndAction(new Runnable() {
            @Override
            public void run() {
              if (!visible) {
                statusBarScrimView.setVisibility(View.GONE);
              }
            }
          });
    }
  }

  public boolean isOverflowMenuVisible() {
    return mOverflowMenuVisible;
  }

}
