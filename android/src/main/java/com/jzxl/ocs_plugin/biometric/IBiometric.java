package com.jzxl.ocs_plugin.biometric;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

/**
 * 生物识别认证接口
 */
public abstract class IBiometric {

    /**
     * 检测生物识别硬件是否可用
     * @param context
     * @param callback
     * @return
     */
    public abstract boolean canAuthenticate(Context context, @NonNull BiometricCallback callback);

    /**
     * 初始化并调起生物识别验证
     *
     * @param activity
     * @param callback
     */
    public void authenticate(FragmentActivity activity, @NonNull BiometricCallback callback){
        authenticate(activity, true, null, callback);
    }


    /**
     * 初始化并调起生物识别验证
     * @param activity
     * @param encrypt 是否加密
     * @param ivBytes 解密向量字节数组，加密可为 `null`
     * @param callback 结果回调
     */
    public abstract void authenticate(FragmentActivity activity, boolean encrypt, byte[] ivBytes, @NonNull BiometricCallback callback);

}