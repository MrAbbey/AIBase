package com.ai.webplugin;

import android.os.Handler;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import com.ai.base.AIBaseActivity;
import com.ai.base.util.Utility;

import org.json.JSONObject;

/**
 * Created by wuyoujian on 17/3/30.
 */

public class AIWebViewBasePlugin {

    private AIBaseActivity mActivity;
    private WebView mWebView;
    private Handler mHandler = new Handler();

    public AIWebViewBasePlugin(AIBaseActivity activity,WebView webView) {
        this.mActivity = activity;
        this.mWebView = webView;
    }
    public AIBaseActivity getActivity() {
        return mActivity;
    }
    public void setActivity(AIBaseActivity activity) {
        this.mActivity = activity;
    }

    public WebView getWebView() {
        return mWebView;
    }
    public void setWebView(WebView webView) {
        mWebView = webView;
    }


    public void excuteJavascript(String js, final ValueCallback<String> callback) {
        final String javascript = "javascript:" + js;
        excuteJSInWebView(javascript,callback);
    }

    public void callback(String actionName, String param, final ValueCallback<String> callback) {
        param = Utility.encodeForJs(param);
        final String javascript = "javascript:window.WadeNAObj.callback(\'"+actionName+"\',\'"+param+"\')";
        excuteJSInWebView(javascript,callback);
    }

    private void excuteJSInWebView(final String javascript, final ValueCallback<String> callback){
        if (mWebView != null) {
            int currentapiVersion = android.os.Build.VERSION.SDK_INT;
            if (currentapiVersion < android.os.Build.VERSION_CODES.LOLLIPOP) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mWebView.loadUrl(javascript);
                        callback.onReceiveValue("success");
                    }
                });
            } else {
                mWebView.evaluateJavascript(javascript,callback);
            }
        }
    }

    // 扩展原生能力接口
    public void JN_Test(final JSONObject obj){
        Log.d("JSONObject",obj.toString());
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                callback("JN_Test",obj.toString(),null);
            }
        });

    }
}

