package android.app.job;

import android.os.Parcel;
import android.os.Parcelable;

public class JobInfo implements Parcelable {
    protected JobInfo(Parcel in) {
    }

    public static final Creator<JobInfo> CREATOR = new Creator<JobInfo>() {
        @Override
        public JobInfo createFromParcel(Parcel in) {
            return new JobInfo(in);
        }

        @Override
        public JobInfo[] newArray(int size) {
            return new JobInfo[size];
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
