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
import android.widget.Toast;

import com.ai.base.okHttp.OkHttpBaseAPI;
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
        netStatusListener();
        // 初始化app参数
        initGlobalParam();
        checkUpdate(null);
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

    // 待完善！！！！
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void netStatusListener() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            // 请注意这里会有一个版本适配bug，所以请在这里添加非空判断
            if (connectivityManager != null) {
                connectivityManager.requestNetwork(new NetworkRequest.Builder().build(), new ConnectivityManager.NetworkCallback() {

                    /**
                     * 网络可用的回调
                     * */
                    @Override
                    public void onAvailable(Network network) {
                        super.onAvailable(network);
                    }

                    /**
                     * 网络丢失的回调
                     * */
                    @Override
                    public void onLost(Network network) {
                        super.onLost(network);
                    }

                    /**
                     * 当建立网络连接时，回调连接的属性
                     * */
                    @Override
                    public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
                        super.onLinkPropertiesChanged(network, linkProperties);
                    }

                    /**
                     *  按照官方的字面意思是，当我们的网络的某个能力发生了变化回调，那么也就是说可能会回调多次
                     *
                     *  之后在仔细的研究
                     * */
                    @Override
                    public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                        super.onCapabilitiesChanged(network, networkCapabilities);
                    }

                    /**
                     * 在网络失去连接的时候回调，但是如果是一个生硬的断开，他可能不回调
                     * */
                    @Override
                    public void onLosing(Network network, int maxMsToLive) {
                        super.onLosing(network, maxMsToLive);
                    }

                });
            }
        }
    }

    /* check 版本 helper API begin */
    public void checkUpdate(final String versionConfigUrl) {
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
                                        .setMessage("远端发现新版本请更新后重新启动应用")
                                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                checkPermission();
                                                updateApk(versionURL);
                                                dialog.dismiss();
                                            }
                                        });
                                AlertDialog dialog = builder.create();
                                dialog.setCancelable(false);
                                dialog.show();
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
        Runtime.getRuntime().exit(0);
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
