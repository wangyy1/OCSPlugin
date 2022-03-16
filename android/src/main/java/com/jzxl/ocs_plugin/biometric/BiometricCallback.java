package com.jzxl.ocs_plugin.biometric;

import javax.crypto.Cipher;

/**
 * 验证结果回调，供使用者调用
 */
public interface BiometricCallback {

    /**
     * 验证成功
     */
    void onSucceeded(Cipher cipher);

    /**
     * 验证失败
     */
    void onFailed();

    /**
     * 取消验证
     */
    void onError();

}