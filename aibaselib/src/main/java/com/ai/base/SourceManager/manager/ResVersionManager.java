package com.ai.base.SourceManager.manager;

import android.content.Context;
import android.content.ContextWrapper;

import com.ai.base.SourceManager.utils.MobileProperties;
import com.ai.base.okHttp.OkHttpBaseAPI;
import com.ai.base.util.SharedPrefHelper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by song on 2017/6/12.
 */

public class ResVersionManager {
    private static final String LOCAL_RES_VERSION = "LOCAL_RES_VERSION";
    public static int updateCount = 0;
    public static float filesSize = 0;//下载文件的总大小
    static Map<String, ?> remoteResVersions;
    static Map<String, Map<String, ?>> multipleRemoteResVersions = new HashMap();
    static Map<String, String> localResVersions;
    static Map<String, Map<String, String>> multipleLocalResVersions = new HashMap();
    private static String subStr = "\\|";
    public ResVersionManager() {
    }

    public static void setLocalResVersion(ContextWrapper context, String resPath, String resVersion) {
        getLocalResVersions(context).put(resPath, resVersion);
        if(MultipleManager.isMultiple()) {
            (new SharedPrefHelper(context)).put("LOCAL_RES_VERSION_" + MultipleManager.getCurrAppId(), resPath, resVersion);
        } else {
            (new SharedPrefHelper(context)).put("LOCAL_RES_VERSION", resPath, resVersion);
        }

    }

    public static String getLocalResVersion(ContextWrapper context, String resPath) {
        return (String)getLocalResVersions(context).get(resPath);
    }

    public static void removeLocalResVersion(ContextWrapper context, String resPath) {
        getLocalResVersions(context).remove(resPath);
        if(MultipleManager.isMultiple()) {
            (new SharedPrefHelper(context)).remove("LOCAL_RES_VERSION_" + MultipleManager.getCurrAppId(), resPath);
        } else {
            (new SharedPrefHelper(context)).remove("LOCAL_RES_VERSION", resPath);
        }

    }

    public static Map<String, String> getLocalResVersions(ContextWrapper context) {
        if(MultipleManager.isMultiple()) {
            Object subAppLocalResVersionsMap = (Map)multipleLocalResVersions.get(MultipleManager.getCurrAppId());
            if(null == subAppLocalResVersionsMap) {
                try {
                    subAppLocalResVersionsMap = (new SharedPrefHelper(context)).getAll("LOCAL_RES_VERSION_" + MultipleManager.getCurrAppId());
                } catch (Exception var3) {
                    ;
                }

                if(subAppLocalResVersionsMap == null) {
                    subAppLocalResVersionsMap = new HashMap();
                }

                multipleLocalResVersions.put(MultipleManager.getCurrAppId(), (Map<String, String>) subAppLocalResVersionsMap);
            }

            return (Map)subAppLocalResVersionsMap;
        } else {
            if(localResVersions == null) {
                localResVersions = (Map<String, String>) (new SharedPrefHelper(context)).getAll("LOCAL_RES_VERSION");
            }

            return localResVersions;
        }
    }

