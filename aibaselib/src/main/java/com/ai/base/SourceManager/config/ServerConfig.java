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

public class ServerConfig extends AbstractConfig {
    private static ServerConfig config;
    private final String XML_FILE_PATH = "template/server-config.xml";
    private final String CONFIG_PATH = "CONFIGS/CONFIG";
    private final String ATTR_NAME = "name";
    private final String ATTR_VALUE = "value";
    private final String ATTR_VISIBLE = "visible";

    private ServerConfig() {
    }

    public static ServerConfig getInstance() {
        Class var0;
        if (MultipleManager.isMultiple()) {
            if (MultipleManager.getCurrServerConfig() != null) {
                return MultipleManager.getCurrServerConfig();
            } else {
                var0 = ServerConfig.class;
                synchronized (ServerConfig.class) {
                    MultipleAppConfig appConfig = MultipleManager.getCurrAppConfig();
                    appConfig.setServerConfig(new ServerConfig());
                }

                return MultipleManager.getCurrServerConfig();
            }
        } else {
            if (config == null) {
                var0 = ServerConfig.class;
                synchronized (ServerConfig.class) {
                    if (config == null) {
                        config = new ServerConfig();
                    }
                }
            }

            return config;
        }
    }

    protected Map<String, ?> loadConfig() throws Exception {
//        MobileXML xml = new MobileXML(FileUtil.connectFilePath(TemplateManager.getBasePath(), "template/server-config.xml"), true);
//        Map actions = this.transActions((List) xml.getConfig().get("CONFIGS/CONFIG"));
//        MobileCache.getInstance().put("SERVER_CONFIG", actions);
        return null;
    }

    private Map<String, String> transActions(List<Map<String, String>> list) {
        HashMap actions = new HashMap();
        Iterator i$ = list.iterator();

        while (i$.hasNext()) {
            Map action = (Map) i$.next();
            actions.put(((String) action.get("name")).toString(), ((String) action.get("value")).toString());
        }

        return actions;
    }

    private Map<String, String> transVisibleActions(List<Map<String, String>> list) {
        HashMap actions = new HashMap();
        Iterator i$ = list.iterator();

        while (i$.hasNext()) {
            Map action = (Map) i$.next();
            if ("true".equals(action.get("visible"))) {
                actions.put(((String) action.get("name")).toString(), ((String) action.get("value")).toString());
            }
        }

        return actions;
    }

    public String getValue(String action) throws Exception {
        return this.getConfigMap().get(action) == null ? null : this.getConfigMap().get(action).toString();
    }
}