package com.jzxl.ocs_plugin;

import android.app.Activity;
import android.content.Intent;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

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
        String method = methodCall.method;
        try {
            if ("appJump.jump".equals(method)) {
                String componentName = methodCall.argument("identify");
                assert componentName != null;
                Intent intent = activity.getPackageManager().getLaunchIntentForPackage(componentName);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);
            } else if ("SystemAppJump.EMAIL".equals(method)) { // 邮箱
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);
            } else if ("SystemAppJump.GALLERY".equals(method)) { // 图库
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_GALLERY);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);
            } else if ("SystemAppJump.CALL".equals(method)) { // 拨号
                Intent intent = new Intent(Intent.ACTION_CALL_BUTTON);
                activity.startActivity(intent);
            } else if ("SystemAppJump.CONTACTS".equals(method)) { // 联系人
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_CONTACTS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);
            } else if ("SystemAppJump.SMS".equals(method)) { // 短信
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_MESSAGING);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);
            } else if ("SystemAppJump.CAMERA".equals(method)) { // 相机
                Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);
            }
            result.success(true);
        } catch (Exception e) {
            result.success(false);
        }
    }
}
