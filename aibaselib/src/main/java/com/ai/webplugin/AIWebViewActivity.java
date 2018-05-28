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

    private void startAlphaAnimationJavaCode() {
//        //渐变动画    从显示（1.0）到隐藏（0.0）
//        AlphaAnimation alphaAnim = new AlphaAnimation(1.0f, 0.0f);
//        //执行三秒
//        alphaAnim.setDuration(1000);
//        alphaAnim.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//
//            }
//        });
//        mWelcomeImage.startAnimation(alphaAnim);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initParam();
        initWebView();
        checkVersion();
    }

    private void initParam () {
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

    // 权限控制
    private void checkPermission() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final int permissionCode = PermissionUitls.PERMISSION_STORAGE_CODE;
                PermissionUitls.mContext = AIWebViewActivity.this;
                final String checkPermissinos [] = {Manifest.permission.INTERNET,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE};

                PermissionUitls.PermissionListener permissionListener = new PermissionUitls.PermissionListener() {
                    @Override
                    public void permissionAgree() {
                        switch (permissionCode) {
                            case PermissionUitls.PERMISSION_STORAGE_CODE :
                                break;
                        }
                    }

                    @Override
                    public void permissionReject() {

                    }
                };
                PermissionUitls permissionUitls = PermissionUitls.getInstance(null, permissionListener);
                permissionUitls.permssionCheck(permissionCode,checkPermissinos);
            }
        });
    }


    // 自动更新
    private void checkVersion() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                GlobalCfg globalCfg = GlobalCfg.getInstance();
                String url = globalCfg.attr(GlobalCfg.CONFIG_FIELD_VESSIONURL);
                if (url == null || url.length() == 0) {
                    return;
                }
                String locationVersion = globalCfg.attr(GlobalCfg.CONFIG_FIELD_VERSION);

                OkHttpBaseAPI okHttpBaseAPI = new OkHttpBaseAPI();
                String data = okHttpBaseAPI.httpGetTask(url, "getVersion");
                try{
                    if (data == null || data.length() <=0) {
                        checkPermission();
                    }

                    Properties versionInfo = new Properties();
                    InputStream inputStream = new ByteArrayInputStream(data.getBytes("UTF-8"));
                    versionInfo.load(inputStream);
                    final String versionURL = versionInfo.getProperty("android.versionURL");
                    String versionNumber = versionInfo.getProperty("android.version");
                    if (versionNumber.compareToIgnoreCase(locationVersion)>0 ) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                checkUpdate(versionURL);
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                checkPermission();
                            }
                        });
                    }
                } catch (Exception e){
                    checkPermission();
                }
            }
        }).start();
    }

    private void checkUpdate(final String apkURL) {
        // 创建构建器
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // 设置参数
        builder.setTitle("提示")
                .setMessage("远端发现新版本请更新后重新启动应用")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        checkPermission();
                        updateApk(apkURL);
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }

    private ProgressDialog dialog;
    private void updateApk(final String apkURL) {
        dialog = ProgressDialog.show(this, "", "新版本下载中……", true, false, null);
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpBaseAPI okHttpBaseAPI = new OkHttpBaseAPI();
                byte[] data = okHttpBaseAPI.httpGetFileDataTask(apkURL, "apkDonwload");

                String filePath = MobileAppInfo.getSdcardPath() + "/" + "apk";
                final String apkPath = filePath + "/temp.apk";
                FileUtilCommon.writeByte2File(filePath, "temp.apk", data, "");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        if (isExist(apkPath)){
                            installApkarchive(apkPath);
                        }
                    }
                });
            }
        }).start();
    }

    public void installApkarchive(String apkFilePath) {

        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uriPath = Uri.fromFile(new File(apkFilePath));
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            //7.0+版本手机
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            GlobalCfg globalCfg = GlobalCfg.getInstance();
            String fileprovider = globalCfg.attr(GlobalCfg.CONFIG_FIELD_FILEPROVIDER);
            if (fileprovider == null) {
                Toast.makeText(AIWebViewActivity.this,"请在manifest中配置FileProvider",Toast.LENGTH_LONG).show();
                return;
            }
            uriPath = FileProvider.getUriForFile(this,fileprovider,new File(apkFilePath));
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(uriPath, "application/vnd.android.package-archive");
        startActivity(intent);
        finish();
        Runtime.getRuntime().exit(0);
    }

    private boolean isExist(String apkFilePath) {
        File file = new File(apkFilePath);
        if (file.exists()) {
            return true;
        } else {
            return false;
        }
    }

}
