package com.ai.base.webviewCacheInterceptor;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class AICacheWebViewClient extends WebViewClient {

    public AICacheWebViewClient(AIWebViewResRequestInterceptor.Builder builder) {
        AIWebViewResRequestInterceptor.getInstance().init(builder);
    }

    // 配合测试
    private String filterList[] = {"http://www.wuyoujian.com"};

    private boolean isFilter(String url) {
        boolean needFilter = false;
        for (String filterUrl : filterList){
            if(url.contains(filterUrl)){
                return true;
            }
        }
        return needFilter;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (isFilter(url)) return true;
        return super.shouldOverrideUrlLoading(view, url);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        String url = request.getUrl().toString();
        if (isFilter(url)) return true;
        return super.shouldOverrideUrlLoading(view,request);
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view,String url) {
        return AIWebViewResRequestInterceptor.getInstance().interceptRequest(view,url);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        return AIWebViewResRequestInterceptor.getInstance().interceptRequest(view,request);
    }
}
