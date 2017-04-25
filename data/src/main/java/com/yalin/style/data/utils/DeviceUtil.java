package com.yalin.style.data.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;

/**
 * @author jinyalin
 * @since 2017/4/25.
 */

public class DeviceUtil {

    private DeviceUtil() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    public static int getSDKVersion() {
        return android.os.Build.VERSION.SDK_INT;
    }

    @SuppressLint("HardwareIds")
    public static String getAndroidID(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static String getManufacturer() {
        return Build.MANUFACTURER;
    }

    public static String getModel() {
        String model = Build.MODEL;
        if (model != null) {
            model = model.trim().replaceAll("\\s*", "");
        } else {
            model = "";
        }
        return model;
    }
}
