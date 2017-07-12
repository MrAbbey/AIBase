package com.ai.webplugin;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.view.KeyEvent;
import android.webkit.ClientCertRequest;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ai.base.SourceManager.app.MobileAppInfo;
import com.ai.base.SourceManager.common.LRUCache;
import com.ai.base.util.LogUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by wuyoujian on 17/4/5.
 */

public class AIWebViewClient extends WebViewClient {
    private WebViewManager webViewManager;
    private String filterList[] = {"211.137.132.89","http://www.wuyoujian.com"};

    public AIWebViewClient(String hostName,String appId) {
        //http://10.131.67.86:8080/mbosscentre
        webViewManager = new WebViewManager(hostName,appId);
    }
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (IpAddressfilter(url)) return true;
        return super.shouldOverrideUrlLoading(view, url);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {

        String url = request.getUrl().toString();
        if (IpAddressfilter(url)) return true;
        return super.shouldOverrideUrlLoading(view,request);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        super.onLoadResource(view, url);
    }

    @Override
    public void onPageCommitVisible(WebView view, String url) {
        super.onPageCommitVisible(view, url);
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
    }

    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        super.onReceivedHttpError(view, request, errorResponse);
    }

    @Override
    public void onFormResubmission(WebView view, Message dontResend, Message resend) {
        super.onFormResubmission(view, dontResend, resend);
    }

    @Override
    public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
        super.doUpdateVisitedHistory(view, url, isReload);
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        super.onReceivedSslError(view, handler, error);
    }

    @Override
    public void onReceivedClientCertRequest(WebView view, ClientCertRequest request) {
        super.onReceivedClientCertRequest(view, request);
    }

    @Override
    public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
        super.onReceivedHttpAuthRequest(view, handler, host, realm);
    }

    @Override
    public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
        return super.shouldOverrideKeyEvent(view, event);
    }

    @Override
    public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
        super.onUnhandledKeyEvent(view, event);
    }

    @Override
    public void onScaleChanged(WebView view, float oldScale, float newScale) {
        super.onScaleChanged(view, oldScale, newScale);
    }

    @Override
    public void onReceivedLoginRequest(WebView view, String realm, String account, String args) {
        super.onReceivedLoginRequest(view, realm, account, args);
    }

    // 复写shouldInterceptRequest
    //API21以下用shouldInterceptRequest(WebView view, String url)
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view,String url) {
        final String tempUrl = url;
        final WebResourceResponse responseDefault = super.shouldInterceptRequest(view, url);
        WebResourceResponse responseLocal = webViewManager.getWebLocalResourceResponseByUrl(tempUrl);
        LogUtil.d("webViewClient url ----API21以下---", tempUrl);
        WebResourceResponse response = responseLocal == null ? responseDefault : responseLocal;
        if(response != null){
            webViewManager.saveResoponeByUrl(tempUrl,response);
        }
        return response;
    }


    // API21以上用shouldInterceptRequest(WebView view, WebResourceRequest request)
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        String tempUrl =  request.getUrl().toString();
        WebResourceResponse responseDefault = super.shouldInterceptRequest(view, request);
        String method = request.getMethod();
        if (!method.equals("GET")){
            return responseDefault;
        }
        WebResourceResponse responseLocal = webViewManager.getWebLocalResourceResponseByUrl(tempUrl);
        LogUtil.d("webViewClient url ----API21以上---", tempUrl);
        WebResourceResponse response = responseLocal == null ? responseDefault : responseLocal;
        if(response != null){
            webViewManager.saveResoponeByUrl(tempUrl,response);
        }
        return response;
    }

    //犀利了。被攻击了。
    private boolean IpAddressfilter(String url) {
        boolean needFilter = false;
        for (String filterUrl : filterList){
            if(url.contains(filterUrl)){
                return true;
            }
        }
        return needFilter;
    }

}
