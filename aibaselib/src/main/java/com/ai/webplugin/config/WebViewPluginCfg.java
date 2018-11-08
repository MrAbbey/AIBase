package com.ai.webplugin.config;

import com.ai.base.util.Parser;
import com.ai.base.util.Utility;

import java.util.HashMap;
import java.util.HashSet;

public class WebViewPluginCfg extends AbstractCfg {

	public static final String CONFIG_FIND_PATH = "action";
	public static final String CONFIG_ATTR_NAME = "name";
	public static final String CONFIG_ATTR_CLASS = "class";

	private static WebViewPluginCfg instance;


	/**
	 * get instance 主要给宿主app使用,默认情况
	 * @return DataCfg
	 * @throws Exception
	 */
	public static WebViewPluginCfg getInstance() {
		if (instance == null) {
			synchronized (WebViewPluginCfg.class) {
				instance = new WebViewPluginCfg();
			}
		}
		return instance;
	}
	
	/**
	 * load config
	 * @return
	 */
	@Override
	protected HashMap<String,Object> loadConfig() {
		super.loadConfig();
		HashMap<String,Object> config = new HashMap<>();
		try {
			HashSet hashSet;
			if (this.stream != null) {
				hashSet = Parser.loadXML(this.stream, CONFIG_FIND_PATH);
			} else {
				hashSet = Parser.loadXML(this.fileName, CONFIG_FIND_PATH);
			}

			for (int i=0; i<hashSet.size(); i++) {
				HashMap<String,String> data = (HashMap)hashSet.toArray()[i];
				String name = data.get(CONFIG_ATTR_NAME);
				String clsname = data.get(CONFIG_ATTR_CLASS);
				if (name == null || "".equals(name)) {
					Utility.error(CONFIG_ATTR_NAME + " not nullable, [" + fileName + "]");
				}
				if (clsname == null || "".equals(clsname)) {
					Utility.error(CONFIG_ATTR_CLASS + " not nullable, [" + fileName + ":" + CONFIG_ATTR_NAME + "=" + name + "]");
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
	 * @return
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
