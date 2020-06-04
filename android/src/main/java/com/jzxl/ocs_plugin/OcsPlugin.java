package com.jzxl.ocs_plugin;

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

    public OcsPlugin(Registrar registrar) {
        this.registrar = registrar;
    }

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "ocs_plugin");
        // 初始化百度地图
        channel.setMethodCallHandler(new OcsPlugin(registrar));

        AudioplayersPlugin.registerWith(registrar);
        NotificationPlugin.registerWith(registrar);
        AppJump.registerWith(registrar);
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        switch (call.method) {
            case "getPlatformVersion":
                result.success("Android " + android.os.Build.VERSION.RELEASE);
                break;
            default:
                result.notImplemented();
                break;
        }
    }
}
