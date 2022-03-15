package com.jzxl.ocs_plugin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.fragment.app.FragmentActivity;

import com.jzxl.ocs_plugin.biometric.BiometricCallback;
import com.jzxl.ocs_plugin.biometric.BiometricVerifyManager;
import com.jzxl.ocs_plugin.utils.BadgeUtils;

import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.PluginRegistry;

public class BiometricPlugin implements MethodCallHandler, FlutterPlugin, ActivityAware {

    private FlutterPluginBinding mFlutterPluginBinding;
    private MethodChannel channel;
    private Activity mActivity;
    private BiometricVerifyManager mBiometricVerifyManager;

    @Override
    public void onMethodCall(final MethodCall call, final MethodChannel.Result response) {
        try {
            handleMethodCall(call, response);
        } catch (Exception e) {
            response.error("Unexpected error!", e.getMessage(), e);
        }
    }

    private void handleMethodCall(final MethodCall call, final MethodChannel.Result response) {
        switch (call.method) {
            case "canAuthenticate": {
                response.success(mBiometricVerifyManager.canAuthenticate(mActivity));
                break;
            }
            case "authenticate": {
                if (mActivity instanceof FragmentActivity) {
                    Map<String, String> result = new HashMap<>();
                    mBiometricVerifyManager.setCallback(new BiometricCallback() {
                        @Override
                        public void onHwUnavailable() {
                            result.put("RESULT", "UNAVAILABLE");
                            response.success(result);
                        }

                        @Override
                        public void onNoneEnrolled() {
                            result.put("RESULT", "NONE_ENROLLED");
                            response.success(result);
                        }

                        @Override
                        public void onSucceeded(Cipher cipher) {
                            result.put("RESULT", "SUCCEEDED");
                            response.success(result);
                        }

                        @Override
                        public void onFailed() {
//                            Toast.makeText(mActivity, "指纹不匹配", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError() {
                            result.put("RESULT", "ERROR");
                            response.success(result);
                        }
                    });
                    mBiometricVerifyManager.authenticate((FragmentActivity) mActivity);
                }
                break;
            }
            default: {
                response.notImplemented();
                return;
            }
        }
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
        mBiometricVerifyManager = new BiometricVerifyManager();
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
