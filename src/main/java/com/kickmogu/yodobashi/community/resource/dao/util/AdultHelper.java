/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.util;

import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;

import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseLostProductDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.UsedProductDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.Verification;

/**
 * アダルト処理ヘルパーです。
 * @author kamiike
 *
 */
public class AdultHelper {

	/**
	 * アダルト表示確認ステータスです。
	 */
	private Verification adultVerification;

	/**
	 * コンストラクタです。
	 * @param adultVerification アダルト表示確認ステータス
	 */
	public AdultHelper(Verification adultVerification) {
		this.adultVerification = adultVerification;
	}


	/**
	 * アダルトコンテンツを含んでいるかの検証が必要かどうかを返します。
	 * @return アダルトコンテンツを含んでいるかの検証が必要な場合、true
	 */
	public boolean isRequireCheckAdult() {
		if (adultVerification == null) {
			return false;
		}
		return adultVerification.equals(Verification.ATANYTIME);
	}

	/**
	 * クエリをフィルタリングして返します。
	 * @return フィルタリングされたクエリ
	 */
	public String toFilterQuery(String baseQuery) {
		if (adultVerification == null || adultVerification.equals(Verification.AUTHORIZED)) {
			return baseQuery;
		} else {
			return "(" + baseQuery + ") AND adult_b:false";
		}
	}

	/**
	 * アダルトコンテンツを含んでいるかどうかです。
	 * @param reviews レビューリスト
	 * @return アダルトコンテンツを含んでいる場合、true
	 */
	public boolean hasAdult(
			List<ReviewDO> reviews) {
		for (ReviewDO review : reviews) {
			if (review.isAdult()) {
				return true;
			}
			for (UsedProductDO target : review.getUsedProducts()) {
				if (target.getProduct() != null && target.getProduct().isAdult()) {
					return true;
				}
			}
			for (PurchaseLostProductDO target : review.getPurchaseLostProducts()) {
				if (target.getProduct() != null && target.getProduct().isAdult()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * アダルトコンテンツを含んでいるかどうかです。
	 * @param baseQuery ベースクエリ
	 * @param type タイプ
	 * @param solrOperations solrオペレーション
	 * @return アダルトコンテンツを含んでいる場合、true
	 */
	public boolean hasAdult(
			String baseQuery,
			Class<?> type,
			SolrOperations solrOperations) {
		SolrQuery query = new SolrQuery(
				"(" + baseQuery + ") AND adult_b:true");
		query.setRows(0);
		return solrOperations.findByQuery(query, type).getNumFound() > 0;
	}
}
