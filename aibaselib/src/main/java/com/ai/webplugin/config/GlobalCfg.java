package com.ai.webplugin.config;

import com.ai.base.util.Parser;
import com.ai.base.util.Utility;
import com.ailk.common.data.IData;

public class GlobalCfg extends AbstractCfg {
	public static final String CONFIG_FIELD_ONLINEADDR = "online.addr";
	public static final String CONFIG_FIELD_ONLINEADDRTEST = "online.addrTest";
	public static final String CONFIG_FIELD_ONLINEADDR_APK = "online.apksAddr";
	public static final String CONFIG_FIELD_ENCRYPTKEY = "encryptKey";
	public static final String CONFIG_FIELD_ZHSTRING = "ZH_String";
	public static final String CONFIG_FIELD_IDMD5Key = "IDMD5Key";
	public static final String CONFIG_FIELD_VERSION = "version";
	public static final String CONFIG_FIELD_PUBLICKEY = "publicKey";
	public static final String CONFIG_FIELD_USERAGENT = "userAgent";
	public static final String CONFIG_FIELD_WELCOMEURL = "welcomeURL";
	public static final String CONFIG_FIELD_VESSIONURL = "versionURL";
	public static final String CONFIG_FIELD_FILEPROVIDER = "fileprovider";

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