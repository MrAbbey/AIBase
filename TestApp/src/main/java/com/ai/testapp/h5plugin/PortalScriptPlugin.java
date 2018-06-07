package com.ai.testapp.h5plugin;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import com.ai.base.AIBaseActivity;
import com.ai.base.util.AESEncrypt;
import com.ai.webplugin.AIWebViewBasePlugin;
import com.qihoo360.replugin.RePlugin;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.Properties;

/**
 * Created by wuyoujian on 2017/5/4.
 *
 */

public class PortalScriptPlugin extends AIWebViewBasePlugin {

    public PortalScriptPlugin(AIBaseActivity activity, WebView webView) {
        super(activity,webView);
    }

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

    public void JN_Test(String object) {
        Log.d("JN_Array",object);

        ClipboardManager cm = (ClipboardManager)getActivity().getSystemService(getActivity().CLIPBOARD_SERVICE);
        if(Build.VERSION.SDK_INT <= 11) {
            cm.setText(object);
        } else {
            ClipData data = ClipData.newPlainText("JN_Test", object);
            cm.setPrimaryClip(data);
        }

        Toast.makeText(getActivity(),object,Toast.LENGTH_LONG).show();

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                callback("JN_Test","我就是我，不一样的我",null);
            }
        });
    }

    public void JN_ShowMessage(String object) {
        Toast.makeText(getActivity(),"JN_ShowMessage",Toast.LENGTH_LONG).show();
    }

