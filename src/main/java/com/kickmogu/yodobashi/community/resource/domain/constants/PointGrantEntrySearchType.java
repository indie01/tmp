package com.kickmogu.yodobashi.community.resource.domain.constants;

import java.util.ArrayList;
import java.util.List;

import com.kickmogu.lib.core.constants.LabeledEnum;

/**
 * 商品取得タイプ
 */
public enum PointGrantEntrySearchType implements LabeledEnum<PointGrantEntrySearchType, String>{

	POINT_GRANT_TARGET(PointGrantResultStatus.POINT_GRANT_TARGET.getCode(),"未確認",PointGrantEntrySearchType.GRANT),
	POINT_GRANT_PERMIT(PointGrantResultStatus.POINT_GRANT_PERMIT.getCode(), "承認",PointGrantEntrySearchType.GRANT),
	POINT_GRANT_HOLD(PointGrantResultStatus.POINT_GRANT_HOLD.getCode(), "保留",PointGrantEntrySearchType.GRANT),
	FINISH_FOR_POINT_GRANT_NOT_PERMIT(PointGrantResultStatus.FINISH_FOR_POINT_GRANT_NOT_PERMIT.getCode(), "却下",PointGrantEntrySearchType.GRANT),
	
	POINT_DEPRIVATION_TARGET(PointGrantResultStatus.POINT_DEPRIVATION_TARGET.getCode(),"未確認",PointGrantEntrySearchType.DEPRIVATION),
	POINT_DEPRIVATION_PERMIT(PointGrantResultStatus.POINT_DEPRIVATION_PERMIT.getCode(),"承認",PointGrantEntrySearchType.DEPRIVATION),
	POINT_DEPRIVATION_HOLD(PointGrantResultStatus.POINT_DEPRIVATION_HOLD.getCode(),"保留",PointGrantEntrySearchType.DEPRIVATION),
	FINISH_FOR_POINT_DEPRIVATION_NOT_PERMIT(PointGrantResultStatus.FINISH_FOR_POINT_DEPRIVATION_NOT_PERMIT.getCode(),"却下",PointGrantEntrySearchType.DEPRIVATION),
	
	POINT_ALREADY_GRANTED(PointGrantResultStatus.POINT_DEPRIVATION_NOTSET.getCode(), "ポイント付与済み",PointGrantEntrySearchType.OTHER),
	FINISH(PointGrantResultStatus.FINISH.getCode(), "終了",PointGrantEntrySearchType.OTHER),
	;

	private final static int GRANT = 0;
	private final static int DEPRIVATION = 1;
	private final static int OTHER = 9;
	
	/**
	 * コードです。
	 */
	private String code;

	/**
	 * ラベルです。
	 */
	private String label;

	/**
	 * 承認/剥奪です。
	 */
	private int type;

	/**
	 * コンストラクタです。
	 * @param code コード
	 * @param label ラベル
	 */
	private PointGrantEntrySearchType(String code, String label, int type) {
		this.code = code;
		this.label = label;
		this.type = type;
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
	public static PointGrantEntrySearchType codeOf(String code) {
		for (PointGrantEntrySearchType element : values()) {
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
	 * 承認/剥奪を返します。
	 * @return 承認/剥奪
	 */
	public int getType() {
		return type;
	}

	public static List<PointGrantEntrySearchType> grantValues(){
		return getValues(GRANT);
	}

	public static List<PointGrantEntrySearchType> deprivationValues(){
		return getValues(DEPRIVATION);
	}
	
	public static List<PointGrantEntrySearchType> otherValues(){
		return getValues(OTHER);
	}
	
	private static List<PointGrantEntrySearchType> getValues(int type){
		List<PointGrantEntrySearchType> pointGrantEntrySearchTypes = new ArrayList<PointGrantEntrySearchType>();
		for (PointGrantEntrySearchType element : values()) {
			if (element.type == type) {
				pointGrantEntrySearchTypes.add(element);
			}
		}
		return pointGrantEntrySearchTypes;
	}
	
}