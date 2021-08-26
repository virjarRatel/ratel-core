package com.virjar.ratel.envmock.binder;

import android.os.Parcel;

/**
 * binder机制hook
 */
public interface BinderHookParcelHandler {

    /**
     * 远程调用参数，在transact的时候，会被压缩成Parcel
     *
     * @param parcel Parcel
     * @return Object[]
     */
    Object[] parseInvokeParam(String descriptor, Parcel parcel);

    /**
     * 重新将参数转化为Parcel
     *
     * @param parcel Parcel容器
     * @param args   参数
     */
    void writeParamsToParcel(String descriptor, Parcel parcel, Object[] args);

    /**
     * 远程调用的结果，进行反序列化，以此我们可以修改反序列化
     *
     * @param parcel Parcel
     * @return mode
     */
    Object parseInvokeResult(Parcel parcel);

    /**
     * 重新将调用结果压回parcel
     *
     * @param parcel parcel
     * @param object object
     */
    void writeNewResultToParcel(Parcel parcel, Object object);

    String name();

    String method();

    int transactCode();
}
