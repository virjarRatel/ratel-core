package android.net;

import android.os.Parcel;
import android.os.Parcelable;

public class NetworkCapabilities implements Parcelable {
    protected NetworkCapabilities(Parcel in) {
    }

    public static final Creator<NetworkCapabilities> CREATOR = new Creator<NetworkCapabilities>() {
        @Override
        public NetworkCapabilities createFromParcel(Parcel in) {
            return new NetworkCapabilities(in);
        }

        @Override
        public NetworkCapabilities[] newArray(int size) {
            return new NetworkCapabilities[size];
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
