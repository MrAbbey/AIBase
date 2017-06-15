package com.ai.base.SourceManager.app;

import android.content.ContextWrapper;
import android.content.pm.PackageManager;

import com.ai.base.SourceManager.manager.MultipleManager;
import com.ai.base.util.SharedPrefHelper;

/**
 * Created by song on 2017/6/12.
 */

public class AppRecord {
    private static final String APP_RECORD = "APP_RECORD";
    private static final String IS_FIRST = "IS_FIRST";
    private static final String LUA_VERSION = "LUA_VERSION";
    private static final String RESOURCE_VERSION = "RESOURCE_VERSION";
    private static final String CLIENT_VERSION = "CLIENT_VERSION";
    private static final String APP_RECORD_ = "APP_RECORD_";

    public AppRecord() {
    }

    public static boolean isFirst(ContextWrapper context) {
        String isFirst;
        if(MultipleManager.isMultiple()) {
            isFirst = (new SharedPrefHelper(context)).get(APP_RECORD_ + MultipleManager.getCurrAppId(), IS_FIRST, null);
            return isFirst == null;
        } else {
            isFirst = (new SharedPrefHelper(context)).get(APP_RECORD, IS_FIRST, null);
            return isFirst == null;
        }
    }

    public static void dirtyFirst(ContextWrapper context) {
        if(MultipleManager.isMultiple()) {
            (new SharedPrefHelper(context)).put(APP_RECORD_ + MultipleManager.getCurrAppId(), IS_FIRST, "false");
        } else {
            (new SharedPrefHelper(context)).put(APP_RECORD, IS_FIRST, "false");
        }

    }

    public static void setLuaVersion(ContextWrapper context) throws PackageManager.NameNotFoundException {
        (new SharedPrefHelper(context)).put(APP_RECORD, LUA_VERSION, MobileAppInfo.getInstance(context).getVersionName());
        if(MultipleManager.isMultiple()) {
            (new SharedPrefHelper(context)).put(APP_RECORD_ + MultipleManager.getCurrAppId(), LUA_VERSION, MobileAppInfo.getInstance(context).getVersionName());
        } else {
            (new SharedPrefHelper(context)).put(APP_RECORD, LUA_VERSION, MobileAppInfo.getInstance(context).getVersionName());
        }

    }

    public static String getLuaVersion(ContextWrapper context) {
        return MultipleManager.isMultiple()?(new SharedPrefHelper(context)).get(APP_RECORD_ + MultipleManager.getCurrAppId(), LUA_VERSION, ""):(new SharedPrefHelper(context)).get(APP_RECORD, LUA_VERSION, "");
    }

    public static void setResVersion(ContextWrapper context, String resVersion) {
        (new SharedPrefHelper(context)).put(APP_RECORD, RESOURCE_VERSION, resVersion);
    }

    public static String getResVersion(ContextWrapper context) {
        return (new SharedPrefHelper(context)).get(APP_RECORD, RESOURCE_VERSION, "");
    }

    public static void setClientVersion(ContextWrapper context, String clientVersion) {
        (new SharedPrefHelper(context)).put(APP_RECORD, CLIENT_VERSION, clientVersion);
    }

    public static String getClientVersion(ContextWrapper context) {
        return (new SharedPrefHelper(context)).get(APP_RECORD, CLIENT_VERSION, "");
    }
}
