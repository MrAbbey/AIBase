package com.ai.webplugin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.Toast;

import com.ai.base.AIBaseActivity;
import com.ai.base.okHttp.OkHttpBaseAPI;
import com.ai.base.util.FileUtilCommon;
import com.ai.base.util.PermissionUitls;
import com.ai.base.util.Utility;
import com.ai.webplugin.config.GlobalCfg;
import com.ai.webplugin.config.WebViewPluginCfg;
import com.ai.base.util.BeanInvoker;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


/**
 * Created by wuyoujian on 17/3/30.
 */

public class AIWebViewPluginEngine {

    private WebView mWebView;
    private AIBaseActivity mActivity;
    private String  mPluginCfgFile = "modular-plugin-adr.xml";
    private Map<String, AIWebViewBasePlugin> mPlugins = new HashMap<>();
    private Map<String, Method> mMethods = new HashMap<>();
    private Handler mHandler = new Handler();
    private static String mHanderName = "modularHander";

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

    @Nullable
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
            final String pluginName = jsonObject.optString("pluginName");
            Object paramObject = jsonObject.opt("params");

            AIWebViewBasePlugin pluginObj = mPlugins.get(pluginName);

            if (pluginObj == null) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mActivity,"插件方法:" + pluginName + "未在modular-plugin-adr.xml中配置",Toast.LENGTH_SHORT).show();
                    }
                });
                return;
            }

            Class<?> clazz = pluginObj.getClass();
            Method method = mMethods.get(pluginName);
            if (method == null) {
                if (paramObject == null) {
                    method = getMethod(clazz,pluginName,null);
                } else {
                    method = getMethod(clazz,pluginName,new Class[]{paramObject.getClass()});
                }

                if(method == null) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mActivity,"插件类未实现方法：" +pluginName,Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                } else {
                    mMethods.put(pluginName,method);
                }
            }

            synchronized(method) {
                if (paramObject == null) {
                    method.invoke(pluginObj);
                } else {
                    method.invoke(pluginObj, new Object[]{paramObject});
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void registerPlugins(AIBaseActivity activity, WebView webView,String configFileName) {

        this.mActivity = activity;
        this.mWebView = webView;
        this.mWebView.getSettings().setSavePassword(false);
        this.mPluginCfgFile = configFileName;

        if (!Utility.isFileExists(mActivity,mPluginCfgFile)) return;
        try {
            // 向webView中注入原生对象
            mWebView.addJavascriptInterface(this, mHanderName);

            // 这种方式可以用来测试陕西版本的插件模式,把测试的接口实现在本类中。
            // 并增加@JavascriptInterface注解
            //mWebView.addJavascriptInterface(this,"ExtendScriptPlugin");


            // 存储系统中插件对象
            InputStream is = mActivity.getResources().getAssets().open(mPluginCfgFile);
            WebViewPluginCfg plugincfg = WebViewPluginCfg.getInstance();
            plugincfg.parseConfig(is);

            String[] names = plugincfg.getNames();
            if (names.length > 0) mWebView.getSettings().setJavaScriptEnabled(true);

            Map<String,String> allClass = new HashMap<>();
            for (String name : names) {
                String className = plugincfg.attr(name, WebViewPluginCfg.CONFIG_ATTR_CLASS);

                if (allClass.containsKey(className)) {
                    String pluginName = allClass.get(className);
                    AIWebViewBasePlugin readyObj = mPlugins.get(pluginName);
                    mPlugins.put(name,readyObj);
                    continue;
                }

                AIWebViewBasePlugin plugin = (AIWebViewBasePlugin) BeanInvoker.instance(className,AIBaseActivity.class, mActivity,WebView.class,mWebView,false);
                mPlugins.put(name,plugin);
                allClass.put(className,name);
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
