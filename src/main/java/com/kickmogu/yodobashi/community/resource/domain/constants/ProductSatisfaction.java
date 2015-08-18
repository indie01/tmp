package com.kickmogu.yodobashi.community.resource.domain.constants;

import com.kickmogu.lib.core.constants.LabeledEnum;
import com.kickmogu.web.common.domain.Selectable;

/**
 * 商品の満足度
 */
public enum ProductSatisfaction implements LabeledEnum<ProductSatisfaction, String>, Selectable{

	FIVE ("5", "★★★★★", "大満足"),
	FOUR ("4", "★★★★", "満足"),
	THREE("3", "★★★", "普通"),
	TWO  ("2", "★★", "まあまあ"),
	ONE  ("1", "★", "残念"),
	NONE ("9", "未回答", "未回答"),
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
	 * 評価です。
	 */
	private String satisfaction;

	
	/**
	 * コンストラクタです。
	 * @param code コード
	 * @param label ラベル
	 */
	private ProductSatisfaction(String code, String label, String satisfaction) {
		this.code = code;
		this.label = label;
		this.satisfaction = satisfaction;
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
	public static ProductSatisfaction codeOf(String code) {
		for (ProductSatisfaction element : values()) {
			if (element.code.equals(code)) {
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

	/**
	 * 評価を返します。
	 * @return 評価
	 */
	public String getSatisfaction() {
		return satisfaction;
	}

}