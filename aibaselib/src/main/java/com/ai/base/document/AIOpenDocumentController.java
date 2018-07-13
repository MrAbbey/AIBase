package com.ai.base.document;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import com.ai.base.SourceManager.app.MobileAppInfo;
import com.ai.base.okHttp.OkHttpBaseAPI;
import com.ai.base.util.FileUtilCommon;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class AIOpenDocumentController {

    private static AIOpenDocumentController instance;
    public static AIOpenDocumentController getInstance() {
        if (instance == null) {
            synchronized (AIOpenDocumentController.class) {
                instance = new AIOpenDocumentController();
            }
        }
        return instance;
    }

    public void openInContext(Activity context, String filePath, String fileProvider) {
        try {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setAction(Intent.ACTION_VIEW);

            File file = new File(filePath);
            if (!file.exists()) {
                Toast.makeText(context,"未下载的文件不存在！",Toast.LENGTH_SHORT).show();
                return;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri uri = FileProvider.getUriForFile(context, fileProvider, file);
                intent.setData(uri);
            } else {
                Uri uri = Uri.fromFile(file);
                intent.setData(uri);
            }

            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "sorry不能打开，请下载相关软件！", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 从网络Url中下载文件
     *
     * @param urlStr
     * @param fileName
     * @param savePath
     * @throws IOException
     */
    private void downLoadFromUrl(String urlStr, String fileName, String savePath) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        //设置超时间为3秒
        conn.setConnectTimeout(3 * 1000);
        //防止屏蔽程序抓取而返回403错误
        conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        //得到输入流
        InputStream inputStream = conn.getInputStream();
        //获取数组

        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while ((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }

        byte[] getData = bos.toByteArray();
        bos.close();

        //文件保存位置
        File saveDir = new File(savePath);
        if (!saveDir.exists()) {
            saveDir.mkdir();
        }
        File file = new File(saveDir + File.separator + fileName);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(getData);
        if (fos != null) {
            fos.close();
        }
        if (inputStream != null) {
            inputStream.close();
        }
    }

    public void openOnlineFileInContext(final Activity context, final String url, final String fileProvider) {

        try {
            // 下载文件到本地
            String tempName = url.substring(url.lastIndexOf(File.separatorChar) + 1);
            final String fileName = URLDecoder.decode(tempName,"utf-8");
            final String savePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        downLoadFromUrl(url,fileName,savePath);
                    } catch (IOException e) {
                        Toast.makeText(context,"下载文件失败，请重试！",Toast.LENGTH_SHORT).show();
                    }

                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            openInContext(context,savePath+File.separator + fileName,fileProvider);
                        }
                    });

                }
            }).start();
        } catch (Exception e) {

        }

    }
}
