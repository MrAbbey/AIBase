package com.ai.testapp.h5plugin;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.ai.base.AIBaseActivity;
import com.ai.base.util.AESEncrypt;
import com.ai.base.util.Utility;
import com.ai.base.webviewCacheInterceptor.AICacheWebViewClient;
import com.ai.base.webviewCacheInterceptor.AIWebViewResRequestInterceptor;
import com.ai.webplugin.AIWebViewPluginEngine;
import com.ai.webplugin.config.GlobalCfg;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;

public class PortalActivity extends AIBaseActivity {

    private WebView mWebView;
    private LinearLayout mLinearLayout;

    private static String mGlabalCfgFile = "global.properties";
    private static String mPluginCfgFile = "modular-plugin-adr.xml";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initParam();
        initWebView();

        try {
            String key = "_weimeitiancheng";
            String content1 = "你好，我是伍友健！！！！***#";
            String encryptString1 = AESEncrypt.encrypt(content1,key,"aiaiaiaiaiaiaiai");

            String content2 = "你好，我是伍友健！13223232**@@@";
            String encryptString2 = AESEncrypt.encrypt(content2,key,"aiaiaiaiaiaiaiai");

            String en = "n3sz56iSPxrQ8Ql4u/dagc09MLg1fBIa44yrtY3TRalEwvKW2bLs0JsXnn9BBRbW";
            String des = AESEncrypt.decrypt(en,key,"aiaiaiaiaiaiaiai");

            Log.d("888",encryptString2);
        } catch (Exception e) {
            e.printStackTrace();

        }


//        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        String date = sDateFormat.format(new java.util.Date());
//
//        int serverCode = Integer.parseInt("-2");
//        Log.d("tag","wuyoujian");
    }

    /**
     * 将文件从assets目录，考贝到 /data/data/包名/files/ 目录中。assets 目录中的文件，会不经压缩打包至APK包中，使用时还应从apk包中导出来
     * @param fileName 文件名,如aaa.txt
     */
    public static void copyAssetsFile2Phone(Activity activity, String fileName){
        try {
            InputStream inputStream = activity.getAssets().open(fileName);
            //getFilesDir() 获得当前APP的安装路径 /data/data/包名/files 目录
            File file = new File(activity.getFilesDir().getAbsolutePath() + File.separator + fileName);
            if(!file.exists() || file.length()==0) {
                FileOutputStream fos =new FileOutputStream(file);//如果文件不存在，FileOutputStream会自动创建文件
                int len=-1;
                byte[] buffer = new byte[1024];
                while ((len=inputStream.read(buffer))!=-1){
                    fos.write(buffer,0,len);
                }
                fos.flush();//刷新缓存区
                inputStream.close();
                fos.close();
            } else {

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initParam () {
        try {

            copyAssetsFile2Phone(this,"18.pdf");
            copyAssetsFile2Phone(this,"电子协议.xlsx");
            copyAssetsFile2Phone(this,"记录.doc");
            copyAssetsFile2Phone(this,"test.csv");

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
        mWebView.getSettings().setDomStorageEnabled(true);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setDisplayZoomControls(false);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        GlobalCfg globalCfg = GlobalCfg.getInstance();
        boolean isCache = globalCfg.attr(GlobalCfg.CONFIG_FIELD_CACHE).equalsIgnoreCase("true");
        boolean isDebug = globalCfg.attr(GlobalCfg.CONFIG_FIELD_DEBUG).equalsIgnoreCase("true");
        String encryptKey = globalCfg.attr(GlobalCfg.CONFIG_FIELD_ENCRYPTKEY);
        mWebView.getSettings().setDefaultTextEncodingName("utf-8");
        mWebView.setWebViewClient(new AICacheWebViewClient(new AIWebViewResRequestInterceptor.Builder(this).setConnectTimeoutSecond(30000)
                .setForceCache(isCache)
                .setReadTimeoutSecond(30000)
                .setEncryptKey(encryptKey)
                .setDebug(isDebug)));

        mWebView.setWebChromeClient(new WebChromeClient());

        LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        mLinearLayout.addView(mWebView,tvParams);
        setContentView(mLinearLayout);

        try {
            // 设置H5插件引擎
            setH5PluginEngine();
            String url = (String) GlobalCfg.getInstance().attr(GlobalCfg.CONFIG_FIELD_ONLINEADDR);
            mWebView.loadUrl(url);

            //url = "http://10.131.68.158:8080/order/newmbosslogin";

            SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String timestamp = sDateFormat.format(new java.util.Date());

            // 解密中文词
            String key = "mboss陕西移动订单中心";
            // 解密加密密钥
            String aesKey = "www.asiainfo.com";

            String staffId = "TESTSX37";

            String signContent = key +"|" + timestamp + "|" + staffId;
            String sign = AESEncrypt.encrypt(signContent,aesKey,"aiaiaiaiaiaiaiai");
            String tokenContent = timestamp + key + staffId;
            String token = Utility.md5(tokenContent);

            String address = url+"?sign=" + URLEncoder.encode(sign,"utf-8") + "&token=" + URLEncoder.encode(token,"utf-8");
            //mWebView.loadUrl(address);
        } catch (Exception e) {

        }

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
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //moveTaskToBack(false);
    }
}
