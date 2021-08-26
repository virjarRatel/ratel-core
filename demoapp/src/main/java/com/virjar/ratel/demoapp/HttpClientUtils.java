package com.virjar.ratel.demoapp;


import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by zhuyin on 2018/3/28.<br>
 * http网络层统一封装
 */

public class HttpClientUtils {
    private static OkHttpClient client;
    /**
     * a OKHttpClient instance witch with long timeout setting,used for blocking since
     */
    private static OkHttpClient blockingClient;


    private static OkHttpClient.Builder createBaseHttpClientBuilder() {
        //由于大量请求都是心跳请求，需要心跳keepAlive，同时考虑心跳时间间隔来确定链接存活时长
        ConnectionPool connectionPool = new ConnectionPool(5, 30, TimeUnit.SECONDS);
        return new OkHttpClient.Builder()

                .connectionPool(connectionPool)
                .retryOnConnectionFailure(false)
                .dispatcher(new Dispatcher(new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
                        new SynchronousQueue<Runnable>(), new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable runnable) {
                        Thread result = new Thread(runnable, "OkHttp Dispatcher");
                        result.setDaemon(false);
                        //这里，如果网络handler发生了异常，那么只记录日志，而不进行程序中断
                        result.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                            @Override
                            public void uncaughtException(Thread t, Throwable e) {
                                Log.e("weijia", "network callback exception", e);
                            }
                        });
                        //result.setUncaughtExceptionHandler(LogedExceptionHandler.wrap(null));
                        return result;
                    }
                })));
    }


    public static OkHttpClient getClient() {
        if (client != null) {
            return client;
        }
        synchronized (HttpClientUtils.class) {
            if (client == null) {
                client = createBaseHttpClientBuilder()
                        .readTimeout(15, TimeUnit.SECONDS)
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .writeTimeout(15, TimeUnit.SECONDS)
                        .build();
            }
        }
        return client;
    }

    public static OkHttpClient getBlockingClient() {
        if (blockingClient != null) {
            return blockingClient;
        }
        synchronized (HttpClientUtils.class) {
            if (blockingClient != null) {
                return blockingClient;
            }
            blockingClient = createBaseHttpClientBuilder()
                    .readTimeout(30, TimeUnit.SECONDS)
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();
        }
        return blockingClient;
    }


    private static final MediaType JSON = MediaType.parse("application/json; charset=utf8");

    public static String get(String url) {
        return executeSync(getRequest(url));
    }

    public static Request getRequest(String url) {
        return new Request.Builder()
                .get()
                .url(url)
                .build();
    }

    public static String postJSON(String url, String json) {
        return executeSync(postJsonRequest(url, json));
    }

    public static Request postJsonRequest(String url, String json) {
        RequestBody body = RequestBody.create(JSON, json);
        return new Request.Builder()
                .url(url)
                .post(body)
                .build();
    }

    public static String post(String url, Map<String, String> param) {
        return executeSync(postRequest(url, param));
    }

    public static void notifyPost(String url, Map<String, String> param, final String logTag) {
        Request request = postRequest(url, param);
        getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(logTag, "request failed", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ResponseBody body = response.body();
                if (body == null) {
                    return;
                }
                Log.i(logTag, "request success:" + body.string());
            }
        });
    }

    public static Request postRequest(String url, Map<String, String> param) {
        FormBody.Builder builder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : param.entrySet()) {
            builder.add(entry.getKey(), entry.getValue());
        }
        return new Request.Builder().url(url).post(builder.build()).build();
    }

    public static boolean inMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }

    private static String executeSync(final Request request) {
        Response response = null;
        try {
            response = getClient().newCall(request).execute();
            if (response.isSuccessful()) {
                ResponseBody responseBody = response.body();
                if (responseBody == null) {
                    return null;
                }
                return responseBody.string();
            }
            return null;

        } catch (IOException e) {
            Log.e("COMMON_HOOK", "http client access error", e);
            return null;
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }
}
