package com.jzxl.ocs_plugin;

import android.content.Intent;

import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * OcsPlugin
 */
public class OcsPlugin implements MethodCallHandler, FlutterPlugin, ActivityAware, PluginRegistry.NewIntentListener {

    private MethodChannel channel;


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

    // 跳转插件
    private AppJump appJump;
    // 语音播放插件
    private AudioplayersPlugin audioplayersPlugin;
    // 通知插件
    private NotificationPlugin notificationPlugin;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPlugin.FlutterPluginBinding binding) {
        channel = new MethodChannel(binding.getBinaryMessenger(), "ocs_plugin");
        // 初始化百度地图
        channel.setMethodCallHandler(this);

        appJump = new AppJump();
        audioplayersPlugin = new AudioplayersPlugin();
        notificationPlugin = new NotificationPlugin();


        appJump.onAttachedToEngine(binding);
        audioplayersPlugin.onAttachedToEngine(binding);
        notificationPlugin.onAttachedToEngine(binding);

//        AudioplayersPlugin.registerWith(registrar);
//        NotificationPlugin.registerWith(registrar);
//        AppJump.registerWith(registrar);
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPlugin.FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);

        appJump.onDetachedFromEngine(binding);
        audioplayersPlugin.onDetachedFromEngine(binding);
        notificationPlugin.onDetachedFromEngine(binding);
    }

    @Override
    public void onAttachedToActivity(@NonNull @org.jetbrains.annotations.NotNull ActivityPluginBinding binding) {
        appJump.onAttachedToActivity(binding);
        audioplayersPlugin.onAttachedToActivity(binding);
        notificationPlugin.onAttachedToActivity(binding);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        appJump.onDetachedFromActivityForConfigChanges();
        audioplayersPlugin.onDetachedFromActivityForConfigChanges();
        notificationPlugin.onDetachedFromActivityForConfigChanges();
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull @org.jetbrains.annotations.NotNull ActivityPluginBinding binding) {
        appJump.onReattachedToActivityForConfigChanges(binding);
        audioplayersPlugin.onReattachedToActivityForConfigChanges(binding);
        notificationPlugin.onReattachedToActivityForConfigChanges(binding);
    }

    @Override
    public void onDetachedFromActivity() {
        appJump.onDetachedFromActivity();
        audioplayersPlugin.onDetachedFromActivity();
        notificationPlugin.onDetachedFromActivity();
    }

    @Override
    public boolean onNewIntent(Intent intent) {
        notificationPlugin.onNewIntent(intent);
        return false;
    }
}
