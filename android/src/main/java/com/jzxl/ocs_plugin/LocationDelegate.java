package com.jzxl.ocs_plugin;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.annotation.VisibleForTesting;
import androidx.core.app.ActivityCompat;

import com.jzxl.ocs_plugin.activity.BaiDuMapLocationActivity;
import com.jzxl.ocs_plugin.entity.LocationInfo;

import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;

public class LocationDelegate implements PluginRegistry.ActivityResultListener, PluginRegistry.RequestPermissionsResultListener {
    private static final String TAG = "LocationDelegate";
    private static final int REQUEST_CODE_CHOOSE_LOCATION = 1;
    private static final int REQUEST_CODE_LOOK_LOCATION = 2;
    @VisibleForTesting
    static final int REQUEST_EXTERNAL_LOCATION_PERMISSION = 2342;
    private String[] baiDuMapPerms = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};


    private Activity mActivity;
    private final PermissionManager permissionManager;
    private MethodChannel.Result pendingResult;
    private MethodCall methodCall;

    @Override
    public boolean onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        int grantedCount = 0;
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                grantedCount++;
            }
        }
        boolean permissionGranted =
                grantResults.length == permissions.length && grantResults.length == grantedCount;
        switch (requestCode) {
            case REQUEST_EXTERNAL_LOCATION_PERMISSION:
                if (permissionGranted) {
                    mActivity.startActivityForResult(BaiDuMapLocationActivity.getSendMapIntent(mActivity), REQUEST_CODE_CHOOSE_LOCATION);
                }
                break;
            default:
                return false;
        }
        if (!permissionGranted) {
            finishWithError("拒绝定位", "用户不允许访问位置");
        }
        return true;
    }

    interface PermissionManager {
        boolean isPermissionGranted(String permissionName);

        void askForPermission(String[] permissionNames, int requestCode);

    }

    public LocationDelegate(Activity activity) {
        this.mActivity = activity;
        permissionManager = new PermissionManager() {
            @Override
            public boolean isPermissionGranted(String permissionName) {
                return ActivityCompat.checkSelfPermission(mActivity, permissionName)
                        == PackageManager.PERMISSION_GRANTED;
            }

            @Override
            public void askForPermission(String[] permissionNames, int requestCode) {
                ActivityCompat.requestPermissions(mActivity, permissionNames, requestCode);
            }
        };
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_CHOOSE_LOCATION:
                finishChooseLocationWithSuccess(resultCode, data);
                break;
            case REQUEST_CODE_LOOK_LOCATION:
                finishChooseLocationWithSuccess(resultCode, data);
                break;
            default:
                return false;
        }
        return true;
    }

    /**
     * 选择位置
     *
     * @param methodCall 参数
     * @param result     结果
     */

    public void chooseLocation(MethodCall methodCall, MethodChannel.Result result) {
        if (!setPendingMethodCallAndResult(methodCall, result)) {
            finishWithAlreadyActiveError(result);
            return;
        }
        if (!permissionManager.isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION) && !permissionManager.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            permissionManager.askForPermission(
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_EXTERNAL_LOCATION_PERMISSION);
            return;
        }

        mActivity.startActivityForResult(BaiDuMapLocationActivity.getSendMapIntent(mActivity), REQUEST_CODE_CHOOSE_LOCATION);
    }

    /**
     * 查看位置
     *
     * @param methodCall 参数
     * @param result     结果
     */
    public void lookLocation(MethodCall methodCall, MethodChannel.Result result) {
        if (!setPendingMethodCallAndResult(methodCall, result)) {
            finishWithAlreadyActiveError(result);
            return;
        }
        String latitude = methodCall.argument("latitude");
        String longitude = methodCall.argument("longitude");
        String address = methodCall.argument("address");
        LocationInfo locationInfo = new LocationInfo(Double.valueOf(latitude), Double.valueOf(longitude), null, address);
        mActivity.startActivityForResult(BaiDuMapLocationActivity.getLookMapIntent(mActivity, locationInfo), REQUEST_CODE_LOOK_LOCATION);
    }

    private void finishChooseLocationWithSuccess(int resultCode, Intent data) {
        if (pendingResult == null) {
            return;
        }
        Map<String, String> map = null;
        if (resultCode == Activity.RESULT_OK && data != null) {
            LocationInfo locationInfo = BaiDuMapLocationActivity.getSelectedLocation(data);
            map = new HashMap<>();
            map.put("latitude", locationInfo.latitude + "");
            map.put("longitude", locationInfo.longitude + "");
            map.put("address", locationInfo.address);
        }
        pendingResult.success(map);
        clearMethodCallAndResult();

    }


    private void finishWithAlreadyActiveError(MethodChannel.Result result) {
        result.error("already_active", "正在使用定位功能", null);
    }

    private void finishWithError(String errorCode, String errorMessage) {
        if (pendingResult == null) {
            return;
        }
        pendingResult.error(errorCode, errorMessage, null);
        clearMethodCallAndResult();
    }

    private boolean setPendingMethodCallAndResult(
            MethodCall methodCall, MethodChannel.Result result) {
        if (pendingResult != null) {
            return false;
        }
        this.methodCall = methodCall;
        pendingResult = result;
        return true;
    }

    private void clearMethodCallAndResult() {
        methodCall = null;
        pendingResult = null;
    }

}
