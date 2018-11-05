package com.ai.testapp;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;

import com.ai.base.AIBaseActivity;
import com.ai.base.util.Utility;
import com.ai.base.webviewCacheInterceptor.AIResURLUtils;
import com.ai.testapp.h5plugin.PortalActivity;
import com.ai.webplugin.AIWebViewActivity;

import java.io.File;
import java.util.Map;

public class SplashActivity extends AIBaseActivity {

    private WebView mWebView;
    private ImageView imageView;
    private static String kSplashHtml = "file:///android_asset/welcome.html";
    private static String mGlabalCfgFile = "global.properties";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //
        setContentView(R.layout.splash_layout);

        String url = "https://plan.wadecn.com";
        Map<String,String> fields = AIResURLUtils.getResURLFieldsFromUrl(url);
        String resUrl = fields.get("url");
        String extension = fields.get("extension");
        String saveFileName = Utility.md5(resUrl)+'.' + extension;

//        mWebView = (WebView) findViewById(R.id.splash_webView);
//        mWebView.loadUrl(kSplashHtml);

        imageView = (ImageView)findViewById(R.id.iv_splash);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterHomeActivity();
            }
        });

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
        Intent intent = new Intent(this, PortalActivity.class);
        //Intent intent = new Intent(this, AICertificateCameraActivity.class);
        startActivityForResult(intent,1);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            String path = data.getStringExtra("photoPath");
            File file = new File(path);
            if (file.exists()){
                Bitmap bitmap = BitmapFactory.decodeFile(path);

                imageView.setImageBitmap(bitmap);

            }
        }

    }
}
