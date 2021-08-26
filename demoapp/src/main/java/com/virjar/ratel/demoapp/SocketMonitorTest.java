package com.virjar.ratel.demoapp;

import android.annotation.SuppressLint;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

public class SocketMonitorTest {
    public static void testSocketMonitor() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                while (!Thread.currentThread().isInterrupted()) {

                    try {
                        doTest();
                        Thread.sleep(35000);
                    } catch (InterruptedException e) {
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    private static void doTest() throws IOException {
        Request request = new Request.Builder()
                .get()
                .url("https://mobile.southwest.com/api/mobile-air-booking/v1/mobile-air-booking/page/flights/products?origination-airport=LAX&destination-airport=LAS&departure-date=2019-10-29&number-adult-passengers=1&number-senior-passengers=0&currency=USD")
                //.url("http://sekiro.virjar.com/natChannelStatus?group=sekiro-demo")
                .header("X-User-Experience-ID", "07728841-3657-4ca9-a6d2-bdf31675e93f")
                .header("X-Channel-ID", "ANDROID")
                .header("X-API-Key", "l7xx8389a5ba9eb24ae68bad068bd1860bfc")
                .header("User-Agent", "SouthwestAndroid/6.10.2 android/8.1.0")
                .header("Accept-Encoding", "gzip")
                .header("X-dUblrIiu-a", "cHIbbhxc3LiDjSicugaZOip2rpn_=rEmpTxZOEnyC0qIN34mb43EfsXSK-3nx93HlLic5muLRdlatCPky2Z2=V6BTMoYDgMCqiZ6nUeudhoHCrwBWfsPGhpVLPyBppoE0p8=u9t7o6_4gkWZCXx6uZGBha4G-aPUeDzxH42Crk3phimOG73fR6RlcjMyKgm5K4WfkxC3DG-kqehHe=VRyLYKilYDp5vDPvXoRJlSF_UNhWv-Oq2Sj7xk5ivXLEnWdVivjkpX2vGQSpQT6h9t60y=tXQo3NsnN3ZPq90QdwJkfwop8BwuUYG8Jk7RUyW8F60_6LrEffzOEnkUl_g0uoGMpISNyHG5djBdtDza7qX9JHVHravYKvFEJd9a5fPDy638aRkTA0PAkxN90h4TDtSA7gZu4Gv2jpuy6-zsiEGYB8=d6CbzZnHwUi6tr8IadGictIu-9VfsJVWgtDw=2Yy2CgJAyaIGT_TcrwkpCjLoc8iHdRIRwYBsQcDXATOlsdi7OWI3mSOg5aJWPhmf878NoMdSBw3qNBjjymX-E6UQIOSyniqdYxWEO7fpcBcWpBSe_sF=DeumLnXvqvHkmNSQJ9Mpn8TserdpFt8wx_3txpMyo7NyYHArAYlcISBQXbJuxtutW3Kisq9PMa0gx-H4ylQAEUO=MlhvERQ7nWH4fFVrA3avMJBFQXLMmzLDvjS-jgkXehQUdmngLiuhAp8zezJQJvkwDSBgKyDHEEKS7khDg0Cs3m58qjVLmARY2xh_PWcC4=ajKpW-3AN7GTWNFgY5VpZD9iKSaS2Mt39LD0VzkOp_DkIz=NlBiuTNI3i6RtzuR7A=0-Em_PjKWr7=t2RI9X0rwcFJdXoNSFR3uEUkMHItDZvYjFgm3Cljljjlp2RE3_KBJJvxnqoi3gBaaXIE7mke6CykXgwHkCSiKkfEZ6tQNrMiaveUeFcTlPJpw0SYVS65nYx2uowIwwgCORLXNn_q3eBiRligQVaOXlBdM7zJ3sdR-TL=IWWlTDz0tgBs8mskEdpUPTn9hDYN0n8TNxb4tds5AYSFLbehHcHJjRCkJAYT0CxTD9qIN_9=tjfp9PdEaU5LMkuXTwVlg6FgwEraBVDU-xqaI8ystQlsut2HvWSQqBKxeewTGNIhgzUzDjBaOCpxjcLWgs9xCy3ievJZBEeQ=kLW=2MFa4_mulvV7ONSEbKj6hKRnZFvQ6JUNP_FSA4UleSno3gR=XL8XAylBMD4TV0_nFahdAd4qZQT92TJXZUYP0yQMG=nkylWEm3rA3qtKHDlB8CejM28H5TCyboUh8bk5FYRJsIAj5yrwTtspeF95n72IKcUq6M2J40jp7X_xQeEUtn_TeRHN7SB2EOobQTCYLdO=48IhCsS4wJsmCxy5XazgSEy5xN=fCwL9RcnjBWHJJK4oMRD0A_P7b-wsfChidTCChCUBiFuoCLl_nkOrLWnR=_=ZzVc3SFPncHmA7muZHCoUTuw_bkO3XkbKBoxbcmYwVqPvId6")
                .header("X-dUblrIiu-b", "13nfp8")
                .header("X-dUblrIiu-c", "A_pmoRFuAQAAAyvH-MrsjVlx4r9FUY7bYFcYJ8l-KgepG4FoTKmFbUkrqiuLAWPLV7AAAAAAAAAAAAAAAAAAAA==")
                .header("X-dUblrIiu-d", "o_2")
                .header("X-dUblrIiu-e", "a;SFiTu-glc91cAYtIwviEnW_-qz-NGa-t0lxwx8lBmRKJ_4FL4DLig1TdmWH79cP4IJ_P21mQRaPjxSFDN8Igw2Ml6vaQskl0AT1ChCGeNtDpTlq8sLVKKL3DOICnvfp8T2FtkbKVQxWOAignByDOogi-9csggeST3v5NsjQOpMF3cxCiGY0pdE9gVx8Jy62uhFSHI5crY0ew2Q8Q1OT7lwv9RuydcWlyLlQZXEymdEYpBR4scsbiYwvVDJVm9xtZr_hq3GmFcnqSwQNmyoxJFFsUrlIGE7dvqTDocg5DkEHDmgq74IpmsTCQARI-jdXfHdDNU76cgI742Oiz5EPj4z_Eld_X4VqM9lSxWVO8Iw8KUEIgCrq9PORsVE21OgUcRMXzoQOhbatF595Fj5q78mIwu5MC8cZm4Gp_JMfaPvVuR5ecHj2ft9Qc6YZGMoPDoBWyMVIJtIM7bw5IT1cr1FzI1-35HP1pBIc2J2jmzSFZdiTks3mxq17Z5WfDKh9lS76Kaq8KG068S6cdSM1bA-CppRLVzDxdPh0KU4rLgHkd11H8_0Ob-RZWOIB_4v_0YylCFRIuNUKrmz4fbfjWx8PXGbv1biZALVmSPfTcFYQGmHJXmggCNTr6zHyebT2mCFGa9f2RRIfQvCeJvuzKWEQ7joFT44Kt6oGn0yleKOLxCXvNymEC_vOiokd6d4A4HYboMBjHsxzh5y4cLne8aeSiCEujy8EwvR_nVA3_HNhVqPL7oNKmovfBHfpidm3cjn6QYCG4JurqJeU8HlpZWqlGe1ZNOAhlor3mnvNlU1udUtCdu1Uqi9Jm5f-2KlGCCRuT6dLuRhRqeUZYdEKoMuUqSLZoBQ==;bsIlKTNBG53GEx0spwiAanXm40SQdXVQ1zSjPYTevjk=")
                .header("X-dUblrIiu-f", "AzkTpBFuAQAAHjvoKpUqdzdIBVsEtzpOWJ79Nb6mD9m_03Z1zhmgM8cFQjWyAXWIAIUAAAAAAAAAAAAAAAAAAA==")
                .header("X-dUblrIiu-z", "p")
                .build();

        SSLSocketFactory sslSocketFactory = createSSLSocketFactory();
        if (sslSocketFactory == null) {
            return;
        }
        OkHttpClient okHttpClient = createBaseHttpClientBuilder()
                .sslSocketFactory(sslSocketFactory, new TrustAllManager())
                .hostnameVerifier(new TrustAllHostnameVerifier())
                .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("192.168.2.1", 8888))).build();

        ResponseBody body = okHttpClient.newCall(request).execute().body();
        byte[] bytes = body.bytes();
        String string = new String(bytes);
        System.out.println(string);


    }

    /**
     * 默认信任所有的证书
     * TODO 最好加上证书认证，主流App都有自己的证书
     *
     * @return
     */
    @SuppressLint("TrulyRandom")
    private static SSLSocketFactory createSSLSocketFactory() {

        SSLSocketFactory sSLSocketFactory = null;

        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllManager()},
                    new SecureRandom());
            sSLSocketFactory = sc.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sSLSocketFactory;
    }

    private static class TrustAllManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)

                throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    private static class TrustAllHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    private static OkHttpClient.Builder createBaseHttpClientBuilder() {
        //由于大量请求都是心跳请求，需要心跳keepAlive，同时考虑心跳时间间隔来确定链接存活时长
        return new OkHttpClient.Builder()

                .retryOnConnectionFailure(false)
                .connectionPool(new ConnectionPool(1, 10, TimeUnit.SECONDS))
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
}
