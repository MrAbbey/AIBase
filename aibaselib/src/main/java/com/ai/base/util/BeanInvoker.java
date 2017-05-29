package com.ai.base.util;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class BeanInvoker {
	
	private static Map<String, Object> beans = new HashMap<String, Object>();
	
	/**
	 * instance
	 * @param className
	 * @param singleton
	 * @return Object
	 */
	public static Object instance(String className, boolean singleton) {
		return instance(className,null,null,singleton);
	}
	
	/**
	 * instance
	 * @param className
	 * @param constrCls
	 * @oaran constrObj
	 * @param singleton
	 * @return Object
	 */
	public static Object instance(String className, Class<?> constrCls, Object constrObj, boolean singleton) {
		if (singleton && beans.containsKey(className)) {
			return beans.get(className);
		}
		
		try {
			Constructor<?> constructor = constrCls == null? Class.forName(className).getConstructor() :
					Class.forName(className).getConstructor(constrCls);
			Object bean = constrObj == null? constructor.newInstance() : constructor.newInstance(constrObj);
			if (singleton) beans.put(className, bean);
			return bean;
		} catch (ClassNotFoundException e) {
			Utility.error(className + " not found", e);
		} catch (Exception e) {
			Utility.error(e);
		}
		return null;
	}
}