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
		return instance(className,null,null,null,null,singleton);
	}

	public static Object instance(String className, Class<?> constrCls1, Object constrObj1,Class<?> constrCls2,Object constrObj2, boolean singleton) {
		if (singleton && beans.containsKey(className)) {
			return beans.get(className);
		}
		
		try {
			Constructor<?> constructor = constrCls1 == null && constrCls2 == null? Class.forName(className).getConstructor() :
					Class.forName(className).getConstructor(constrCls1,constrCls2);
			Object bean = (constrObj1 == null && constrObj2 == null)? constructor.newInstance() : constructor.newInstance(constrObj1,constrObj2);
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