package com.ai.webplugin;

import android.webkit.WebResourceResponse;

import com.ai.base.SourceManager.app.MobileAppInfo;
import com.ai.base.SourceManager.common.LRUCache;
import com.ai.base.util.LogUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by baggio on 2017/7/11.
 */

public class WebViewManager {
    private String hostName;
    private String appId;
    public WebViewManager(String hostName,String appId){
        this.hostName = hostName;
        this.appId = appId;
    }
    public WebResourceResponse getWebLocalResourceResponseByUrl(String tempUrl) {
        WebResourceResponse response = null;
        if (hostName != null && appId != null && tempUrl.contains(hostName)) {
            if (tempUrl.endsWith(".png")) {
                response = getWebResourceResponse(tempUrl, "image/png", ".png");
            } else if (tempUrl.endsWith(".gif")) {
                response = getWebResourceResponse(tempUrl, "image/gif", ".gif");
            } else if (tempUrl.endsWith(".jpg")) {
                response = getWebResourceResponse(tempUrl, "image/jepg", ".jpg");
            } else if (tempUrl.endsWith(".jepg")) {
                response = getWebResourceResponse(tempUrl, "image/jepg", ".jepg");
            } else if (tempUrl.endsWith(".js") || tempUrl.contains(".js?v=") ) {
                response = getWebResourceResponse(tempUrl, "text/javascript", ".js");
            } else if (tempUrl.endsWith(".css") || tempUrl.contains(".css?v=") ) {
                response = getWebResourceResponse(tempUrl, "text/css", ".css");
            } else if (tempUrl.endsWith(".html") ) {
                response = getWebResourceResponse(tempUrl, "text/html", ".html");
            }else if (tempUrl.endsWith(".ttf") ) {
                response = getWebResourceResponse(tempUrl, "application/octet-stream", ".ttf");
            }
            if (response != null) {
                return response;
            }
        }
        return response;
    }

    public WebResourceResponse getWebResourceResponse(String url, String mime, String style) {
        WebResourceResponse response = null;
        String localSourceFileName = getLocalSoruceFileNameByUrl(url);
        if (localSourceFileName == null) {
            return null;
        }
        /**
         * 第一次冲内存里面获取，当有资源文件更新的时候内存里的数据也会更新
         * 如果内存里没有则取本的，取到之后继续保存到内存中下次使用。
         */

        response = (WebResourceResponse) LRUCache.getInstance(8).get(localSourceFileName);
        if (response == null){
            response = getWebResourceResponseFromLocalFile(mime,localSourceFileName);
        }else {
            LogUtil.d("lrucache****get",localSourceFileName + "-----" +response);
        }
        return response;
    }


    public WebResourceResponse getWebResourceResponseFromLocalFile(String mime,String localSourceFileName) {
        WebResourceResponse response = null;
        File file = new File(localSourceFileName);
        if (file.exists()){
            try {
                InputStream is = new FileInputStream(file);
                response = new WebResourceResponse(mime, "UTF-8", is);
                LogUtil.d("LocalWebViewClient url ----", localSourceFileName);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }
        return response;
    }
    public String getLocalSoruceFileNameByUrl(String url) {
        //http://211.137.133.80:8010/mbosscentre/v5/jcl/i18n/code.zh_CN.js?v=1
        //hostName = http://211.137.133.80:8010/mbosscentre;
        String fileName = null;
        int cutLength = hostName.length();
        int ulrLength = url.length();
        String localPath = url.substring(cutLength, ulrLength);
        if (localPath.contains("?v=")) {
            localPath = localPath.split("\\?v=")[0];
        }
        fileName =  MobileAppInfo.getSdcardPath()+"/" + appId + localPath;
        return fileName;
    }

    public void saveResoponeByUrl(String url,WebResourceResponse response) {
        if(response != null){
            String localSourceFileName = getLocalSoruceFileNameByUrl(url);
            saveResoponeByFileName(localSourceFileName,response);
        }
    }

    public void saveResoponeByFileName(String localSourceFileName,WebResourceResponse response) {
        if(response != null){
            LRUCache.getInstance(8).put(localSourceFileName,response);
            LogUtil.d("lrucache****put",localSourceFileName + "-----" +response);
        }
    }
}
