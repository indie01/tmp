/**
 *
 */
package com.kickmogu.yodobashi.kalmia.cms.test.util;

import java.io.InputStream;

/**
 * @author kamiike
 *
 */
public class TextLoadHelper {

	public static String getFileContents(Class<?> calledClass, String fileName) {
		try {
			String path = calledClass.getPackage().getName().replace(".", "/") + "/" + fileName;
			ClassLoader loader = calledClass.getClassLoader();
			InputStream inputStream = null;
			if (loader == null) {
				inputStream = ClassLoader.getSystemResourceAsStream(path);
			} else {
				inputStream = loader.getResourceAsStream(path);
			}
			if (inputStream == null) {
				throw new IllegalArgumentException(fileName + " is not found.");
			}
			byte[] data = new byte[inputStream.available()];
			inputStream.read(data);
			return new String(data, "UTF-8");
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

}
