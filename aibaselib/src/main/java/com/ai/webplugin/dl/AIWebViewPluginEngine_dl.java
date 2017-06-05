package com.ai.webplugin.dl;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import com.ai.base.util.BeanInvoker;
import com.ai.webplugin.config.WebViewPluginCfg;
import com.ryg.dynamicload.DLBasePluginActivity;
import com.ryg.dynamicload.internal.DLPluginManager;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;

import dalvik.system.DexClassLoader;

/**
 * Created by wuyoujian on 17/3/30.
 */

public class AIWebViewPluginEngine_dl {

    private WebView mWebView;
    private String  mPluginCfgFile = "h5Plugin.xml";

    private DLBasePluginActivity mDLActivity;
    protected DexClassLoader classLoader = null;
    private boolean isFromAPP = false;

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


    public boolean isFromAPP() {
        return isFromAPP;
    }

    public void setFromAPP(boolean fromAPP) {
        isFromAPP = fromAPP;
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void registerPlugins(DLBasePluginActivity activity, WebView webView,String configFileName) {

        this.mDLActivity = activity;
        this.mWebView = webView;
        this.mPluginCfgFile = configFileName;
        try {
            InputStream is = mDLActivity.that.getResources().getAssets().open(mPluginCfgFile);
            WebViewPluginCfg_dl plugincfg = WebViewPluginCfg_dl.getInstance();
            plugincfg.parseConfig(is);

            String[] names = plugincfg.getNames();
            if (names.length > 0) mWebView.getSettings().setJavaScriptEnabled(true);

            File dexPath = mDLActivity.that.getDir("dex", 0);

            for (String name : names) {
                String className = plugincfg.attr(name, WebViewPluginCfg_dl.CONFIG_ATTR_CLASS);
                String packageName = plugincfg.attr(name,WebViewPluginCfg_dl.CONFIG_ATTR_PACKAGENAME);
                if (isFromAPP) {
                    // 从插件里加载
                    classLoader = DLPluginManager.getInstance(mDLActivity.that).getPackage(packageName).classLoader;
                    //new DexClassLoader(mApkPath, dexPath.getAbsolutePath(), null, mDLActivity.that.getClassLoader());
                    Class  mLoadClassDynamic = classLoader.loadClass(className);

                    Constructor<?> constructor = mLoadClassDynamic.getConstructor(DLBasePluginActivity.class);
                    AIWebViewBasePlugin_dl plugin = (AIWebViewBasePlugin_dl)constructor.newInstance(mDLActivity);
                    mWebView.addJavascriptInterface(plugin, name);
                } else {
                    // 插件app作为独立的app发布
                    AIWebViewBasePlugin_dl plugin = (AIWebViewBasePlugin_dl) BeanInvoker.instance(className,DLBasePluginActivity.class, mDLActivity,false);
                    mWebView.addJavascriptInterface(plugin, name);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
}
