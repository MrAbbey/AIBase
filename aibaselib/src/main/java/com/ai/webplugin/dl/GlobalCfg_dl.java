package com.ai.webplugin.dl;

import com.ai.base.util.Parser;
import com.ai.base.util.Utility;
import com.ai.webplugin.config.AbstractCfg;
import com.ailk.common.data.IData;

public class GlobalCfg_dl extends AbstractCfg {
	public static final String CONFIG_FIELD_ONLINEADDR = "online.addr";

	public static final String CONFIG_FIELD_ONLINEADDR_APK = "online.apksAddr";
	public static final String CONFIG_FIELD_ENCRYPTKEY = "encryptKey";
	public static final String CONFIG_FIELD_ZHSTRING = "ZH_String";
	public static final String CONFIG_FIELD_IDMD5Key = "IDMD5Key";
	public static final String CONFIG_FIELD_VERSION = "version";
	public static final String CONFIG_FIELD_PUBLICKEY = "publicKey";

	private static GlobalCfg_dl instance;
	
	/**
	 * get instance
	 * @return GlobalCfg
	 */
	public static GlobalCfg_dl getInstance() {
		if (instance == null) {
			synchronized (GlobalCfg_dl.class) {
				instance = new GlobalCfg_dl();
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
		try {
			if (this.stream != null) {
				return Parser.loadProperties(this.stream);
			}
			return Parser.loadProperties(fileName);
		} catch (Exception e) {
			Utility.error(e);
		}
		return null;
	}

	/**
	 * attr
	 * @param name
	 * @return String
	 */
	public String attr(String name) {
		return cache.getString(name);
	}
	
}