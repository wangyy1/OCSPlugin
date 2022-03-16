package com.jzxl.ocs_plugin.biometric;

public class EnumType {
    /**
     * 生物是否可用
     */
    public static enum BiometricResult{
        SUCCESS(0, "生物认证可用"),
        HW_UNAVAILABLE(1, "硬件不可用，稍后再试"),
        NONE_ENROLLED(2, "用户没有注册任何生物识别系统"),
        NO_HARDWARE(3, "没有生物识别硬件");

        public int result;
        public String msg;

        BiometricResult(int result, String msg) {
            this.result = result;
            this.msg = msg;
        }
    }
    
}
