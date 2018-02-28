package com.ai.webplugin;

import android.webkit.JavascriptInterface;
import com.ai.base.AIBaseActivity;

/**
 * Created by wuyoujian on 17/3/30.
 */

public class AIWebViewBasePlugin {

    private AIBaseActivity mActivity;
    public AIWebViewBasePlugin(AIBaseActivity activity) {
        this.mActivity = activity;
    }
    public AIBaseActivity getActivity() {
        return mActivity;
    }
    public void setActivity(AIBaseActivity activity) {
        this.mActivity = activity;
    }

    @JavascriptInterface
    public void JN_Test(String str){
    }
}

