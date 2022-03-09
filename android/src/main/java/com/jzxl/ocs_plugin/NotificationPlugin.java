package com.jzxl.ocs_plugin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.NonNull;

import com.jzxl.ocs_plugin.utils.BadgeUtils;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

public class NotificationPlugin implements MethodCallHandler, PluginRegistry.NewIntentListener, FlutterPlugin, ActivityAware {
    private static final String SELECT_NOTIFICATION = "SELECT_NOTIFICATION";
    private static final String PAYLOAD = "payload";

    private FlutterPluginBinding mFlutterPluginBinding;
    private MethodChannel channel;
    private Activity activity;

    @Override
    public void onMethodCall(final MethodCall call, final MethodChannel.Result response) {
        try {
            handleMethodCall(call, response);
            response.success(true);
        } catch (Exception e) {
            response.error("Unexpected error!", e.getMessage(), e);
            response.success(false);
        }
    }

    private void handleMethodCall(final MethodCall call, final MethodChannel.Result response) {
        switch (call.method) {
            case "show": {
                int count = call.argument("count");
                int sumCount = call.argument("sumCount");
                int notificationId = call.argument("notificationId");
                String iconName = call.argument("iconName");
                String contentTitle = call.argument("contentTitle");
                String contentText = call.argument("contentText");
                String payload = call.argument("payload");
                byte[] largeIcon = call.argument("largeIcon");
                String className = call.argument("className");
                Bitmap bitmap = largeIcon != null ? getBitmapFromByte(largeIcon) : null;
                Intent intent = null;
                try {
                    intent = new Intent(activity, Class.forName(className));
                    intent.setAction(SELECT_NOTIFICATION);
                    intent.putExtra(PAYLOAD, payload);
//                    intent = activity.getPackageManager().getLaunchIntentForPackage(className);

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                BadgeUtils.setNotificationBadge(notificationId, count, sumCount, activity, iconName, bitmap, contentTitle, contentText, intent);
                break;
            }
            case "cancel": {
                BadgeUtils.cancelNotification(activity);
                BadgeUtils.setCount(0, activity);
                break;
            }
            default: {
                response.notImplemented();
                return;
            }
        }
    }


    /**
     * 将二进制文件转化为Bitmap
     *
     * @param temp
     * @return
     */
    public Bitmap getBitmapFromByte(byte[] temp) {
        try {
            if (temp != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(temp, 0, temp.length);
                return bitmap;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取主页面
     *
     * @param context
     * @return
     */
    private static Class getMainActivityClass(Context context) {
        String packageName = context.getPackageName();
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        String className = launchIntent.getComponent().getClassName();
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean onNewIntent(Intent intent) {
        return sendNotificationPayloadMessage(intent);
    }

    private Boolean sendNotificationPayloadMessage(Intent intent) {
        if (NotificationPlugin.SELECT_NOTIFICATION.equals(intent.getAction())) {
            String payload = intent.getStringExtra(NotificationPlugin.PAYLOAD);
            channel.invokeMethod("selectNotification", payload);
            return true;
        }
        return false;
    }

    @Override
    public void onAttachedToEngine(@NonNull FlutterPlugin.FlutterPluginBinding binding) {
        mFlutterPluginBinding = binding;
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPlugin.FlutterPluginBinding binding) {

    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        channel = new MethodChannel(mFlutterPluginBinding.getBinaryMessenger(), "ocs.notification.channel");
        channel.setMethodCallHandler(this);
        this.activity = binding.getActivity();
        binding.addOnNewIntentListener(this);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {

    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {

    }

    @Override
    public void onDetachedFromActivity() {

    }
}
