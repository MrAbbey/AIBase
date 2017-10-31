package com.ai.webplugin.dl;

import android.webkit.JavascriptInterface;

import com.ai.base.AIBaseActivity;

/**
 * Created by wuyoujian on 2017/5/28.
 * 主要给插件使用的
 */

public class AIWebViewBasePlugin_dl {

    // 采用动态插件
    private AIBaseActivity mActivity;
    public AIWebViewBasePlugin_dl(AIBaseActivity activity) {
        this.mActivity = activity;
    }
    public AIBaseActivity getActivity() {
        return mActivity;
    }
    public void setActivity(AIBaseActivity activity) {
        this.mActivity = activity;
    }

    @JavascriptInterface
    public void test(){
    }
}
