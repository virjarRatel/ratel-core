package android.net;

import android.os.Parcel;
import android.os.Parcelable;

public class NetworkRequest implements Parcelable {
    protected NetworkRequest(Parcel in) {
    }

    public static final Creator<NetworkRequest> CREATOR = new Creator<NetworkRequest>() {
        @Override
        public NetworkRequest createFromParcel(Parcel in) {
            return new NetworkRequest(in);
        }

        @Override
        public NetworkRequest[] newArray(int size) {
            return new NetworkRequest[size];
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
