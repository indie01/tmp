package com.kickmogu.yodobashi.community.resource.domain.constants;

import com.kickmogu.lib.core.constants.LabeledEnum;


/**
 *
 * UniversalSessionManagerがデフォルトで持っている状態。
 * 下記状態のほかに、任意状態を持っているfieldの場合、別途
 * その状態を追記したEnumを定義する必要がある。
 *
 * @author kamiike
 *
 */
public enum UniversalSessionStatus implements LabeledEnum<UniversalSessionStatus, String> {

	READY("READY", "準備"), /** 準備 **/
	CANCELLED("CANCELLED", "キャンセル"), /** キャンセル **/
	COMPLETED("COMPLETED", "完了"), /** 完了 **/
	EXPIRED("EXPIRED", "期限切れ"), /** 期限切れ **/
	;

	private String code;
	private String label;

	private UniversalSessionStatus(String code, String label) {
		this.code = code;
		this.label = label;
	}

	public String getCode() {
		return code;
	}

	public static UniversalSessionStatus codeOf(String code) {
		for (UniversalSessionStatus element : values()) {
			if (code.equals(element.code)) {
				return element;
			}
		}
		return null;
	}

	public String toString() {
		return name();
	}

	public boolean canceled() {
		return this.equals(CANCELLED);
	}

	public boolean completed() {
		return this.equals(COMPLETED);
	}

	public boolean expired() {
		return this.equals(EXPIRED);
	}

	public boolean ready() {
		return this.equals(READY);
	}

	public boolean finished() {
		return canceled() || completed() ||  expired();
	}

	public boolean alive() {
		return !finished();
	}

	public String getLabel() {
		return label;
	}


}