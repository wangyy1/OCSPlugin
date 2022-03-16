package com.jzxl.ocs_plugin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.biometric.BiometricManager;

import com.jzxl.ocs_plugin.biometric.AuthenticateResult;
import com.jzxl.ocs_plugin.biometric.BiometricActivity;
import com.jzxl.ocs_plugin.biometric.EnumType;

import java.util.HashMap;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.PluginRegistry;

public class BiometricPlugin implements MethodCallHandler, FlutterPlugin, ActivityAware, PluginRegistry.ActivityResultListener {
    
    private FlutterPluginBinding mFlutterPluginBinding;
    private MethodChannel channel;
    private Activity mActivity;
    private MethodChannel.Result mResponse;

    @Override
    public void onMethodCall(final MethodCall call, final MethodChannel.Result response) {
        try {
            handleMethodCall(call, response);
        } catch (Exception e) {
            response.error("Unexpected error!", e.getMessage(), e);
        }
    }

    private void handleMethodCall(final MethodCall call, final MethodChannel.Result response) {
        this.mResponse = response;
        switch (call.method) {
            case "canAuthenticate": {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    response.success(canAuthenticate(mActivity).result);
                } else {
                    response.success(EnumType.BiometricResult.NO_HARDWARE);
                }
                break;
            }
            case "authenticate": {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    BiometricActivity.startActivity(mActivity);
                }
                break;
            }
            default: {
                response.notImplemented();
                return;
            }
        }
    }


    private EnumType.BiometricResult canAuthenticate(Context context) {
        BiometricManager biometricManager = BiometricManager.from(context);
//        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
        switch (biometricManager.canAuthenticate()) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Log.d("MY_APP_TAG", "App can authenticate using biometrics.");
                return EnumType.BiometricResult.SUCCESS;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Log.e("MY_APP_TAG", "No biometric features available on this device.");
                return EnumType.BiometricResult.NO_HARDWARE;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Log.e("MY_APP_TAG", "Biometric features are currently unavailable.");
                return EnumType.BiometricResult.HW_UNAVAILABLE;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                // Prompts the user to create credentials that your app accepts.
                return EnumType.BiometricResult.NONE_ENROLLED;
        }
        return EnumType.BiometricResult.NO_HARDWARE;
    }

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        mFlutterPluginBinding = binding;
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {

    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        channel = new MethodChannel(mFlutterPluginBinding.getBinaryMessenger(), "ocs.biometric.channel");
        channel.setMethodCallHandler(this);
        this.mActivity = binding.getActivity();
        binding.addActivityResultListener(this);
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != mActivity.RESULT_OK) {
            return false;
        }
        switch (requestCode) {
            case BiometricActivity.REQUEST_CODE_AUTHENTICATE:
                AuthenticateResult result = BiometricActivity.getAuthenticateResult(data);
                Map<String, Object> map = new HashMap<>();
                map.put("result", result.result());
                map.put("msg", result.getMsg());
                mResponse.success(map);
                break;
        }
        return false;
    }
}
