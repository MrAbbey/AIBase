package com.ai.base.SourceManager.manager;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;

import com.ai.Interfaces.UploadSourceFileListener;
import com.ai.base.SourceManager.config.ServerPageConfig;
import com.ai.base.SourceManager.ui.progressDialog.SimpleProgressDialog;
import com.ai.base.okHttp.OkHttpBaseAPI;
import com.ai.base.util.FileUtilCommon;
import com.ai.base.util.LogUtil;
import com.ailk.common.data.IData;
import com.ailk.common.data.impl.DataMap;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.ai.base.SourceManager.app.MobileAppInfo.getSdcardPath;

/**
 * Created by song on 2017/6/12.
 * 用于控制管理webview的资源文件的更新
 */

public class ResourceManager {
    private Context mContext;
    private String baseAddress;
    private ContextWrapper mContextWapper;
    private int filecount_Done = 0;//已经下载的文件总数
    private ProgressDialog updateResProgressDialog;
    private UploadSourceFileListener uploadSourceFileListener;
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    updateResProgressDialog.setProgress(ResVersionManager.updateCount);
                    updateResProgressDialog.dismiss();
                    if (uploadSourceFileListener != null) {
                        uploadSourceFileListener.uploadDone();
                    }
                    break;
                case 1:
                    updateResource();
                    break;
                case 3:
                    updateResProgressDialog.setProgress(filecount_Done);
                    if (ResVersionManager.updateCount <= filecount_Done) {
                        updateResProgressDialog.setProgress(ResVersionManager.updateCount);

                        if(updateResProgressDialog.isShowing()){
                            if (uploadSourceFileListener != null) {
                                uploadSourceFileListener.uploadDone();
                            }
                            updateResProgressDialog.dismiss();
                        }

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
        ResVersionManager.updateCount = 0;
        filecount_Done = 0;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map remoteResVersions = null;
                try {
                    remoteResVersions = ResVersionManager.getRemoteResVersions(mContext,baseAddress,true);
                    if (remoteResVersions == null) {
                        return;
                    }
                    if (ResVersionManager.isUpdateResource(mContextWapper, remoteResVersions)) {
                        handler.sendEmptyMessage(1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    public void updateDelay(final long delayTime) throws Exception {
        ResVersionManager.updateCount = 0;
        filecount_Done = 0;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map remoteResVersions = null;
                try {
                    Thread.sleep(delayTime);
                    remoteResVersions = ResVersionManager.getRemoteResVersions(mContext,baseAddress,true);
                    if (remoteResVersions == null) {
                        return;
                    }
                    if (ResVersionManager.isUpdateResource(mContextWapper, remoteResVersions)) {
                        handler.sendEmptyMessage(1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    /**
     * 从本地获取版本信息
     * @throws Exception
     */
    public void updateFromLocalFile() throws Exception {
        ResVersionManager.updateCount = 0;
        filecount_Done = 0;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map remoteResVersions = null;
                try {
                    float startTime = System.currentTimeMillis();
                    remoteResVersions = ResVersionManager.getRemoteResVersions(mContext,null,false);
                    if (remoteResVersions == null) return;
                    final Iterator it = remoteResVersions.keySet().iterator();
                    String sharedName;
                    if(MultipleManager.isMultiple()) {
                        sharedName = "LOCAL_RES_VERSION_" + MultipleManager.getCurrAppId();
                    } else {
                        sharedName = "LOCAL_RES_VERSION";
                    }
                    SharedPreferences.Editor shareEditor = mContextWapper.getSharedPreferences(sharedName, 0).edit();
                    while (it.hasNext()) {
                        String path = it.next().toString();
                        String value = String.valueOf(remoteResVersions.get(path));
                        ResVersionManager.getLocalResVersions(mContextWapper).put(path, value);
                        shareEditor.putString(path, value);
//                        try {
//                            String url = baseAddress + "/" + path;
//                            WebViewManager webViewManager = new WebViewManager(baseAddress, MultipleManager.getCurrAppId());
//                            WebResourceResponse webResourceResponse = webViewManager.getWebLocalResourceResponseByUrl(url);
//                            {
//                                if (webResourceResponse != null) {
//                                    webViewManager.saveResoponeByFileName(path, webResourceResponse);
//                                }
//                            }
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
                    }
                    shareEditor.commit();
                    float endTime = System.currentTimeMillis();
                    LogUtil.d("updateSource----------",(endTime-startTime)/1000 + "秒");
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

        // 创建构建器
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        // 设置参数
        String message = "远端发现新资源," + sizeMessage + "建议在WIFI环境下下载";
        builder.setTitle("资源更新")
                .setMessage(message)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        progressDialogShow();
                        updateRes();
                        ResVersionManager.filesSize = 0;
                        dialog.dismiss();
                    }
                })
               ;
        builder.create().show();
//        ConfirmDialog confirmDialog = new ConfirmDialog(mContext, "资源更新", "远端发现新资源," + sizeMessage + "建议在WIFI环境下下载") {
//            protected void okEvent() {
//                super.okEvent();
//                progressDialogShow();
//                updateRes();
//                ResVersionManager.filesSize = 0;
//            }
//
//            protected void cancelEvent() {
//                ResVersionManager.filesSize = 0;
//                super.cancelEvent();
//            }
//        };
//        confirmDialog.show();

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
            //downPath = FileUtil.connectFilePath(GlobalString.baseResPath, path.substring(8));
            downloadFile(context,path);
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

    private void downloadFile(ContextWrapper context,String path) {
        String [] temps = path.split("\\/");
        int length = temps.length;
        String fileName = temps[length-1];
        String downPath = path.substring(0, path.length() - fileName.length());
        //http://211.137.133.80:8010/mbosscentre/v5/jcl/i18n/code.zh_CN.js?v=1
        if (fileName.contains("?v=")){
            fileName = fileName.split("\\?v=")[0];
        }
        String url = baseAddress + "/" + path;
        byte[] data = OkHttpBaseAPI.getInstance().httpGetFileDataTask(url, "song");
        FileUtilCommon.writeByte2File(getSdcardPath() + "/" +MultipleManager.getCurrAppId() + "/" +downPath , fileName, data, "");
        data = null;
        fileCountDoneCount(context, path);
    }

    private synchronized void fileCountDoneCount(ContextWrapper context,String path) {
        try {
            ResVersionManager.setLocalResVersion(context, path, ResVersionManager.getRemoteResVersion(path));
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public void setUploadSourceFileListener(UploadSourceFileListener uploadSourceFileListener) {
        this.uploadSourceFileListener = uploadSourceFileListener;
    }
}
