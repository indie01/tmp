/**
 *
 */
package com.kickmogu.yodobashi.community.resource.domain.util;

import java.util.List;

import com.kickmogu.lib.core.constants.LabeledEnum;

/**
 * ENUMのユーティリティです。
 * @author kamiike
 *
 */
public class EnumUtils {

	/**
	 * コンストラクタをカプセル化します。
	 */
	private EnumUtils() {
	}
	
	/**
	 * CustomEnum 型のクラスリストを返します。
	 */
	@SuppressWarnings({ "rawtypes" })
	public static List<Class<LabeledEnum>> getCustomEnumClassList() throws Exception {
		return com.kickmogu.lib.core.utils.EnumUtils.getCustomEnumClassList("com.kickmogu.yodobashi");
	}
}