    public static boolean isUpdateResource(ContextWrapper context, Map<String, ?> remoteResVersions) throws Exception {
        filesSize = 0;
        updateCount = 0;
        Map localResVersions;
        Object var15;
        if(MultipleManager.isMultiple()) {
            localResVersions = getLocalResVersions(context);
            Map remoteResVersios = getRemoteResVersions();
            HashSet tempSet = new HashSet();//需要充下载列表中剔除的列表
            Iterator remoteIterator = remoteResVersios.keySet().iterator();
            while(remoteIterator.hasNext()) {
                String key = (String)remoteIterator.next();
                if (localResVersions.containsKey(key)){
                    if (remoteResVersions.get(key).equals(localResVersions.get(key))){
                        tempSet.add(key);
                    }
                }
            }

            Iterator tempSetIterator = tempSet.iterator();

            while(tempSetIterator.hasNext()) {
                remoteResVersions.remove((String)tempSetIterator.next());
            }
            if (remoteResVersions != null && remoteResVersions.size() >0){
                updateCount = remoteResVersions.size();
            }
            return updateCount > 0;
        } else {
            localResVersions = getLocalResVersions(context);
            HashSet tempSet = new HashSet();//需要充下载列表中剔除的列表
            Iterator remoteIterator = remoteResVersions.keySet().iterator();
            float fileSize;
            while(remoteIterator.hasNext()) {
                String key = (String)remoteIterator.next();
                String remoteValue = (String) remoteResVersions.get(key);
                if (remoteValue.contains("|")){
                    fileSize = Float.parseFloat(remoteValue.split(subStr)[1]);
                    filesSize = filesSize + fileSize;
                }
                if (localResVersions.containsKey(key)){
                    if (remoteValue.equals(localResVersions.get(key))){
                        tempSet.add(key);
                    }
                }
            }
            //删除不需要更新的key
            Iterator tempSetIterator = tempSet.iterator();
            while(tempSetIterator.hasNext()) {
                String key = (String)tempSetIterator.next();
                String remoteValue = (String) remoteResVersions.get(key);
                if (remoteValue.contains("|")){
                    fileSize = Float.parseFloat(remoteValue.split(subStr)[1]);
                    filesSize = filesSize - fileSize;
                }
                remoteResVersions.remove(key);
            }
            if (remoteResVersions != null && remoteResVersions.size() >0){
                updateCount = remoteResVersions.size();
            }
            return updateCount > 0;
        }
    }
    //
    public static Map<String, ?> getRemoteResVersions() throws Exception {

        // TODO: 2017/6/12  从服务器获取版本文件。并解析保存到map中
        InputStream in = null;

        MobileProperties pro = new MobileProperties(in);
        if(MultipleManager.isMultiple()) {
            Map subAppRemoteResVersionsMap = pro.getProMap();
            ResVersionManager.multipleRemoteResVersions.put(MultipleManager.getCurrAppId(), subAppRemoteResVersionsMap);
        } else {
            ResVersionManager.remoteResVersions = pro.getProMap();
        }
        return MultipleManager.isMultiple()?(Map)multipleRemoteResVersions.get(MultipleManager.getCurrAppId()):remoteResVersions;
    }

    /**
     *
     * @param fromServer 版本文件是否来自服务器
     * @param baseAddress 版本文件的服务器下载地址，如果是本地则设置为null
     * @return
     * @throws Exception
     */
    public static Map<String, ?> getRemoteResVersions(Context context,String baseAddress,boolean fromServer) throws Exception {

        // TODO: 2017/6/12  从服务器获取版本文件。并解析保存到map中
        InputStream in = null;
        // TODO: 2017/6/12 测试文件
        if (fromServer){
            OkHttpBaseAPI okHttpBaseAPI = new OkHttpBaseAPI();
            byte[] data = okHttpBaseAPI.httpGetFileDataTask(baseAddress +"/res.version.properties", "download web view resource");
            if (data == null) return null;
            in = new ByteArrayInputStream(data);
        }else {
            in = context.getResources().getAssets().open("res.version.properties");
        }
        MobileProperties pro = new MobileProperties(in);
        if(MultipleManager.isMultiple()) {
            Map subAppRemoteResVersionsMap = pro.getProMap();
            ResVersionManager.multipleRemoteResVersions.put(MultipleManager.getCurrAppId(), subAppRemoteResVersionsMap);
        } else {
            ResVersionManager.remoteResVersions = pro.getProMap();
        }
        return MultipleManager.isMultiple()?(Map)multipleRemoteResVersions.get(MultipleManager.getCurrAppId()):remoteResVersions;
    }

    public static String getRemoteResVersion(String resPath) throws Exception {
        if(MultipleManager.isMultiple()) {
            Map subAppRemoteResVersionMap = (Map)multipleRemoteResVersions.get(MultipleManager.getCurrAppId());
            return (String)subAppRemoteResVersionMap.get(resPath);
        } else {
            return (String)(remoteResVersions.get(resPath) == null?"":remoteResVersions.get(resPath));
        }
    }
}
