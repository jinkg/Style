package com.yalin.style.engine;

import android.content.Context;

import com.yalin.style.engine.classloader.WrapperClassLoader;

/**
 * YaLin
 * On 2017/7/27.
 */

public class WrapperApi {

    public static final String ADVANCE_PROXY_CLASS =
            "com.yalin.component.ProviderImpl";


    private static IProvider sProvider = null;

    private static void check(Context context) throws Exception {
        if (sProvider == null) {
            synchronized (WrapperApi.class) {
                if (sProvider == null) {
                    IProvider provider;
                    Class providerClazz = WrapperClassLoader.loadClass(context, ADVANCE_PROXY_CLASS);
                    if (providerClazz != null) {
                        provider = (IProvider) providerClazz.newInstance();
                    } else {
                        throw new IllegalStateException("Load Provider error.");
                    }
                    sProvider = provider;
                }
            }
        }
    }

    public static WallpaperServiceProxy getProxy(Context context) {
        try {
            check(context);
            return sProvider.provideProxy(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
