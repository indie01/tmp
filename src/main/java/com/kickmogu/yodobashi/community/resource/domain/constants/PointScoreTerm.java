package com.kickmogu.yodobashi.community.resource.domain.constants;

import com.kickmogu.lib.core.constants.LabeledEnum;

/**
 * 文字数スコア範囲タイプです。
 */
public enum PointScoreTerm implements LabeledEnum<PointScoreTerm, String>{

	SCORE_0_TO_99   ("scoreFactor.review.contents.count.term.0to99"   , "0～99文字"   , 0, 99),
	SCORE_100_TO_199("scoreFactor.review.contents.count.term.100to199", "100～199文字", 100, 199),
	SCORE_200_TO_299("scoreFactor.review.contents.count.term.200to299", "200～299文字", 200, 299),
	SCORE_300_TO_399("scoreFactor.review.contents.count.term.300to399", "300～399文字", 300, 399),
	SCORE_400_TO_449("scoreFactor.review.contents.count.term.400to449", "400～449文字", 400, 449),
	SCORE_450_TO_499("scoreFactor.review.contents.count.term.450to499", "450～499文字", 450, 499),
	SCORE_MORE_500  ("scoreFactor.review.contents.count.term.more500" , "500～"       , 500, 99999),
	;
	/**
	 * コードです。
	 */
	private String code;
	/**
	 * ラベルです。
	 */
	private String label;

	private long startTerm;

	private long endTerm;

	/**
	 * コンストラクタです。
	 * @param code コード
	 * @param label ラベル
	 * @param group グループ
	 */
	private PointScoreTerm(String code, String label, long startTerm, long endTerm) {
		this.code = code;
		this.label = label;
		this.startTerm = startTerm;
		this.endTerm = endTerm;
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
	public static PointScoreTerm codeOf(String code) {
		for (PointScoreTerm element : values()) {
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
	 * @return the startTerm
	 */
	public long getStartTerm() {
		return startTerm;
	}

	/**
	 * @return the endTerm
	 */
	public long getEndTerm() {
		return endTerm;
	}

}