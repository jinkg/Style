package com.yalin.style.data.entity;

import com.yalin.style.data.BuildConfig;

/**
 * @author jinyalin
 * @since 2017/4/25.
 */

public class DeviceInfo {
    private int sdkVersion;
    private String androidId;
    private String manufacturer;
    private String brand;
    private String model;
    private String type;
    private String versionName;
    private int versionCode;
    private String channel;

    public DeviceInfo(int sdkVersion, String androidId, String manufacturer,
                      String brand, String model) {
        this.sdkVersion = sdkVersion;
        this.androidId = androidId;
        this.manufacturer = manufacturer;
        this.brand = brand;
        this.model = model;
        type = "Android";
        versionName = BuildConfig.VERSION_NAME;
        versionCode = BuildConfig.VERSION_CODE;
        channel = BuildConfig.CHANNEL;
    }

    public int getSdkVersion() {
        return sdkVersion;
    }

    public String getAndroidId() {
        return androidId;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getModel() {
        return model;
    }

    public String getType() {
        return type;
    }
}
