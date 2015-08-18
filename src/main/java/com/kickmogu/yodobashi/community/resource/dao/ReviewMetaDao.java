package com.kickmogu.yodobashi.community.resource.dao;

import java.util.List;

import com.kickmogu.yodobashi.community.resource.domain.ReviewMetaDO;

/**
 * レビュー投稿時のタイトルや説明のMeta情報のインターフェース
 * カテゴリ毎に設定が可能で指定のカテゴリ階層の最下層のカテゴリから順に検索し該当する
 * レビューMeta情報を返す。
 * 該当が無い場合は、既定値のMeta情報を返す。
 * @author sugimoto
 *
 */
public interface ReviewMetaDao {
	
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
