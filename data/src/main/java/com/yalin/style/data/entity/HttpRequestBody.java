package com.yalin.style.data.entity;

/**
 * @author jinyalin
 * @since 2017/4/25.
 */

public class HttpRequestBody {
    private DeviceInfo deviceInfo;

    public HttpRequestBody(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }
}
