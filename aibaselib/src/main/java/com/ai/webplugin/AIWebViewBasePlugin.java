package com.ai.webplugin;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.Toast;
import com.ai.base.AIBaseActivity;
import com.ai.base.document.AIOpenDocumentController;
import com.ai.base.loading.AILoadingViewBuilder;
import com.ai.base.util.LocalStorageManager;
import com.ai.base.util.Utility;
import com.ai.webplugin.config.GlobalCfg;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;


/**
 * Created by wuyoujian on 17/3/30.
 */

public class AIWebViewBasePlugin {

    private AIBaseActivity mActivity;
    private WebView mWebView;
    private Handler mHandler = new Handler();

    public AIWebViewBasePlugin(AIBaseActivity activity, WebView webView) {
        this.mActivity = activity;
        this.mWebView = webView;
    }

    public AIBaseActivity getActivity() {
        return mActivity;
    }

    public void setActivity(AIBaseActivity activity) {
        this.mActivity = activity;
    }

    public WebView getWebView() {
        return mWebView;
    }

    public void setWebView(WebView webView) {
        mWebView = webView;
    }


    public void excuteJavascript(String js, final ValueCallback<String> callback) {
        final String javascript = "javascript:" + js;
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                excuteJSInWebView(javascript, callback);
            }
        });
    }

    public void callback(String actionName, String param, final ValueCallback<String> callback) {
        param = Utility.encodeForJs(param);
        final String javascript = "window.WadeNAObj.callback(\'" + actionName + "\',\'" + param + "\')";
        excuteJavascript(javascript,callback);
    }

    private void excuteJSInWebView(final String javascript, final ValueCallback<String> callback) {
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
                mWebView.evaluateJavascript(javascript, callback);
            }
        }
    }

    // 扩展原生能力接口
    public void JN_Test(final String obj) {
        Log.d("JSONObject", obj);
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                callback("JN_Test", obj, null);
            }
        });

    }

    // 退出程序
    public void JN_Quit(final String param) {
        // 创建构建器
        String msg = String.format("您确定要退出%s", param);
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        // 设置参数
        builder.setTitle("提示")
                .setMessage(msg)
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //
                        //callback("JN_Quit","0",null);
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        callback("JN_Quit", "1", null);
                        System.exit(0);
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }

    // 分享链接到系统剪切板
    public void JN_Sharing(final String url) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // 从API11开始android推荐使用android.content.ClipboardManager
                ClipboardManager cm = (ClipboardManager) mActivity.getSystemService(mActivity.CLIPBOARD_SERVICE);
                // 将文本内容放到系统剪贴板里。
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB) {
                    cm.setText(url);
                } else {
                    ClipData data = ClipData.newPlainText("JN_Sharing", url);
                    cm.setPrimaryClip(data);
                }

                Toast.makeText(mActivity, "已帮您复制分享内容到剪切板中", Toast.LENGTH_LONG).show();
            }
        });

        callback("JN_Sharing", "0", null);
    }

    // 调用系统中可以打开对应文档的应用
    public void JN_OpenDocument(final String url) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                GlobalCfg globalCfg = GlobalCfg.getInstance();
                String fileProvider = globalCfg.attr(GlobalCfg.CONFIG_FIELD_FILEPROVIDER);
                if (fileProvider == null || fileProvider.length()== 0) {
                    try {
                        InputStream is = getActivity().getResources().getAssets().open("global.properties");
                        globalCfg.parseConfig(is);
                    } catch (Exception e) {

                    }

                    fileProvider = globalCfg.attr(GlobalCfg.CONFIG_FIELD_FILEPROVIDER);
                }
                AIOpenDocumentController.getInstance().openOnlineFileInContext(getActivity(), url, fileProvider);
            }
        });
    }

    // 自动更新
    public void JN_CheckVersion(final String versionConfigUrl) {
        AIWebViewPluginEngine.getInstance().checkUpdate(versionConfigUrl);
    }

    // 获取版本号
    public void JN_VersionNumber() {
        GlobalCfg globalCfg = GlobalCfg.getInstance();
        String versionNumber = globalCfg.attr(GlobalCfg.CONFIG_FIELD_VERSION);
        if (versionNumber == null || versionNumber.length()== 0) {
            try {
                InputStream is = getActivity().getResources().getAssets().open("global.properties");
                globalCfg.parseConfig(is);
            } catch (Exception e) {

            }

            versionNumber = globalCfg.attr(GlobalCfg.CONFIG_FIELD_VERSION);
        }

        callback("JN_VersionNumber",versionNumber,null);
    }

    // 启动loading
    public void JN_ShowLoading(final String text) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AILoadingViewBuilder.getInstance().show(getActivity(),text);
            }
        });
    }

    // 退出loading
    public void JN_DismissLoading() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AILoadingViewBuilder.getInstance().dismiss();
            }
        });
    }

    // 提示语
    public void JN_ShowMessage(final String message) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(),message,Toast.LENGTH_LONG).show();
            }
        });
    }


    public void JN_SetValueWithKey(JSONArray array) {
        LocalStorageManager.getInstance().setContext(getActivity());
        if (array != null && array.length() >= 2) {
            try {
                LocalStorageManager.getInstance().setString(array.getString(0),array.getString(1));
            } catch (JSONException e) {

            }
        }
    }


    public void JN_GetValueWithKey(String key) {
        LocalStorageManager.getInstance().setContext(getActivity());
        String value = LocalStorageManager.getInstance().getString(key);
        callback("JN_GetValueWithKey",value,null);
    }
}

