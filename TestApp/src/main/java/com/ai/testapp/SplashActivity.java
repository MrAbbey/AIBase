package com.ai.testapp;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.webkit.WebView;

import com.ai.testapp.common.CameraActivity;
import com.ai.testapp.common.MainActivity;
import com.ai.testapp.h5plugin.PortalActivity;
import com.ai.base.AIBaseActivity;
import com.ai.webplugin.AIWebViewActivity;
import com.ai.webplugin.config.GlobalCfg;

import java.io.InputStream;

public class SplashActivity extends AIBaseActivity {

    private WebView mWebView;
    private static String kSplashHtml = "file:///android_asset/welcome.html";
    private static String mGlabalCfgFile = "global.properties";

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
        }, 3000);
    }

    private void enterHomeActivity() {
        // 简单模式
        //Intent intent = new Intent(this, MainActivity.class);
        // 结合我们H5框架的模式
        //Intent intent = new Intent(this, PortalActivity.class);
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
        finish();
    }
}
