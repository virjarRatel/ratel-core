package com.virjar.ratel.manager.bridge;

import android.os.Parcel;
import android.os.Parcelable;

public class ClientInfo implements Parcelable {
    private String packageName;
    private String processName;
    private int pid;

    public ClientInfo(Parcel in) {
        packageName = in.readString();
        processName = in.readString();
        pid = in.readInt();
    }

    public ClientInfo(String packageName, String processName, int pid) {
        this.packageName = packageName;
        this.processName = processName;
        this.pid = pid;
    }

    public static final Creator<ClientInfo> CREATOR = new Creator<ClientInfo>() {
        @Override
        public ClientInfo createFromParcel(Parcel in) {
            return new ClientInfo(in);
        }

        @Override
        public ClientInfo[] newArray(int size) {
            return new ClientInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(packageName);
        parcel.writeString(processName);
        parcel.writeInt(pid);
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }


}
