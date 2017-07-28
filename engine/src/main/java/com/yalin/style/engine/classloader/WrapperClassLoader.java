package com.yalin.style.engine.classloader;

import android.content.Context;
import android.os.Environment;


import java.io.File;

import dalvik.system.DexClassLoader;

/**
 * @author jinyalin
 * @since 2017/7/1.
 */

public class WrapperClassLoader {

    private static final String CORE_APK_FILE_NAME = "wallpaper.component";
    private static volatile ClassLoader classLoader;

    private static void init(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context can not be null.");
        } else if (classLoader == null) {
            synchronized (WrapperClassLoader.class) {
                if (classLoader == null) {
                    File coreFilePath =
                            new File(Environment.getExternalStorageDirectory(), CORE_APK_FILE_NAME);
                    classLoader = new DexClassLoader(coreFilePath.getAbsolutePath(),
                            context.getCacheDir().getAbsolutePath(),
                            null, context.getClassLoader());
                }
            }
        }
    }

    public static Class<?> loadClass(Context context, String str) throws Exception {
        init(context);
        return classLoader.loadClass(str);
    }
}