package com.jzxl.ocs_plugin.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AsyncQueryHandler;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 应用角标工具类
 *
 * @author SunQiang
 * @since 2018/11/28
 */
public class BadgeUtils {
    private static final String DRAWABLE = "drawable";

    public static boolean setCount(final int count, final Context context) {
        if (count >= 0 && context != null) {
            Log.d("BRAND", Build.BRAND);
            switch (Build.BRAND.toLowerCase()) {
                case "xiaomi":
                    // 小米手机不支持直接设置角标
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            setNotificationBadge(count, context, "应用角标", "您有" + count + "条未读消息");
//                        }
//                    }, 3000);
//                    Toast.makeText(context, "请切到后台，3秒后会收到通知", Toast.LENGTH_SHORT).show();
                    return true;
                case "huawei":
                case "honor":
                    return setHuaweiBadge(count, context);
                case "samsung":
                    return setSamsungBadge(count, context);
                case "oppo":
                    return setOPPOBadge(count, context) || setOPPOBadge2(count, context);
                case "vivo":
                    return setVivoBadge(count, context);
                case "lenovo":
                    return setZukBadge(count, context);
                case "htc":
                    return setHTCBadge(count, context);
                case "sony":
                    return setSonyBadge(count, context);
                default:
                    return false;
            }
        } else {
            return false;
        }
    }


