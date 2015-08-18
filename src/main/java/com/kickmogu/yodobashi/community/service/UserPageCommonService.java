package com.kickmogu.yodobashi.community.service;

import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;

/**
 * マイページとユーザーページの共通サービスです
 * 
 * @author kiryu
 *
 */
public interface UserPageCommonService {

	/**
	 * 指定したコミュニティユーザーの購入商品を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param publicOnly 公開設定のもののみ取得する場合、true
	 * @param limit 最大取得件数
	 * @return 購入商品情報のリスト
	 */
	SearchResult<PurchaseProductDO> findPurchaseProductByCommunityUserId(String communityUserId, boolean publicOnly, int limit);
}
