/**
 *
 */
package com.kickmogu.yodobashi.community.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.kickmogu.lib.core.resource.Path.Condition;
import com.kickmogu.yodobashi.community.performance.PerformanceTest;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Frequency;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Type;
import com.kickmogu.yodobashi.community.resource.domain.ActionHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.MailSettingDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.SocialMediaSettingDO;
import com.kickmogu.yodobashi.community.resource.domain.ValidateAuthSessionDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommunityUserStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.Verification;
import com.kickmogu.yodobashi.community.service.vo.CommunityUserFollowVO;
import com.kickmogu.yodobashi.community.service.vo.CommunityUserSetVO;
import com.kickmogu.yodobashi.community.service.vo.MailSettingCategoryVO;
import com.kickmogu.yodobashi.community.service.vo.NewsFeedVO;
import com.kickmogu.yodobashi.community.service.vo.UserPageInfoAreaVO;

/**
 * コミュニティユーザーサービスです。
 * @author kamiike
 *
 */
public interface UserService {

	/**
	 * 指定したユニバーサルセッションIDからコミュニティIDを発行します。
	 * @param universalSessionId ユニバーサルセッションID
	 * @return コミュニティID
	 */
	public String createCommunityIdByUniversalSessionId(String universalSessionId);

	/**
	 * 指定したユニバーサルセッションIDで保存されたコミュニティIDを返します。
	 * @param univerSessionId ユニバーサルセッションID
	 * @return コミュニティID
	 */
	public String getCommunityIdByUniversalSessionId(String universalSessionId);

	/**
	 * コミュニティIDに紐づくコミュニティユーザーを返します。
	 * @param communityId コミュニティID
	 * @param syncStatus ステータスを厳密に取得するかどうか
	 * @return コミュニティユーザー
	 */
	public CommunityUserDO getCommunityUserByCommunityId(String communityId, boolean syncStatus);

	/**
	 * コミュニティーユーザIDに紐づくコミュニティユーザーを返します。
	 * @param communityUserId
	 * @return
	 */
	public CommunityUserDO getCommunityUser(String communityUserId);
	
	/**
	 * コミュニティユーザーを新規登録します。
	 * @param communityUser コミュニティユーザー
	 * @param icOuterCustomerId IC外部顧客ID
	 * @return 登録されたコミュニティユーザー
	 */
	public CommunityUserDO createCommunityUser(
			CommunityUserDO communityUser,
			String icOuterCustomerId,
			String autoId,
			boolean agreement);

	/**
	 * 注文情報のサマリ処理を非同期で実行します。
	 * @param communityId コミュニティID
	 */
	public void callAggregateOrder(CommunityUserDO communityUser);

