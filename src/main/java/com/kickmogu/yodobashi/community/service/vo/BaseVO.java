package com.kickmogu.yodobashi.community.service.vo;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * View Object のベースクラスです。
 * @author kamiike
 */
public abstract class BaseVO implements Serializable {

	/**
	 * シリアライズに使用する UID です。
	 */
	private static final long serialVersionUID = -3956092657909126316L;

	/**
	 * 文字列表現を返します。
	 * @return 文字列表現
	 */
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

}
