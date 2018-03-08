package com.ai.webplugin;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.R;
import com.ai.base.AIBaseActivity;
import com.ai.webplugin.config.GlobalCfg;
import java.io.InputStream;


public class WebViewKitActivity extends AIBaseActivity {

    private WebView mWebView;
    private LinearLayout mLinearLayout;
    private String mWebUrl;

    private static String mGlabalCfgFile = "global.properties";
    private static String mPluginCfgFile = "h5Plugin.xml";
    private int backgroudColor = 0x000000;

    public static String backgroundColorKey = "backgroundColor";
    public static String webViewURLKey = "webViewURL";
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

            mPluginCfgFile = intent.getStringExtra(pluginConfigKey);
            if (mPluginCfgFile == null || mPluginCfgFile.length() == 0) {
                mPluginCfgFile = "h5Plugin.xml";
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
        mWebView.getSettings().setCacheMode(mWebView.getSettings().LOAD_CACHE_ELSE_NETWORK);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setDefaultTextEncodingName("utf-8");
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.setWebChromeClient(new WebChromeClient());

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
        mEnbleGesturePwd = true;
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
