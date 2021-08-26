package android.net;

import android.os.Parcel;
import android.os.Parcelable;

public class ProxyInfo implements Parcelable {
    protected ProxyInfo(Parcel in) {
    }

    public static final Creator<ProxyInfo> CREATOR = new Creator<ProxyInfo>() {
        @Override
        public ProxyInfo createFromParcel(Parcel in) {
            return new ProxyInfo(in);
        }

        @Override
        public ProxyInfo[] newArray(int size) {
            return new ProxyInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
