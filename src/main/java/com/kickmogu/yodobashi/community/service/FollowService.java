/**
 *
 */
package com.kickmogu.yodobashi.community.service;

import java.util.Date;

import com.kickmogu.yodobashi.community.performance.PerformanceTest;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Frequency;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Type;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.service.vo.CommunityUserFollowVO;
import com.kickmogu.yodobashi.community.service.vo.ProductFollowVO;
import com.kickmogu.yodobashi.community.service.vo.QuestionFollowVO;

/**
 * フォローサービスです。
 * @author kamiike
 *
 */
public interface FollowService {

	/**
	 * フォローしているコミュニティユーザーを
	 * フォロー日時順（降順）に返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param followUserLimit フォローユーザーの最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return 検索結果
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.MEDIUM,
		frequencyComment="マイページ、ユーザページのフォロー一覧の初期表示ともっと見るクリック時のAjax処理で呼ばれるので頻度は中くらい",
		refClassNames={"AjaxJsonMypageFollowController","AjaxJsonUserFollowController","MypageFollowUserListController","MypageWithdrawConfirmController","UserFollowUserListController"}
	)
	public SearchResult<CommunityUserFollowVO> findFollowCommunityUser(
			String communityUserId, int limit, int followUserLimit, Date offsetTime,
			boolean previous);

	/**
	 * フォロワーとなっているコミュニティユーザーを
	 * をフォロー日時順（降順）に返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param followUserLimit フォローユーザーの最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return 検索結果
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.MEDIUM,
		frequencyComment="マイページ、ユーザページのフォロワー一覧の初期表示ともっと見るクリック時のAjax処理で呼ばれるので頻度は中くらい",
		refClassNames={"AjaxJsonMypageFollowController","AjaxJsonUserFollowController","MypageFollowUserListController","MypageWithdrawConfirmController","UserFollowUserListController"}
	)
	public SearchResult<CommunityUserFollowVO> findFollowerCommunityUser(
			String communityUserId, int limit,
			int followUserLimit, Date offsetTime, boolean previous);

	/**
	 * フォローしている商品情報をフォロー日時順（降順）に返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param followUserLimit フォローユーザーの最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return 検索結果
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.MEDIUM,
		frequencyComment="マイページ、ユーザページのフォロー商品一覧の初期表示ともっと見るクリック時のAjax処理で呼ばれるので頻度は中くらい",
		refClassNames={"AjaxJsonMypageFollowController","AjaxJsonUserFollowController","MypageFollowUserListController","MypageWithdrawConfirmController","UserFollowUserListController"}
	)
	public SearchResult<ProductFollowVO> findFollowProduct(
			String communityUserId, int limit,
			int followUserLimit, Date offsetTime, boolean previous);

	/**
	 * フォローしている質問情報をフォロー日時順（降順）に返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param followUserLimit フォローユーザーの最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return 検索結果
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.MEDIUM,
		frequencyComment="マイページ、ユーザページのフォローQ/A一覧の初期表示ともっと見るクリック時のAjax処理で呼ばれるので頻度は中くらい",
		refClassNames={"AjaxJsonMypageFollowController","AjaxJsonUserFollowController","MypageFollowUserListController","MypageWithdrawConfirmController","UserFollowUserListController"}
	)	
	public SearchResult<QuestionFollowVO> findFollowQuestion(
			String communityUserId, int limit,
			int followUserLimit, Date offsetTime, boolean previous);

	/**
	 * コミュニティユーザーをフォローします。
	 * @param communityUserId フォロワーとなるコミュニティユーザーID
	 * @param followCommunityUserId フォローするコミュニティユーザーID
	 * @param release フォローを解除する場合、true
	 * @return 成功した場合、true
	 */
	public boolean followCommunityUser(
			String communityUserId,
			String followCommunityUserId,
			boolean release);
	
	public boolean followCommunityUserByUserName(
			String communityUserId,
			String followCommunityUserName,
			boolean release);

	/**
	 * 指定のコミュニティユーザーがユーザーフォロー可能かを返します
	 * @param communityUserId
	 * @return　可能な場合true
	 */
	public boolean canFollowCommunityUser(String communityUserId);
	
	
	/**
	 * 商品をフォローします。
	 * @param communityUserId フォロワーとなるコミュニティユーザーID
	 * @param followProductId フォローする商品ID
	 * @param release フォローを解除する場合、true
	 * @return 成功した場合、true
	 */
	public boolean followProduct(
			String communityUserId,
			String followProductId,
			boolean release);

	/**
	 * 指定のコミュニティユーザーが商品フォロー可能かを返します
	 * @param communityUserId
	 * @return　可能な場合true
	 */
	public boolean canFollowProduct(String communityUserId);
	
	/**
	 * 質問をフォローします。
	 * @param communityUserId フォロワーとなるコミュニティユーザーID
	 * @param followQuestionId フォローする質問ID
	 * @param release フォローを解除する場合、true
	 * @return 成功した場合、true
	 */
	public boolean followQuestion(
			String communityUserId,
			String followQuestionId,
			boolean release);

	/**
	 * 指定のコミュニティユーザーが質問フォロー可能かを返します
	 * @param communityUserId
	 * @return　可能な場合true
	 */
	public boolean canFollowQuestion(String communityUserId);

	/**
	 * 指定のコミュニティユーザーのフォロー商品数を返します
	 * @param communityUserId
	 * @return フォロー商品数
	 */
	public long countFollowProduct(String communityUserId);
	
	/**
	 * 指定のコミュニティユーザーのフォロー質問数を返します
	 * @param communityUserId
	 * @return フォロー質問数
	 */
	public long countFollowQuestion(String communityUserId);
	
	/**
	 * 指定のコミュニティユーザーのフォローユーザー数を返します
	 * @param communityUserId
	 * @return　フォローユーザー数
	 */
	public long countFollowUser(String communityUserId);
	
	/**
	 * 指定のコミュニティユーザーのフォロワーユーザー数を返します
	 * @param communityUserId
	 * @return　フォロワーユーザー数
	 */
	public long countFollowerUser(String communityUserId);

}


