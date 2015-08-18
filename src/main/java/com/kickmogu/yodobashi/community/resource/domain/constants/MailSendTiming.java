package com.kickmogu.yodobashi.community.resource.domain.constants;

import com.kickmogu.lib.core.constants.LabeledEnum;
import com.kickmogu.web.common.domain.Selectable;

/**
 * メール送信タイミングです。
 */
public enum MailSendTiming implements LabeledEnum<MailSendTiming, String>, Selectable {

	NOT_NOTIFY ("1", "通知しない"),
	EVERYTIME_NOTIFY ("2", "1回ごとに通知"),
	DAILY_NOTIFY ("3", "1日1回通知"),
	FIVE_DAYS_AGO ("4", "5日前に通知"),
	TEN_DAYS_AGO ("5", "10日前に通知"),
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
	private MailSendTiming(String code, String label) {
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
	public static MailSendTiming codeOf(String code) {
		for (MailSendTiming element : values()) {
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