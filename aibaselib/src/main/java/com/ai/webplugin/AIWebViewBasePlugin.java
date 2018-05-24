package com.ai.webplugin;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.Toast;

import com.ai.base.AIBaseActivity;
import com.ai.base.util.SystemUtil;
import com.ai.base.util.Utility;

import org.json.JSONObject;

/**
 * Created by wuyoujian on 17/3/30.
 */

public class AIWebViewBasePlugin {

    private AIBaseActivity mActivity;
    private WebView mWebView;
    private Handler mHandler = new Handler();

    public AIWebViewBasePlugin(AIBaseActivity activity,WebView webView) {
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
                excuteJSInWebView(javascript,callback);
            }
        });
    }

    public void callback(String actionName, String param, final ValueCallback<String> callback) {
        param = Utility.encodeForJs(param);
        final String javascript = "javascript:window.WadeNAObj.callback(\'"+actionName+"\',\'"+param+"\')";
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

    // 扩展原生能力接口
    public void JN_Test(final JSONObject obj){
        Log.d("JSONObject",obj.toString());
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                callback("JN_Test",obj.toString(),null);
            }
        });

    }

    public void JN_Quit(final String param) {
        // 创建构建器
        String msg = String.format("您确定要退出%s",param);
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        // 设置参数
        builder.setTitle("提示")
                .setMessage(msg)
                .setPositiveButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //
                        callback("JN_Quit","0",null);
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        callback("JN_Quit","1",null);
                        System.exit(0);
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }

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
                    ClipData data = ClipData.newPlainText("JN_Sharing",url);
                    cm.setPrimaryClip(data);
                }

                Toast.makeText(mActivity,"已给你复制分享内容到剪切板中",Toast.LENGTH_LONG).show();
            }
        });

        callback("JN_Sharing","0",null);
    }
}

