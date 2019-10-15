package com.jzxl.ocs_plugin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.jzxl.ocs_plugin.audioplayer.HeadsetReceiver;
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
    private final Activity activity;

    public static void registerWith(final Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "ocs.notification.channel");
        channel.setMethodCallHandler(new NotificationPlugin(channel, registrar.activity()));
    }

    private NotificationPlugin(final MethodChannel channel, Activity activity) {
        this.channel = channel;
        this.channel.setMethodCallHandler(this);
        this.activity = activity;
        HeadsetReceiver headsetPlugReceiver = new HeadsetReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.HEADSET_PLUG");
        activity.registerReceiver(headsetPlugReceiver, intentFilter);
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
                String iconName = call.argument("iconName");
                String contentTitle = call.argument("contentTitle");
                String contentText = call.argument("contentText");
                String payload = call.argument("payload");
                Intent intent = new Intent(activity, getMainActivityClass(activity));
                intent.setAction(SELECT_NOTIFICATION);
                intent.putExtra(PAYLOAD, payload);
                BadgeUtils.setNotificationBadge(count, activity, iconName, contentTitle, contentText, intent);
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
