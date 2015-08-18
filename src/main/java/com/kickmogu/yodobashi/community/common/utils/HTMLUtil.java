package com.kickmogu.yodobashi.community.common.utils;

import org.apache.commons.lang.StringUtils;

public class HTMLUtil {

	private static final String CR = "\r";
	private static final String LF = "\n";
	/**
	 * 改行のＴｒｉｍ
	 * @param src
	 * @return
	 */
	public static String removeLine(String src){
		String dest = src;
		if(!StringUtils.isEmpty(dest)){
			dest = dest.replace(LF, "");
			dest = dest.replace(CR, "");
			dest = dest.replace(" ", "");
			dest = dest.replace("　", "");
			dest = dest.replace("<br>", "");
			dest = dest.replace("<br/>", "");
		}
		return dest;
	}
}

