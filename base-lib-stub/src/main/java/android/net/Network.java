package android.net;

import android.os.Parcel;
import android.os.Parcelable;

public class Network implements Parcelable {
    protected Network(Parcel in) {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Network> CREATOR = new Creator<Network>() {
        @Override
        public Network createFromParcel(Parcel in) {
            return new Network(in);
        }

        @Override
        public Network[] newArray(int size) {
            return new Network[size];
        }
    };

    public void writeToParcel(Parcel data, int i) {
        throw new UnsupportedOperationException("STUB");
    }
}
