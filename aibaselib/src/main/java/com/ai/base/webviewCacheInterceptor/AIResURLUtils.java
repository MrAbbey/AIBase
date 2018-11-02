package com.ai.base.webviewCacheInterceptor;

import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;


public class AIResURLUtils {

    // 解析的范例:
    // https://plan.wadecn.com/static/js/1.691bfa0f04239df1f079.js
    // https://plan.wadecn.com/static/js/1.js

    public static Map getResURLFieldsFromUrl(String url) {
        if (!TextUtils.isEmpty(url)) {
            int fragment = url.lastIndexOf('#');
            if (fragment > 0) {
                url = url.substring(0, fragment);
            }

            int query = url.lastIndexOf('?');
            if (query > 0) {
                url = url.substring(0, query);
            }

            int filenamePos = url.lastIndexOf('/');
            String urlString = url.substring(0,filenamePos);
            String filename = 0 <= filenamePos ? url.substring(filenamePos + 1) : url;

            if (!filename.isEmpty()) {
                int dotPos = filename.lastIndexOf('.');
                String extension = "";
                if (0 <= dotPos) {
                    extension = filename.substring(dotPos+1);
                }
                dotPos = filename.indexOf('.');
                String resName = "";
                if (0 <= dotPos) {
                    resName = filename.substring(0, dotPos);
                }

                Map<String, String> temp = new HashMap();

                temp.put("filename", resName + "." + extension);
                temp.put("extension", extension);
                temp.put("url", urlString + "/" + resName + "." + extension);

                return temp;
            }
        }

        return null;
    }

    public static String getURLToFileNameFromUrl(String url) {
        if (!TextUtils.isEmpty(url)) {
            int fragment = url.lastIndexOf('#');
            if (fragment > 0) {
                url = url.substring(0, fragment);
            }

            int query = url.lastIndexOf('?');
            if (query > 0) {
                url = url.substring(0, query);
            }

            int filenamePos = url.lastIndexOf('/');
            String urlString = url.substring(0,filenamePos);
            return urlString;
        }

        return "";
    }

    public static String getFileExtensionFromUrl(String url) {
        String fileName = getFileNameFromUrl(url);

        if (!fileName.isEmpty()) {
            int dotPos = fileName.lastIndexOf('.');
            if (0 <= dotPos) {
                return fileName.substring(dotPos + 1);
            }
        }

        return "";
    }

    public static String getFileNameFromUrl(String url) {
        if (!TextUtils.isEmpty(url)) {
            int fragment = url.lastIndexOf('#');
            if (fragment > 0) {
                url = url.substring(0, fragment);
            }

            int query = url.lastIndexOf('?');
            if (query > 0) {
                url = url.substring(0, query);
            }

            int filenamePos = url.lastIndexOf('/');
            String filename = 0 <= filenamePos ? url.substring(filenamePos + 1) : url;

            return filename;
        }

        return "";
    }

    public static String getMimeTypeFromUrl(String url) {
        return  MimeTypeMap.getSingleton().getMimeTypeFromExtension(getFileExtensionFromUrl(url));
    }

    public static String getMimeTypeFromExtension(String extension) {
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    public static  String getOriginUrl(String referer) {
        String ou = referer;
        if (TextUtils.isEmpty(ou)) {
            return "";
        }
        try {
            URL url = new URL(ou);
            int port = url.getPort();
            ou = url.getProtocol() + "://" + url.getHost() + (port == -1 ? "" : ":" + port);
        } catch (Exception e) {
        }
        return ou;
    }
}
