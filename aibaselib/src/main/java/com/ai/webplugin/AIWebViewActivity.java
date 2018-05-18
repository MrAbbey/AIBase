package com.ai.webplugin;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.R;
import com.ai.base.AIBaseActivity;
import com.ai.webplugin.config.GlobalCfg;
import java.io.InputStream;


public class AIWebViewActivity extends AIBaseActivity {

    private WebView mWebView;
    private LinearLayout mLinearLayout;
    private String mWebUrl;
    private String mUserAgent;

    private static String mGlabalCfgFile = "global.properties";
    private static String mPluginCfgFile = "wade-plugin.xml";
    private int backgroudColor = 0x000000;

    public static String backgroundColorKey = "backgroundColor";
    public static String webViewURLKey = "webViewURL";
    public static String webViewUserAgentKey = "webViewUserAgent";
    public static String globalConfigKey = "globalConfig";
    public static String pluginConfigKey = "pluginConfig";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initParam();
        initWebView();
    }

    private void initParam () {
        try {
            Intent intent = getIntent();
            backgroudColor = intent.getIntExtra(backgroundColorKey,0x000000);
            mWebUrl = intent.getStringExtra(webViewURLKey);
            mUserAgent = intent.getStringExtra(webViewUserAgentKey);

            mPluginCfgFile = intent.getStringExtra(pluginConfigKey);
            if (mPluginCfgFile == null || mPluginCfgFile.length() == 0) {
                mPluginCfgFile = "wade-plugin.xml";
            }

            if (mWebUrl == null || mWebUrl.length() == 0) {
                // 解析全局配置
                mGlabalCfgFile = intent.getStringExtra(globalConfigKey);
                if (mGlabalCfgFile == null || mGlabalCfgFile.length() == 0 ) {
                    mGlabalCfgFile = "global.properties";
                }
                InputStream is = this.getResources().getAssets().open(mGlabalCfgFile);
                GlobalCfg globalCfg = GlobalCfg.getInstance();
                globalCfg.parseConfig(is);

                mWebUrl = GlobalCfg.getInstance().attr(GlobalCfg.CONFIG_FIELD_ONLINEADDR);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initWebView() {
        mLinearLayout = new LinearLayout(this);
        mLinearLayout.setBackgroundColor(backgroudColor);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        mLinearLayout.setLayoutParams(params);
        mLinearLayout.setOrientation(LinearLayout.VERTICAL);

        mWebView = new WebView(this);
        mWebView.setBackgroundColor(backgroudColor);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setCacheMode(mWebView.getSettings().LOAD_NO_CACHE);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setDefaultTextEncodingName("utf-8");
        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                GlobalCfg globalCfg = GlobalCfg.getInstance();
                String version = globalCfg.attr(GlobalCfg.CONFIG_FIELD_VERSION);
                String js = String.format("setAppVersion('版本：%s');",version);
                view.evaluateJavascript(js, new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {

                    }
                });
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient());

        // 修改ua使得web端正确判断
        if (!mUserAgent.isEmpty()) {
            String ua = mWebView.getSettings().getUserAgentString();
            mWebView.getSettings().setUserAgentString(ua+";" + mUserAgent);
        }

        LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        mLinearLayout.addView(mWebView,tvParams);
        setContentView(mLinearLayout);

        // 设置H5插件引擎
        setH5PluginEngine(mWebView);
        mWebView.loadUrl(mWebUrl);
    }

    private void setH5PluginEngine(WebView webView) {
        AIWebViewPluginEngine engine = AIWebViewPluginEngine.getInstance();
        engine.registerPlugins(this, webView,mPluginCfgFile);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()){
            //http://plan.wadecn.com/#/home
            if (mWebView.getUrl().endsWith("/#/home")){
                moveTaskToBack(true);
                return;
            }
            mWebView.goBack();
        } else {
            moveTaskToBack(true);
        }
    }
}