    /**
     * 
     * @param notificationId
     * @param count 单个通知数字
     * @param sumCount api26以下使用，显示角标数字，api26以上使用count数字（如果有两个通知，则显示两个通知count的和），
     * @param context
     * @param iconName
     * @param largeIcon
     * @param contentTitle
     * @param contentText
     * @param intent
     * @return
     */
    public static boolean setNotificationBadge(int notificationId, int count, int sumCount, Context context, String iconName, Bitmap largeIcon, String contentTitle, String contentText, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            // 8.0之后添加角标需要NotificationChannel
            NotificationChannel channel = new NotificationChannel("com.ocs.flutter.channel_id", "新消息通知",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("程序收到新消息时使用的通知类别");
            channel.setShowBadge(true);
            channel.enableVibration(false);
            channel.enableLights(true);
            channel.setSound(null, null);
            nm.createNotificationChannel(channel);
        }

        NotificationManagerCompat nmc = NotificationManagerCompat.from(context);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new NotificationCompat.Builder(context, "com.ocs.flutter.channel_id")
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setLargeIcon(largeIcon == null ? BitmapFactory.decodeResource(context.getResources(), getDrawableResourceId(context, iconName)) : largeIcon)
                .setSmallIcon(getDrawableResourceId(context, iconName))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setChannelId("com.ocs.flutter.channel_id")
                .setNumber(count)
                .setSound(null)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE) // 通知类型
                .build();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            // 小米
            if (Build.BRAND.equalsIgnoreCase("xiaomi")) {
                setXiaomiBadge(1, notification);
            } else {
                setCount(sumCount, context);
            }
        }
        nmc.notify(notificationId, notification);
        return true;
    }


    public static void cancelNotification(Context context) {
        NotificationManagerCompat.from(context).cancelAll();
    }

    private static void setXiaomiBadge(int count, Notification notification) {
        try {
            Field field = notification.getClass().getDeclaredField("extraNotification");
            Object extraNotification = field.get(notification);
            Method method = extraNotification.getClass().getDeclaredMethod("setMessageCount", int
                    .class);
            method.invoke(extraNotification, count);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean setHuaweiBadge(int count, Context context) {
        try {
            String launchClassName = getLauncherClassName(context);
            if (TextUtils.isEmpty(launchClassName)) {
                return false;
            }
            Bundle bundle = new Bundle();
            bundle.putString("package", context.getPackageName());
            bundle.putString("class", launchClassName);
            bundle.putInt("badgenumber", count);
            context.getContentResolver().call(Uri.parse("content://com.huawei.android.launcher" +
                    ".settings/badge/"), "change_badge", null, bundle);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean setSamsungBadge(int count, Context context) {
        try {
            String launcherClassName = getLauncherClassName(context);
            if (TextUtils.isEmpty(launcherClassName)) {
                return false;
            }
            Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
            intent.putExtra("badge_count", count);
            intent.putExtra("badge_count_package_name", context.getPackageName());
            intent.putExtra("badge_count_class_name", launcherClassName);
            context.sendBroadcast(intent);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Deprecated
    private static boolean setOPPOBadge(int count, Context context) {
        try {
            Bundle extras = new Bundle();
            extras.putInt("app_badge_count", count);
            context.getContentResolver().call(Uri.parse("content://com.android.badge/badge"),
                    "setAppBadgeCount", String.valueOf(count), extras);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Deprecated
    private static boolean setOPPOBadge2(int count, Context context) {
        try {
            Intent intent = new Intent("com.oppo.unsettledevent");
            intent.putExtra("packageName", context.getPackageName());
            intent.putExtra("number", count);
            intent.putExtra("upgradeNumber", count);
            PackageManager packageManager = context.getPackageManager();
            List<ResolveInfo> receivers = packageManager.queryBroadcastReceivers(intent, 0);
            if (receivers != null && receivers.size() > 0) {
                context.sendBroadcast(intent);
            } else {
                Bundle extras = new Bundle();
                extras.putInt("app_badge_count", count);
                context.getContentResolver().call(Uri.parse("content://com.android.badge/badge"),
                        "setAppBadgeCount", null, extras);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Deprecated
    private static boolean setVivoBadge(int count, Context context) {
        try {
            String launcherClassName = getLauncherClassName(context);
            if (TextUtils.isEmpty(launcherClassName)) {
                return false;
            }
            Intent intent = new Intent("launcher.action.CHANGE_APPLICATION_NOTIFICATION_NUM");
            intent.putExtra("packageName", context.getPackageName());
            intent.putExtra("className", launcherClassName);
            intent.putExtra("notificationNum", count);
            context.sendBroadcast(intent);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean setZukBadge(int count, Context context) {
        try {
            Bundle extra = new Bundle();
            ArrayList<String> ids = new ArrayList<>();
            // 以列表形式传递快捷方式id，可以添加多个快捷方式id
//        ids.add("custom_id_1");
//        ids.add("custom_id_2");
            extra.putStringArrayList("app_shortcut_custom_id", ids);
            extra.putInt("app_badge_count", count);
            Uri contentUri = Uri.parse("content://com.android.badge/badge");
            Bundle bundle = context.getContentResolver().call(contentUri, "setAppBadgeCount", null,
                    extra);
            return bundle != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean setHTCBadge(int count, Context context) {
        try {
            ComponentName launcherComponentName = getLauncherComponentName(context);
            if (launcherComponentName == null) {
                return false;
            }

            Intent intent1 = new Intent("com.htc.launcher.action.SET_NOTIFICATION");
            intent1.putExtra("com.htc.launcher.extra.COMPONENT", launcherComponentName
                    .flattenToShortString());
            intent1.putExtra("com.htc.launcher.extra.COUNT", count);
            context.sendBroadcast(intent1);

            Intent intent2 = new Intent("com.htc.launcher.action.UPDATE_SHORTCUT");
            intent2.putExtra("packagename", launcherComponentName.getPackageName());
            intent2.putExtra("count", count);
            context.sendBroadcast(intent2);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean setSonyBadge(int count, Context context) {
        String launcherClassName = getLauncherClassName(context);
        if (TextUtils.isEmpty(launcherClassName)) {
            return false;
        }
        try {
            //官方给出方法
            ContentValues contentValues = new ContentValues();
            contentValues.put("badge_count", count);
            contentValues.put("package_name", context.getPackageName());
            contentValues.put("activity_name", launcherClassName);
            SonyAsyncQueryHandler asyncQueryHandler = new SonyAsyncQueryHandler(context
                    .getContentResolver());
            asyncQueryHandler.startInsert(0, null, Uri.parse("content://com.sonymobile.home" +
                    ".resourceprovider/badge"), contentValues);
            return true;
        } catch (Exception e) {
            try {
                //网上大部分使用方法
                Intent intent = new Intent("com.sonyericsson.home.action.UPDATE_BADGE");
                intent.putExtra("com.sonyericsson.home.intent.extra.badge.SHOW_MESSAGE", count > 0);
                intent.putExtra("com.sonyericsson.home.intent.extra.badge.ACTIVITY_NAME",
                        launcherClassName);
                intent.putExtra("com.sonyericsson.home.intent.extra.badge.MESSAGE", String
                        .valueOf(count));
                intent.putExtra("com.sonyericsson.home.intent.extra.badge.PACKAGE_NAME", context
                        .getPackageName());
                context.sendBroadcast(intent);
                return true;
            } catch (Exception e1) {
                e1.printStackTrace();
                return false;
            }
        }
    }

    private static String getLauncherClassName(Context context) {
        ComponentName launchComponent = getLauncherComponentName(context);
        if (launchComponent == null) {
            return "";
        } else {
            return launchComponent.getClassName();
        }
    }

    private static ComponentName getLauncherComponentName(Context context) {
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(context
                .getPackageName());
        if (launchIntent != null) {
            return launchIntent.getComponent();
        } else {
            return null;
        }
    }

    static class SonyAsyncQueryHandler extends AsyncQueryHandler {

        SonyAsyncQueryHandler(ContentResolver cr) {
            super(cr);
        }
    }


    /**
     * 根据图标名称获取加载图标
     *
     * @param context
     * @param name
     * @return
     */
    private static int getDrawableResourceId(Context context, String name) {
        return context.getResources().getIdentifier(name, DRAWABLE, context.getPackageName());
    }

}
