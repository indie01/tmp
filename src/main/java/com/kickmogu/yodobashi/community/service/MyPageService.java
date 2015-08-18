package com.kickmogu.yodobashi.community.service;

import java.util.Date;
import java.util.List;

import com.kickmogu.yodobashi.community.performance.PerformanceTest;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Frequency;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Type;
import com.kickmogu.yodobashi.community.resource.domain.InformationDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.service.vo.MyPageInfoAreaVO;

/**
 * マイページサービスです。
 * @author kamiike
 */
public interface MyPageService {

	/**
	 * 指定したユーザーのマイページ向け共通情報エリア情報を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param purchaseProductLimit 購入商品の最大取得件数
	 * @param productMasterLimit 商品マスターの最大取得件数
	 * @param isAdmin 
	 * @param mypageInformationNoReadLimit 
	 * @return 共通情報エリア情報
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.MEDIUM,
		frequencyComment="マイページの共通情報エリア表示に共通で使われるので頻度は中",
		refClassNames={"MypageBaseController","SnsConnectController"}
	)
	public MyPageInfoAreaVO getMyPageInfoAreaByCommunityUserId(
			String communityUserId,
			int purchaseProductLimit,
			int productMasterLimit, 
			int informationNoReadLimit,
			boolean isAdmin);
	/**
	 * 指定したコミュニティユーザーに対する未読お知らせ情報を検索して、
	 * 登録日時順（降順）に返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return 検索結果
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.LOW,
		frequencyComment="マイページのニュースフィードの初期化処理に使われるので頻度は低",
		refClassNames={"MypageFeedsController"}
	)
	public SearchResult<InformationDO> findNoReadInformationByCommunityUserId(
			String communityUserId,
			int limit,
			int offset);

	/**
	 * 指定したコミュニティユーザーに対するお知らせ情報を検索して、
	 * 返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始日時
	 * @param previous より前を取得する場合、true
	 * @return 検索結果
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.LOW,
		frequencyComment="マイページのお知らせ一覧の初期化処理ともっと見るAJaxに使われるので頻度は低",
		refClassNames={"AjaxJsonMypageInformationController","MypageInformationController"}
	)
	public SearchResult<InformationDO> findInformationByCommunityUserId(
			String communityUserId,
			int limit,
			Date offsetTime,
			boolean previous);

	public SearchResult<InformationDO> findInformationByCommunityUserId(
			String communityUserId,
			int limit,
			Date offsetTime,
			boolean previous,
			boolean excludeProduct
			);
	
	/**
	 * 指定したお知らせを既読に更新します。
	 * @param informations
	 */
	public void updateAllRead(List<InformationDO> informations) ;

	/**
	 * 指定したコミュニティユーザーに対するお知らせ情報で未読のカウントを返します。
	 * @param communityUserId コミュニティユーザーID
	 * @return 未読カウント
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.NONE,
		frequencyComment="WebServiceから呼ばれるのでテスト対象外",
		refClassNames={"MypageWebServiceImpl"}
	)
	public long countNoReadInformation(String communityUserId);
}
