package com.jzxl.ocs_plugin.biometric;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.jzxl.ocs_plugin.R;

import java.util.concurrent.Executor;

/**
 * 生物识别页面，中间页面，用来弹出生物认证Dialog
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public class BiometricActivity extends AppCompatActivity {
    
    public static final int REQUEST_CODE_AUTHENTICATE = 3001;
    // 加密
    private static final String EXTRA_ENCRYPT = "EXTRA_ENCRYPT";
    // 向量
    private static final String EXTRA_IV_BYTES = "EXTRA_IV_BYTES";
    
    private static final String EXTRA_RESULT = "EXTRA_RESULT";

    public static void startActivity(Activity activity) {
        startActivity(activity, true, new byte[0]);
    }
    
    public static void startActivity(Activity activity, boolean encrypt, byte[] ivBytes) {
        Intent intent = new Intent(activity, BiometricActivity.class);
        intent.putExtra(EXTRA_ENCRYPT, encrypt);
        intent.putExtra(EXTRA_IV_BYTES, ivBytes);
        activity.startActivityForResult(intent, REQUEST_CODE_AUTHENTICATE);
    }

    public static AuthenticateResult getAuthenticateResult(Intent mResponse) {
        return mResponse.getParcelableExtra(EXTRA_RESULT);
    }
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_biometric);


        AuthenticateResult authenticateResult = new AuthenticateResult();

        boolean encrypt = getIntent().getBooleanExtra(EXTRA_ENCRYPT, true);
        byte[] ivBytes = getIntent().getByteArrayExtra(EXTRA_IV_BYTES);

        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    authenticateResult.setResult(false);
                    authenticateResult.setMsg("取消");
                } else {
                    authenticateResult.setResult(false);
                    authenticateResult.setMsg(errString.toString());
                }
                finish(authenticateResult);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                authenticateResult.setResult(true);
                authenticateResult.setMsg("认证成功");
                finish(authenticateResult);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(BiometricActivity.this, "指纹不匹配", Toast.LENGTH_SHORT).show();
            }
        });
        try {
            biometricPrompt.authenticate(promptInfo(this), new BiometricPrompt.CryptoObject(CipherHelper.createCipher(encrypt, ivBytes)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    private void finish(AuthenticateResult result) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_RESULT, result);
        setResult(RESULT_OK, intent);
        finish();
    }
    
    /**
     * PromptInfo 信息
     * @param context
     * @return
     */
    private BiometricPrompt.PromptInfo promptInfo (Context context) {
        return new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getAppName(context) + "认证")
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
