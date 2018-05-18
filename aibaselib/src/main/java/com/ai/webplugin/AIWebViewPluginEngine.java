package com.ai.webplugin;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import com.ai.base.AIBaseActivity;
import com.ai.base.util.Utility;
import com.ai.webplugin.config.WebViewPluginCfg;
import com.ai.base.util.BeanInvoker;
import org.json.JSONObject;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by wuyoujian on 17/3/30.
 */

public class AIWebViewPluginEngine {

    private WebView mWebView;
    private AIBaseActivity mActivity;
    private String  mPluginCfgFile = "wade-plugin.xml";
    private Map<String, AIWebViewBasePlugin> mPlugins = new HashMap<>();
    private Map<String, Method> mMethods = new HashMap<>();
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

    public AIWebViewPluginEngine() {}

    public static Method getMethod(Class<?> clazz, String methodName, Class<?>[] classes) {
        Method method = null;

        try {
            method = clazz.getDeclaredMethod(methodName, classes);
        } catch (NoSuchMethodException var5) {
            if(clazz.getSuperclass() == null) {
                return null;
            }
            method = getMethod(clazz.getSuperclass(), methodName, classes);
        }

        if(method != null) {
            method.setAccessible(true);
        }

        return method;
    }


    @JavascriptInterface
    public void JN_EXECUTE(String paramJSON) {

        try {
            JSONObject jsonObject = new JSONObject(paramJSON);
            String methodName = jsonObject.optString("methodName");
            JSONObject paramObject = jsonObject.optJSONObject("params");
            AIWebViewBasePlugin pluginObj = mPlugins.get(methodName);

            Class<?> clazz = pluginObj.getClass();
            Method method = mMethods.get(methodName);
            if (method == null) {
                method = getMethod(clazz,methodName,new Class[]{JSONObject.class});
                if(method == null) {
                    return;
                } else {
                    mMethods.put(methodName,method);
                }
            }

            synchronized(method) {
                method.invoke(pluginObj, new Object[]{paramObject});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            // 向webView中注入原生对象
            String handerName = "WadeNAObjHander";
            mWebView.addJavascriptInterface(this, handerName);

            // 存储系统中插件对象
            InputStream is = mActivity.getResources().getAssets().open(mPluginCfgFile);
            WebViewPluginCfg plugincfg = WebViewPluginCfg.getInstance();
            plugincfg.parseConfig(is);

            String[] names = plugincfg.getNames();
            if (names.length > 0) mWebView.getSettings().setJavaScriptEnabled(true);
            for (String name : names) {
                String className = plugincfg.attr(name, WebViewPluginCfg.CONFIG_ATTR_CLASS);
                String methodName = plugincfg.attr(name, WebViewPluginCfg.CONFIG_ATTR_CLASS);
                AIWebViewBasePlugin plugin = (AIWebViewBasePlugin) BeanInvoker.instance(className,AIBaseActivity.class, mActivity,mWebView,false);
                mPlugins.put(className,plugin);
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public void excuteJavascript(String js, final ValueCallback<String> callback) {
        final String javascript = "javascript:" + js;
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
}
