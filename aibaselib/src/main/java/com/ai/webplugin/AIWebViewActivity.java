package com.ai.webplugin;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ai.base.AIBaseActivity;
import com.ai.base.SourceManager.app.MobileAppInfo;
import com.ai.base.okHttp.OkHttpBaseAPI;
import com.ai.base.util.FileUtilCommon;
import com.ai.base.util.PermissionUitls;
import com.ai.webplugin.config.GlobalCfg;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Properties;


public class AIWebViewActivity extends AIBaseActivity {

    private WebView mWebView;
    private ImageView mWelcomeImage;
    private LinearLayout mLinearLayout;
    private String mWebUrl;
    private static String mGlabalCfgFile = "global.properties";
    private static String mPluginCfgFile = "wade-plugin.xml";
    private int mBackgroudColor = 0x000000;
    private int mBackgroundResId = -1;
    private int mWelcomeImageResId = -1;

    // intent的参数key
    public static String backgroundColorKey = "backgroundColor";
    public static String backgroundResIdKey = "backgroundResID";
    public static String welcomeImageResId = "welcomeImageResId";
    public static String webViewURLKey = "webViewURL";
    public static String globalConfigKey = "globalConfig";
    public static String pluginConfigKey = "pluginConfig";


    public interface  ConnectTimeoutListener {
        public void connectTimeout();
    }

    ConnectTimeoutListener timeoutListener;

    private long myCountDownTime = 0;
    private static final long LONGMAX = 300000L;
    private static final long INTERVAL = 1000L;
    private CountDownTimer mTimer = new CountDownTimer(LONGMAX, INTERVAL) {
        @Override
        public void onTick(long millisUntilFinished) {
            myCountDownTime = millisUntilFinished;
        }

        @Override
        public void onFinish() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (myCountDownTime <= 2*INTERVAL) {
                        if (timeoutListener != null) {
                            timeoutListener.connectTimeout();
                        }
                        return;
                    }

                    mWelcomeImage.setVisibility(View.GONE);
                    mWebView.setVisibility(View.VISIBLE);
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initConfigParam();
        initWebView();
        AIWebViewPluginEngine.getInstance().checkUpdate(null);
    }

    private void initConfigParam () {
        try {
            Intent intent = getIntent();
            mBackgroudColor = intent.getIntExtra(backgroundColorKey,0x000000);
            mWebUrl = intent.getStringExtra(webViewURLKey);
            mBackgroundResId = intent.getIntExtra(backgroundResIdKey,0);
            mWelcomeImageResId = intent.getIntExtra(welcomeImageResId,0);

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
        mLinearLayout.setBackgroundColor(mBackgroudColor);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        mLinearLayout.setLayoutParams(params);
        mLinearLayout.setOrientation(LinearLayout.VERTICAL);

        initWelcomeView();
        initContentWebView();

        setContentView(mLinearLayout);
    }

    // 初始化主webview
    private void initWelcomeView () {
        if (mWelcomeImageResId != -1) {
            mWelcomeImage = new ImageView(this);
            mWelcomeImage.setImageResource(mWelcomeImageResId);
            mWelcomeImage.setAdjustViewBounds(true);
            mWelcomeImage.setScaleType(ImageView.ScaleType.CENTER_CROP);

            LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            mLinearLayout.addView(mWelcomeImage,tvParams);
        }
    }

    // 初始化主webview
    private void initContentWebView () {

        mWebView = new WebView(this);
        mWebView.setBackgroundColor(mBackgroudColor);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            if (mBackgroundResId != - 1) {
                mWebView.setBackground(getDrawable(mBackgroundResId));
                mWebView.getBackground().setAlpha(0);
            }
        }

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
                    public void onReceiveValue(String value) {}
                });

                mTimer.onFinish();
            }
        });

        //可以采用默认的方式
        mWebView.setWebChromeClient(new WebChromeClient());

        //设置响应js 的Alert(); Confirm()函数
        /*
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                AlertDialog.Builder b = new AlertDialog.Builder(AIWebViewActivity.this);
                b.setTitle("提示");
                b.setMessage(message);
                b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }
                });
                b.setCancelable(false);
                b.create().show();
                return true;
            }
            //设置响应js 的Confirm()函数
            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                AlertDialog.Builder b = new AlertDialog.Builder(AIWebViewActivity.this);
                b.setTitle("提示");
                b.setMessage(message);
                b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }
                });
                b.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.cancel();
                    }
                });
                b.create().show();
                return true;
            }
        });
        */

        // 修改ua使得web端正确判断
        GlobalCfg globalCfg = GlobalCfg.getInstance();
        String userAgent = globalCfg.attr(GlobalCfg.CONFIG_FIELD_USERAGENT);
        if (!userAgent.isEmpty()) {
            String ua = mWebView.getSettings().getUserAgentString();
            mWebView.getSettings().setUserAgentString(ua+";" + userAgent);
        }

        LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        mLinearLayout.addView(mWebView,tvParams);

        // 设置H5插件引擎
        setH5PluginEngine(mWebView);
        mWebView.loadUrl(mWebUrl);
        mTimer.start();
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
