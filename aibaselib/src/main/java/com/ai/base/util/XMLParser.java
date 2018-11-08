package com.ai.base.util;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class XMLParser {

	public static HashSet loadXML(InputStream stream, String findPath) throws Exception {
		if (stream == null) Utility.error(" not data found");

		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = builder.parse(stream);
		stream.close();

		NodeList list = document.getDocumentElement().getElementsByTagName(findPath);
		HashSet dataset = new HashSet();
		for (int i=0; i<list.getLength(); i++) {
			NamedNodeMap attrs = list.item(i).getAttributes();
			HashMap<String,Object> data = new HashMap();
			for (int j=0; j<attrs.getLength(); j++) {
				Node attr = attrs.item(j);
				data.put(attr.getNodeName(), attr.getNodeValue());
			}
			dataset.add(data);
		}

		return dataset;
	}

	public static HashSet loadXML(String filePath, String findPath) throws Exception {
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath);
		if (stream == null) Utility.error(filePath + " not found");

		return loadXML(stream,findPath);
	}

	public static HashMap<String,Object> loadProperties(InputStream stream) throws Exception {
		if (stream == null) Utility.error("not data found");

		Properties properties = new Properties();
		properties.load(stream);
		stream.close();

		HashMap<String,Object> data = new HashMap();
		Enumeration<Object> keys = properties.keys();
		while(keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			String value = properties.getProperty(key);
			data.put(key, value.trim());
		}
		return data;
	}

	public static HashMap<String,Object> loadProperties(String filePath) throws Exception {
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath);
		if (stream == null) Utility.error(filePath + " not found");

		return loadProperties(stream);
	}
}