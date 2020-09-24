package com.jzxl.ocs_plugin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.jzxl.ocs_plugin.utils.BadgeUtils;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

public class NotificationPlugin implements MethodCallHandler, PluginRegistry.NewIntentListener {
    private static final String SELECT_NOTIFICATION = "SELECT_NOTIFICATION";
    private static final String PAYLOAD = "payload";

    private final MethodChannel channel;
    private final Context context;

    public static void registerWith(final Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "ocs.notification.channel");
        channel.setMethodCallHandler(new NotificationPlugin(channel, registrar));
    }

    private NotificationPlugin(final MethodChannel channel, Registrar registrar) {
        this.channel = channel;
        this.channel.setMethodCallHandler(this);
        this.context = registrar.context();
        registrar.addNewIntentListener(this);
    }

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
                    intent = new Intent(context, Class.forName(className));
                    intent.setAction(SELECT_NOTIFICATION);
                    intent.putExtra(PAYLOAD, payload);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                BadgeUtils.setNotificationBadge(notificationId, count, context, iconName, bitmap, contentTitle, contentText, intent);
                break;
            }
            case "cancel": {
                BadgeUtils.cancelNotification(context);
                BadgeUtils.setCount(0, context);
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
}
