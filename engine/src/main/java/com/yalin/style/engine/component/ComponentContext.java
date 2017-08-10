package com.yalin.style.engine.component;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.content.res.Resources;

import com.yalin.style.engine.WallpaperActiveCallback;
import com.yalin.style.engine.resource.ResourcesManager;


/**
 * @author jinyalin
 * @since 2017/7/3.
 */

public class ComponentContext extends ContextWrapper implements WallpaperActiveCallback {
    private String componentPath;
    private WallpaperActiveCallback origin;

    private Resources mResources;
    private ClassLoader mClassLoader;

    public ComponentContext(Context base, String componentPath) {
        super(base.getApplicationContext());
        this.componentPath = componentPath;
        if (base instanceof WallpaperActiveCallback) {
            origin = (WallpaperActiveCallback) base;
        }
    }

    @Override
    public Resources getResources() {
        if (mResources == null) {
            mResources = ResourcesManager.createResources(getBaseContext(), componentPath);
        }
        return mResources;
    }

    @Override
    public ClassLoader getClassLoader() {
        return getClassLoader(componentPath);
    }


    private ClassLoader getClassLoader(String componentFilePath) {
        if (mClassLoader == null) {
            mClassLoader = new StyleClassLoader(this, componentFilePath,
                    getCacheDir().getAbsolutePath(), null, getBaseContext().getClassLoader());
        }
        return mClassLoader;
    }

    @Override
    public AssetManager getAssets() {
        return getResources().getAssets();
    }

    @Override
    public Context getApplicationContext() {
        return this;
    }

    @Override
    public void onWallpaperActivate() {
        if (origin != null) {
            origin.onWallpaperActivate();
        }
    }

    @Override
    public void onWallpaperDeactivate() {
        if (origin != null) {
            origin.onWallpaperDeactivate();
        }
    }
}