	/**
	 * 指定したユーザーのユーザーページ向け情報エリア情報を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param purchaseProductLimit 購入商品の最大取得件数
	 * @param productMasterLimit 商品マスターの最大取得件数
	 * @param imageLimit 画像の最大取得件数
	 * @return ユーザーページ向け共通情報エリア情報
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.HIGH,
		frequencyComment="ユーザページの共通共通情報エリア表示で使われるので頻度は高",
		refClassNames={
			"UserBaseController"
			}
	)
	public UserPageInfoAreaVO getUserPageInfoAreaByCommunityUserId(
			String communityUserId,
			int purchaseProductLimit,
			int productMasterLimit,
			int imageLimit);
	
	/**
	 * 指定したコミュニティユーザーの投稿系のアクティビティを投稿日時順（降順）に返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return アクティビティ一覧
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.MEDIUM,
		frequencyComment="マイページ,ユーザページの投稿系のアクティビティ一覧で使われるので頻度は中",
		refClassNames={
			"AjaxJsonMypageActivityController",
			"AjaxJsonUserActivityController",
			"MypageActivityListController",
			"UserActivityListController"
			}
	)
	public SearchResult<NewsFeedVO> findTimelineActivityByCommunityUserId(
			String communityUserId,
			int limit,
			Date offsetTime,
			boolean previous);

	/**
	 * 指定したアクション履歴が一時停止中かどうかを返します。
	 * @param actionHistory アクション履歴
	 * @param stopCommunityUserIds 一時停止中のコミュニティユーザーIDのリスト
	 * @return 一時停止中の場合、true
	 */
	//
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.MEDIUM,
		frequencyComment="マイページ,ユーザページのアクティビティ一覧で使われるので頻度は中",
		refClassNames={
			"ActivityDataMaker"
			}
	)
	public boolean isStop(ActionHistoryDO actionHistory,
			Set<String> stopCommunityUserIds);

	/**
	 * 指定したコミュニティユーザーのニュースフィードを検索開始時間より前、
	 * もしくはより後から取得して返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return ニュースフィード
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.SUPER_HIGH,
		frequencyComment="Feedで使われるので頻度は極高",
		refClassNames={
			"AjaxJsonMypageFeedsController",
			"AjaxJsonUserFeedsController",
			"MypageFeedsController",
			"UserFeedController"
			}
	)
	public SearchResult<NewsFeedVO> findNewsFeedByCommunityUserId(
			String communityUserId, int limit, Date offsetTime, boolean previous);

	/**
	 * 指定したニックネームのコミュニティユーザーIDを返します。
	 * @param communityName ニックネーム
	 * @return コミュニティユーザーID
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.SUPER_HIGH,
		frequencyComment="Feedで使われるので頻度は極高",
		refClassNames={
			"AjaxJsonComponentController",
			"AjaxJsonUserActivityController",
			"AjaxJsonUserController",
			"AjaxJsonUserFeedsController",
			"AjaxJsonUserFollowController",
			"AjaxJsonUserMasterController",
			"AjaxHtmlUserController",
			"UserBaseController"
		}
	)
	public String getCommunityUserIdByCommunityName(String communityName);
	
	/**
	 * 指定したニックネームのコミュニティユーザーを返します。
	 * @param communityName ニックネーム
	 * @return コミュニティユーザー
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.SUPER_HIGH,
		frequencyComment="ユーザーページで利用するため頻度は高い",
		refClassNames={
			"AjaxJsonUserFeedsController",
			"AjaxJsonUserFollowController",
			"AjaxJsonUserImageController",
			"AjaxJsonUserActivityController"
		}
	)
	public CommunityUserDO getCommunityUserByCommunityName(String communityName);

	/**
	 * 指定したニックネームのコミュニティユーザーを返します。
	 * @param communityName ニックネーム
	 * @return コミュニティユーザー
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.SUPER_HIGH,
		frequencyComment="画像をマウスオーバーしたとき呼ばれるので頻度は極高",
		refClassNames={
			"AjaxJsonFollowController",
			"AjaxHtmlUserController"
		}
	)
	public CommunityUserSetVO getCommunityUserSetByCommunityName(String communityName);

	/**
	 * 指定したニックネームのコミュニティユーザーのステータスを返します。
	 * @param communityName ニックネーム
	 * @return ステータス
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.HIGH,
		frequencyComment="ユーザ系ページのIntercepterで呼ばれるので頻度は高",
		refClassNames={
			"SuspendUserIntercepter"
		}
	)
	public CommunityUserStatus getCommunityUserStatusByCommunityName(String communityName);


	/**
	 * 指定したコミュニティユーザーIDのコミュニティユーザーのステータスを返します。
	 * @param communityUserId　コミュニティユーザーID
	 * @return ステータス
	 */
	public CommunityUserStatus loadCommunityUserStatusByCommunityUserId(String communityUserId);

	/**
	 * 指定したニックネームのコミュニティユーザーを返します。
	 * @param communityName ニックネーム
	 * @param sku sku
	 * @return コミュニティユーザー
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.SUPER_HIGH,
		frequencyComment="画像をマウスオーバーしたとき呼ばれるので頻度は極高",
		refClassNames={
			"AjaxHtmlUserController"
		}
	)
	public CommunityUserSetVO getCommunityUserByCommunityName(String communityName, String sku);

	/**
	 * 指定したIDのコミュニティユーザーを返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param sku sku
	 * @return コミュニティユーザー
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.HIGH,
		frequencyComment="商品画像詳細の初期表示で呼ばれるので頻度は高",
		refClassNames={
			"ProductImageDetailController"
		}
	)
	public CommunityUserSetVO getCommunityUserByCommunityUserId(String communityUserId, String sku);

	/**
	 * ハッシュ化されたコミュニティIDに紐づくコミュニティユーザーを返します。
	 * @param hashCommunityId ハッシュ化されたコミュニティID
	 * @return コミュニティユーザー
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.RARE,
		frequencyComment="ログイン系処理なので頻度は稀",
		refClassNames={
			"LoginController",
			"ProfileReAdmissionCompleteController",
			"AutoLoginIntercepter"
		}
	)
	public CommunityUserDO getCommunityUserByHashCommunityId(String hashCommunityId);

	public CommunityUserDO getCommunityUserByHashCommunityId(String hashCommunityId, Condition condition);
	/**
	 * コミュニティユーザーを新規登録します。
	 * @param universalSessionId ユニバーサルセッションID
	 * @param communityUser コミュニティユーザー
	 * @return 登録されたコミュニティユーザー
	 */
	public CommunityUserDO createCommunityUser(
			String universalSessionId,
			CommunityUserDO communityUser);

	/**
	 * ソーシャルネットワークのユーザーIDから該当するコミュニティユーザーを返します。
	 * @param providerId プロバイダーID
	 * @param providerUserIds ソーシャルネットワークのユーザーIDのリスト
	 * @return コミュニティユーザーのリスト
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.RARE,
		frequencyComment="友達招待の処理で呼ばれるので頻度は稀",
		refClassNames={
			"AjaxJsonMypageFriendInvitationController",
			"MypageFriendInvitationController"
		}
	)
	public Map<String, CommunityUserDO> findCommunityUserBySocialProviderUserIds(
			String providerId, Set<String> providerUserIds);

	/**
	 * 指定したコミュニティユーザーマップを返します。
	 * @param communityUserId コミュニティユーザー
	 * @param communityUserIds コミュニティユーザーリスト
	 * @param followUserLimit フォローユーザーの最大取得件数
	 * @return コミュニティユーザーマップ
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.RARE,
		frequencyComment="友達招待の処理で呼ばれるので頻度は稀",
		refClassNames={
			"AjaxJsonMypageFriendInvitationController",
			"MypageFriendInvitationController"
		}
	)
	public Map<String, CommunityUserFollowVO> loadCommunityUserMap(
			String communityUserId, List<String> communityUserIds,
			int followUserLimit);

	/**
	 * コミュニティユーザーを再登録します。
	 * @param communityUser コミュニティユーザー
	 * @return コミュニティユーザー
	 */
	public CommunityUserDO reCreateCommunityUser(CommunityUserDO communityUser, String catalogAutoId, boolean agreement);

	/**
	 * コミュニティユーザーの情報を更新します。
	 * @param communityUser コミュニティユーザー
	 * @return コミュニティユーザー
	 */
	public CommunityUserDO updateCommunityUser(CommunityUserDO communityUser);

	/**
	 * コミュニティ名が重複しているかチェックします。
	 * @param communityUserId コミュニティユーザーID
	 * @param commuityName コミュニティ名
	 * @return 重複する場合、true。ただし自身のニックネームの場合、false
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.RARE,
		frequencyComment="ユーザ登録更新系の処理で呼ばれるので頻度は稀",
		refClassNames={
			"AjaxJsonUserController",
			"MypageProfileUpdateConfirmController",
			"ProfileCreateCompleteController",
			"ProfileReAdmissionCompleteController"
		}
	)
	public boolean duplicateCommunityName(
			String communityUserId, String communityName);

	/**
	 * コミュニティ名が重複しているかチェックします。
	 * @param icOuterCustomerId IC外部顧客ID
	 * @param commuityName コミュニティ名
	 * @return 重複する場合、true。ただし自身のニックネームの場合、false
	 */
	public boolean duplicateCommunityNameForCreate(
			String icOuterCustomerId, String communityName);

	/**
	 * 指定したコミュニティユーザーのメール配信設定を返します。
	 * @param communityUserId コミュニティユーザー
	 * @return メール配信設定情報
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.RARE,
		frequencyComment="メール配信設定処理で呼ばれるので頻度は稀",
		refClassNames={
			"MypageMailInputController"
		}
	)
	public List<MailSettingCategoryVO> findMailSettingList(String communityUserId);

	/**
	 * メール設定を保存します。
	 * @param mailSettings メール設定
	 * @return 保存したメール設定
	 */
	public List<MailSettingDO> saveMailSettings(List<MailSettingDO> mailSettings);

	/**
	 * 指定したコミュニティユーザーにはじめてのヒント画面を表示するかを返します。
	 * @param communityUserId コミュニティユーザー
	 * @return 表示する場合、true
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.RARE,
		frequencyComment="ニュースフィードの初期処理で呼ばれるので頻度は稀",
		refClassNames={
			"MypageFeedsController"
		}
	)
	public boolean isShowWelcomeHint(String communityUserId);

	/**
	 * 指定したコミュニティユーザーにはじめてのヒント画面を表示させないように設定します。
	 * @param communityUserId コミュニティユーザー
	 */
	public void hideWelcomeHint(String communityUserId);

	/**
	 * 指定したコミュニティユーザーのSNS連携情報を返します。
	 * @param コミュニティID
	 * @return SNS連携情報
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.HIGH,
		frequencyComment="ソーシャルメディア連携設定ページ,ユーザページ系の共通情報エリアで呼ばれるので頻度は高",
		refClassNames={
			"MypageSnsInputController",
			"UserBaseController"
		}
	)
	public List<SocialMediaSettingDO> findSocialMediaSettingList(String communityUserId);

	/**
	 * 指定したコミュニティユーザーのSNS連携情報を保存します。
	 * @param socialMediaSettings メール設定
	 * @return 保存したメール設定
	 */
	public List<SocialMediaSettingDO> saveSocialMediaSetting(List<SocialMediaSettingDO> socialMediaSettings);

	/**
	 * 指定したコミュニティID（外部顧客ID）のユーザーのステータスを退会状態に
	 * 同期し、関連データも退会状態に更新します。
	 *
	 * @param communityId コミュニティID（外部顧客ID）
	 */
	public void syncCommunityUserStatusForWithdraw(String communityId);

	/**
	 * 停止状態を更新します。
	 * @param communityUserId コミュニティユーザーID
	 * @param stop 停止する場合、true
	 */
	public void updateStop(
			String communityUserId,
			boolean stop);

	/**
	 * 退会処理を行います。
	 * @param communityUserId コミュニティユーザーID
	 * @param force 強制退会フラグ
	 * @param reviewDelete 自身のレビュー＋自身のレビューに対するコメントを削除する場合、true
	 * @param qaDelete 自身の質問＋自身の回答＋自身の回答に関わるコメントを削除する場合、true
	 * @param imageDelete 自身の投稿画像＋自身の投稿画像に関わるコメントを削除する場合、true
	 * @param commentDelete 自身が投稿した全てのコメントを削除する場合、true
	 */
	public void withdraw(
			String communityUserId,
			boolean force,
			boolean reviewDelete,
			boolean qaDelete,
			boolean imageDelete,
			boolean commentDelete,
			boolean mngToolOperation);

	/**
	 * 退会に伴い、コミュニティユーザーのデータを削除します。
	 * @param communityUserId コミュニティユーザーID
	 * @param withdrawKey 退会キー
	 * @param force 強制退会フラグ
	 * @param reviewDelete 自身のレビュー＋自身のレビューに対するコメントを削除する場合、true
	 * @param qaDelete 自身の質問＋自身の回答＋自身の回答に関わるコメントを削除する場合、true
	 * @param imageDelete 自身の投稿画像＋自身の投稿画像に関わるコメントを削除する場合、true
	 * @param commentDelete 自身が投稿した全てのコメントを削除する場合、true
	 */
	public void deleteCommunityUserDataForWithdraw(
			String communityUserId,
			String withdrawKey,
			Boolean force,
			Boolean reviewDelete,
			Boolean qaDelete,
			Boolean imageDelete,
			Boolean commentDelete);

	/**
	 * アダルト表示確認ステータスを更新します。
	 * @param communityUserId コミュニティユーザーID
	 * @param verification アダルト表示確認ステータス
	 */
	public void updateAdultVerification(String communityUserId, Verification verification);

	/**
	 * CERO商品表示確認ステータスを更新します。
	 * @param communityUserId コミュニティユーザーID
	 * @param verification CERO商品表示確認ステータス
	 */
	public void updateCeroVerification(String communityUserId, Verification verification);

	/**
	 * 退会キャンセル処理を行います。
	 * @param communityUserId コミュニティユーザーID
	 * @return 正常に終了した場合、true
	 */
	public boolean cancelWithdraw(
			String communityUserId);

	/**
	 * コンテンツ投稿可能ステータスチェック
	 * @param communityUserId
	 * @return true 投稿可能
	 */
	public boolean validateUserStatusForPostContents(String communityUserId);

	public boolean existsCommunityUserFollow(String followCommunityUserId);

	public CommunityUserDO getCommunityUserByCommunityNameforMail(String communityName);
	
	public boolean catalogValidateAuthSession(String catalogAutoId);
	
	public ValidateAuthSessionDO catalogValidateAuthSession(String catalogAutoId, Map<String, String> params);
	
	public void authenticate(String catalogAutoId, String communityId);
	
	public void updateLastAccessTime(String autoId);
	
	public String loadCommunityUserIdByAutoId(String catalogAutoId);
	
	public void removeLogin(String autoId);
	
	public void modifyAuthenticate(String oldAutoId, String newAutoId);
}
