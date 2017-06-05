package com.ai.webplugin.dl;

import com.ai.base.util.Parser;
import com.ai.base.util.Utility;
import com.ai.webplugin.config.AbstractCfg;
import com.ailk.common.data.IData;
import com.ailk.common.data.IDataset;
import com.ailk.common.data.impl.DataMap;

public class WebViewPluginCfg_dl extends AbstractCfg {

	public static final String CONFIG_FIND_PATH = "plugin";
	public static final String CONFIG_ATTR_NAME = "name";
	public static final String CONFIG_ATTR_CLASS = "class";
	public static final String CONFIG_ATTR_PACKAGENAME = "packageName";

	private static WebViewPluginCfg_dl instance;
	/**
	 * get instance
	 * @return DataCfg
	 * @throws Exception
	 */
	public static WebViewPluginCfg_dl getInstance() {
		if (instance == null) {
			synchronized (WebViewPluginCfg_dl.class) {
				instance = new WebViewPluginCfg_dl();
			}
		}
		return instance;
	}

	/**
	 * load config
	 * @return IData
	 */
	@Override
	protected IData loadConfig() {
		super.loadConfig();
		IData config = new DataMap();
		try {
			IDataset dataset;
			if (this.stream != null) {
				dataset = Parser.loadXML(this.stream, CONFIG_FIND_PATH);
			} else {
				dataset = Parser.loadXML(this.fileName, CONFIG_FIND_PATH);
			}

			for (int i=0; i<dataset.size(); i++) {
				IData data = dataset.getData(i);
				String name = data.getString(CONFIG_ATTR_NAME);
				String clsname = data.getString(CONFIG_ATTR_CLASS);
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
	 * @return IData
	 */
	public IData get(String name) {
		IData data = cache.getData(name);
		if (data == null) Utility.error(name + " not exist, [" + fileName + "]");
		return data;
	}
	
	/**
	 * get
	 * @param name
	 * @param defval
	 * @return IData
	 */
	public IData get(String name, IData defval) {
		IData data = cache.getData(name);
		if (data == null) data = defval;
		return data;
	}
	
	/**
	 * attr
	 * @param name
	 * @param attr
	 * @return String
	 */
	public String attr(String name, String attr) {
		IData data = get(name);
		return data.getString(attr);
	}
	
}
