package com.ai.webplugin;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ai.base.AIBaseActivity;
import com.ai.base.util.PermissionUitls;
import com.ai.base.webviewCacheInterceptor.AICacheWebViewClient;
import com.ai.base.webviewCacheInterceptor.AIWebViewResRequestInterceptor;
import com.ai.webplugin.config.GlobalCfg;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class AIWebViewActivity extends AIBaseActivity {

    private WebView mWebView;
    private ImageView mWelcomeImage;
    private LinearLayout mLinearLayout;
    private String mWebUrl;
    private static String mPluginCfgFile = "wade-plugin.xml";
    private int mBackgroudColor = 0x000000;
    private int mBackgroundResId = -1;
    private int mWelcomeImageResId = -1;
    private AIWebViewBasePlugin mPluginObj;

    // intent的参数key
    public static String backgroundColorKey = "backgroundColor";
    public static String backgroundResIdKey = "backgroundResID";
    public static String welcomeImageResId = "welcomeImageResId";
    public static String webViewURLKey = "webViewURL";
    public static String pluginConfigKey = "pluginConfig";


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
                        Toast.makeText(AIWebViewActivity.this,"连接网路超时，请重试",Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (mWelcomeImageResId != -1) {
                        mWelcomeImage.setVisibility(View.GONE);
                    }

                    mWebView.setVisibility(View.VISIBLE);
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setPortraitFullscreen();
        super.onCreate(savedInstanceState);
        initConfigParam();
        initWebView();
    }

    private void setPortraitFullscreen() {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    private void initConfigParam () {
        try {
            Intent intent = getIntent();
            mBackgroudColor = intent.getIntExtra(backgroundColorKey,0x000000);
            mWebUrl = intent.getStringExtra(webViewURLKey);
            mBackgroundResId = intent.getIntExtra(backgroundResIdKey,-1);
            mWelcomeImageResId = intent.getIntExtra(welcomeImageResId,-1);

            mPluginCfgFile = intent.getStringExtra(pluginConfigKey);
            if (mPluginCfgFile == null || mPluginCfgFile.length() == 0) {
                mPluginCfgFile = "wade-plugin.xml";
            }

            if (mWebUrl == null || mWebUrl.length() == 0) {
                mWebUrl = (String) GlobalCfg.getInstance().attr(GlobalCfg.CONFIG_FIELD_ONLINEADDR);
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mWebView.setWebContentsDebuggingEnabled(true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
            mWebView.getSettings().setAllowUniversalAccessFromFileURLs(true);
            try {
                Class<?> clazz = mWebView.getSettings().getClass();
                Method method = clazz.getMethod(
                        "setAcceptThirdPartyCookies", boolean.class);
                if (method != null) {
                    method.invoke(mWebView.getSettings(), true);
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        } else {
            try {
                Class<?> clazz = mWebView.getSettings().getClass();
                Method method = clazz.getMethod("setAllowUniversalAccessFromFileURLs", boolean.class);
                if (method != null) {
                    method.invoke(mWebView.getSettings(), true);
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setCacheMode(mWebView.getSettings().LOAD_NO_CACHE);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setDefaultTextEncodingName("utf-8");

        GlobalCfg globalCfg = GlobalCfg.getInstance();
        boolean isCache = globalCfg.attr(GlobalCfg.CONFIG_FIELD_CACHE).equalsIgnoreCase("true");
        boolean isDebug = globalCfg.attr(GlobalCfg.CONFIG_FIELD_DEBUG).equalsIgnoreCase("true");
        String encryptKey = globalCfg.attr(GlobalCfg.CONFIG_FIELD_ENCRYPTKEY);
        mWebView.setWebViewClient(new AICacheWebViewClient(new AIWebViewResRequestInterceptor.Builder(this).setConnectTimeoutSecond(30000)
                .setForceCache(isCache)
                .setReadTimeoutSecond(30000)
                .setEncryptKey(encryptKey)
                .setDebug(isDebug)){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                GlobalCfg globalCfg = GlobalCfg.getInstance();
                String version = (String)globalCfg.attr(GlobalCfg.CONFIG_FIELD_VERSION);
                String js = String.format("setAppVersion('版本：%s');",version);
                view.evaluateJavascript(js, new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {}
                });

                mTimer.onFinish();
            }
        });

        //可采用默认的方式
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
        String userAgent = (String)globalCfg.attr(GlobalCfg.CONFIG_FIELD_USERAGENT);
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

    public void startActivityForResult(AIWebViewBasePlugin plugin, Intent intent, int requestCode) {
        mPluginObj = plugin;
        startActivityForResult(intent,requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mPluginObj.onActivityResult(requestCode,resultCode,data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        PermissionUitls.getInstance();
        PermissionUitls.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
