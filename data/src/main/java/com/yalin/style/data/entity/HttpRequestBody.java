package com.yalin.style.data.entity;

import android.content.Context;

import com.yalin.style.data.utils.FacetIdUtil;

/**
 * @author jinyalin
 * @since 2017/4/25.
 */

public class HttpRequestBody {
    private DeviceInfo deviceInfo;
    private String facetId;

    public HttpRequestBody(Context context, DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
        this.facetId = FacetIdUtil.getFacetId(context);
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }
}
