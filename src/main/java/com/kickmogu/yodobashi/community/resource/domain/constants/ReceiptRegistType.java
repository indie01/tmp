package com.kickmogu.yodobashi.community.resource.domain.constants;

import com.kickmogu.lib.core.constants.LabeledEnum;

/**
 * レシート登録区分です。

0 登録
1 返品
2 没伝
3 両替
4 中止
6 ﾏｲﾅｽP回収
7 ﾏｲﾅｽP回収没伝
8 SC商品引換
M 延長加入
N 延長解除
O 延長還元
P 延長還元取消
Q 精算
R 店舗引渡(代済)控有
S 店舗引渡(代済)控無
T 店舗引渡取消(代済)
u ｲﾝﾀｰﾈｯﾄ売上
 */
public enum ReceiptRegistType implements LabeledEnum<ReceiptRegistType, String>{

	REGIST("0", "登録"),
	RETURENED("1", "返品"),
	REJECTED("2", "没伝"),
	CURRENCY_EXCHANGE("3", "両替"),
	STOP("4", "中止"),
	MINUS_P_CALL_IN("6", "マイナスP回収"),
	MINUS_P_CALL_IN_REJECTED("7", "マイナスP回収没伝"),
	SC_GOODS_EXCHANGE("8", "SC商品引換"),
	ALLONGEMENT("M", "延長加入"),
	CANCEL_ALLONGEMENT("N", "延長解除"),
	REDUCE_ALLONGEMENT("O", "延長還元"),
	CANCEL_REDUCE_ALLONGEMENT("P", "延長還元取消"),
	ACCOUNTING("Q", "精算"),
	HAND_SHOP("R", "店舗引渡(代済)控有"),
	HAND_SHOP_WITHOUT_DUPLICATE("S", "店舗引渡(代済)控無"),
	CANCEL_HAND_SHOP("T", "店舗引渡取消(代済)"),
	SALES_ON_EC("u", "インターネット売上"),
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
	private ReceiptRegistType(String code, String label) {
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
	public static ReceiptRegistType codeOf(String code) {
		for (ReceiptRegistType element : values()) {
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