package com.ai.base.SourceManager.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Created by baggio on 2017/6/12.
 */

public class FileUtil {
    public static final String[][] MIME_MapTable = new String[][]{{"", "*/*"}, {".3gp", "video/3gpp"}, {".apk", "application/vnd.android.package-archive"}, {".asf", "video/x-ms-asf"}, {".avi", "video/x-msvideo"}, {".bin", "application/octet-stream"}, {".bmp", "image/bmp"}, {".c", "text/plain"}, {".class", "application/octet-stream"}, {".conf", "text/plain"}, {".cpp", "text/plain"}, {".doc", "application/msword"}, {".exe", "application/octet-stream"}, {".gif", "image/gif"}, {".gtar", "application/x-gtar"}, {".gz", "application/x-gzip"}, {".h", "text/plain"}, {".htm", "text/html"}, {".html", "text/html"}, {".jar", "application/java-archive"}, {".java", "text/plain"}, {".jpeg", "image/jpeg"}, {".jpg", "image/jpeg"}, {".js", "application/x-javascript"}, {".log", "text/plain"}, {".m3u", "audio/x-mpegurl"}, {".m4a", "audio/mp4a-latm"}, {".m4b", "audio/mp4a-latm"}, {".m4p", "audio/mp4a-latm"}, {".m4u", "video/vnd.mpegurl"}, {".m4v", "video/x-m4v"}, {".mov", "video/quicktime"}, {".mp2", "audio/x-mpeg"}, {".mp3", "audio/x-mpeg"}, {".mp4", "video/mp4"}, {".mpc", "application/vnd.mpohun.certificate"}, {".mpe", "video/mpeg"}, {".mpeg", "video/mpeg"}, {".mpg", "video/mpeg"}, {".mpg4", "video/mp4"}, {".mpga", "audio/mpeg"}, {".msg", "application/vnd.ms-outlook"}, {".ogg", "audio/ogg"}, {".pdf", "application/pdf"}, {".png", "image/png"}, {".pps", "application/vnd.ms-powerpoint"}, {".ppt", "application/vnd.ms-powerpoint"}, {".prop", "text/plain"}, {".rar", "application/x-rar-compressed"}, {".rc", "text/plain"}, {".rmvb", "audio/x-pn-realaudio"}, {".rtf", "application/rtf"}, {".sh", "text/plain"}, {".tar", "application/x-tar"}, {".tgz", "application/x-compressed"}, {".txt", "text/plain"}, {".wav", "audio/x-wav"}, {".wma", "audio/x-ms-wma"}, {".wmv", "audio/x-ms-wmv"}, {".wps", "application/vnd.ms-works"}, {".xml", "text/plain"}, {".z", "application/x-compress"}, {".zip", "application/zip"}};

    public FileUtil() {
    }

