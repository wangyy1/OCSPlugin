package com.jzxl.ocs_plugin;

import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;

import com.jzxl.ocs_plugin.audioplayer.HeadsetReceiver;
import com.jzxl.ocs_plugin.audioplayer.Player;
import com.jzxl.ocs_plugin.audioplayer.PlayerModel;
import com.jzxl.ocs_plugin.audioplayer.ReleaseMode;
import com.jzxl.ocs_plugin.audioplayer.WrappedMediaPlayer;
import com.jzxl.ocs_plugin.utils.InitUtils;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * OcsPlugin
 */
public class OcsPlugin implements MethodCallHandler {
    private Registrar registrar;
    private LocationDelegate locationDelegate;


    public OcsPlugin(Registrar registrar, LocationDelegate locationDelegate) {
        this.registrar = registrar;
        this.locationDelegate = locationDelegate;
    }

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "ocs_plugin");
        // 初始化百度地图
        InitUtils.initBaiDuSDK(registrar.activity().getApplicationContext());
        LocationDelegate locationDelegate = new LocationDelegate(registrar.activity());
        registrar.addActivityResultListener(locationDelegate);
        registrar.addRequestPermissionsResultListener(locationDelegate);
        channel.setMethodCallHandler(new OcsPlugin(registrar, locationDelegate));

        AudioplayersPlugin.registerWith(registrar);
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        switch (call.method) {
            case "getPlatformVersion":
                result.success("Android " + android.os.Build.VERSION.RELEASE);
                break;
            case "sendLocation":// 选择位置
                locationDelegate.chooseLocation(call, result);
                break;
            case "lookLocation": // 查看位置
                locationDelegate.lookLocation(call, result);
                break;
            default:
                result.notImplemented();
                break;
        }
    }
}
