package com.kickmogu.yodobashi.community.resource.domain;

import java.util.Map;

import com.kickmogu.yodobashi.community.resource.domain.constants.ReviewMetaType;

/**
 * レビュー投稿時のタイトルや説明のMeta情報
 * @author sugimoto
 *
 */
public class ReviewMetaDO extends BaseWithTimestampDO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6736229348466193174L;
	
	/**
	 * カテゴリコード
	 */
	private String categoryCode;
	
	private Map<ReviewMetaType, String> meta;

	public String getCategoryCode() {
		return categoryCode;
	}

	public void setCategoryCode(String categoryCode) {
		this.categoryCode = categoryCode;
	}

	public Map<ReviewMetaType, String> getMeta() {
		return meta;
	}

	public void setMeta(Map<ReviewMetaType, String> meta) {
		this.meta = meta;
	}
	
	public String displayValue(ReviewMetaType key){
		return meta.get(key);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ReviewMetaDO [categoryCode=");
		builder.append(categoryCode);
		builder.append(", meta=");
		builder.append(meta);
		builder.append("]");
		return builder.toString();
	}
	
}
