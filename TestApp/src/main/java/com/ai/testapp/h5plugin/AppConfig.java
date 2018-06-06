package com.ai.testapp.h5plugin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.ai.base.AIBaseActivity;
import com.ai.base.util.HttpUtil;
import com.ai.base.util.LocalStorageManager;
import com.ai.webplugin.config.ApkPluginCfg;
import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.model.PluginInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by wuyoujian on 2017/5/6.
 */

public class AppConfig {

    private static String kDeviceIdKey = "kDeviceIdKey";
    private static String folderName = "Plugins";
    private static String mApkPluginsFile = "apkPlugin.xml";
    private AIBaseActivity context;
    private String downloadPath;

    private String subAcc;
    private String token;

    private List<APKPluginInfo> apkPluginList = new ArrayList<>();

    private static AppConfig instance;
    public static AppConfig getInstance() {
        if (instance == null) {
            synchronized (AppConfig.class) {
                instance = new AppConfig();
            }
        }
        return instance;
    }

    public void setContext(AIBaseActivity context) {
        this.context = context;
        LocalStorageManager.getInstance().setContext(context);
    }


    // 临时代码,替代接口数据
    // 要从接口获取数据
    public void loadAPKPluginInfo() {

        try {
            InputStream Apkis = context.getResources().getAssets().open(mApkPluginsFile);
            ApkPluginCfg apkPluginCfg = ApkPluginCfg.getInstance();
            apkPluginCfg.parseConfig(Apkis);

            String[] names = apkPluginCfg.getNames();
            for (String name : names) {

                String pluginName = apkPluginCfg.attr(name, ApkPluginCfg.CONFIG_ATTR_NAME);;

                String activityClass = apkPluginCfg.attr(pluginName, ApkPluginCfg.CONFIG_ATTR_CLASS);
                String packageName = apkPluginCfg.attr(pluginName, ApkPluginCfg.CONFIG_ATTR_PACKAGENAME);
                String apksAddr = apkPluginCfg.attr(pluginName,"apkAddr");
                APKPluginInfo pluginInfo = new APKPluginInfo();
                pluginInfo.setDownloading(0);
                pluginInfo.setActivityName(activityClass);
                pluginInfo.setPackageName(packageName);
                pluginInfo.setUrl(apksAddr + pluginName);
                pluginInfo.setName(pluginName);

                apkPluginList.add(pluginInfo);

            }
//            if (names.length > 0) {
//                String pluginName = apkPluginCfg.attr(names[0],ApkPluginCfg.CONFIG_ATTR_NAME);;
//
//                String activityClass = apkPluginCfg.attr(pluginName,ApkPluginCfg.CONFIG_ATTR_CLASS);
//                String packageName = apkPluginCfg.attr(pluginName,ApkPluginCfg.CONFIG_ATTR_PACKAGENAME);
//                String apksAddr = apkPluginCfg.attr(pluginName,"apkAddr");
//                APKPluginInfo pluginInfo = new APKPluginInfo();
//                pluginInfo.setDownloading(0);
//                pluginInfo.setActivityName(activityClass);
//                pluginInfo.setPackageName(packageName);
//                pluginInfo.setUrl(apksAddr + pluginName);
//                pluginInfo.setName(pluginName);
//
//                apkPluginList.add(pluginInfo);
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getAPKDownloadPath() {

        if (downloadPath != null && downloadPath.length() > 0) {
        } else {
            downloadPath = context.getFilesDir().getAbsolutePath()+ File.separator + AppConfig.folderName;
        }

        File file = new File(downloadPath);
        if (!file.exists()) {
            file.mkdir();
        }
        return  downloadPath;
    }



    public String getAPKAbsolutePath(String apkName) {
        return getAPKDownloadPath() + File.separatorChar + apkName;
    }

    public void downloadAPKPlugin(final APKPluginInfo plugin,String token, String subAcc) {

        this.token = token;
        this.subAcc = subAcc;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    downloadAPK(plugin);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void downloadAPK(final APKPluginInfo plugin) {

        final String ApkName = plugin.name;

        plugin.setDownloading(1);
        HttpUtil.sendOkHttpRequest(plugin.url,new okhttp3.Callback(){
            @Override
            public void onFailure(Call call, IOException e) {}

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String pluginFile = getAPKDownloadPath() + File.separator  + ApkName;
                File file = new File(pluginFile);
                byte[] data = response.body().bytes();

                final RandomAccessFile savedFile = new RandomAccessFile(file,"rw");
                savedFile.write(data);
                response.body().close();
                savedFile.close();

                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadAPK(plugin,token,subAcc);
                    }
                });

                plugin.setDownloading(2);
            }
        });
    }

    public void loadAPK(APKPluginInfo pluginInfo,String token,String subAcc) {
        //
        String apkPath = getAPKAbsolutePath(pluginInfo.name);
        String packageName = pluginInfo.getPackageName();
        String activityName = pluginInfo.getActivityName();
        if (packageName != null && activityName != null && apkPath != null) {

            PluginInfo inf =  RePlugin.install(apkPath);
            if (inf != null ) {
                RePlugin.preload(inf);
            }

            if (pluginInfo.name.equalsIgnoreCase("biapp.apk")) {

                RePlugin.startActivity(context, RePlugin.createIntent("biapp",
                        activityName));
            } else if (pluginInfo.name.equalsIgnoreCase("plugin.apk")) {

                RePlugin.startActivity(context, RePlugin.createIntent("crmapp",
                        activityName));
            } else if (pluginInfo.name.equalsIgnoreCase("wltx.apk")) {

                RePlugin.startActivity(context, RePlugin.createIntent("com.cmos.customManagerApp",
                        activityName));
            }  else if (pluginInfo.name.equalsIgnoreCase("rnapp.apk")) {

                RePlugin.startActivity(context, RePlugin.createIntent("com.hellorectnative",
                        activityName));
            } else if (pluginInfo.name.equalsIgnoreCase("bangshou.apk")) {


                Bundle bundle = new Bundle();
                bundle.putString("staff_code","jiangqi");
                bundle.putBoolean("auto_login",true);
                SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                String dateString = sDateFormat.format(new java.util.Date());
                bundle.putString("partTime",dateString);
                bundle.putString("sys_identifier","XSMH");

                String pluginCode = "bangshou";
                Intent intent = RePlugin.createIntent(pluginCode,activityName);
                intent.putExtras(bundle);

                RePlugin.startActivity(context,intent);
            }
        } else {
            Toast.makeText(context,"插件包异常",Toast.LENGTH_SHORT).show();
        }
    }

    public int getAPKDownloadStatus(String apkName) {
        for (APKPluginInfo plugin: apkPluginList) {
            if (plugin.name.equalsIgnoreCase(apkName)){
                return plugin.getDownloading();
            }
        }

        return 3;
    }

    public APKPluginInfo getAPKPlugin(String apkName) {

        for (APKPluginInfo plugin: apkPluginList) {
            if (plugin.name.equalsIgnoreCase(apkName)){
                return plugin;
            }
        }
        return null;
    }


    public class APKPluginInfo {
        private String name;
        private String activityName;
        private String packageName;
        private int downloading = 0; // 0 没有下载, 1 下载中, 2 下载完成, 3 插件异常
        private String url;
        private String version;
        private String param;

        public int getDownloading() {
            return downloading;
        }

        public void setDownloading(int downloading) {
            this.downloading = downloading;
        }

        public void setName(String pluginName) {
            this.name = pluginName;
        }

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getActivityName() {
            return activityName;
        }

        public void setActivityName(String activityName) {
            this.activityName = activityName;
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getParam() {
            return param;
        }

        public void setParam(String param) {
            this.param = param;
        }
    }

}
