package com.ai.base.SourceManager.manager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.WebResourceResponse;

import com.ai.base.SourceManager.common.MobileThread;
import com.ai.base.SourceManager.config.ServerPageConfig;
import com.ai.base.SourceManager.ui.ConfirmDialog;
import com.ai.base.SourceManager.ui.progressDialog.SimpleProgressDialog;
import com.ai.base.okHttp.OkHttpBaseAPI;
import com.ai.base.okHttp.OkHttpUtils;
import com.ai.base.okHttp.cookie.CookieJarImpl;
import com.ai.base.okHttp.cookie.store.PersistentCookieStore;
import com.ai.base.okHttp.https.HttpsUtils;
import com.ai.base.okHttp.log.LoggerInterceptor;
import com.ai.base.util.FileUtilCommon;
import com.ai.base.util.LogUtil;
import com.ailk.common.data.IData;
import com.ailk.common.data.impl.DataMap;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

import static com.ai.base.SourceManager.app.MobileAppInfo.getSdcardPath;

/**
 * Created by song on 2017/6/12.
 * 用于控制管理webview的资源文件的更新
 */

public class ResourceManager {
    private Context mContext;
    private String baseAddress;
    private ContextWrapper mContextWapper;
    private static int fileCount = 0;//下载文件的总数
    private int filecount_Done = 0;//已经下载的文件总数
    private ProgressDialog updateResProgressDialog;
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    updateResProgressDialog.setProgress(ResVersionManager.updateCount);
                    updateResProgressDialog.dismiss();
                    break;
                case 1:
                    updateResource();
                    break;
                case 3:
                    updateResProgressDialog.setProgress(filecount_Done);
                    if (ResVersionManager.updateCount <= filecount_Done) {
                        updateResProgressDialog.setProgress(ResVersionManager.updateCount);
                        updateResProgressDialog.dismiss();
                    }
                    break;
                case 4:
                    progressDialogShow();
                    break;
            }

        }
    };

    /**
     *
     * @param context
     * @param baseAddress 远程资源文件的hostname
     * @param currAppid
     */
    public ResourceManager(Context context,String baseAddress,String currAppid) {
        this.mContext = context;
        this.mContextWapper = (ContextWrapper) context;
        this.baseAddress = baseAddress;
        MultipleManager.setCurrAppId(currAppid);
        MultipleManager.setMultBasePath(mContext.getResources().getAssets().toString());
    }

    public void update() throws Exception {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map remoteResVersions = null;
                try {
                    remoteResVersions = ResVersionManager.getRemoteResVersions(mContext,baseAddress);
                    if (remoteResVersions == null) return;
                    if (ResVersionManager.isUpdateResource(mContextWapper, remoteResVersions)) {
                        handler.sendEmptyMessage(1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    protected IData getVersion() throws Exception {
        //// TODO: 2017/6/12  服务器返回文件的版本
        String result = "";
        //return new DataMap(result);
        return null;
    }

    protected String getResKey() throws Exception {
        //// TODO: 2017/6/12 服务器返回
        return (new DataMap("")).getString("KEY");
    }

    protected void updateResource() {
        double fileSize = ResVersionManager.filesSize / 1048576;
        String size = String.format("%.1f", fileSize);
        String sizeMessage;
        if (fileSize < 1){
            sizeMessage = "文件小于1M";
        }else {
            sizeMessage = "文件大小为：" + size + "M";
        }
        ConfirmDialog confirmDialog = new ConfirmDialog(mContext, "资源更新", "远端发现新资源," + sizeMessage + "建议在WIFI环境下下载") {
            protected void okEvent() {
                super.okEvent();
                progressDialogShow();
                updateRes();
                ResVersionManager.filesSize = 0;
            }

            protected void cancelEvent() {
                ResVersionManager.filesSize = 0;
                super.cancelEvent();
            }
        };
        confirmDialog.show();

    }

    public void updateRes() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                downloadResource();
            }
        }).start();
    }

    public void downloadResource(){
        long start = System.currentTimeMillis();
        Map remoteResVersions = null;
        if (MultipleManager.isMultiple()) {
            remoteResVersions = (Map) ResVersionManager.multipleRemoteResVersions.get(MultipleManager.getCurrAppId());
        } else {
            remoteResVersions = ResVersionManager.remoteResVersions;
        }

        final Iterator it = remoteResVersions.keySet().iterator();
        fileCount = remoteResVersions.size();
        int threadNumber = 40;
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(threadNumber);
        while (it.hasNext()) {
//            if (Thread.currentThread().isInterrupted()) {
//                return;
//            }
            final String path = it.next().toString();
            fixedThreadPool.execute(new Runnable() {
                public void run() {
                    try {
                        checkResource(path, mContextWapper, handler);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            //checkResource(path, mContextWapper, handler);
        }

        ServerPageConfig.getInstance();
    }

    public void checkResource(String path, ContextWrapper context, Handler handler) throws Exception {
        {
            ResVersionManager.setLocalResVersion(context, path, ResVersionManager.getRemoteResVersion(path));
            //downPath = FileUtil.connectFilePath(GlobalString.baseResPath, path.substring(8));
            downloadFile(path);
            // TODO: 2017/6/13 发送http请求获取资源文件
//            if (handler != null) {
//                handler.sendEmptyMessage(3);
//            }

        }
    }

    private void progressDialogShow() {
        updateResProgressDialog = createUpdateResProgressDialog();
        updateResProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface arg0) {

            }
        });
        updateResProgressDialog.show();
    }

    private void downloadFile(String path) {
        String [] temps = path.split("\\/");
        int length = temps.length;
        String fileName = temps[length-1];
        String downPath = path.substring(0, path.length() - fileName.length());
        //http://211.137.133.80:8010/mbosscentre/v5/jcl/i18n/code.zh_CN.js?v=1
        if (fileName.contains("?v=")){
            fileName = fileName.split("\\?v=")[0];
        }
        byte[] data = OkHttpBaseAPI.getInstance().httpGetFileDataTask(baseAddress + "/" + path, "song");
        FileUtilCommon.writeByte2File(getSdcardPath() + "/" +MultipleManager.getCurrAppId() + "/" +downPath , fileName, data, "");
        data = null;
        fileCountDoneCount();
    }

    private synchronized void fileCountDoneCount() {
        filecount_Done++;
        if (handler != null) {
               handler.sendEmptyMessage(3);
        }
    }

    private ProgressDialog createUpdateResProgressDialog() {
        SimpleProgressDialog simpleProgressDialog = (SimpleProgressDialog) new SimpleProgressDialog(mContext).setMessage("资源更新中...");
        simpleProgressDialog.setProgressStyle(1);
        simpleProgressDialog.setCancelable(false);
        simpleProgressDialog.getProgressDialog().setMax(ResVersionManager.updateCount);
        simpleProgressDialog.getProgressDialog().getWindow().setGravity(17);

        ProgressDialog progressDialog = simpleProgressDialog.build();
        return progressDialog;
    }
}
