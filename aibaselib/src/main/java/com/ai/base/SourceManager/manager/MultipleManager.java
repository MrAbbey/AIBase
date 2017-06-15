package com.ai.base.SourceManager.manager;

import com.ai.base.SourceManager.Multiple.MultipleAppConfig;
import com.ai.base.SourceManager.config.ServerConfig;
import com.ai.base.SourceManager.config.ServerDataConfig;
import com.ai.base.SourceManager.config.ServerPageConfig;

import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by song on 2017/6/12.
 */

public class MultipleManager {
    private static Map<String, MultipleAppConfig> multipleAppConfigs = new HashMap();
    private static String currAppId;
    private static boolean isMultiple;
    private static String multBasePath;
    public MultipleManager() {
    }

    public static void setMultBasePath(String path) {
        multBasePath = path;
    }

    public static String getMultBasePath() {
        return multBasePath;
    }

    public static void putAppConfig(String appId, MultipleAppConfig config) {
        multipleAppConfigs.put(appId, config);
    }

    public static MultipleAppConfig getAppConfig(String appId) {
        return (MultipleAppConfig)multipleAppConfigs.get(appId);
    }

    public static String getCurrAppId() {
        return currAppId;
    }

    public static void setCurrAppId(String currAppId) {
        MultipleManager.currAppId = currAppId;
    }

    public static boolean isMultiple() {
        return isMultiple;
    }

    public static void setMultiple(boolean isMultiple) {
        MultipleManager.isMultiple = isMultiple;
    }

    public static MultipleAppConfig getCurrAppConfig() {
        return (MultipleAppConfig)multipleAppConfigs.get(currAppId);
    }

    public static String getCurrRequestHost() {
        return ((MultipleAppConfig)multipleAppConfigs.get(currAppId)).getRequestHost();
    }

    public static String getCurrRequestPath() {
        return ((MultipleAppConfig)multipleAppConfigs.get(currAppId)).getRequestPath();
    }

    public static String getCurrRequestServlet() {
        return ((MultipleAppConfig)multipleAppConfigs.get(currAppId)).getRequestServlet();
    }

    public static String getCurrAppPath() {
        return ((MultipleAppConfig)multipleAppConfigs.get(currAppId)).getAppPath();
    }

    public static ServerPageConfig getCurrServerPageConfig() {
        return ((MultipleAppConfig)multipleAppConfigs.get(currAppId)).getServerPageConfig();
    }

    public static ServerDataConfig getCurrServerDataConfig() {
        return ((MultipleAppConfig)multipleAppConfigs.get(currAppId)).getServerDataConfig();
    }

    public static ServerConfig getCurrServerConfig() {
        return ((MultipleAppConfig)multipleAppConfigs.get(currAppId)).getServerConfig();
    }

    public static RSAPublicKey getCurrPublicKey() {
        return ((MultipleAppConfig)multipleAppConfigs.get(currAppId)).getPublicKey();
    }
}
