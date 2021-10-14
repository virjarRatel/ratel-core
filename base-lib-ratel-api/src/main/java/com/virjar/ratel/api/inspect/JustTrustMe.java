package com.virjar.ratel.api.inspect;


import android.annotation.SuppressLint;
import android.net.http.SslError;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;

import com.virjar.ratel.api.RatelToolKit;
import com.virjar.ratel.api.rposed.RC_MethodHook;
import com.virjar.ratel.api.rposed.RC_MethodReplacement;
import com.virjar.ratel.api.rposed.RposedBridge;
import com.virjar.ratel.api.rposed.RposedHelpers;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.HostNameResolver;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


/**
 * Created by virjar on 2018/5/7.<br>强制信任任何代理服务器证书，便于抓包
 * migrated from https://github.com/Fuzion24/JustTrustMe/tree/master
 */
public class JustTrustMe {

    private static final String TAG = "JustTrustMe";
    private static String currentPackageName = "";

    static {
        try {
            trustAllCertificateInternal();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public static void trustAllCertificate() {
        //do nothing
    }

    private static void trustAllCertificateInternal() throws Throwable {

        currentPackageName = RatelToolKit.packageName;

        //这个需要第一个
        trustAndroidRootTrustManager();

        trustApache();

        trustJSSE();

        trustWebView();

        trustConscrypt();

        //SSLContext.init >> (null,ImSureItsLegitTrustManager,null)
        RposedHelpers.findAndHookMethod("javax.net.ssl.SSLContext", RatelToolKit.sContext.getClassLoader(), "init", KeyManager[].class, TrustManager[].class, SecureRandom.class, new RC_MethodHook() {

            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                param.args[0] = null;
                param.args[1] = new TrustManager[]{new ImSureItsLegitTrustManager()};
                param.args[2] = null;

            }
        });

        processOkHttp(RatelToolKit.hostClassLoader);
        processHttpClientAndroidLib(RatelToolKit.hostClassLoader);
        processXutils(RatelToolKit.hostClassLoader);


    } // End Hooks


    private interface ClassExistEvent {
        void onClassFind(Class clazz);
    }

    private static void hookIfClassExist(String className, ClassExistEvent classExistEvent) {
        Class<?> classIfExists = RposedHelpers.findClassIfExists(className, RatelToolKit.sContext.getClassLoader());
        if (classIfExists != null) {
            try {
                classExistEvent.onClassFind(classIfExists);
            } catch (Throwable throwable) {
                Log.w(RatelToolKit.TAG, "handle class load callback failed", throwable);
            }
        }
    }


    private static void trustAndroidRootTrustManager() {
        Class<?> rootTrustManagerClass;
        try {
            rootTrustManagerClass = ClassLoader.getSystemClassLoader().loadClass("android.security.net.config.RootTrustManager");
        } catch (Throwable throwable) {
            return;
        }

        for (Method method : rootTrustManagerClass.getDeclaredMethods()) {
            if (method.getName().equals("checkServerTrusted")
                    && method.getReturnType().equals(Void.TYPE)) {
                RposedBridge.hookMethod(method, new RC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                        return null;
                    }
                });
            }
        }
    }

    private static void trustApache() {
        /* Apache Hooks */
        /* external/apache-http/src/org/apache/http/impl/client/DefaultHttpClient.java */
        /* public DefaultHttpClient() */

        hookIfClassExist("org.apache.http.impl.client.DefaultHttpClient", new ClassExistEvent() {
            @Override
            public void onClassFind(Class clazz) {
                Log.d(TAG, "Hooking DefaultHTTPClient for: " + currentPackageName);
                RposedHelpers.findAndHookConstructor(clazz, new RC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                        RposedHelpers.setObjectField(param.thisObject, "defaultParams", null);
                        RposedHelpers.setObjectField(param.thisObject, "connManager", getSCCM());
                    }
                });
            }
        });


        /* external/apache-http/src/org/apache/http/impl/client/DefaultHttpClient.java */
        /* public DefaultHttpClient(HttpParams params) */
        hookIfClassExist("org.apache.http.impl.client.DefaultHttpClient", new ClassExistEvent() {
            @Override
            public void onClassFind(Class clazz) {
                Log.d(TAG, "Hooking DefaultHTTPClient(HttpParams) for: " + currentPackageName);
                RposedHelpers.findAndHookConstructor(clazz, HttpParams.class, new RC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                        RposedHelpers.setObjectField(param.thisObject, "defaultParams", param.args[0]);
                        RposedHelpers.setObjectField(param.thisObject, "connManager", getSCCM());
                    }
                });
            }
        });


        /* external/apache-http/src/org/apache/http/impl/client/DefaultHttpClient.java */
        /* public DefaultHttpClient(ClientConnectionManager conman, HttpParams params) */
        hookIfClassExist("org.apache.http.impl.client.DefaultHttpClient", new ClassExistEvent() {
            @Override
            public void onClassFind(Class clazz) {
                Log.d(TAG, "Hooking DefaultHTTPClient(ClientConnectionManager, HttpParams) for: " + currentPackageName);
                RposedHelpers.findAndHookConstructor(clazz, "org.apache.http.conn.ClientConnectionManager", "org.apache.http.params.HttpParams", new RC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                        HttpParams params = (HttpParams) param.args[1];

                        RposedHelpers.setObjectField(param.thisObject, "defaultParams", params);
                        RposedHelpers.setObjectField(param.thisObject, "connManager", getCCM(param.args[0], params));
                    }
                });
            }
        });


        /* external/apache-http/src/org/apache/http/conn/ssl/SSLSocketFactory.java */
        /* public SSLSocketFactory( ... ) */
        Log.d(TAG, "Hooking SSLSocketFactory(String, KeyStore, String, KeyStore) for: " + currentPackageName);
        RposedHelpers.findAndHookConstructor(SSLSocketFactory.class, String.class, KeyStore.class, String.class, KeyStore.class,
                SecureRandom.class, HostNameResolver.class, new RC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                        String algorithm = (String) param.args[0];
                        KeyStore keystore = (KeyStore) param.args[1];
                        String keystorePassword = (String) param.args[2];
                        SecureRandom random = (SecureRandom) param.args[4];

                        KeyManager[] keyManagers = null;
                        TrustManager[] trustManagers;

                        if (keystore != null) {
                            keyManagers = (KeyManager[]) RposedHelpers.callStaticMethod(SSLSocketFactory.class, "createKeyManagers", keystore, keystorePassword);
                        }

                        trustManagers = new TrustManager[]{new ImSureItsLegitTrustManager()};

                        RposedHelpers.setObjectField(param.thisObject, "sslcontext", SSLContext.getInstance(algorithm));
                        RposedHelpers.callMethod(RposedHelpers.getObjectField(param.thisObject, "sslcontext"), "init", keyManagers, trustManagers, random);
                        RposedHelpers.setObjectField(param.thisObject, "socketfactory",
                                RposedHelpers.callMethod(RposedHelpers.getObjectField(param.thisObject, "sslcontext"), "getSocketFactory"));
                    }

                });


        /* external/apache-http/src/org/apache/http/conn/ssl/SSLSocketFactory.java */
        /* public static SSLSocketFactory getSocketFactory() */
        hookIfClassExist("org.apache.http.conn.ssl.SSLSocketFactory", new ClassExistEvent() {
            @Override
            public void onClassFind(Class clazz) {
                Log.d(TAG, "Hooking static SSLSocketFactory(String, KeyStore, String, KeyStore) for: " + currentPackageName);
                RposedHelpers.findAndHookMethod(clazz, "getSocketFactory", new RC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                        return RposedHelpers.newInstance(SSLSocketFactory.class);
                    }
                });
            }
        });


        /* external/apache-http/src/org/apache/http/conn/ssl/SSLSocketFactory.java */
        /* public boolean isSecure(Socket) */
        hookIfClassExist("org.apache.http.conn.ssl.SSLSocketFactory", new ClassExistEvent() {
            @Override
            public void onClassFind(Class clazz) {
                Log.d(TAG, "Hooking SSLSocketFactory(Socket) for: " + currentPackageName);
                RposedHelpers.findAndHookMethod(clazz, "isSecure", Socket.class, new RC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                        return true;
                    }
                });
            }
        });

    }

    private static void trustJSSE() {
        /* JSSE Hooks */
        /* libcore/luni/src/main/java/javax/net/ssl/TrustManagerFactory.java */
        /* public final TrustManager[] getTrustManager() */
        Log.d(TAG, "Hooking TrustManagerFactory.getTrustManagers() for: " + currentPackageName);
        RposedHelpers.findAndHookMethod("javax.net.ssl.TrustManagerFactory", RatelToolKit.sContext.getClassLoader(), "getTrustManagers", new RC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                if (hasTrustManagerImpl()) {
                    Class<?> cls = RposedHelpers.findClass("com.android.org.conscrypt.TrustManagerImpl", RatelToolKit.sContext.getClassLoader());

                    TrustManager[] managers = (TrustManager[]) param.getResult();
                    if (managers.length > 0 && cls.isInstance(managers[0]))
                        return;
                }
                TrustManager[] trustManagers = (TrustManager[]) param.getResult();
                for (TrustManager trustManager : trustManagers) {
                    Class<? extends TrustManager> aClass = trustManager.getClass();
                    //Android 9以后，被 android.security.net.config.RootTrustManager管理
                    //此时不能替换为ImSureItsLegitTrustManager
                    if (aClass.getName().equals("android.security.net.config.RootTrustManager")) {
                        return;
                    }
                }

                param.setResult(new TrustManager[]{new ImSureItsLegitTrustManager()});
            }
        });

        /* libcore/luni/src/main/java/javax/net/ssl/HttpsURLConnection.java */
        /* public void setDefaultHostnameVerifier(HostnameVerifier) */
        Log.d(TAG, "Hooking HttpsURLConnection.setDefaultHostnameVerifier for: " + currentPackageName);
        RposedHelpers.findAndHookMethod("javax.net.ssl.HttpsURLConnection", RatelToolKit.sContext.getClassLoader(), "setDefaultHostnameVerifier",
                HostnameVerifier.class, new RC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                        return null;
                    }
                });

        /* libcore/luni/src/main/java/javax/net/ssl/HttpsURLConnection.java */
        /* public void setSSLSocketFactory(SSLSocketFactory) */
        Log.d(TAG, "Hooking HttpsURLConnection.setSSLSocketFactory for: " + currentPackageName);
        RposedHelpers.findAndHookMethod("javax.net.ssl.HttpsURLConnection", RatelToolKit.sContext.getClassLoader(), "setSSLSocketFactory", javax.net.ssl.SSLSocketFactory.class,
                new RC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                        return null;
                    }
                });

        /* libcore/luni/src/main/java/javax/net/ssl/HttpsURLConnection.java */
        /* public void setHostnameVerifier(HostNameVerifier) */
        Log.d(TAG, "Hooking HttpsURLConnection.setHostnameVerifier for: " + currentPackageName);
        RposedHelpers.findAndHookMethod("javax.net.ssl.HttpsURLConnection", RatelToolKit.sContext.getClassLoader(), "setHostnameVerifier", HostnameVerifier.class,
                new RC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                        return null;
                    }
                });
    }


    private static void trustWebView() {
        /* WebView Hooks */
        /* frameworks/base/core/java/android/webkit/WebViewClient.java */
        /* public void onReceivedSslError(Webview, SslErrorHandler, SslError) */
        Log.d(TAG, "Hooking WebViewClient.onReceivedSslError(WebView, SslErrorHandler, SslError) for: " + currentPackageName);

        RposedHelpers.findAndHookMethod("android.webkit.WebViewClient", RatelToolKit.sContext.getClassLoader(), "onReceivedSslError",
                WebView.class, SslErrorHandler.class, SslError.class, new RC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                        ((SslErrorHandler) param.args[1]).proceed();
                        return null;
                    }
                });

        /* frameworks/base/core/java/android/webkit/WebViewClient.java */
        /* public void onReceivedError(WebView, int, String, String) */
        Log.d(TAG, "Hooking WebViewClient.onReceivedSslError(WebView, int, string, string) for: " + currentPackageName);

        RposedHelpers.findAndHookMethod("android.webkit.WebViewClient", RatelToolKit.sContext.getClassLoader(), "onReceivedError",
                WebView.class, int.class, String.class, String.class, new RC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                        return null;
                    }
                });
    }

    private static void trustConscrypt() {
        /* Only for newer devices should we try to hook TrustManagerImpl */
        if (hasTrustManagerImpl()) {
            /* TrustManagerImpl Hooks */
            /* external/conscrypt/src/platform/java/org/conscrypt/TrustManagerImpl.java */
            Log.d(TAG, "Hooking com.android.org.conscrypt.TrustManagerImpl for: " + currentPackageName);

            /* public void checkServerTrusted(X509Certificate[] chain, String authType) */
            RposedHelpers.findAndHookMethod("com.android.org.conscrypt.TrustManagerImpl", RatelToolKit.sContext.getClassLoader(),
                    "checkServerTrusted", X509Certificate[].class, String.class,
                    new RC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                            return 0;
                        }
                    });

            /* public List<X509Certificate> checkServerTrusted(X509Certificate[] chain,
                                    String authType, String host) throws CertificateException */
            RposedHelpers.findAndHookMethod("com.android.org.conscrypt.TrustManagerImpl", RatelToolKit.sContext.getClassLoader(),
                    "checkServerTrusted", X509Certificate[].class, String.class,
                    String.class, new RC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                            return new ArrayList<X509Certificate>();
                        }
                    });


            try {
            /* public List<X509Certificate> checkServerTrusted(X509Certificate[] chain,
                                    String authType, SSLSession session) throws CertificateException */
                RposedHelpers.findAndHookMethod("com.android.org.conscrypt.TrustManagerImpl", RatelToolKit.sContext.getClassLoader(),
                        "checkServerTrusted", X509Certificate[].class, String.class,
                        SSLSession.class, new RC_MethodReplacement() {
                            @Override
                            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                                return new ArrayList<X509Certificate>();
                            }
                        });
            } catch (NoSuchMethodError ignore) {
                //
            }
        }
    }


    /* Helpers */
    // Check for TrustManagerImpl class
    @SuppressLint("PrivateApi")
    private static boolean hasTrustManagerImpl() {

        try {
            Class.forName("com.android.org.conscrypt.TrustManagerImpl", false, RatelToolKit.hostClassLoader);
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
    }

    private static javax.net.ssl.SSLSocketFactory getEmptySSLFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new ImSureItsLegitTrustManager()}, null);
            return sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            return null;
        }
    }

    //Create a SingleClientConnManager that trusts everyone!
    private static ClientConnectionManager getSCCM() {

        KeyStore trustStore;
        try {

            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            SSLSocketFactory sf = new TrustAllSSLSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            return new SingleClientConnManager(null, registry);

        } catch (Exception e) {
            return null;
        }
    }

    //This function creates a ThreadSafeClientConnManager that trusts everyone!
    private static ClientConnectionManager getTSCCM(HttpParams params) {

        KeyStore trustStore;
        try {

            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            SSLSocketFactory sf = new TrustAllSSLSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            return new ThreadSafeClientConnManager(params, registry);

        } catch (Exception e) {
            return null;
        }
    }

    //This function determines what object we are dealing with.
    private static ClientConnectionManager getCCM(Object o, HttpParams params) {

        String className = o.getClass().getSimpleName();

        if (className.equals("SingleClientConnManager")) {
            return getSCCM();
        } else if (className.equals("ThreadSafeClientConnManager")) {
            return getTSCCM(params);
        }

        return null;
    }

    private static void processXutils(ClassLoader classLoader) {
        Log.d(TAG, "Hooking org.xutils.http.RequestParams.setSslSocketFactory(SSLSocketFactory) (3) for: " + currentPackageName);
        try {
            classLoader.loadClass("org.xutils.http.RequestParams");
            RposedHelpers.findAndHookMethod("org.xutils.http.RequestParams", classLoader, "setSslSocketFactory", javax.net.ssl.SSLSocketFactory.class, new RC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    param.args[0] = getEmptySSLFactory();
                }
            });
            RposedHelpers.findAndHookMethod("org.xutils.http.RequestParams", classLoader, "setHostnameVerifier", HostnameVerifier.class, new RC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    param.args[0] = new ImSureItsLegitHostnameVerifier();
                }
            });
        } catch (Exception e) {
            Log.d(TAG, "org.xutils.http.RequestParams not found in " + currentPackageName + "-- not hooking");
        }
    }

    private static void processOkHttp(ClassLoader classLoader) {
        /* hooking OKHTTP by SQUAREUP */
        /* com/squareup/okhttp/CertificatePinner.java available online @ https://github.com/square/okhttp/blob/master/okhttp/src/main/java/com/squareup/okhttp/CertificatePinner.java */
        /* public void check(String hostname, List<Certificate> peerCertificates) throws SSLPeerUnverifiedException{}*/
        /* Either returns true or a exception so blanket return true */
        /* Tested against version 2.5 */
        Log.d(TAG, "Hooking com.squareup.okhttp.CertificatePinner.check(String,List) (2.5) for: " + currentPackageName);

        try {
            classLoader.loadClass("com.squareup.okhttp.CertificatePinner");
            RposedHelpers.findAndHookMethod("com.squareup.okhttp.CertificatePinner",
                    classLoader,
                    "check",
                    String.class,
                    List.class,
                    new RC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                            return true;
                        }
                    });
        } catch (ClassNotFoundException e) {
            // pass
            Log.d(TAG, "OKHTTP 2.5 not found in " + currentPackageName + "-- not hooking");
        }

        //https://github.com/square/okhttp/blob/parent-3.0.1/okhttp/src/main/java/okhttp3/CertificatePinner.java#L144
        Log.d(TAG, "Hooking okhttp3.CertificatePinner.check(String,List) (3.x) for: " + currentPackageName);

        try {
            classLoader.loadClass("okhttp3.CertificatePinner");
            RposedHelpers.findAndHookMethod("okhttp3.CertificatePinner",
                    classLoader,
                    "check",
                    String.class,
                    List.class,
                    new RC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                            return null;
                        }
                    });
        } catch (ClassNotFoundException e) {
            Log.d(TAG, "OKHTTP 3.x not found in " + currentPackageName + " -- not hooking");
            // pass
        }

        //https://github.com/square/okhttp/blob/parent-3.0.1/okhttp/src/main/java/okhttp3/internal/tls/OkHostnameVerifier.java
        try {
            classLoader.loadClass("okhttp3.internal.tls.OkHostnameVerifier");
            RposedHelpers.findAndHookMethod("okhttp3.internal.tls.OkHostnameVerifier",
                    classLoader,
                    "verify",
                    String.class,
                    SSLSession.class,
                    new RC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                            return true;
                        }
                    });
        } catch (ClassNotFoundException e) {
            Log.d(TAG, "OKHTTP 3.x not found in " + currentPackageName + " -- not hooking OkHostnameVerifier.verify(String, SSLSession)");
            // pass
        }

        //https://github.com/square/okhttp/blob/parent-3.0.1/okhttp/src/main/java/okhttp3/internal/tls/OkHostnameVerifier.java
        try {
            classLoader.loadClass("okhttp3.internal.tls.OkHostnameVerifier");
            RposedHelpers.findAndHookMethod("okhttp3.internal.tls.OkHostnameVerifier",
                    classLoader,
                    "verify",
                    String.class,
                    X509Certificate.class,
                    new RC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                            return true;
                        }
                    });
        } catch (ClassNotFoundException e) {
            Log.d(TAG, "OKHTTP 3.x not found in " + currentPackageName + " -- not hooking OkHostnameVerifier.verify(String, X509)(");
            // pass
        }
    }

    private static void processHttpClientAndroidLib(ClassLoader classLoader) {
        /* httpclientandroidlib Hooks */
        /* public final void verify(String host, String[] cns, String[] subjectAlts, boolean strictWithSubDomains) throws SSLException */
        Log.d(TAG, "Hooking AbstractVerifier.verify(String, String[], String[], boolean) for: " + currentPackageName);

        try {
            classLoader.loadClass("ch.boye.httpclientandroidlib.conn.ssl.AbstractVerifier");
            RposedHelpers.findAndHookMethod("ch.boye.httpclientandroidlib.conn.ssl.AbstractVerifier", classLoader, "verify",
                    String.class, String[].class, String[].class, boolean.class,
                    new RC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                            return null;
                        }
                    });
        } catch (ClassNotFoundException e) {
            // pass
            Log.d(TAG, "httpclientandroidlib not found in " + currentPackageName + "-- not hooking");
        }
    }

    private static class ImSureItsLegitTrustManager implements X509TrustManager {
        @SuppressLint("TrustAllX509TrustManager")
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @SuppressLint("TrustAllX509TrustManager")
        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @SuppressLint("TrustAllX509TrustManager")
        public void checkServerTrusted(X509Certificate[] chain, String authType, String str2, String str3) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    private static class ImSureItsLegitHostnameVerifier implements HostnameVerifier {

        @SuppressLint("BadHostnameVerifier")
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    /* This class creates a SSLSocket that trusts everyone. */
    public static class TrustAllSSLSocketFactory extends SSLSocketFactory {

        SSLContext sslContext = SSLContext.getInstance("TLS");

        TrustAllSSLSocketFactory(KeyStore truststore) throws
                NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
            super(truststore);

            TrustManager tm = new X509TrustManager() {

                @SuppressLint("TrustAllX509TrustManager")
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @SuppressLint("TrustAllX509TrustManager")
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            sslContext.init(null, new TrustManager[]{tm}, null);
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
            return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        }

        @Override
        public Socket createSocket() throws IOException {
            return sslContext.getSocketFactory().createSocket();
        }
    }
}
