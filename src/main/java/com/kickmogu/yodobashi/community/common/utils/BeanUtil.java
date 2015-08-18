package com.kickmogu.yodobashi.community.common.utils;

import java.beans.PropertyDescriptor;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;

import com.kickmogu.lib.core.utils.Reflections;

public class BeanUtil {

	public static void nullStringToEmpty(Object object) {

		Reflections.traverseFields(object, new Reflections.FieldCallback() {
			public void visit(String path, Field field, Object obj, Object parent)
					throws Throwable {
				field.set(parent, StringUtils.defaultString((String)obj));
			}
		},  Reflections.typeFieldFilter(String.class));
	}

	/**
	 * ビーン情報をディープコピーします。
	 * @param src 元情報
	 * @return コピーインスタンス
	 */
	public static Object deepCopy(Object src) {
		try {
			ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
			XMLEncoder encoder = new XMLEncoder(byteOutputStream);
			encoder.writeObject(src);
			encoder.close();
			XMLDecoder decoder = new XMLDecoder(
					new ByteArrayInputStream(byteOutputStream.toByteArray()));
			Object object = decoder.readObject();
			decoder.close();
			return object;
			/*
			ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
			ObjectOutputStream outputStream = new ObjectOutputStream(byteOutputStream);
			outputStream.writeObject(src);
			ByteArrayInputStream byteInputStream =
					new ByteArrayInputStream(byteOutputStream.toByteArray());
			ObjectInputStream inputStream = new ObjectInputStream(byteInputStream);
			return (Serializable) inputStream.readObject();
			*/
		} catch (Exception e) {
			throw new IllegalArgumentException("deep copy failed.", e);
		}
	}

	/**
	 * プロパティをコピーします。
	 * @param src コピー元
	 * @param dest コピー先
	 */
	public static void copyProperties(
			Object src,
			Object dest) {
		copyProperties(src, dest, null);
	}

	/**
	 * プロパティをコピーします。
	 * @param src コピー元
	 * @param dest コピー先
	 * @param ignoreList コピーしないリスト
	 */
	public static void copyProperties(
			Object src,
			Object dest,
			String[] ignoreList) {
		try {
			Set<String> destPropertySet = new HashSet<String>();
			PropertyDescriptor[] dpdList
					= PropertyUtils.getPropertyDescriptors(dest.getClass());
			for (PropertyDescriptor pd : dpdList) {
				if (pd.getWriteMethod() != null) {
					destPropertySet.add(pd.getName());
				}
			}

			Set<String> copyPropertySet = new HashSet<String>();
			PropertyDescriptor[] spdList
					= PropertyUtils.getPropertyDescriptors(src.getClass());
			for (PropertyDescriptor pd : spdList) {
				if (destPropertySet.contains(pd.getName())) {
					copyPropertySet.add(pd.getName());
				}
			}
			copyPropertySet.remove("class");
			if (ignoreList != null) {
				for (String ignore : ignoreList) {
					copyPropertySet.remove(ignore);
				}
			}

			for (String name : copyPropertySet) {
				Object srcProperty = PropertyUtils.getProperty(src, name);
				PropertyUtils.setProperty(dest, name, srcProperty);
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

}
