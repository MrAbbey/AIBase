package com.ai.webplugin;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import com.ai.base.AIBaseActivity;
import com.ai.base.util.Utility;
import com.ai.webplugin.config.WebViewPluginCfg;
import com.ai.base.util.BeanInvoker;
import java.io.InputStream;



/**
 * Created by wuyoujian on 17/3/30.
 */

public class AIWebViewPluginEngine {

    private WebView mWebView;
    private AIBaseActivity mActivity;
    private String  mPluginCfgFile = "h5Plugin.xml";

    private Handler mHandler = new Handler();

    private static AIWebViewPluginEngine instance;
    public static AIWebViewPluginEngine getInstance() {
        if (instance == null) {
            synchronized (AIWebViewPluginEngine.class) {
                instance = new AIWebViewPluginEngine();
            }
        }
        return instance;
    }

    public AIWebViewPluginEngine() {
        //
    }


    @SuppressLint("SetJavaScriptEnabled")
    public void registerPlugins(AIBaseActivity activity, WebView webView,String configFileName) {

        this.mActivity = activity;
        this.mWebView = webView;
        webView.getSettings().setSavePassword(false);
        this.mWebView.getSettings().setSavePassword(false);
        this.mPluginCfgFile = configFileName;

        if (!Utility.isFileExists(mActivity,mPluginCfgFile)) return;
        try {
            InputStream is = mActivity.getResources().getAssets().open(mPluginCfgFile);
            WebViewPluginCfg plugincfg = WebViewPluginCfg.getInstance();
            plugincfg.parseConfig(is);

            String[] names = plugincfg.getNames();
            if (names.length > 0) mWebView.getSettings().setJavaScriptEnabled(true);
            for (String name : names) {
                String className = plugincfg.attr(name, WebViewPluginCfg.CONFIG_ATTR_CLASS);
                AIWebViewBasePlugin plugin = (AIWebViewBasePlugin) BeanInvoker.instance(className,AIBaseActivity.class, mActivity,false);
                mWebView.addJavascriptInterface(plugin, name);
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public void excuteJavascript(String js, final ValueCallback<String> callback) {
        js = encodeForJs(js);
        final String javascript = "javascript:" + js;
        excuteJSInWebView(javascript,callback);
    }

    public void excutePluginCallback(String pluginAPIName, String param, final ValueCallback<String> callback) {
        param = encodeForJs(param);
        final String javascript = "javascript:window.WadeNAObj.callback(\'"+pluginAPIName+"\',\'"+param+"\')";
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

    private String encodeForJs(String data) {
        if(data != null && data.length() > 0) {
            int length = data.length();
            StringBuilder temp = new StringBuilder();

            for(int i = 0; i < length; ++i) {
                char c = data.charAt(i);
                switch(c) {
                    case '\b':
                        temp.append("\\b");
                        break;
                    case '\t':
                        temp.append("\\t");
                        break;
                    case '\n':
                        temp.append("\\n");
                        break;
                    case '\f':
                        temp.append("\\f");
                        break;
                    case '\r':
                        temp.append("\\r");
                        break;
                    case '\'':
                        temp.append("\\\'");
                        break;
                    case '\\':
                        temp.append("\\\\");
                        break;
                    default:
                        temp.append(c);
                }
            }

            return temp.toString();
        } else {
            return "";
        }
    }
}
