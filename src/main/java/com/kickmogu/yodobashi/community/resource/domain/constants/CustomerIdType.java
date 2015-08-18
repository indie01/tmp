package com.kickmogu.yodobashi.community.resource.domain.constants;

import java.util.LinkedHashMap;
import java.util.Map;

import com.kickmogu.lib.core.constants.LabeledEnum;

/**
 * アカウントタイプです。
 */
public enum CustomerIdType implements LabeledEnum<CustomerIdType, String>{

	NONE("00", "指定なし"),
	COMMUNITYNAME ("01", "コミュニティ名"),
	OUTERCUSTOMERID ("02", "外部顧客ID"),
	CUSTOMERCODE ("03", "得意先コード")
	;

	/**
	 * コードです。
	 */
	private String code;

	/**
	 * ラベルです。
	 */
	private String label;

	/**
	 * コンストラクタです。
	 * @param code コード
	 * @param label ラベル
	 */
	private CustomerIdType(String code, String label) {
		this.code = code;
		this.label = label;
	}

	/**
	 * コードを返します。
	 * @return コード
	 */
	public String getCode() {
		return code;
	}

	/**
	 * 指定したコードの値を返します。
	 * @param code コード
	 * @return
	 */
	public static CustomerIdType codeOf(String code) {
		for (CustomerIdType element : values()) {
			if (code.equals(element.code)) {
				return element;
			}
		}
		return null;
	}

	/**
	 * 文字列表現を返します。
	 * @return 文字列表現
	 */
	@Override
	public String toString() {
		return name();
	}

	/**
	 * ラベルを返します。
	 * @return ラベル
	 */
	public String getLabel() {
		return label;
	}
	
	public static Map<String, String> getMap(){
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		
		for (CustomerIdType element : values())
			map.put(element.code, element.label);
		
		return map;
	}

}