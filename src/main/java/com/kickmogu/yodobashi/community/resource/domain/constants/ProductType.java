package com.kickmogu.yodobashi.community.resource.domain.constants;

import com.kickmogu.lib.core.constants.LabeledEnum;

/**
 * 商品種別です。
 */
public enum ProductType implements LabeledEnum<ProductType, String>{

	NORMAL ("00", "通常商品"),
	DIGITAL_PRINT ("01", "デジタルプリント商品"),
	POST_CARD ("02", "ポストカード商品"),
	DOWNLOAD_WAU ("03", "ダウンロード商品(WAU)"),
	DOWNLOAD_WITHOUT_WAU ("04", "ダウンロード商品(WAU以外)"),
	DVD_WAU ("05", "DVD商品(WAU)"),
	DOWNLOAD_NUMBER ("06", "ダウンロード番号販売商品"),
	NORMAL_WITH_JMD ("10", "通常商品(JMDデータ有り)"),
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
	private ProductType(String code, String label) {
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
	public static ProductType codeOf(String code) {
		for (ProductType element : values()) {
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

}