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
        Map localResVersions;
        Object var15;
        if(MultipleManager.isMultiple()) {
            localResVersions = getLocalResVersions(context);
            Map var10 = getRemoteResVersions();
            Iterator var12 = localResVersions.keySet().iterator();
            HashSet var14 = new HashSet();

            while(var12.hasNext()) {
                String var11 = (String)var12.next();
                if(!var10.containsKey(var11)) {
                    var14.add(var11);
                }
            }

            Iterator var16 = var14.iterator();

            while(var16.hasNext()) {
                String var17 = (String)var16.next();
                removeLocalResVersion(context, var17);
            }

            updateCount = 0;
            Iterator it1 = var10.keySet().iterator();
            float fileSize;
            String value;
            while(it1.hasNext()) {
                var15 = it1.next();
                if(!localResVersions.containsKey(var15)) {
                    value = String.valueOf(remoteResVersions.get(var15));
                    if (value.contains("|")){
                        fileSize = Float.parseFloat(value.split(subStr)[1]);
                        filesSize = filesSize + fileSize;
                    }
                    ++updateCount;
                } else {
                    Object var18 = var10.get(var15);
                    if(!var18.equals(localResVersions.get(var15))) {
                        ++updateCount;
                    }
                }
            }

            return updateCount > 0;
        } else {
            localResVersions = getLocalResVersions(context);
            Iterator itLocal = localResVersions.keySet().iterator();
            HashSet itLocalDel = new HashSet();

            while(itLocal.hasNext()) {
                String keyLocal = (String)itLocal.next();
                if(!remoteResVersions.containsKey(keyLocal)) {
                    itLocalDel.add(keyLocal);
                }
            }

            Iterator key = itLocalDel.iterator();

            while(key.hasNext()) {
                String value = (String)key.next();
                removeLocalResVersion(context, value);
            }

            updateCount = 0;
            Iterator it = remoteResVersions.keySet().iterator();
            float fileSize;
            String value;
            while(it.hasNext()) {
                Object var13 = it.next();
                if(!localResVersions.containsKey(var13)) {
                    value = String.valueOf(remoteResVersions.get(var13));
                    if (value.contains("|")){
                        fileSize = Float.parseFloat(value.split(subStr)[1]);
                        filesSize = filesSize + fileSize;
                    }
                    ++updateCount;
                } else {
                    var15 = remoteResVersions.get(var13);
                    if(!var15.equals(localResVersions.get(var13))) {
                        ++ updateCount;
                    }
                }
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

    public static Map<String, ?> getRemoteResVersions(Context context,String baseAddress) throws Exception {

        // TODO: 2017/6/12  从服务器获取版本文件。并解析保存到map中
        InputStream in = null;
        // TODO: 2017/6/12 测试文件
        //in = context.getResources().getAssets().open("res.version.properties");
        OkHttpBaseAPI okHttpBaseAPI = new OkHttpBaseAPI();
        byte[] data = okHttpBaseAPI.httpGetFileDataTask(baseAddress +"/res.version.properties", "download web view resource");
        if (data == null) return null;
        in = new ByteArrayInputStream(data);
        MobileProperties pro = new MobileProperties(in);
        data = null;
        in = null;
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
