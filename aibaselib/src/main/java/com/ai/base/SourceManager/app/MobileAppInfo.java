package com.ai.base.SourceManager.app;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.lang.reflect.Method;

/**
 * Created by baggio on 2017/6/12.
 */

public class MobileAppInfo {
    public static final int Android_1_5 = 3;
    public static final int Android_1_6 = 4;
    public static final int Android_2_1 = 5;
    public static final int Android_2_2 = 8;
    public static final int Android_2_3 = 9;
    public static final int Android_2_3_3 = 10;
    public static final int Android_3_0 = 11;
    public static final int Android_3_1 = 12;
    public static final int Android_3_2 = 13;
    public static final int Android_4_0 = 14;
    public static final int Android_4_0_3 = 15;
    public static final int Android_4_1_2 = 16;
    public static final int Android_4_2_2 = 17;
    public static final int Android_4_3 = 18;
    public static final int Android_4_4 = 19;
    private static MobileAppInfo info;
    private Context context;

    private MobileAppInfo(Context context) {
        this.context = context;
    }

    public static MobileAppInfo getInstance(Context context) {
        if(info == null) {
            info = new MobileAppInfo(context);
        }

        return info;
    }

    public boolean isTablet(Context context) {
        if(Build.VERSION.SDK_INT >= 11) {
            Configuration con = context.getResources().getConfiguration();

            try {
                Method x = con.getClass().getMethod("isLayoutSizeAtLeast", new Class[]{Integer.TYPE});
                Boolean r = (Boolean)x.invoke(con, new Object[]{Integer.valueOf(4)});
                return r.booleanValue();
            } catch (Exception var5) {
                return false;
            }
        } else {
            return false;
        }
    }

    public String getAppName() {
        return this.context.getPackageManager().getApplicationLabel(this.context.getApplicationInfo()).toString();
    }

    public String getVersionName() throws PackageManager.NameNotFoundException {
        PackageManager pm = this.context.getPackageManager();
        PackageInfo info = pm.getPackageInfo(this.context.getPackageName(), 0);
        return info.versionName;
    }

    public int getVersionCode() throws PackageManager.NameNotFoundException {
        PackageManager pm = this.context.getPackageManager();
        PackageInfo info = pm.getPackageInfo(this.context.getPackageName(), 0);
        return info.versionCode;
    }

    public String getClassName() {
        return ((Activity)this.context).getLocalClassName();
    }

    public String getSourceDir() {
        return this.context.getPackageResourcePath();
    }

    public String getProcessName() {
        return this.context.getApplicationInfo().processName;
    }

    public String getPackageName() {
        return this.context.getPackageName();
    }

    public static String getOsVersion() {
        return Build.VERSION.RELEASE;
    }

    public static int getOsVersionNumber() {
        return Build.VERSION.SDK_INT;
    }

    public File getApk() {
        //return ApkHelper.getFile(this.getPackageName());
        return null;
    }

    public Drawable getIcon() {
        return this.context.getApplicationInfo().loadIcon(this.context.getPackageManager());
    }

    public static String getSdcardPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals("mounted");
        if(sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();
            return sdDir.toString();
        }
        return null;
    }

    public String getAppPath() {
        String appPath = "";
        if("".equals(appPath)) {
            appPath = getInstance(this.context).getAppName();
        }

        return appPath;
    }

    public String getSdcardAppPath() {
        String appPath = getInstance(this.context).getAppPath();
        return getSdcardPath() + File.separator + appPath;
    }

    public static String getSdcardAppPath(Context context) {
        return getInstance(context).getSdcardAppPath();
    }

    public String getAssetsAppPath() {
        String appPath = getInstance(this.context).getAppPath();
        return "assets" + File.separator + appPath;
    }
}
