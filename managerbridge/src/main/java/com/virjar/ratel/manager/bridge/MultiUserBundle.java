package com.virjar.ratel.manager.bridge;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class MultiUserBundle implements Parcelable {
    private String nowUser;
    private List<String> availableUserSet;
    private boolean isMultiVirtualEnv;
    private boolean disableMultiUserAPiSwitch;

    public MultiUserBundle(String nowUser, List<String> availableUserSet, boolean isMultiVirtualEnv, boolean disableMultiUserAPiSwitch) {
        this.nowUser = nowUser;
        this.availableUserSet = availableUserSet;
        this.isMultiVirtualEnv = isMultiVirtualEnv;
        this.disableMultiUserAPiSwitch = disableMultiUserAPiSwitch;
    }

    protected MultiUserBundle(Parcel in) {
        nowUser = in.readString();
        availableUserSet = in.createStringArrayList();
        isMultiVirtualEnv = in.readByte() != 0;
        disableMultiUserAPiSwitch = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(nowUser);
        dest.writeStringList(availableUserSet);
        dest.writeByte((byte) (isMultiVirtualEnv ? 1 : 0));
        dest.writeByte((byte) (disableMultiUserAPiSwitch ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MultiUserBundle> CREATOR = new Creator<MultiUserBundle>() {
        @Override
        public MultiUserBundle createFromParcel(Parcel in) {
            return new MultiUserBundle(in);
        }

        @Override
        public MultiUserBundle[] newArray(int size) {
            return new MultiUserBundle[size];
        }
    };

    public String getNowUser() {
        return nowUser;
    }

    public List<String> getAvailableUserSet() {
        return availableUserSet;
    }

    public boolean isMultiVirtualEnv() {
        return isMultiVirtualEnv;
    }

    public boolean isDisableMultiUserAPiSwitch() {
        return disableMultiUserAPiSwitch;
    }

    public void setDisableMultiUserAPiSwitch(boolean disableMultiUserAPiSwitch) {
        this.disableMultiUserAPiSwitch = disableMultiUserAPiSwitch;
    }
}
