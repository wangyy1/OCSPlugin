package com.jzxl.ocs_plugin.utils;

import android.content.Context;

import com.baidu.mapapi.SDKInitializer;

/**
 * 初始化工具类
 */
public class InitUtils {

    /**
     * 初始化百度地图SDK
     * @param context
     */
    public static void initBaiDuSDK(Context context) {
        SDKInitializer.initialize(context);
    }

}
