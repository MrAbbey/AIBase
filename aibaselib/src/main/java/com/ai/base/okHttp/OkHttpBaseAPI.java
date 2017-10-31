package com.ai.base.okHttp;

import android.content.ContextWrapper;
import android.text.TextUtils;
import android.util.Log;

import com.ai.base.okHttp.callback.OnDownloadListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSource;
import okio.Timeout;

/**
 * Created by song
 */
public class OkHttpBaseAPI {
    public static OkHttpBaseAPI instance;
    private final String TAG = OkHttpBaseAPI.class.getSimpleName();
    private  long requestEndTime;
    private long requestStartTime;
    public static OkHttpBaseAPI getInstance() {
        if (instance == null) {
            synchronized (OkHttpBaseAPI.class) {
                if (instance == null) {
                    instance = new OkHttpBaseAPI();
                }
            }
        }
        return instance;
    }
    public String getResponeStr(Response response, String url, String taskName) {
        String result = "";
        try {
            if (response != null) {
                int code = response.code();
                result = response.body().string();

                if (code == 200 || code == 201) {
                    return result;
                } else if (code == 401) {

                } else {
                }
            } else {
            }
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    public String httpGetTask(String url, String taskName) {
        String responeStr = "";
        String tokeType = null;
        String token = null;

        if (TextUtils.isEmpty(token)) {
        }
        try {
            Map<String, String> headers = new HashMap<>();
            //headers.put("Authorization", tokeType + token);
            headers.put("Accept", "application/json");
            headers.put("ContentType", "application/json; charset=utf-8");
            //headers.put("If-None-Match", "{E6AF2E17-2B74-4A2C-B874-10620B3730C3}&2015-02-13T06:17:10.7429968Z");
            requestStartTime = System.currentTimeMillis();
            Log.e("request-----start---" , requestStartTime + "");
            Response response = OkHttpUtils
                    .get()
                    .url(url)
                    .headers(headers)
                    .build()
                    .execute();
            BufferedSource bufferedSource = response.body().source();
            Timeout timeout = bufferedSource.timeout();
            timeout.deadline(20, TimeUnit.SECONDS);
            if (timeout.hasDeadline() ) {
                timeout.deadline(200, TimeUnit.MILLISECONDS);
            }
            long requestEndTime = System.currentTimeMillis();
            Log.e("request--------time---" , (requestEndTime - requestStartTime)/1000+"");

            responeStr = getResponeStr(response, url, taskName);
            long responseEndTime = System.currentTimeMillis();
            Log.e("response--------time---" , (responseEndTime - requestStartTime)/1000+responeStr);
        } catch (IOException e) {
            long timeout = System.currentTimeMillis();
            Log.e("timeout-----------" , (timeout - requestStartTime)/1000+"");
            e.printStackTrace();
        }
        return responeStr;
    }

    /**
     * @param url
     * @param postDataString
     * @param taskName
     * @return if return is "" means the task failed
     * @throws
     */
    public String httpPostTask(String url, String postDataString, String taskName) {
        String responeStr = "";
        url = url.replaceAll(" ", "%20");
        String token = null;

        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", token);
            headers.put("Accept", "application/json");
            headers.put("ContentType", "application/json; charset=utf-8");
            Response response;

            response = OkHttpUtils
                    .postString()
                    .url(url)
                    .content(postDataString)
                    .headers(headers)
                    .build()
                    .execute();

            responeStr = getResponeStr(response, url, taskName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responeStr;
    }


    /**
     * @param url
     * @param postDataString
     * @param taskName
     * @return if return is "" means the task failed
     */
    public String httpPutTask(String url, String postDataString, String taskName) {
        String responeStr = "";
        String token = null;
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", token);
            headers.put("Accept", "application/json");
            headers.put("ContentType", "application/json; charset=utf-8");
            Response response;

            response = OkHttpUtils
                    .put()
                    .url(url)
                    .requestBody(postDataString)
                    .headers(headers)
                    .build()
                    .execute();


            responeStr = getResponeStr(response, url, taskName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responeStr;
    }

    public String httpDeleteTask(String url, String postDataString, String taskName) {
        String responeStr = "";
        String token = null;
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", token);
            headers.put("Accept", "application/json");
            headers.put("ContentType", "application/json; charset=utf-8");
            Response response;

            response = OkHttpUtils
                    .delete()
                    .url(url)
                    .headers(headers)
                    .requestBody(postDataString)
                    .build()
                    .execute();


            responeStr = getResponeStr(response, url, taskName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responeStr;
    }

    public String httpPostImageTask(String postUrl, String taskName, String FilePath) {
        String responeStr = "";
//        String fileName =  Environment.getExternalStorageDirectory().getAbsolutePath() + "/Small/song.exe";
        File uploadFile = new File(FilePath);
        String token = null;

        try {

            Map<String, String> headers = new HashMap<>();
            //headers.put("Authorization", token);
            headers.put("Accept", "application/json");
            headers.put("ContentType", "image/jpeg");
            Response response;
            requestStartTime = System.currentTimeMillis();
            Log.e("request-----start---" , requestStartTime + "");
            response = OkHttpUtils
                    .postFile()
                    .url(postUrl)
                    .file(uploadFile)
                    .headers(headers)
                    .build()
                    .execute();
           // BufferedSource bufferedSource = response.body().source();
             //Timeout timeout = bufferedSource.timeout();
            //timeout.deadline(10, TimeUnit.SECONDS);
            //if (timeout.hasDeadline() ) {
               // timeout.deadline(50, TimeUnit.MILLISECONDS);
            //}
            requestEndTime = System.currentTimeMillis();
            Log.e("request--------time---" , (requestEndTime - requestStartTime)/1000+"");
            responeStr = getResponeStr(response, postUrl, taskName);
            long responseEndTime = System.currentTimeMillis();
            Log.e("response--------time---" , (responseEndTime - requestStartTime)/1000+"");
        } catch (IOException e) {
            long timeout = System.currentTimeMillis();
            Log.e("timeout-----------" , (timeout - requestStartTime)/1000+"");
            e.printStackTrace();
        }
        return responeStr;
    }


    public byte[] getInputStreamByHttpPostTask(String url, String postDataString, String taskName) {

        String token = null;
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", token);
        headers.put("Accept", "application/json");
        headers.put("ContentType", "application/json; charset=utf-8");
        Response response = null;

        try {
            response = OkHttpUtils
                    .postString()
                    .url(url)
                    .content(postDataString)
                    .headers(headers)
                    .build()
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        long startTime = System.currentTimeMillis();
        if (response == null) {
        } else {
            try {
                InputStream is = response.body().byteStream();

                ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len = -1;
                while ((len = is.read(buffer)) != -1) {
                    outSteam.write(buffer, 0, len);
                }
                outSteam.close();
                is.close();
                return outSteam.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return null;
    }

    public byte[] httpGetFileDataTaskNew(String url, String taskName) {

        String token = null;
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", token);
        headers.put("Accept", "application/json");
        headers.put("ContentType", "application/json; charset=utf-8");
        Response response = null;

        try {
            response = OkHttpUtils
                    .postString()
                    .url(url)
                    .headers(headers)
                    .build()
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        long startTime = System.currentTimeMillis();
        if (response == null) {
        } else {
            try {
                InputStream is = response.body().byteStream();

                ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len = -1;
                while ((len = is.read(buffer)) != -1) {
                    outSteam.write(buffer, 0, len);
                }
                outSteam.close();
                is.close();
                return outSteam.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return null;
    }

    public byte[] httpGetFileDataTask(String url, String taskName) {
        String token = null;
        OkHttpClient okHttpClient = OkHttpUtils.getInstance().getOkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                //.header("Authorization", token)
                .header("Accept", "application/json")
                .header("ContentType", "application/json")
                .build();
        InputStream is = null;

        byte[] data = null;
        long startTime = System.currentTimeMillis();
        try {
            Response response = okHttpClient.newCall(request).execute();
            if (response != null) {
                is = response.body().byteStream();
                int length = (int) response.body().contentLength();
                if (length > 0) {
                    data = new byte[length];
                    byte[] buffer = new byte[4098];
                    int readLen = 0;
                    int destPos = 0;
                    while ((readLen = is.read(buffer)) >= 0) {
                        if (readLen > 0) {
                            System.arraycopy(buffer, 0, data, destPos, readLen);
                            destPos += readLen;
                        } else {
                            Log.w(TAG, "");
                        }
                    }
                }
            } else {
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                    is = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return data;
    }

    public byte[] httpGetFileDataTask(String url, String taskName, OnDownloadListener downloadListener) {
        String token = null;
        OkHttpClient okHttpClient = OkHttpUtils.getInstance().getOkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                //.header("Authorization", token)
                .header("Accept", "application/json")
                .header("ContentType", "application/json")
                .build();
        InputStream is = null;

        byte[] data = null;

        long startTime = System.currentTimeMillis();
        try {
            Response response = okHttpClient.newCall(request).execute();
            if (response != null) {
                is = response.body().byteStream();
                //获取文件的总长度
                int length = (int) response.body().contentLength();
                if (length > 0) {
                    data = new byte[length];
                    byte[] buffer = new byte[4098];
                    int readLen = 0;
                    int destPos = 0;
                    while ((readLen = is.read(buffer)) >= 0) {
                        if (readLen > 0) {
                            System.arraycopy(buffer, 0, data, destPos, readLen);

                            destPos += readLen;
                            int progress = (int) (destPos * 1.0f / length * 100f);
                            downloadListener.onDownloading(progress);
                        } else {
                            Log.w(TAG, "");
                            downloadListener.onDownloadFailed();
                        }
                    }
                    //下载成功监听
                    downloadListener.onDownloadSucceed();
                }
            } else {
            }
        } catch (IOException e) {
            downloadListener.onDownloadFailed();
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                    is = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return data;
    }


}
