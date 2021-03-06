package com.ai.base;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ai.base.loading.AILoadingViewBuilder;
import com.ai.base.okHttp.OkHttpBaseAPI;
import com.ai.base.util.AIListenNetWorkingStatus;
import com.ai.base.util.FileUtilCommon;
import com.ai.base.util.LogUtil;
import com.ai.base.util.PermissionUitls;
import com.ai.webplugin.AIWebViewBasePlugin;
import com.ai.webplugin.config.GlobalCfg;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by wuyoujian on 17/3/29.
 */

public abstract class AIBaseActivity extends AppCompatActivity {

    private static String mGlabalCfgFile = "global.properties";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //AIStatusBarCompat.compat(this, 0xFF000000);
        AIActivityCollector.getInstance().addActivity(this);
        // 初始化app参数
        initGlobalParam();
        checkVersion(null);
        AIListenNetWorkingStatus.getInstance().listener(this);
    }

    private void initGlobalParam() {
        // 解析全局配置
        if (mGlabalCfgFile == null || mGlabalCfgFile.length() == 0 ) {
            mGlabalCfgFile = "global.properties";
        }
        try {
            InputStream is = this.getResources().getAssets().open(mGlabalCfgFile);
            GlobalCfg globalCfg = GlobalCfg.getInstance();
            globalCfg.parseConfig(is);
        } catch (Exception e) {

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AIActivityCollector.getInstance().removeActivity(this);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        PermissionUitls.getInstance();
        PermissionUitls.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void startActivityForResult(AIWebViewBasePlugin plugin, Intent intent, int requestCode) {
        super.startActivityForResult(intent,requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStart() {
        super.onStart();
    }
    @Override
    public void onStop() {
        super.onStop();
    }

    public boolean isRunningForeground() {
        if (AIActivityLifecycleListener.getInstance().getRefCount() == 0) {
            return false;
        }

        return true;

//        ActivityManager activityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
//        List<ActivityManager.RunningAppProcessInfo> appProcessInfos = activityManager.getRunningAppProcesses();
//        // 枚举进程
//        for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessInfos) {
//            if (appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
//                if (appProcessInfo.processName.equals(this.getApplicationInfo().processName)) {
//                    LogUtil.d("song","EntryActivity isRunningForeGround");
//                    return true;
//                }
//            }
//        }
//        LogUtil.d("song", "EntryActivity isRunningBackGround");
//        return false;
    }

    @NonNull
    private String getClassName() {
        String contextString = this.toString();
        return contextString.substring(0, contextString.indexOf("@"));
    }

//    private String getClassName(){
//        ActivityManager manager = (ActivityManager)   getSystemService(Context.ACTIVITY_SERVICE);
//        List<ActivityManager.RunningTaskInfo> runningTasks = manager .getRunningTasks(1);
//        ActivityManager.RunningTaskInfo cinfo = runningTasks.get(0);
//        ComponentName component = cinfo.topActivity;
//        String className = component.getClassName();
//        return className;
//    }

    /* check 版本 helper API begin */
    public void checkVersion(final String versionConfigUrl) {
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
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // 创建构建器
                                AlertDialog.Builder builder = new AlertDialog.Builder(AIBaseActivity.this);
                                // 设置参数
                                builder.setTitle("提示")
                                        .setMessage("远端发现新版本请更新后重新启动应用");
                                builder.setPositiveButton("确定",null);
                                AlertDialog dialog = builder.create();
                                dialog.setCancelable(false);
                                dialog.show();
                                //现在builder中这样写确定按钮
                                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //如果想关闭dialog直接加上下面这句代码就行
                                        //dialog.cancel();
                                        updateApk(versionURL);
                                    }
                                });
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
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
        dialog = ProgressDialog.show(this, "", "新版本下载中……", true, false, null);
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpBaseAPI okHttpBaseAPI = new OkHttpBaseAPI();
                byte[] data = okHttpBaseAPI.httpGetFileDataTask(apkURL, "apkDonwload");

                String filePath = getFilesDir() + "/" + "apk";
                final String apkPath = filePath + "/temp.apk";
                File file = new File(apkPath);
                if (file.exists()) file.delete();
                FileUtilCommon.writeByte2File(filePath, "temp.apk", data, "");
                runOnUiThread(new Runnable() {
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
            String fileprovider =  (String)globalCfg.attr(GlobalCfg.CONFIG_FIELD_FILEPROVIDER);
            if (fileprovider == null) {
                Toast.makeText(this,"请在manifest中配置FileProvider",Toast.LENGTH_LONG).show();
                return;
            }
            uriPath = FileProvider.getUriForFile(this,fileprovider,new File(apkFilePath));
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(uriPath, "application/vnd.android.package-archive");
        startActivity(intent);
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final int permissionCode = PermissionUitls.PERMISSION_STORAGE_CODE;
                PermissionUitls.mContext = AIBaseActivity.this;
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

    /* check 版本 helper API end */

}
