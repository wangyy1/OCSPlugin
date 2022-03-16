package com.jzxl.ocs_plugin.biometric;

import android.os.Parcel;
import android.os.Parcelable;

public class AuthenticateResult implements Parcelable {

    // 认证结果
    private boolean result;
    // 结果内容
    private String msg;

    public AuthenticateResult() {
    }

    protected AuthenticateResult(Parcel in) {
        result = in.readByte() != 0;
        msg = in.readString();
    }

    public static final Creator<AuthenticateResult> CREATOR = new Creator<AuthenticateResult>() {
        @Override
        public AuthenticateResult createFromParcel(Parcel in) {
            return new AuthenticateResult(in);
        }

        @Override
        public AuthenticateResult[] newArray(int size) {
            return new AuthenticateResult[size];
        }
    };

    public boolean result() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (result ? 1 : 0));
        dest.writeString(msg);
    }
}