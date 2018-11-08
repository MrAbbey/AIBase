package com.ai.webplugin.config;

import com.ai.base.util.XMLParser;
import com.ai.base.util.Utility;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by wuyoujian on 17/5/1.
 */

public class ApkPluginCfg extends AbstractCfg {

    public static final String CONFIG_FIND_PATH = "apk";
    public static final String CONFIG_ATTR_NAME = "name";
    public static final String CONFIG_ATTR_PACKAGENAME = "packageName";
    public static final String CONFIG_ATTR_CLASS = "activityClass";

    private static ApkPluginCfg instance;

    /**
     * get instance
     * @return DataCfg
     * @throws Exception
     */
    public static ApkPluginCfg getInstance() {
        if (instance == null) {
            synchronized (ApkPluginCfg.class) {
                instance = new ApkPluginCfg();
            }
        }
        return instance;
    }

    /**
     * load config
     * @return IData
     */
    @Override
    protected HashMap<String,Object> loadConfig() {
        super.loadConfig();
        HashMap<String,Object> config = new HashMap();
        try {
            HashSet hashSet;
            if (this.stream != null) {
                hashSet = XMLParser.loadXML(stream, CONFIG_FIND_PATH);
            } else {
                hashSet = XMLParser.loadXML(fileName, CONFIG_FIND_PATH);
            }

            for (int i=0; i<hashSet.size(); i++) {
                HashMap<String,String> data = (HashMap)hashSet.toArray()[i];
                String name = data.get(CONFIG_ATTR_NAME);
                String packageName = data.get(CONFIG_ATTR_PACKAGENAME);
                String clsname = data.get(CONFIG_ATTR_CLASS);
                if (name == null || "".equals(name)) {
                    Utility.error(CONFIG_ATTR_NAME + " not nullable, [" + fileName + "]");
                }
                if (clsname == null || "".equals(clsname)) {
                    Utility.error(CONFIG_ATTR_CLASS + " not nullable, [" + fileName + ":" + CONFIG_ATTR_NAME + "=" + name + "]");
                }
                if (packageName == null || "".equals(packageName)) {
                    Utility.error(CONFIG_ATTR_PACKAGENAME + " not nullable, [" + fileName + ":" + CONFIG_ATTR_PACKAGENAME + "=" + packageName + "]");
                }
                if (config.containsKey(name)) {
                    Utility.error(name + " already, [" + fileName + "]");
                }
                config.put(name, data);
            }

        } catch (Exception e) {
            Utility.error(e);
        }
        return config;
    }

    /**
     * get
     * @param name
     * @return IData
     */
    public HashMap<String,String> get(String name) {
        HashMap<String,String> data = (HashMap<String,String>)cache.get(name);
        if (data == null) Utility.error(name + " not exist, [" + fileName + "]");
        return data;
    }

    /**
     * attr
     * @param name
     * @param attr
     * @return String
     */
    public String attr(String name, String attr) {
        HashMap<String,String> data = get(name);
        return data.get(attr);
    }
}
