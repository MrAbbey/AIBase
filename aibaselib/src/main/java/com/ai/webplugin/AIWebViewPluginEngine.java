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
import com.ai.base.SourceManager.app.MobileAppInfo;
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

//    @JavascriptInterface
//    public void JN_PageBack() {
//        mActivity.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                mActivity.finish();
//            }
//        });
//    }


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
                        Toast.makeText(mActivity,"插件方法:" + pluginName + "未在wade-plugin.xml中配置",Toast.LENGTH_SHORT).show();
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
            String handerName = "WadeNAObjHander";
            mWebView.addJavascriptInterface(this, handerName);

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

    /*
     check 版本 helper API begin
     */
    public void checkUpdate(final String versionConfigUrl) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                GlobalCfg globalCfg = GlobalCfg.getInstance();
                String url = globalCfg.attr(GlobalCfg.CONFIG_FIELD_VESSIONURL);

                // 如果接口传入了URL就这个URL生效
                if (versionConfigUrl != null && versionConfigUrl.length() > 0) {
                    url = versionConfigUrl;
                }

                if (url == null || url.length() == 0) {
                    return;
                }

                String locationVersion = globalCfg.attr(GlobalCfg.CONFIG_FIELD_VERSION);

                OkHttpBaseAPI okHttpBaseAPI = new OkHttpBaseAPI();
                String data = okHttpBaseAPI.httpGetTask(url, "getVersion");
                try{

                    Properties versionInfo = new Properties();
                    InputStream inputStream = new ByteArrayInputStream(data.getBytes("UTF-8"));
                    versionInfo.load(inputStream);
                    final String versionURL = versionInfo.getProperty("android.versionURL");
                    String versionNumber = versionInfo.getProperty("android.version");
                    if (versionNumber.compareToIgnoreCase(locationVersion)>0 ) {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // 创建构建器
                                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                                // 设置参数
                                builder.setTitle("提示")
                                        .setMessage("远端发现新版本请更新后重新启动应用")
                                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                checkPermission();
                                                updateApk(versionURL);
                                                dialog.dismiss();
                                            }
                                        });
                                AlertDialog dialog = builder.create();
                                dialog.setCancelable(false);
                                dialog.show();
                            }
                        });
                    } else {
                        mActivity.runOnUiThread(new Runnable() {
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

    private ProgressDialog dialog;
    private void updateApk(final String apkURL) {
        dialog = ProgressDialog.show(mActivity, "", "新版本下载中……", true, false, null);
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpBaseAPI okHttpBaseAPI = new OkHttpBaseAPI();
                byte[] data = okHttpBaseAPI.httpGetFileDataTask(apkURL, "apkDonwload");

                String filePath = MobileAppInfo.getSdcardPath() + "/" + "apk";
                final String apkPath = filePath + "/temp.apk";
                FileUtilCommon.writeByte2File(filePath, "temp.apk", data, "");
                mActivity.runOnUiThread(new Runnable() {
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

    private void installApkarchive(String apkFilePath) {

        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uriPath = Uri.fromFile(new File(apkFilePath));
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            //7.0+版本手机
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            GlobalCfg globalCfg = GlobalCfg.getInstance();
            String fileprovider = globalCfg.attr(GlobalCfg.CONFIG_FIELD_FILEPROVIDER);
            if (fileprovider == null) {
                Toast.makeText(mActivity,"请在manifest中配置FileProvider",Toast.LENGTH_LONG).show();
                return;
            }
            uriPath = FileProvider.getUriForFile(mActivity,fileprovider,new File(apkFilePath));
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(uriPath, "application/vnd.android.package-archive");
        mActivity.startActivity(intent);
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

    // 权限控制
    private void checkPermission() {

        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final int permissionCode = PermissionUitls.PERMISSION_STORAGE_CODE;
                PermissionUitls.mContext = mActivity;
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

    /*
     check 版本 helper API end
     */
}
