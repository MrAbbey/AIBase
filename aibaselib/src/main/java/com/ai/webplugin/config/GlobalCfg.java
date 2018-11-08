package com.ai.webplugin.config;

import com.ai.base.util.Parser;
import com.ai.base.util.Utility;

import java.util.HashMap;

public class GlobalCfg extends AbstractCfg {
	public static final String CONFIG_FIELD_ONLINEADDR = "online.addr";
	public static final String CONFIG_FIELD_ENCRYPTKEY = "encryptKey";
	public static final String CONFIG_FIELD_ZHSTRING = "ZH_String";
	public static final String CONFIG_FIELD_IDMD5Key = "IDMD5Key";
	public static final String CONFIG_FIELD_PUBLICKEY = "publicKey";

	public static final String CONFIG_FIELD_VERSION = "version";
	public static final String CONFIG_FIELD_USERAGENT = "userAgent";
	public static final String CONFIG_FIELD_VESSIONURL = "version.url";
	public static final String CONFIG_FIELD_FILEPROVIDER = "fileprovider";
	public static final String CONFIG_FIELD_APPNAME = "app.name";
	public static final String CONFIG_FIELD_CACHE = "resCache";
    public static final String CONFIG_FIELD_DEBUG = "isDebug";

	//
	private static GlobalCfg instance;

	/**
	 * get instance 主要是给宿主app使用
	 * @return GlobalCfg
	 */
	public static GlobalCfg getInstance() {
		if (instance == null) {
			synchronized (GlobalCfg.class) {
				instance = new GlobalCfg();
			}
		}
		return instance;
	}

	/**
	 * load config
	 * @return
	 */
	@Override
	protected HashMap<String, Object> loadConfig() {
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
		return (String)cache.get(name);
	}
	
}