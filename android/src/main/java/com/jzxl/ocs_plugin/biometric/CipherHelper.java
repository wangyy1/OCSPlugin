package com.jzxl.ocs_plugin.biometric;

import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 * 指纹识别Cipher加密/解密工具类
 */
public class CipherHelper {

    /**
     * 生物识别加密key
     */
    private static final String KEY_NAME = "com.jzxl.ocsplugin.biometric";

    @RequiresApi(api = Build.VERSION_CODES.M)
    private static void generateSecretKey(KeyGenParameterSpec keyGenParameterSpec) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        keyGenerator.init(keyGenParameterSpec);
        keyGenerator.generateKey();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private static SecretKey getSecretKey() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException, NoSuchProviderException, InvalidAlgorithmParameterException {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        // Before the keystore can be accessed, it must be loaded.
        keyStore.load(null);
        if (!keyStore.isKeyEntry(KEY_NAME)) {
            generateSecretKey();
        }
        return ((SecretKey) keyStore.getKey(KEY_NAME, null));
    }

    private static Cipher getCipher() throws NoSuchPaddingException, NoSuchAlgorithmException {
        return Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                + KeyProperties.BLOCK_MODE_CBC + "/"
                + KeyProperties.ENCRYPTION_PADDING_PKCS7);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private static void generateSecretKey() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        // 仅使用生物识别凭据进行身份验证
        generateSecretKey(new KeyGenParameterSpec.Builder(
                KEY_NAME,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationRequired(true)
                // Invalidate the keys if the user has registered a new biometric
                // credential, such as a new fingerprint. Can call this method only
                // on Android 7.0 (API level 24) or higher. The variable
                // "invalidatedByBiometricEnrollment" is true by default.
                .setInvalidatedByBiometricEnrollment(true)
                .build());
    }


    /**
     * 创建Cipher
     *
     * @param encrypt 加密
     * @param ivByte 向量，只有解密时使用，解密成功后返回
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static Cipher createCipher(boolean encrypt, byte[] ivByte) throws NoSuchAlgorithmException, NoSuchPaddingException, UnrecoverableKeyException, CertificateException, KeyStoreException, IOException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchProviderException {
        Cipher cipher = getCipher();
        SecretKey secretKey = getSecretKey();
        if (encrypt) {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        } else {
            IvParameterSpec iv = new IvParameterSpec(ivByte);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
        }
        return cipher;
    }

}
