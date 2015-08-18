package com.kickmogu.yodobashi.community.resource.domain.constants;

import com.kickmogu.lib.core.constants.LabeledEnum;

/**
 * 非同期メッセージタイプです。
 */
public enum AsyncMessageType implements LabeledEnum<AsyncMessageType, String>{

	INSTANCYSERVICE ("1", "サービス（即時）", "asyncInstancyHandler"),
	MAIL ("2", "メール", "mailHandler"),
	DELAYSERVICE ("3", "サービス（遅延）", "asyncDelayHandler"),
	ACTIONHISTORY ("4", "アクションヒストリー", "asyncActionHandler"),
	INFORMATION ("5", "お知らせ", "asyncInformationHandler"),
	CLEARCACHE ("6", "画像削除", "asyncClearCacheImageHandler"),
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
	 * キュー名です。
	 */
	private String queueName;

	/**
	 * コンストラクタです。
	 * @param code コード
	 * @param label ラベル
	 * @param queueName キュー名
	 */
	private AsyncMessageType(String code, String label, String queueName) {
		this.code = code;
		this.label = label;
		this.queueName = queueName;
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
	public static AsyncMessageType codeOf(String code) {
		for (AsyncMessageType element : values()) {
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

	/**
	 * キュー名を返します。
	 * @return キュー名
	 */
	public String getQueueName() {
		return queueName;
	}

}