package com.yalin.style.engine.resource;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;

import java.lang.ref.WeakReference;
import java.util.Map;

/**
 * @author jinyalin
 * @since 2017/7/1.
 */

public class ResourcesManager {
    public static synchronized Resources createCompatResources(Context context, String apkFile) {
        Resources hostResources = context.getResources();
        AssetManager assetManager = createAssetManager(context, apkFile);
        return new CompatResources(hostResources, assetManager, hostResources.getDisplayMetrics(),
                hostResources.getConfiguration());
    }

    public static synchronized Resources createResources(Context context, String apkFile) {
        Resources hostResources = context.getResources();
        AssetManager assetManager = createAssetManager(context, apkFile);
        return new Resources(assetManager, hostResources.getDisplayMetrics(),
                hostResources.getConfiguration());
    }

    private static AssetManager createAssetManager(Context context, String apkFile) {
        try {
            AssetManager am = AssetManager.class.newInstance();
            ReflectUtil.invoke(AssetManager.class, am, "addAssetPath", apkFile);
            return am;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static ResourcesCompat createResourcesCompat(Resources hostResources) {
        if (BrandUtil.isMiUi(hostResources)) {
            return new MiUiResourcesCompat();
        } else if (BrandUtil.isVivo(hostResources)) {
            return new VivoResourcesCompat();
        } else if (BrandUtil.isNubia(hostResources)) {
            return new NubiaResourcesCompat();
        } else if (BrandUtil.isNotRawResources(hostResources)) {
            return new AdaptationResourcesCompat();
        } else {
            // is raw android resources
            return new AndroidResourcesCompat();
        }
    }

    private static void hookResources(Context context, Resources resources) {
        if (Build.VERSION.SDK_INT >= 24) {
            return;
        }

        try {
            context = getContextImpl(context);
            ReflectUtil.setField(context.getClass(), context, "mResources", resources);
            Object loadedApk = ReflectUtil.getField(context.getClass(), context, "mPackageInfo");
            ReflectUtil.setField(loadedApk.getClass(), loadedApk, "mResources", resources);

            Object activityThread = getActivityThread(context);
            Object resManager = ReflectUtil.getField(activityThread.getClass(),
                    activityThread, "mResourcesManager");
            //noinspection unchecked
            Map<Object, WeakReference<Resources>> map =
                    (Map<Object, WeakReference<Resources>>)
                            ReflectUtil.getField(resManager.getClass(),
                                    resManager, "mActiveResources");
            Object key = map.keySet().iterator().next();
            map.put(key, new WeakReference<>(resources));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Context getContextImpl(Context base) throws Exception {
        Context impl;
        if (base instanceof ContextWrapper) {
            impl = (Context) ReflectUtil.getField(ContextWrapper.class, base, "mBase");
        } else {
            impl = base;
        }
        return impl;
    }

    private static Object getActivityThread(Context base) throws Exception {
        Class<?> activityThreadClazz = Class.forName("android.app.ActivityThread");
        Object activityThread = null;
        try {
            activityThread = ReflectUtil.getField(activityThreadClazz,
                    null, "sCurrentActivityThread");
        } catch (Exception e) {
            // ignored
        }
        if (activityThread == null) {
            activityThread = ((ThreadLocal<?>) ReflectUtil.getField(activityThreadClazz,
                    null, "sThreadLocal")).get();
        }
        return activityThread;

    }

    private static abstract class ResourcesCompat {
        abstract Resources createResources(Context context,
                                           Resources hostResources,
                                           AssetManager assetManager) throws Exception;
    }

    private static final class MiUiResourcesCompat extends ResourcesCompat {
        @Override
        Resources createResources(Context context,
                                  Resources hostResources,
                                  AssetManager assetManager) throws Exception {
            Class resourcesClazz = Class.forName("android.content.res.MiuiResources");
            return (Resources) ReflectUtil.invokeConstructor(resourcesClazz,
                    new Class[]{AssetManager.class, DisplayMetrics.class, Configuration.class},
                    assetManager, hostResources.getDisplayMetrics(),
                    hostResources.getConfiguration());
        }
    }

    private static final class VivoResourcesCompat extends ResourcesCompat {
        @Override
        Resources createResources(Context hostContext,
                                  Resources hostResources,
                                  AssetManager assetManager) throws Exception {
            Class resourcesClazz = Class.forName("android.content.res.VivoResources");
            Resources newResources = (Resources) ReflectUtil.invokeConstructor(resourcesClazz,
                    new Class[]{AssetManager.class, DisplayMetrics.class, Configuration.class},
                    assetManager, hostResources.getDisplayMetrics(),
                    hostResources.getConfiguration());
            ReflectUtil.invokeNoException(resourcesClazz, newResources, "init",
                    new Class[]{String.class}, hostContext.getPackageName());
            Object themeValues = ReflectUtil.getFieldNoException(resourcesClazz,
                    hostResources, "mThemeValues");
            ReflectUtil.setFieldNoException(resourcesClazz, newResources,
                    "mThemeValues", themeValues);
            return newResources;
        }
    }

    private static final class NubiaResourcesCompat extends ResourcesCompat {
        @Override
        Resources createResources(Context context,
                                  Resources hostResources,
                                  AssetManager assetManager) throws Exception {
            Class resourcesClazz = Class.forName("android.content.res.NubiaResources");
            return (Resources) ReflectUtil.invokeConstructor(resourcesClazz,
                    new Class[]{AssetManager.class, DisplayMetrics.class, Configuration.class},
                    assetManager, hostResources.getDisplayMetrics(), hostResources.getConfiguration());
        }
    }

    private static final class AdaptationResourcesCompat extends ResourcesCompat {
        @Override
        Resources createResources(Context context,
                                  Resources hostResources,
                                  AssetManager assetManager) throws Exception {
            Resources newResources;
            try {
                Class resourcesClazz = hostResources.getClass();
                newResources = (Resources) ReflectUtil.invokeConstructor(resourcesClazz,
                        new Class[]{AssetManager.class, DisplayMetrics.class, Configuration.class},
                        assetManager, hostResources.getDisplayMetrics(),
                        hostResources.getConfiguration());
            } catch (Exception e) {
                newResources = new Resources(assetManager,
                        hostResources.getDisplayMetrics(),
                        hostResources.getConfiguration());
            }

            return newResources;
        }
    }

    private static final class AndroidResourcesCompat extends ResourcesCompat {

        @Override
        Resources createResources(Context context,
                                  Resources hostResources,
                                  AssetManager assetManager) throws Exception {
            return new Resources(assetManager,
                    hostResources.getDisplayMetrics(),
                    hostResources.getConfiguration());
        }
    }
}
