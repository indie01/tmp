/**
 *
 */
package com.kickmogu.yodobashi.community.service;

import java.util.List;

import com.kickmogu.yodobashi.community.performance.PerformanceTest;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Frequency;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Type;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.service.vo.CommunityUserSetVO;

/**
 * 関係するユーザーを検索するサービスです。
 * @author kamiike
 *
 */
public interface SocialUserFindService {

	/**
	 * 指定した商品にレビューを書いたユーザーを返します。
	 * @param sku SKU
	 * @param excludeCommunityUserId 除外するコミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.HIGH,
		frequencyComment="商品レビュー詳細の初期表示(他のユーザーもこの商品のレビューを書いています)とレビュー一覧(左側のレビューを投稿したユーザー)で呼ばれるので頻度は高",
		refClassNames={
			"AjaxJsonComponentController",
			"ProductReviewDetailController"
			}
	)
	public SearchResult<CommunityUserDO> findReviewerBySKU(
			String sku, String excludeCommunityUserId, int limit, int offset, boolean excludeProduct);
	public SearchResult<CommunityUserDO> findReviewerBySKU(
			String sku, String excludeCommunityUserId, int limit, int offset);

	/**
	 * 指定した商品の質問に回答を書いたユーザーを返します。
	 * @param sku SKU
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.HIGH,
		frequencyComment="商品系各一覧左側(Q&Aに回答したユーザー)で呼ばれるので頻度は高",
		refClassNames={
			"AjaxJsonComponentController"
			}
	)
	public SearchResult<CommunityUserDO> findAnswererBySKU(
			String sku, int limit, int offset);

	public SearchResult<CommunityUserDO> findAnswererBySKU(
			String sku, int limit, int offset, boolean excludeProduct);

	/**
	 * SKUから商品のフォロワーのコミュニティユーザーを検索して返します。
	 * @param sku SKU
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @param asc 昇順ソート
	 * @return コミュニティユーザーのリスト
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.HIGH,
		frequencyComment="商品系各一覧左側(商品をフォローしているユーザー)で呼ばれるので頻度は高",
		refClassNames={
			"AjaxJsonComponentController"
			}
	)
	public SearchResult<CommunityUserDO> findProductFollowerBySKU(
			String sku, int limit, int offset, boolean asc);

	/**
	 * 指定した商品の画像（レビュー、質問、回答本文内含む）を投稿したユーザーを返します。
	 * @param sku SKU
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.HIGH,
		frequencyComment="商品系各一覧左側(画像を投稿したユーザー)で呼ばれるので頻度は高",
		refClassNames={
			""
			}
	)
	public SearchResult<CommunityUserDO> findAllTypeImagePostCommunityUserBySKU(
			String sku, int limit, int offset);

	/**
	 * 指定した商品の画像を投稿したユーザーを返します。
	 * @param sku SKU
	 * @param excludeCommunityUserId 除外するコミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.HIGH,
		frequencyComment="商品系各一覧左側(画像を投稿したユーザー)で呼ばれるので頻度は高",
		refClassNames={
			"AjaxJsonComponentController"
			}
	)
	public SearchResult<CommunityUserDO> findImagePostCommunityUserBySKU(
			String sku, String excludeCommunityUserId, int limit, int offset);

	/**
	 * 指定したコミュニティユーザーがフォローした商品に対してレビューを書いている
	 * ユーザーを返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.MEDIUM,
		frequencyComment="マイページ,ユーザ系各一覧右側(フォローした商品のレビューを書いているユーザー)で呼ばれるので頻度は中",
		refClassNames={
			"AjaxJsonComponentController"
			}
	)
	public SearchResult<CommunityUserDO> findReviewerByFollowProduct(
			String communityUserId, int limit, int offset);

	/**
	 * 指定したコミュニティユーザーがフォローした商品の質問に対して回答を書いている
	 * ユーザーを返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.MEDIUM,
		frequencyComment="マイページ,ユーザ系各一覧右側(フォローした商品のレビューを書いているユーザー)で呼ばれるので頻度は中",
		refClassNames={
			"AjaxJsonComponentController"
			}
	)
	public SearchResult<CommunityUserDO> findAnswererByFollowProduct(
			String communityUserId, int limit, int offset);

	/**
	 * 指定したコミュニティユーザーが投稿した質問に対して回答を書いている
	 * ユーザーを返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.MEDIUM,
		frequencyComment="マイページ,ユーザ系各一覧右側(あなたのQ/Aに回答したユーザー)で呼ばれるので頻度は中",
		refClassNames={
			"AjaxJsonComponentController"
			}
	)
	public SearchResult<CommunityUserDO> findAnswererByPostQuestion(
			String communityUserId, int limit, int offset);

	/**
	 * 指定したコミュニティユーザーがフォローした質問に対して回答を書いている
	 * ユーザーを返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.MEDIUM,
		frequencyComment="マイページ,ユーザ系各一覧右側(フォローした商品のQ/Aに回答したユーザー)で呼ばれるので頻度は中",
		refClassNames={
			"AjaxJsonComponentController"
			}
	)
	public SearchResult<CommunityUserDO> findAnswererByFollowQuestion(
			String communityUserId, int limit, int offset);


	/**
	 * 指定したコミュニティユーザーがフォローした商品の商品マスターである
	 * コミュニティユーザーを返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.MEDIUM,
		frequencyComment="マイページ,ユーザ系各一覧右側(フォローした商品の商品マスター)で呼ばれるので頻度は中",
		refClassNames={
			"AjaxJsonComponentController"
			}
	)
	public SearchResult<CommunityUserDO> findProductMasterByFollowProduct(
			String communityUserId, int limit, int offset);

	/**
	 * 指定したコミュニティユーザーが購入した商品に対してレビューを書いている
	 * ユーザーを返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @param publicSetting 非公開情報を表示する場合はtrue
	 * @return コミュニティユーザーのリスト
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.MEDIUM,
		frequencyComment="マイページ,ユーザ系各一覧右側(購入した商品のレビューを書いているユーザ)で呼ばれるので頻度は中",
		refClassNames={
			"AjaxJsonComponentController"
			}
	)
	public SearchResult<CommunityUserDO> findReviewerByPurchaseProduct(
			String communityUserId, int limit, int offset, boolean publicSetting);

	/**
	 * 指定したコミュニティユーザーが購入した商品の質問に対して回答を書いている
	 * ユーザーを返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @param publicSetting 非公開情報を表示する場合はtrue
	 * @return コミュニティユーザーのリスト
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.MEDIUM,
		frequencyComment="マイページ,ユーザ系各一覧右側(購入した商品のQ/Aに回答を書いているユーザ)で呼ばれるので頻度は中",
		refClassNames={
			"AjaxJsonComponentController"
			}
	)
	public SearchResult<CommunityUserDO> findAnswererByQuestionForPurchaseProduct(
			String communityUserId, int limit, int offset, boolean publicSetting);

	/**
	 * 指定したコミュニティユーザーが投稿したレビューに対していいねをしている
	 * ユーザーを返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.MEDIUM,
		frequencyComment="マイページ系各一覧右側(あなたのレビューにいいねしたユーザ)で呼ばれるので頻度は中",
		refClassNames={
			"AjaxJsonComponentController"
			}
	)	
	public SearchResult<CommunityUserDO> findLikeCommunityUserByReview(
			String communityUserId, int limit, int offset);

	/**
	 * 指定したコミュニティユーザーが投稿した回答に対していいねをしている
	 * ユーザーを返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.MEDIUM,
		frequencyComment="マイページ,ユーザ系各一覧右側(あなたのQ/A回答にいいねしたユーザ)で呼ばれるので頻度は中",
		refClassNames={
			"AjaxJsonComponentController"
		}
	)	
	public SearchResult<CommunityUserDO> findLikeCommunityUserByQuestionAnswer(
			String communityUserId, int limit, int offset);

	/**
	 * 指定したコミュニティユーザーが投稿した画像に対していいねをしている
	 * ユーザーを返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.MEDIUM,
		frequencyComment="マイページ,ユーザ系各一覧右側(あなたの画像にいいねしたユーザ)で呼ばれるので頻度は中",
		refClassNames={
			"AjaxJsonComponentController"
		}
	)
	public SearchResult<CommunityUserDO> findLikeCommunityUserByImage(
			String communityUserId, int limit, int offset);

	/**
	 * 指定したコミュニティユーザーが購入した商品をフォローしている
	 * ユーザーを返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @param publicSetting 非公開情報を表示する場合はtrue
	 * @return コミュニティユーザーのリスト
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.MEDIUM,
		frequencyComment="マイページ,ユーザ系各一覧右側(購入した商品をフォローしているユーザー)で呼ばれるので頻度は中",
		refClassNames={
			"AjaxJsonComponentController"
		}
	)
	public SearchResult<CommunityUserDO> findFollowerByPurchaseProduct(
			String communityUserId, int limit, int offset, boolean publicSetting);

	/**
	 * 指定した質問をフォローしているユーザーを返します。
	 * @param questionId 質問ID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.MEDIUM,
		frequencyComment="Q/A詳細左側(このQ/Aをフォローしている人)で呼ばれるので頻度は中",
		refClassNames={
			"AjaxJsonComponentController"
		}
	)
	public SearchResult<CommunityUserDO> findFollowerByQuestion(
			String questionId, int limit, int offset);

	/**
	 * 指定した（最近閲覧した）商品のレビューを書いているユーザーを返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.NONE,
		frequencyComment="カタログWebサービスが呼ばれるのでテスト対象外",
		refClassNames={
			"AjaxJsonComponentController"
		}
	)
	public SearchResult<CommunityUserDO> findReviewerByViewProducts(
			List<String> skus, int limit, int offset);

	/**
	 * 指定した（最近閲覧した）商品の質問に回答を書いている自分以外のユーザーを返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.NONE,
		frequencyComment="カタログWebサービスが呼ばれるのでテスト対象外",
		refClassNames={
			"AjaxJsonComponentController"
		}
	)
	public SearchResult<CommunityUserDO> findAnswererByViewProducts(
			List<String> skus, int limit, int offset);


	/**
	 * 指定したニックネームに部分一致するコミュニティユーザーを返します。<br />
	 * @param communityUserId コミュニティユーザーID
	 * @param keyword キーワード
	 * @param offsetUserName 検索済みユーザー名
	 * @param limit 最大取得件数
	 * @return コミュニティユーザーのリスト
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.MEDIUM,
		frequencyComment="ユーザー検索で呼ばれるので中",
		refClassNames={
			"AjaxJsonMypageSearchUserController"
		}
	)
	public SearchResult<CommunityUserSetVO> findCommunityUserByPartialMatch(
			String communityUserId, String keyword, String offsetUserName, int limit);
}
