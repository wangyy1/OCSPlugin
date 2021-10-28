package com.jzxl.ocs_plugin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;

import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;

public class AppJump implements MethodChannel.MethodCallHandler, FlutterPlugin, ActivityAware {

    private FlutterPluginBinding mFlutterPluginBinding;
    private MethodChannel methodChannel;
    private Activity activity;



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

    @Override
    public void onAttachedToEngine(@NonNull FlutterPlugin.FlutterPluginBinding binding) {
        this.mFlutterPluginBinding = binding;
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPlugin.FlutterPluginBinding binding) {

    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        methodChannel = new MethodChannel(mFlutterPluginBinding.getBinaryMessenger(), "ocs.appJump.channel");
        methodChannel.setMethodCallHandler(this);
        activity = binding.getActivity();
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {

    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {

    }

    @Override
    public void onDetachedFromActivity() {
        methodChannel.setMethodCallHandler(null);
    }
}