    public static void writeFile(String content, OutputStream out) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(content.getBytes());
        writeFile((InputStream)in, (OutputStream)out);
    }

    public static void writeFile(String content, String filePath) throws Exception {
        writeFile(content, filePath, false);
    }

    public static void writeFile(InputStream in, String filePath) throws Exception {
        writeFile(in, filePath, false);
    }

    public static void writeFile(String content, String filePath, boolean append) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(content.getBytes());
        writeFile((InputStream)in, filePath, append);
    }

    public static void writeFile(InputStream in, String filePath, boolean append) throws Exception {
        FileOutputStream out = new FileOutputStream(filePath, append);
        writeFile((InputStream)in, (OutputStream)out);
    }

    public static void writeFile(InputStream in, OutputStream out) throws Exception {
        BufferedInputStream buffIn = null;
        BufferedOutputStream buffOut = null;

        try {
            buffIn = new BufferedInputStream(in);
            buffOut = new BufferedOutputStream(out);
            byte[] bytes = new byte[8192];

            int c;
            while((c = buffIn.read(bytes)) != -1) {
                buffOut.write(bytes, 0, c);
            }
        } finally {
            if(buffIn != null) {
                buffIn.close();
            }

            if(buffOut != null) {
                buffOut.close();
            }

        }

    }

    public static void writeFile(InputStreamReader reader, OutputStreamWriter writer) throws Exception {
        BufferedReader buffIn = null;
        BufferedWriter buffOut = null;

        try {
            buffIn = new BufferedReader(reader);
            buffOut = new BufferedWriter(writer);

            String s;
            while((s = buffIn.readLine()) != null) {
                buffOut.write(s);
            }
        } finally {
            if(buffIn != null) {
                buffIn.close();
            }

            if(buffOut != null) {
                buffOut.close();
            }

        }

    }

    public static String readFile(String fileName) throws Exception {
        FileInputStream in = new FileInputStream(fileName);
        return readFile((InputStream)in);
    }

    public static String readFile(InputStream in) throws Exception {
        InputStreamReader reader = new InputStreamReader(in);
        return readFile(reader);
    }

    public static String readFile(InputStreamReader reader) throws Exception {
        BufferedReader buffIn = null;

        String var4;
        try {
            buffIn = new BufferedReader(reader);
            StringBuilder buff = new StringBuilder();

            String s;
            while((s = buffIn.readLine()) != null) {
                buff.append(s);
                buff.append(Constant.LINE_SEPARATOR);
            }

            if(buff.length() >= 1) {
                buff.setLength(buff.length() - 1);
                var4 = buff.toString();
                return var4;
            }

            var4 = "";
        } finally {
            if(buffIn != null) {
                buffIn.close();
            }

        }

        return var4;
    }

    public static boolean check(String path) {
        File file = new File(path);
        return file.exists() && file.isDirectory();
    }

    public static boolean createDir(String path) {
        File file = new File(path);
        return file.exists() && file.isDirectory()?true:file.mkdirs();
    }

    public static boolean deleteFile(String path) {
        File file = new File(path);
        return file != null && file.exists()?file.delete():false;
    }

    public static boolean deleteFolder(String path) {
        File folder = new File(path);
        String[] childs = folder.list();
        if(childs != null && childs.length > 0) {
            for(int i = 0; i < childs.length; ++i) {
                boolean bo = deleteFolder(childs[i]);
                if(!bo) {
                    return false;
                }
            }
        }

        return folder.delete();
    }

    public static void openFile(Context context, File f) {
        String type = getMIMEType(f);
        openFile(context, f, type);
    }

    public static void openFile(Context context, File f, String type) {
        Intent intent = new Intent();
        intent.addFlags(268435456);
        intent.setAction("android.intent.action.VIEW");
        intent.setDataAndType(Uri.fromFile(f), type);
        context.startActivity(intent);
    }

    private static String getMIMEType(File file) {
        String type = "*/*";
        String fName = file.getName();
        int dotIndex = fName.lastIndexOf(".");
        if(dotIndex < 0) {
            return type;
        } else {
            String end = fName.substring(dotIndex, fName.length()).toLowerCase();
            if(end == "") {
                return type;
            } else {
                for(int i = 0; i < MIME_MapTable.length; ++i) {
                    if(end.equals(MIME_MapTable[i][0])) {
                        type = MIME_MapTable[i][1];
                    }
                }

                return type;
            }
        }
    }

    public static String connectFilePath(String front, String rear) {
        String result;
        if(front.endsWith(Constant.FILE_SEPARATOR)) {
            if(rear.startsWith(Constant.FILE_SEPARATOR)) {
                result = front + rear.substring(1);
            } else {
                result = front + rear;
            }
        } else if(rear.startsWith(Constant.FILE_SEPARATOR)) {
            result = front + rear;
        } else {
            result = front + Constant.FILE_SEPARATOR + rear;
        }

        return result;
    }

    public static String connectFilePath(String... paths) {
        if(paths.length < 2) {
            return paths[0];
        } else {
            StringBuilder result = new StringBuilder();
            String front = paths[0];
            result.append(front);

            for(int i = 1; i < paths.length; ++i) {
                String rear = paths[i];
                if(front.endsWith(File.separator)) {
                    if(rear.startsWith(File.separator)) {
                        result.append(rear.replace(File.separator, ""));
                    } else {
                        result.append(rear);
                    }
                } else if(rear.startsWith(File.separator)) {
                    result.append(rear);
                } else {
                    result.append(File.separator).append(rear);
                }

                front = result.toString();
            }

            return result.toString();
        }
    }
}
