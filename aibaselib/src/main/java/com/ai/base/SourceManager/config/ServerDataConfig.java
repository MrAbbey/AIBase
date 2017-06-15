package com.ai.base.SourceManager.config;

import com.ai.base.SourceManager.Multiple.MultipleAppConfig;
import com.ai.base.SourceManager.manager.MultipleManager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by song on 2017/6/12.
 */

public class ServerDataConfig extends AbstractConfig {
    private static final String XML_FILE_PATH = "template/server-data.xml";
    private static final String ACTION_PATH = "DATAS/ACTION";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_CLASS = "class";
    private static final String ATTR_METHOD = "method";
    private static final String ATTR_ENCRYPT = "encrypt";
    private static ServerDataConfig config;

    private ServerDataConfig() {
    }

    public static ServerDataConfig getInstance() {
        Class var0;
        if(MultipleManager.isMultiple()) {
            if(MultipleManager.getCurrServerDataConfig() != null) {
                return MultipleManager.getCurrServerDataConfig();
            } else {
                var0 = ServerConfig.class;
                synchronized(ServerConfig.class) {
                    MultipleAppConfig appConfig = MultipleManager.getCurrAppConfig();
                    appConfig.setServerDataConfig(new ServerDataConfig());
                }

                return MultipleManager.getCurrServerDataConfig();
            }
        } else {
            if(config == null) {
                var0 = ServerDataConfig.class;
                synchronized(ServerDataConfig.class) {
                    if(config == null) {
                        config = new ServerDataConfig();
                    }
                }
            }

            return config;
        }
    }

    protected Map<String, ?> loadConfig() throws Exception {
//        MobileXML xml = new MobileXML(FileUtil.connectFilePath(TemplateManager.getBasePath(), "template/server-data.xml"), true);
//        Map actions = this.transActions((List)xml.getConfig().get("DATAS/ACTION"));
//        MobileCache.getInstance().put("SERVER_DATA_CONFIG", actions);
        return null;
    }

    private Map transActions(List<Map> list) {
        HashMap actions = new HashMap();
        Iterator i$ = list.iterator();

        while(i$.hasNext()) {
            Map action = (Map)i$.next();
            actions.put(action.get("name").toString(), action);
        }

        return actions;
    }

    protected Map<String, Map> getConfigMap() throws Exception {
        return (Map<String, Map>) super.getConfigMap();
    }

    public static Boolean isEncrypt(String dataAction) throws Exception {
        Map actionMap = (Map)getInstance().getConfigMap().get(dataAction);
        if(actionMap != null) {
            Object encrypt = actionMap.get("encrypt");
            return Boolean.valueOf("true".equals(encrypt));
        } else {
            return Boolean.valueOf(false);
        }
    }

    public static String getActionClass(String action) throws Exception {
        Map actionMap = (Map)getInstance().getConfigMap().get(action);
        return actionMap != null?actionMap.get("class").toString():null;
    }

    public static String getActionMethod(String action) throws Exception {
        Map actionMap = (Map)getInstance().getConfigMap().get(action);
        return actionMap != null?actionMap.get("method").toString():null;
    }
}
