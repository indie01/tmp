/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.kickmogu.yodobashi.community.performance.PerformanceTest;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Frequency;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Type;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductFollowDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;


/**
 * フォロー DAO です。
 * @author kamiike
 *
 */
public interface ProductFollowDao {

	/**
	 * SKUから商品のフォロワーのコミュニティユーザーを検索して返します。
	 * @param sku SKU
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return フォロワーのコミュニティユーザーのリスト
	 */
	public SearchResult<CommunityUserDO> findFollowerCommunityUserBySKUForIndex(
			String sku, int limit, int offset);

	/**
	 * SKUから商品のフォロワーのコミュニティユーザーを検索して返します。
	 * @param sku SKU
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @param asc 昇順ソート
	 * @return フォロワーのコミュニティユーザーのリスト
	 */
	public SearchResult<CommunityUserDO> findFollowerCommunityUserBySKU(
			String sku, int limit, int offset, boolean asc);

	/**
	 * 指定したフォロワー、商品の商品フォロー情報
	 * が存在するか判定します。
	 * @param communityUserId フォロワーのコミュニティユーザーID
	 * @param followProductId フォローする商品ID
	 * @return フォロー済みの場合、true
	 */
	public boolean existsProductFollow(
			String communityUserId, String followProductId);

	/**
	 * 商品フォロー情報を新規に作成します。
	 * @param communityUserId フォロワーのコミュニティユーザーID
	 * @param followProductId フォローする商品ID
	 * @param adult アダルト商品かどうか
	 * @return 商品フォロー情報
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.HIGH, frequencyComment="商品フォローの登録の頻度は高い")
	public ProductFollowDO createProductFollow(
			String communityUserId,
			String followProductId,
			boolean adult);

	/**
	 * 商品フォロー情報を削除します。
	 * @param communityUserId フォロワーのコミュニティユーザーID
	 * @param followProductId フォローする商品ID
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.MEDIUM, frequencyComment="商品フォローの削除の頻度は中くらい")
	public void deleteFollowProduct(
			String communityUserId, String followProductId);

	/**
	 * 商品のフォロワーのコミュニティユーザー数を返します。
	 * @param sku SKU
	 * @return 商品のフォロワーのコミュニティユーザー数
	 */
	public long countFollowerCommunityUser(
			String sku);

	/**
	 * 商品のフォロワーのコミュニティユーザー数を返します。
	 * @param skus SKUリスト
	 * @return 商品のフォロワーのコミュニティユーザー数リスト
	 */
	public Map<String, Long> countFollowerCommunityUserBySku(
			String[] skus);

	/**
	 * 商品フォロー情報のインデックスを作成します。
	 * @param productFollowId 商品フォローID
	 * @return 作成した場合、true
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.HIGH, frequencyComment="商品フォローの登録の頻度は高い")
	public boolean createProductFollowInIndex(
			String productFollowId);

	/**
	 * 商品フォロー情報のインデックスを更新します。
	 * @param communityUserId フォロワーのコミュニティユーザーID
	 * @param followProductId フォローする商品ID
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.HIGH, frequencyComment="商品フォローの登録の頻度は高い")
	public void updateProductFollowInIndex(
			String communityUserId, String followProductId);

	/**
	 * フォローしている商品情報をフォロー日時順（降順）に返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return 検索結果
	 */
	public SearchResult<ProductFollowDO> findFollowProduct(
			String communityUserId, int limit, Date offsetTime, boolean previous);

	/**
	 * 指定したコミュニティユーザーが購入した商品をフォローしている
	 * ユーザーを返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @param publicSetting 非公開情報を表示する場合はtrue
	 * @return レビューを書いているコミュニティユーザーのリスト
	 */
	public SearchResult<CommunityUserDO> findFollowerByPurchaseProduct(
			String communityUserId, int limit, int offset, boolean publicSetting);

	/**
	 * コミュニティユーザーIDからフォロー商品を検索して返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return フォロー商品のリスト
	 */
	public SearchResult<ProductDO> findFollowProductByCommunityUserId(
			String communityUserId, int limit, int offset);

	/**
	 * 商品のフォロー情報マップを返します。
	 * @param communityUserId 対象となるコミュニティユーザーID
	 * @param skus SKUリスト
	 * @return 商品のフォロー情報マップ
	 */
	public Map<String, Boolean> loadProductFollowMap(
			String communityUserId, List<String> skus);
	
	public long countFollowProduct(String communityUserId);
}
