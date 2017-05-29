package com.ai.webplugin.config;

import com.ailk.common.data.IData;

import java.io.InputStream;

public class AbstractCfg {
	
	protected String fileName;
	protected InputStream stream;
	protected IData cache;

	public void parseConfig(String configFileName) {
		fileName = configFileName;
		cache = loadConfig();
	}

	protected IData loadConfig() {return null;}

	public void parseConfig(InputStream configFileStream) {
		stream = configFileStream;
		cache = loadConfig();
	}

	/**
	 * get names
	 * @return String[]
	 */
	public String[] getNames() {
		return cache.getNames();
	}
	
	/**
	 * get all
	 * @return IData
	 */
	public IData getAll() {
		return cache;
	}
	
}