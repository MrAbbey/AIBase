package com.ai.base.webviewCacheInterceptor;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import com.ai.base.util.AESEncrypt;
import com.ai.base.util.Utility;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AIWebViewResRequestInterceptor {

    private OkHttpClient mHttpClient = null;
    private String mCacheFilePath;
    private Activity mContext;
    private long mConnectTimeout;
    private long mReadTimeout;
    // 默认不缓存
    private boolean mForceCache = false;
    //
    private String mEntryptKey = "www.asiainfo.com";

    private String mOrigin = "";
    private String mReferer="";
    private String mUserAgent="";

    // debug模式下，缓存文件夹以及文件不是隐藏的
    private boolean mDebug;


    // 单例
    private static volatile  AIWebViewResRequestInterceptor webViewResRequestInterceptor;
    // 默认缓存的文件后缀
    private HashSet mCacheExtension = new HashSet() {
        {
            add("html");
            add("htm");
            add("js");
            add("ico");
            add("css");
            add("png");
            add("jpg");
            add("jpeg");
            add("gif");
            add("bmp");
            add("ttf");
        }
    };

    public static AIWebViewResRequestInterceptor getInstance(){
        if (webViewResRequestInterceptor==null){
            synchronized (AIWebViewResRequestInterceptor.class){
                if (webViewResRequestInterceptor == null){
                    webViewResRequestInterceptor = new AIWebViewResRequestInterceptor();
                }
            }
        }
        return webViewResRequestInterceptor;
    }

    public void init(Builder builder){
        this.mConnectTimeout = builder.mConnectTimeout;
        this.mReadTimeout = builder.mReadTimeout;
        this.mContext = builder.mContext;
        this.mCacheFilePath = builder.mCacheFilePath;
        this.mForceCache = builder.mForceCache;
        this.mDebug = builder.mDebug;
        if (TextUtils.isEmpty(this.mCacheFilePath)) {
            if (this.mContext != null) {
                this.mCacheFilePath = this.mContext.getFilesDir() + (this.mDebug?"":".") + "/AIWebviewCache";
                File file = new File(this.mCacheFilePath);
                if (!file.exists()) {
                    file.mkdir();
                }
            }
        }

        if (!builder.mCacheExtensions.isEmpty()) {
            for (String ext:builder.mCacheExtensions) {
                addCacheExtension(ext);
            }
        }

        if (!builder.mEntryptKey.isEmpty()) {
            this.mEntryptKey = builder.mEntryptKey;
        }

        initHttpClient();
    }

    public void addCacheExtension(String extension) {
        if (TextUtils.isEmpty(extension)) {
            return;
        }

        String ext = extension.replace(".", "").toLowerCase().trim();
        if (!mCacheExtension.contains(ext)){
            mCacheExtension.add(ext);
        }
    }

    public void removeCacheExtension(String extension) {
        if (TextUtils.isEmpty(extension)) {
            return;
        }
        String ext = extension.replace(".", "").toLowerCase().trim();
        mCacheExtension.remove(ext);
    }

    public WebResourceResponse interceptRequest(WebView view,WebResourceRequest request) {
        return interceptRequest(view,request.getUrl().toString(),request.getRequestHeaders());
    }

    public WebResourceResponse interceptRequest(WebView view,String url) {
        return interceptRequest(view,url,buildHeaders(view,url));
    }

    public void clearCache() {
        File resFile = new File(mCacheFilePath);
        if (resFile.exists() && resFile.isDirectory()) {
            resFile.delete();
        }
    }

    private boolean canCache(String extension) {
        if (TextUtils.isEmpty(extension)) {
            return false;
        }
        extension = extension.toLowerCase().trim();
        return mCacheExtension.contains(extension);
    }

    private void initHttpClient(){
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(mConnectTimeout, TimeUnit.SECONDS)
                .readTimeout(mReadTimeout, TimeUnit.SECONDS);
        builder.hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
        mHttpClient = builder.build();
    }

    private Map<String, String> buildHeaders(final WebView webView,String url){

        Map<String, String> headers  = new HashMap<String, String>();
        mReferer = url;
        mOrigin = AIResURLUtils.getOriginUrl(mReferer);
        if (mContext != null) {
            mContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mUserAgent = webView.getSettings().getUserAgentString();
                }
            });
        }

        if (!TextUtils.isEmpty(mOrigin)){
            headers.put("Origin",mOrigin);
        }
        if (!TextUtils.isEmpty(mReferer)){
            headers.put("Referer",mReferer);
        }

        if (!TextUtils.isEmpty(mUserAgent)){
            headers.put("User-Agent",mUserAgent);
        }

        return headers;
    }

    // 返回null，走正常的webview的请求
    private WebResourceResponse interceptRequest(WebView view,final String url,Map<String, String> headers){
        Map<String,String> fields = AIResURLUtils.getResURLFieldsFromUrl(url);
        if (fields == null || fields.isEmpty()) return null;
        String resUrl = fields.get("url");
        final String extension = fields.get("extension");
        String saveFileName = (this.mDebug?"":".") + Utility.md5(resUrl)+'.' + extension;

        String mimeType = AIResURLUtils.getMimeTypeFromUrl(url);
        final File resFile = new File(mCacheFilePath + "/" + saveFileName);
        if (mForceCache) {
            byte[] bytes = null;
            if (resFile.exists()) {
                // 存在本地缓存
                Log.d("from location",url);
                InputStream inputStream = null;
                try {
                    // 根据path路径实例化一个输入流的对象
                    inputStream = new FileInputStream(resFile);
                    int length = inputStream.available();
                    byte [] buffer = new byte[length];
                    inputStream.read(buffer);
                    if (buffer.length <= 0) {
                        // 文件大小为0，说明保存异常
                        resFile.delete();
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException ee) {

                            }
                        }
                        return null;
                    }
                    bytes = buffer;

                } catch (IOException e) {
                    // 读取异常
                    resFile.delete();
                } finally {
                    if (extension.equalsIgnoreCase("js")) {
                        // 只有js加密存储
                        try {
                            byte[] decrypted = AESEncrypt.decrypt(bytes, mEntryptKey);
                            if (decrypted == null || decrypted.length <= 0) {
                                resFile.delete();
                            }
                            bytes = decrypted;

                        } catch (Exception e) {
                            resFile.delete();
                        } finally {
                        }
                    }

                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException ee) {

                        }
                    }
                }

            } else {
                // 在线获取，并存储到本地
                Log.d("from server",url);

                try {
                    if (canCache(extension)) {
                        Request.Builder reqBuilder = new Request.Builder().url(url);
                        for (Map.Entry<String,String> entry:headers.entrySet()){
                            reqBuilder.addHeader(entry.getKey(),entry.getValue());
                        }
                        Request request =  reqBuilder.build();
                        Response response = mHttpClient.newCall(request).execute();
                        bytes = response.body().bytes();
                    } else {
                        return null;
                    }

                } catch (IOException e) {
                }

                final byte[] temp = bytes;
                // 保存到本地
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        OutputStream outStream = null;
                        try {
                            outStream = new FileOutputStream(resFile);
                            if (temp != null && temp.length > 0) {
                                try {
                                    if (extension.equalsIgnoreCase("js")) {
                                        // 只有js加密存储
                                        byte[] encrypted = AESEncrypt.encrypt(temp,mEntryptKey);
                                        if (encrypted == null || (encrypted != null && encrypted.length <= 0)) {
                                            if (outStream != null) {
                                                try {
                                                    outStream.close();
                                                } catch (IOException ee) {

                                                }
                                            }
                                        } else {
                                            outStream.write(encrypted);
                                            Log.d("save to location",url);
                                        }
                                    } else {
                                        outStream.write(temp);
                                        Log.d("save to location",url);
                                    }
                                } catch (Exception e) {
                                }
                            }
                        } catch (IOException e) {
                        } finally {
                            if (outStream != null) {
                                try {
                                    outStream.close();
                                } catch (IOException ee) {

                                }
                            }
                        }
                    }
                }).start();
            }

            WebResourceResponse webResourceResponse = null;
            if (bytes != null && bytes.length > 0) {
                webResourceResponse  = new WebResourceResponse(mimeType, "", new ByteArrayInputStream(bytes));
            }
            return webResourceResponse;
        }

        Log.d("no cache",url);
        return null;
    }


    public static class Builder {

        private String mCacheFilePath;
        private long mConnectTimeout = 20;
        private long mReadTimeout = 20;
        private Activity mContext;
        private boolean mForceCache = false;
        private ArrayList<String> mCacheExtensions;
        private String mEntryptKey;
        private boolean mDebug;

        public Builder(Activity context){
            mContext = context;
            mCacheExtensions = new ArrayList<>();
        }

        public Builder setCacheExtensions(ArrayList list) {
            if (!list.isEmpty()) {
                mCacheExtensions.addAll(list);
            }
            return this;
        }

        public Builder setForceCache(boolean forceCache){
            mForceCache = forceCache;
            return this;
        }

        public Builder setReadTimeoutSecond(long time){
            if (time>=0){
                mReadTimeout = time;
            }
            return this;
        }
        public Builder setConnectTimeoutSecond(long time){
            if (time>=0){
                mConnectTimeout = time;
            }

            return this;
        }

        public Builder setCacheFilePath(String path){
            if (path != null){
                mCacheFilePath = path;
            }
            return this;
        }

        public Builder setEncryptKey(String encryptKey){
            if (encryptKey != null){
                mEntryptKey  = encryptKey;
            }
            return this;
        }

        public Builder setDebug(boolean debug){
            mDebug = debug;
            return this;
        }

        public AIWebViewResRequestInterceptor build(){
            return new AIWebViewResRequestInterceptor();
        }
    }
}
