package com.ai.webplugin.config;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;

public class AbstractCfg {
	
	protected String fileName;
	protected InputStream stream;
	protected HashMap<String,Object> cache;

	public void parseConfig(String configFileName) {
		fileName = configFileName;
		cache = loadConfig();
	}

	protected  HashMap<String,Object> loadConfig() {return null;}

	public void parseConfig(InputStream configFileStream) {
		stream = configFileStream;
		cache = loadConfig();
	}

	/**
	 * get names
	 * @return String[]
	 */
	public String[] getNames() {
		String[] names = new String[cache.size()];
		Iterator<String> keys = cache.keySet().iterator();

		for(int index = 0; keys.hasNext(); ++index) {
			names[index] = (String)keys.next();
		}
		return names;
	}
	
	/**
	 * get all
	 * @return
	 */
	public HashMap<String,Object> getAll() {
		return cache;
	}
	
}