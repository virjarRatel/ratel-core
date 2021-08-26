package com.virjar.ratel.envmock.binder;

import android.os.Parcelable;

public class BinderInvokeException {
    public int exceptionCode;
    public String exceptionMessage;
    public int remoteStackTraceSize;
    public String remoteStackTraces;
    public int specificServiceErrorCode;
    public int exParcelableSize;
    public Parcelable exParcelable;

    @Override
    public String toString() {
        return "BinderInvokeException{" +
                "exceptionCode=" + exceptionCode +
                ", exceptionMessage='" + exceptionMessage + '\'' +
                ", remoteStackTraceSize=" + remoteStackTraceSize +
                ", remoteStackTraces='" + remoteStackTraces + '\'' +
                ", specificServiceErrorCode=" + specificServiceErrorCode +
                ", exParcelableSize=" + exParcelableSize +
                ", exParcelable=" + exParcelable +
                '}';
    }
}
