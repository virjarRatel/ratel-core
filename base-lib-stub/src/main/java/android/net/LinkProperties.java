package android.net;

import android.os.Parcel;
import android.os.Parcelable;

public class LinkProperties implements Parcelable {
    protected LinkProperties(Parcel in) {
    }

    public static final Creator<LinkProperties> CREATOR = new Creator<LinkProperties>() {
        @Override
        public LinkProperties createFromParcel(Parcel in) {
            return new LinkProperties(in);
        }

        @Override
        public LinkProperties[] newArray(int size) {
            return new LinkProperties[size];
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
