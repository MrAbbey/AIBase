package com.ai.base.SourceManager.common;

import com.ai.base.SourceManager.app.MobileAppInfo;
import com.ai.base.SourceManager.manager.MultipleManager;

/**
 * Created by song on 2017/6/13.
 */

public class GlobalString {
    public static String baseResPath = MobileAppInfo.getSdcardPath()+"/" + MultipleManager.getCurrAppId();
}
