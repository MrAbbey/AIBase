package com.ai.webplugin;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * Created by baggio on 2017/6/16.
 */

public class MobileWebView extends WebView {
    public MobileWebView(Context context) {
        super(context);
        initWebview(context);
    }

    public MobileWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initWebview(context);
    }

    public MobileWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initWebview(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MobileWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initWebview(context);
    }

    public MobileWebView(Context context, AttributeSet attrs, int defStyleAttr, boolean privateBrowsing) {
        super(context, attrs, defStyleAttr, privateBrowsing);
        initWebview(context);
    }

    private void initWebview(Context context) {
        setBackgroundColor(Color.WHITE);
        getSettings().setJavaScriptEnabled(true);
        getSettings().setDefaultTextEncodingName("utf-8");
        getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);  //设置 缓存模式
//        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo info = cm.getActiveNetworkInfo();
//        if(info.isAvailable())
//        {
//            getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
//        }else
//        {
//            getSettings().setCacheMode(WebSettings.LOAD_CACHE_ONLY);//不使用网络，只加载缓存
//        }
    }

}
