package com.jzxl.ocs_plugin.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author wangyongyong
 * @date
 * @Description
 */

public class LocationInfo implements Parcelable {
    /**
     * 维度
     */
    public double latitude;
    /**
     * 经度
     */
    public double longitude;
    /**
     * 当前位置
     */
    public String name;

    /**
     * 当前地址
     */
    public String address;

    /**
     * 是否选中
     */
    private boolean isSelected;

    public LocationInfo(double latitude, double longitude, String name, String address) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.address = address;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public static Creator<LocationInfo> getCREATOR() {
        return CREATOR;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(name);
        dest.writeString(address);
    }

    protected LocationInfo(Parcel in) {
        setLatitude(in.readDouble());
        setLongitude(in.readDouble());
        setName(in.readString());
        setAddress(in.readString());
    }

    public static final Creator<LocationInfo> CREATOR = new Creator<LocationInfo>() {
        @Override
        public LocationInfo createFromParcel(Parcel source) {
            return new LocationInfo(source);
        }

        @Override
        public LocationInfo[] newArray(int size) {
            return new LocationInfo[size];
        }
    };
}
