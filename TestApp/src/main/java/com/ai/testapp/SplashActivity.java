package com.ai.testapp;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;

import com.ai.base.CertificateCamera.CertificateCameraActivity;
import com.ai.base.AIBaseActivity;

import java.io.File;

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
        //Intent intent = new Intent(this, PortalActivity.class);
        Intent intent = new Intent(this, CertificateCameraActivity.class);
        startActivityForResult(intent,1);
       // finish();
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
