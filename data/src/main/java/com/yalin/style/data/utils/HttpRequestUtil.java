package com.yalin.style.data.utils;

import android.content.Context;

import com.google.gson.Gson;
import com.yalin.style.data.entity.DeviceInfo;
import com.yalin.style.data.entity.HttpRequestBody;

/**
 * @author jinyalin
 * @since 2017/4/25.
 */

public class HttpRequestUtil {
    private static Gson gson = new Gson();

    public static String getRequestBody(Context context) {
        HttpRequestBody requestBody = new HttpRequestBody(context, getDeviceJson(context));
        return gson.toJson(requestBody);
    }

    private static DeviceInfo getDeviceJson(Context context) {
        int sdkVersion = DeviceUtil.getSDKVersion();
        String androidId = DeviceUtil.getAndroidID(context);
        String manufacturer = DeviceUtil.getManufacturer();
        String brand = DeviceUtil.getBrand();
        String model = DeviceUtil.getModel();

        return new DeviceInfo(sdkVersion, androidId, manufacturer, brand, model);
    }
}
