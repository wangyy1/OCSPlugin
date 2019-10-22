package com.jzxl.ocs_plugin;

import android.app.Activity;
import android.content.Intent;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;

public class AppJump implements MethodChannel.MethodCallHandler {

    private final MethodChannel methodChannel;
    private final Activity activity;

    public static void registerWith(final PluginRegistry.Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "ocs.appJump.channel");
        channel.setMethodCallHandler(new AppJump(channel, registrar.activity()));
    }

    public AppJump(MethodChannel methodChannel, Activity activity) {
        this.methodChannel = methodChannel;
        this.methodChannel.setMethodCallHandler(this);
        this.activity = activity;
    }

    @Override
    public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {
        if("appJump.jump".equals(methodCall.method)) {
            String componentName = methodCall.argument("identify");
            assert componentName != null;
            Intent intent = activity.getPackageManager().getLaunchIntentForPackage(componentName);
            if (intent != null) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);
                result.success(true);
            } else {
                result.success(false);
            }
        }
    }
}
