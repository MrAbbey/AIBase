package com.ai.webplugin.dl;

import android.webkit.JavascriptInterface;

import com.ryg.dynamicload.DLBasePluginActivity;

/**
 * Created by wuyoujian on 2017/5/28.
 * 主要给插件使用的
 */

public class AIWebViewBasePlugin_dl {

    // 采用动态插件
    private DLBasePluginActivity mDLActivity;
    public AIWebViewBasePlugin_dl(DLBasePluginActivity activity) {
        this.mDLActivity = activity;
    }
    public DLBasePluginActivity getDLActivity() {
        return mDLActivity;
    }
    public void setDLActivity(DLBasePluginActivity activity) {
        this.mDLActivity = activity;
    }

    @JavascriptInterface
    public void test(){
    }
}
