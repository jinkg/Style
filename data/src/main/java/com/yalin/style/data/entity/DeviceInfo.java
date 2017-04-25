package com.yalin.style.data.entity;

/**
 * @author jinyalin
 * @since 2017/4/25.
 */

public class DeviceInfo {
    private int sdkVersion;
    private String androidId;
    private String manufacturer;
    private String model;
    private String type;

    public DeviceInfo(int sdkVersion, String androidId, String manufacturer, String model) {
        this.sdkVersion = sdkVersion;
        this.androidId = androidId;
        this.manufacturer = manufacturer;
        this.model = model;
        type = "Android";
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
