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

public class ServerPageConfig extends AbstractConfig {
    private static final String XML_FILE_PATH = "template/server-page.xml";
    private static final String PAGE_PATH = "PAGES/ACTION";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_TEMPLATE = "template";
    private static final String ATTR_DATA = "data";
    private static ServerPageConfig config;

    private ServerPageConfig() {
    }

    public static ServerPageConfig getInstance() {
        Class var0;
        if(MultipleManager.isMultiple()) {
            if(MultipleManager.getCurrServerPageConfig() != null) {
                return MultipleManager.getCurrServerPageConfig();
            } else {
                var0 = ServerConfig.class;
                synchronized(ServerConfig.class) {
                    MultipleAppConfig appConfig = MultipleManager.getCurrAppConfig();
                    appConfig.setServerPageConfig(new ServerPageConfig());
                }

                return MultipleManager.getCurrServerPageConfig();
            }
        } else {
            if(config == null) {
                var0 = ServerPageConfig.class;
                synchronized(ServerPageConfig.class) {
                    if(config == null) {
                        config = new ServerPageConfig();
                    }
                }
            }

            return config;
        }
    }

    protected Map<String, ?> loadConfig() throws Exception {
//        MobileXML xml = new MobileXML(FileUtil.connectFilePath(TemplateManager.getBasePath(), "template/server-page.xml"), true);
//        Map actions = this.transActions((List)xml.getConfig().get("PAGES/ACTION"));
//        MobileCache.getInstance().put("SERVER_PAGE_CONFIG", actions);
        return null;
    }

    protected Map<String, Map> getConfigMap() throws Exception {
        return (Map<String, Map>) super.getConfigMap();
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

    public static String getTemplate(String action) throws Exception {
        Map actionMap = (Map)getInstance().getConfigMap().get(action);
        return actionMap != null?actionMap.get("template").toString():null;
    }

    public static String getData(String action) throws Exception {
        Map actionMap = (Map)getInstance().getConfigMap().get(action);
        return actionMap != null?(actionMap.get("data") == null?null:actionMap.get("data").toString()):null;
    }
}
