package com.kickmogu.yodobashi.community.service;

import java.util.List;

import com.kickmogu.yodobashi.community.resource.domain.ReviewMetaDO;

public interface ReviewMetaService {

	/**
	 * 指定のカテゴリ一覧から最下層のカテゴリコードに一致するレビューMeta情報を取得する。
	 * 最下層のカテゴリコードから上位階層のカテゴリコード順にレビューMeta情報を検索し該当する
	 * カテゴリコードのレビューMeta情報を取得する。
	 * 該当するカテゴリコードが無い場合は、既定値のレビューMeta情報を返却する。
	 * @param categoryCodes カテゴリコード一覧
	 * @return レビューMeta情報
	 */
	public ReviewMetaDO getReviewMeta(List<String> categoryCodes);
}