//    public void JN_Test(JSONObject object) {
//        Log.d("JN_Array",object.toString());
//
//        Toast.makeText(getActivity(),object.toString(),Toast.LENGTH_LONG).show();
//
//        getActivity().runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                callback("JN_Test","回调'\'数据",null);
//            }
//        });
//    }

    public boolean openApp(final Activity activity, final String packageName, final String activityName, final Intent param) {
        if ((packageName != null && packageName.length() > 0) &&
                (activityName != null && activityName.length() > 0)) {

            String tempActivityName = activityName;
            if (!tempActivityName.contains(".")) {
                tempActivityName = packageName + "." + activityName;
            }

            final String name = tempActivityName;
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ComponentName componetName = new ComponentName(
                            //这个是另外一个应用程序的包名
                            packageName,
                            //这个参数是要启动的Activity
                            name);

                    Intent intent= new Intent();
                    if (param != null) {
                        intent.putExtras(param);
                    }
                    intent.setComponent(componetName);
                    activity.startActivity(intent);
                }
            });

            return true;
        }

        return false;
    }

    @JavascriptInterface
    @NotProguard
    public void JN_OpenApp(){

        Intent intent = new Intent();
        intent.putExtra("key","key");
        openApp(getActivity(),"com.leafact.fingerprintdemo","com.leafact.fingerprintdemo.MainActivity",intent);
    }

    public static Properties getPropertiesInAssets(Context context, String propertiesFileName){
        Properties props = new Properties();
        try {
            InputStream in = context.getAssets().open(propertiesFileName);
            props.load(in);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return props;
    }

    @JavascriptInterface
    @NotProguard
    public void JN_Location() {

        // http://120.194.44.244:30001/ngesoph5/front/sh/busiInfo!login?uid=b004&token=4%2FgizGcBAAC51UVZ37AUQywCOFHwdGPHyw7Wpe7pad7md1z%2BlfMHfHbjtUGZXOCuhQsjhp%2BfH%2F00OuqH%2FsM%2BgA%3D%3D
        // 4%2FgizGcBAAC51UVZ37AUQywCOFHwdGPHyw7Wpe7pad7md1z%2BlfMHfHbjtUGZXOCuhQsjhp%2BfH%2F00OuqH%2FsM%2BgA%3D%3D
        // 4%2FgizGcBAAC51UVZ37AUQywCOFHwdGPHyw7Wpe7pad7md1z%2BlfMHfHbjtUGZXOCuhQsjhp%2BfH%2F00OuqH%2FsM%2BgA%3D%3D
        // 加密数据
        // 13290947036


        // http://120.194.44.244:30001/ngesoph5/front/sh/busiInfo!login?uid=b004&token=4%2FgizGcBAAC51UVZ37AUQywCOFHwdGPHyw7Wpe7pad5s8jJfbXZJNAYHnm0Yl8osvbnXdmIq1MnBCLVUtHXaHg%3D%3D
        // 4%2FgizGcBAAC51UVZ37AUQywCOFHwdGPHyw7Wpe7pad5s8jJfbXZJNAYHnm0Yl8osvbnXdmIq1MnBCLVUtHXaHg%3D%3D
        // 13908711390

        String token = "isLoginNoSms=1&loginToSuccessPage=1&mobile=" + "13908711390";
        try {
            token = AESEncrypt.encrypt(token, "www.asiainfo.com");
            token = URLEncoder.encode(token,"utf-8");
            Log.d("token",token);
        } catch (Exception e) {
        }

        Properties props = getPropertiesInAssets(getActivity(),"global.properties");
        String url = props.getProperty("online.addr");

//        try {
//            String  str = AESEncrypt.encrypt("uid=b004&isLoginNoSms=1&loginToSuccessPage=1&mobile=15093285790","www.asiainfo.com");
//            Log.d("AES",str);
//            String str1 = URLEncoder.encode(str,"utf-8");
//            Log.d("URLEncoder",str1);
//            // G5uhfUEB8T%2FxOvggSrdm3br9Mi81liLWbTVqXI2mEnkwqdZoGbQ%2FFi%2Br8EsY6zStAebP4C2vQOqAyeYGjoH0jA%3D%3D
//
//        } catch (Exception e) {
//            e.printStackTrace();;
//        }
    }

//    @JavascriptInterface
//    @NotProguard
//    public void JN_Test(String string) {
//
//        getActivity().runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                AIWebViewPluginEngine engine = AIWebViewPluginEngine.getInstance();
//                engine.excutePluginCallback("PortalScriptPlugin_JN_Test", "回调'\'数据", new ValueCallback<String>() {
//                    @Override
//                    public void onReceiveValue(String value) {
//
//                    }
//                });
//            }
//        });
//    }

    @JavascriptInterface
    @NotProguard
    public void JN_EnterInPlugin(String pluginName ) {

        AppConfig appConfig = AppConfig.getInstance();
        AppConfig.APKPluginInfo apkPluginInfo = appConfig.getAPKPlugin(pluginName);
        Intent intent = RePlugin.createIntent(pluginName, apkPluginInfo.getActivityName());
        RePlugin.startActivity(getActivity(), intent);
    }

    @JavascriptInterface
    @NotProguard
    public void JN_EnterPlugin(String token,String subAcc, String pluginName ) {

        AppConfig appConfig = AppConfig.getInstance();
        AppConfig.APKPluginInfo apkPluginInfo = appConfig.getAPKPlugin(pluginName);
        String apkPath = AppConfig.getInstance().getAPKAbsolutePath(pluginName);
        File apkFile = new File(apkPath);
        if (apkFile.exists()) {
            appConfig.loadAPK(apkPluginInfo,token,subAcc);
        } else {
            int downloadStatus = appConfig.getAPKDownloadStatus(pluginName);
            if (downloadStatus == 2) {
                appConfig.loadAPK(apkPluginInfo,token,subAcc);
            } else if (downloadStatus == 0){
                // 下载
                Toast.makeText(getActivity(),"开始下载插件...",Toast.LENGTH_LONG).show();
                appConfig.downloadAPKPlugin(apkPluginInfo,token,subAcc);
            } else if (downloadStatus == 1) {
                Toast.makeText(getActivity(),"插件正在下载...",Toast.LENGTH_SHORT).show();
            } else  {
                Toast.makeText(getActivity(),"插件配置异常",Toast.LENGTH_SHORT).show();
            }
        }
    }
}
