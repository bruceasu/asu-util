package me.asu.util;

import java.beans.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
/**
 * BeanUtils.
 * <p>2017 Suk All rights reserved.</p>
 *
 * @author Suk
 * @version 1.0.0
 * @since 2017-12-20 11:19
 */
public class BeanUtils {

	public static Map<Class, BeanInfo> cache = new ConcurrentHashMap<Class, BeanInfo>();
	public static Map<Class, Map<String, Field>> cacheFields = new ConcurrentHashMap<Class, Map<String, Field>>();

	public static <S, T> void copy(S source, T destination) throws Exception {
		BeanInfo beanInfo = getBeanInfo(source.getClass());

		// 遍历所有属性
		for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {
			// 允许读写
			if (descriptor.getWriteMethod() != null) {
				Object value = descriptor.getReadMethod().invoke(destination);
				if (value != null && !"".equals(value)) {
					descriptor.getWriteMethod().invoke(source, value);
				}
			}
		}
	}

	public static Object getPropertyValueQuietly(Object bean, String name) {
		try {
			return getPropertyValue(bean, name);
		} catch (Exception e) {
			return null;
		}
	}

	public static Object getPropertyValue(Object bean, String name)
			throws IntrospectionException, InvocationTargetException, IllegalAccessException {
		if (bean == null || Strings.isEmpty(name)) {
			return null;
		}
		//创建属性描述器
		// PropertyDescriptor descriptor = new PropertyDescriptor(name, bean.getClass());
		PropertyDescriptor descriptor = findPropertyDescriptor(bean.getClass(), name);
				Method readMethod = descriptor.getReadMethod();
		if (readMethod != null) {
			return readMethod.invoke(bean);
		}

		// try get it directly
		return getValue(bean, name);
	}




	public static PropertyDescriptor findPropertyDescriptor(Class cls, String name)
			throws IntrospectionException {
		BeanInfo beanInfo = getBeanInfo(cls);
		for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {
			if (descriptor.getName().equals(name)) {
				return descriptor;
			}
		}

		return null;
	}

	public static BeanInfo getBeanInfo(Class cls) throws IntrospectionException {
		BeanInfo bi = cache.get(cls);
		if (bi == null) {
			bi = Introspector.getBeanInfo(cls);
			cache.put(cls, bi);
		}

		return bi;
	}

	public static Field findField(Class cls, String name) throws IntrospectionException {
		if (Strings.isEmpty(name)) {
			return null;
		}
		Map<String, Field> map = getFields(cls);
		return map.get(name);
	}

	public static Map<String, Field> getFields(Class cls) throws IntrospectionException {
		if (cls == null) {
			return Collections.emptyMap();
		}
		Map<String, Field> map = cacheFields.get(cls);
		if (map == null) {
			map = new HashMap<String, Field>(8);
			Field[] fields = cls.getFields();
			for (Field f: fields) {
				map.put(f.getName(), f);
			}
			cacheFields.put(cls, map);
		}

		return map;
	}


	public static Object getValue(Object bean, String name)
			throws IntrospectionException, IllegalAccessException {
		Field field = findField(bean.getClass(), name);
		if (field == null) {
			return null;
		}
		boolean accessible = field.isAccessible();
		if (accessible) {
			return field.get(bean);
		} else {
			field.setAccessible(true);
			Object o = field.get(bean);
			field.setAccessible(accessible);
			return o;
		}
	}

	public static Map objectToMap(Object obj){
		if (obj == null) {
			return new HashMap();
		}
		try{
			Class    type      = obj.getClass();
			Map      returnMap = new HashMap();
			BeanInfo beanInfo  = Introspector.getBeanInfo(type);

			PropertyDescriptor[] propertyDescriptors =  beanInfo.getPropertyDescriptors();
			for (int i = 0; i< propertyDescriptors.length; i++) {
				PropertyDescriptor descriptor = propertyDescriptors[i];
				String propertyName = descriptor.getName();
				if (!propertyName.equals("class")) {
					Method readMethod = descriptor.getReadMethod();
					Object result     = readMethod.invoke(obj, new Object[0]);
					if(result == null){
						continue;
					}
					//判断是否为 基础类型 String,Boolean,Byte,Short,Integer,Long,Float,Double
					//判断是否集合类，COLLECTION,MAP
					if(result instanceof String
							|| result instanceof Boolean
							|| result instanceof Byte
							|| result instanceof Short
							|| result instanceof Integer
							|| result instanceof Long
							|| result instanceof Float
							|| result instanceof Double
							|| result instanceof Enum
					){
						if (result != null) {
							returnMap.put(propertyName, result);
						}
					}else if(result instanceof Collection){
						Collection<?> lstObj = arrayToMap((Collection<?>)result);
						returnMap.put(propertyName, lstObj);

					}else if(result instanceof Map){
						Map<Object,Object> lstObj = mapToMap((Map<Object,Object>)result);
						returnMap.put(propertyName, lstObj);
					} else {
						Map mapResult = objectToMap(result);
						returnMap.put(propertyName, mapResult);
					}

				}
			}
			return returnMap;
		}catch(Exception e){
			throw new RuntimeException(e);
		}

	}

	private static Map<Object, Object> mapToMap(Map<Object, Object> orignMap) {
		Map<Object,Object> resultMap = new HashMap<Object,Object>();
		for(Entry<Object, Object> entry:orignMap.entrySet()){
			Object key = entry.getKey();
			Object resultKey = null;
			if(key instanceof Collection){
				resultKey = arrayToMap((Collection)key);
			}else if(key instanceof Map){
				resultKey = mapToMap((Map)key);
			}
			else{
				if(key instanceof String
						|| key instanceof Boolean
						|| key instanceof Byte
						|| key instanceof Short
						|| key instanceof Integer
						|| key instanceof Long
						|| key instanceof Float
						|| key instanceof Double
						|| key instanceof Enum
				){
					if (key != null) {
						resultKey = key;
					}
				}else{
					resultKey = objectToMap(key);
				}
			}


			Object value = entry.getValue();
			Object resultValue = null;
			if(value instanceof Collection){
				resultValue = arrayToMap((Collection)value);
			}else if(value instanceof Map){
				resultValue = mapToMap((Map)value);
			}
			else{
				if(value instanceof String
						|| value instanceof Boolean
						|| value instanceof Byte
						|| value instanceof Short
						|| value instanceof Integer
						|| value instanceof Long
						|| value instanceof Float
						|| value instanceof Double
						|| value instanceof Enum
				){
					if (value != null) {
						resultValue = value;
					}
				}else{
					resultValue = objectToMap(value);
				}
			}

			resultMap.put(resultKey, resultValue);
		}
		return resultMap;
	}


	private static Collection arrayToMap(Collection lstObj){
		ArrayList arrayList = new ArrayList();

		for (Object t : lstObj) {
			if(t instanceof Collection){
				Collection result = arrayToMap((Collection)t);
				arrayList.add(result);
			}else if(t instanceof Map){
				Map result = mapToMap((Map)t);
				arrayList.add(result);
			} else {
				if(t instanceof String
						|| t instanceof Boolean
						|| t instanceof Byte
						|| t instanceof Short
						|| t instanceof Integer
						|| t instanceof Long
						|| t instanceof Float
						|| t instanceof Double
						|| t instanceof Enum
				){
					if (t != null) {
						arrayList.add(t);
					}
				}else{
					Object result = objectToMap(t);
					arrayList.add(result);
				}
			}
		}
		return arrayList;
	}
}
