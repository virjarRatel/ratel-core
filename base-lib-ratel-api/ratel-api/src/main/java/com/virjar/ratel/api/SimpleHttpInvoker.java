package com.virjar.ratel.api;

import android.util.Log;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import external.org.apache.commons.io.IOUtils;

public class SimpleHttpInvoker {
    public static String get(String url) {
        try {
            // 不能走代理，否则可能导致热发不生效，1.5.0生效
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection(Proxy.NO_PROXY);
            connection.setRequestMethod("GET");
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                connection.disconnect();
                return null;
            }
            try (InputStream inputStream = connection.getInputStream()) {
                return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            } finally {
                connection.disconnect();
            }
        } catch (Exception e) {
            Log.e(RatelToolKit.TAG, "error for url:" + url, e);
            return null;
        }
    }


}
