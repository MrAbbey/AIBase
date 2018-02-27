package com.ai.webplugin.dl;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import com.ai.base.AIBaseActivity;
import com.ai.base.util.BeanInvoker;
import java.io.InputStream;


/**
 * Created by wuyoujian on 17/3/30.
 */

public class AIWebViewPluginEngine_dl {

    private WebView mWebView;
    private AIBaseActivity mActivity;
    private String  mPluginCfgFile = "h5Plugin.xml";

    private Handler mHandler = new Handler();

    private static AIWebViewPluginEngine_dl instance;
    public static AIWebViewPluginEngine_dl getInstance() {
        if (instance == null) {
            synchronized (AIWebViewPluginEngine_dl.class) {
                instance = new AIWebViewPluginEngine_dl();
            }
        }
        return instance;
    }

    public AIWebViewPluginEngine_dl() {
        //
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void registerPlugins(AIBaseActivity activity, WebView webView,String configFileName) {

        this.mActivity = activity;
        this.mWebView = webView;
        webView.getSettings().setSavePassword(false);
        this.mWebView.getSettings().setSavePassword(false);
        this.mPluginCfgFile = configFileName;
        try {
            InputStream is = mActivity.getResources().getAssets().open(mPluginCfgFile);
            WebViewPluginCfg_dl plugincfg = WebViewPluginCfg_dl.getInstance();
            plugincfg.parseConfig(is);

            String[] names = plugincfg.getNames();
            if (names.length > 0) mWebView.getSettings().setJavaScriptEnabled(true);
            for (String name : names) {
                String className = plugincfg.attr(name, WebViewPluginCfg_dl.CONFIG_ATTR_CLASS);
                AIWebViewBasePlugin_dl plugin = (AIWebViewBasePlugin_dl) BeanInvoker.instance(className,AIBaseActivity.class, mActivity,false);
                mWebView.addJavascriptInterface(plugin, name);
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public void excuteJavascript(String js, final ValueCallback<String> callback) {
        if (mWebView != null) {

            int currentapiVersion = android.os.Build.VERSION.SDK_INT;
            if (currentapiVersion < android.os.Build.VERSION_CODES.LOLLIPOP) {
                final String javascript = js;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mWebView.loadUrl("javascript:" + javascript);
                        callback.onReceiveValue("success");
                    }
                });
            } else {
                mWebView.evaluateJavascript(js,callback);
            }
        }
    }

    public void excutePluginCallback(String pluginAPIName, String param, final ValueCallback<String> callback) {
        if (mWebView != null) {
            int currentapiVersion = android.os.Build.VERSION.SDK_INT;
            final String javascript = String.format("%s(%s)",pluginAPIName,param);
            if (currentapiVersion < android.os.Build.VERSION_CODES.LOLLIPOP) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mWebView.loadUrl("javascript:" + javascript);
                        callback.onReceiveValue("success");
                    }
                });
            } else {
                mWebView.evaluateJavascript(javascript,callback);
            }
        }
    }
}
