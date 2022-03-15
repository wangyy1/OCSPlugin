package com.jzxl.ocs_plugin.biometric;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import java.util.concurrent.Executor;

/**
 * Android N == 6.0
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public class BiometricAndroidN extends IBiometric{
    private static BiometricAndroidN biometricAndroidN;

    public static BiometricAndroidN newInstance() {
        if (biometricAndroidN == null) {
            synchronized (BiometricAndroidN.class) {
                if (biometricAndroidN == null) {
                    biometricAndroidN = new BiometricAndroidN();
                }
            }
        }
        return biometricAndroidN;
    }


    @Override
    public boolean canAuthenticate(Context context, @NonNull BiometricCallback callback) {

        BiometricManager biometricManager = BiometricManager.from(context);
//        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
        switch (biometricManager.canAuthenticate()) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Log.d("MY_APP_TAG", "App can authenticate using biometrics.");
                return true;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Log.e("MY_APP_TAG", "No biometric features available on this device.");
                callback.onHwUnavailable();
                return false;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Log.e("MY_APP_TAG", "Biometric features are currently unavailable.");
                callback.onHwUnavailable();
                return false;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                // Prompts the user to create credentials that your app accepts.
                callback.onNoneEnrolled();
                return false;
        }
        return false;
    }

    @Override
    public void authenticate(FragmentActivity activity, boolean encrypt, byte[] ivBytes, @NonNull BiometricCallback callback) {
        Executor executor = ContextCompat.getMainExecutor(activity);
        BiometricPrompt biometricPrompt = new BiometricPrompt(activity, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {

                } else {
                    callback.onError();
                }
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                callback.onSucceeded(result.getCryptoObject().getCipher());

//                try {
//                    if (mIsEncryptMode) {
//                        encryptedInfo = result.getCryptoObject().getCipher().doFinal(
//                                "123456".getBytes(Charset.defaultCharset()));
//
//                        ivByte = result.getCryptoObject().getCipher().getIV();
//                    } else {
//                        byte[] decryptInfo = result.getCryptoObject().getCipher().doFinal(encryptedInfo);
//                        Log.d(TAG, "onAuthenticationSucceeded: " + new String(decryptInfo));
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                callback.onFailed();
            }
        });
        try {
            biometricPrompt.authenticate(promptInfo(activity), new BiometricPrompt.CryptoObject(CipherHelper.createCipher(encrypt, ivBytes)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * PromptInfo 信息
     * @param context
     * @return
     */
    private BiometricPrompt.PromptInfo promptInfo (Context context) {
        return new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getAppName(context) + "登录")
                .setSubtitle("请验证已有手机指纹")
                .setNegativeButtonText("取消")
//                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .build();
    }

    /**
     * 获取程序名称
     *
     * @param context
     * @return
     */
    private String getAppName(Context context) {
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo;
        try {
            packageManager = context.getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            applicationInfo = null;
        }
        String applicationName = (String) packageManager.getApplicationLabel(applicationInfo);
        return applicationName;
    }

}
