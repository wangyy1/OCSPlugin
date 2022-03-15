package com.jzxl.ocs_plugin.biometric;

import android.content.Context;
import android.os.Build;
import androidx.fragment.app.FragmentActivity;

import javax.crypto.Cipher;

/**
 * 生物认证管理类
 */
public class BiometricVerifyManager implements BiometricCallback {

    private IBiometric iBiometric;

    private BiometricCallback callback;

    public BiometricVerifyManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            iBiometric = BiometricAndroidN.newInstance();
        }
    }

    public void setCallback(BiometricCallback callback) {
        this.callback = callback;
    }

    /**
     * 检测硬件是否可用
     * @return
     */
    public boolean canAuthenticate(Context context) {
        return iBiometric.canAuthenticate(context, callback == null ? this : callback);
    }

    public void authenticate(FragmentActivity activity) {
        authenticate(activity, true, null);
    }

    /**
     * 生物认证
     * @param activity
     * @param encrypt
     * @param ivBytes
     */
    public void authenticate(FragmentActivity activity, boolean encrypt, byte[] ivBytes) {
        if (iBiometric == null) {
            if (callback != null) {
                callback.onHwUnavailable();
            }
            return;
        }

        if (!canAuthenticate(activity)) {
            return;
        }
        iBiometric.authenticate(activity, encrypt, ivBytes, callback == null ? this : callback);
    }


    @Override
    public void onHwUnavailable() {

    }

    @Override
    public void onNoneEnrolled() {

    }

    @Override
    public void onSucceeded(Cipher cipher) {

    }

    @Override
    public void onFailed() {

    }

    @Override
    public void onError() {

    }
}
