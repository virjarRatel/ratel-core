package com.virjar.ratel.demoapp;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.webkit.WebView;

public class WebViewTestActivity extends Activity {

    private WebView webView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);

        setContentView(webView);

        webView.loadUrl("https://s3.nikecdn.com/unite/mobile.html?mid=79791317321337700103936922526061554894&androidSDKVersion=2.8.1&uxid=com.nike.commerce.snkrs.droid&locale=zh_CN&backendEnvironment=identity&view=login&clientId=qG9fJbnMcBPAMGibPRGI72Zr89l8CD4R#{%22event%22%20:%20%22loaded%22}");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webView.destroy();
    }
}
