package com.ai.testapp;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.webkit.WebView;

import com.ai.testapp.h5plugin.PortalActivity;
import com.ai.base.AIBaseActivity;

public class SplashActivity extends AIBaseActivity {

    private WebView mWebView;
    private static String kSplashHtml = "file:///android_asset/welcome.html";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //
        setContentView(R.layout.splash_layout);

        mWebView = (WebView) findViewById(R.id.splash_webView);
        mWebView.loadUrl(kSplashHtml);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                enterHomeActivity();
            }
        }, 4000);
    }

    private void enterHomeActivity() {
        // 简单模式
        //Intent intent = new Intent(this, MainActivity.class);
        // 结合我们H5框架的模式
        Intent intent = new Intent(this, PortalActivity.class);
        startActivity(intent);
        finish();
    }
}
