package com.ai.base.webviewCacheInterceptor;

import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

public interface AIInterceptorInterface {
    WebResourceResponse interceptRequest(WebView view,WebResourceRequest request);
    WebResourceResponse interceptRequest(WebView view,String url);
    void clearCache();
}
