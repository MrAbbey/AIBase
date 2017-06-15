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
import com.ai.base.SourceManager.common.GlobalString;
import com.ai.base.SourceManager.manager.MultipleManager;
import com.ai.base.util.FileUtilCommon;
import com.ai.base.util.LogUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by wuyoujian on 17/4/5.
 */

public class AIWebViewClient extends WebViewClient {
    private String hostName;
    private String appId;
    public AIWebViewClient(String hostName,String appId) {
        //http://10.131.67.86:8080/mbosscentre
        this.hostName = hostName;
        this.appId = appId;
    }
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if ( url.contains("http://www.wuyoujian.com")) {
            return true;
        }
        return super.shouldOverrideUrlLoading(view, url);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {

        String url = request.getUrl().toString();
        if ( url.contains("http://www.wuyoujian.com")) {
            return true;
        }
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
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {

        //String jsStr = JsjjJSHelper.getResInputStream(url);
        if (hostName != null && appId != null && url.contains(hostName)) {
            String jsStr = "test";
            WebResourceResponse response = null;
            if (url.endsWith(".png")) {
                response = getWebResourceResponse(url, "image/png", ".png");
            } else if (url.endsWith(".gif")) {
                response = getWebResourceResponse(url, "image/gif", ".gif");
            } else if (url.endsWith(".jpg")) {
                response = getWebResourceResponse(url, "image/jepg", ".jpg");
            } else if (url.endsWith(".jepg")) {
                response = getWebResourceResponse(url, "image/jepg", ".jepg");
            } else if (url.endsWith(".js") && jsStr != null) {
                response = getWebResourceResponse(url, "text/javascript", ".js");
            } else if (url.endsWith(".css") && jsStr != null) {
                response = getWebResourceResponse(url, "text/css", ".css");
            } else if (url.endsWith(".html") && jsStr != null) {
                response = getWebResourceResponse(url, "text/html", ".html");
            }
            if (response != null) {
                return response;
            }
        }
        LogUtil.d("webViewClient url ----", url);
        return super.shouldInterceptRequest(view, url);
    }


    // API21以上用shouldInterceptRequest(WebView view, WebResourceRequest request)
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        return super.shouldInterceptRequest(view, request);
    }

    private WebResourceResponse getWebResourceResponse(String url, String mime, String style) {
        WebResourceResponse response = null;
        String localSourceFileName = getLocalSoruceFileNameByUrl(url);
        if (localSourceFileName == null) {
            return null;
        }
        File file = new File(localSourceFileName);
        if (file.exists()){
            try {
                InputStream is = new FileInputStream(file);
                response = new WebResourceResponse(mime, "UTF-8", is);
                LogUtil.d("LocalWebViewClient url ----", localSourceFileName);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return response;
    }

    private String getLocalSoruceFileNameByUrl(String url) {
        String fileName = null;
        int cutLength = hostName.length();
        int ulrLength = url.length();
        String localPath = url.substring(cutLength, ulrLength);
        fileName =  MobileAppInfo.getSdcardPath()+"/" + appId + localPath;
        return fileName;
    }
}
