package com.ai.testapp.h5plugin;

import android.graphics.Color;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.ai.base.AIBaseActivity;
import com.ai.webplugin.AIWebViewClient;
import com.ai.webplugin.AIWebViewPluginEngine;
import com.ai.webplugin.config.GlobalCfg;

import java.io.InputStream;

public class PortalActivity extends AIBaseActivity {

    private WebView mWebView;
    private LinearLayout mLinearLayout;

    private static String mGlabalCfgFile = "global.properties";
    private static String mPluginCfgFile = "wade-plugin.xml";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mEnbleGesturePwd = false;
        initParam();
        initWebView();
//        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        String date = sDateFormat.format(new java.util.Date());
//
//        int serverCode = Integer.parseInt("-2");
//        Log.d("tag","wuyoujian");
    }

    private void initParam () {
        try {

            // app相关参数的初始
            AppConfig.getInstance().setContext(this);

            // 临时代码,替代接口数据
            AppConfig.getInstance().loadAPKPluginInfo();

            // 解析全局配置
            InputStream is = this.getResources().getAssets().open(mGlabalCfgFile);
            GlobalCfg globalCfg = GlobalCfg.getInstance();
            globalCfg.parseConfig(is);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initWebView() {
        mLinearLayout = new LinearLayout(this);
        mLinearLayout.setBackgroundColor(0xFF4d5b65);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        mLinearLayout.setLayoutParams(params);
        mLinearLayout.setOrientation(LinearLayout.VERTICAL);


        mWebView = new WebView(this);
        mWebView.setBackgroundColor(Color.WHITE);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDefaultTextEncodingName("utf-8");
        mWebView.setWebViewClient(new AIWebViewClient("","") {

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient(){});

        LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        mLinearLayout.addView(mWebView,tvParams);
        setContentView(mLinearLayout);

        // 设置H5插件引擎
        setH5PluginEngine();
        String url = GlobalCfg.getInstance().attr(GlobalCfg.CONFIG_FIELD_ONLINEADDR);
        mWebView.loadUrl(url);
    }

    private void setH5PluginEngine() {
        AIWebViewPluginEngine engine = AIWebViewPluginEngine.getInstance();
        engine.registerPlugins(this, mWebView,mPluginCfgFile);
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
