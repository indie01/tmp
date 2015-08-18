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
import com.kickmogu.yodobashi.community.resource.domain.QuestionDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionFollowDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;


/**
 * フォロー DAO です。
 * @author kamiike
 *
 */
public interface QuestionFollowDao {

	/**
	 * 指定したフォロワー、質問の質問フォロー情報
	 * が存在するか判定します。
	 * @param communityUserId フォロワーのコミュニティユーザーID
	 * @param followQuestionId フォローする質問ID
	 * @return フォロー済みの場合、true
	 */
	public boolean existsQuestionFollow(
			String communityUserId, String followQuestionId);

	/**
	 * 質問フォロー情報を新規に作成します。
	 * @param communityUserId フォロワーのコミュニティユーザーID
	 * @param followQuestionId フォローする質問ID
	 * @param followQuestionOwnerId フォローする質問オーナーID
	 * @param adult アダルト商品に対する質問かどうか
	 * @return 質問フォロー情報
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.HIGH, frequencyComment="質問フォローの登録の頻度は高い")
	public QuestionFollowDO createQuestionFollow(
			String communityUserId,
			String followQuestionId,
			String followQuestionOwnerId,
			boolean adult);

	/**
	 * 質問フォロー情報を削除します。
	 * @param communityUserId フォロワーのコミュニティユーザーID
	 * @param followQuestionId フォローする質問ID
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.MEDIUM, frequencyComment="質問フォローの削除の頻度は中くらい")
	public void deleteFollowQuestion(
			String communityUserId,
			String followQuestionId);

	/**
	 * 質問から質問のフォロワーのコミュニティユーザーを検索して返します。<br />
	 * 一時停止フラグ、アダルトフラグを無視し、フォロー日時の昇順で返します。
	 * @param questionId 質問ID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return フォロワーのコミュニティユーザーのリスト
	 */
	public SearchResult<CommunityUserDO> findFollowerCommunityUserByQuestionIdForIndex(
			String questionId, int limit,
			int offset);

	/**
	 * 質問から質問のフォロワーのコミュニティユーザーを検索して返します。
	 * @param questionId 質問ID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @param asc 昇順ソート
	 * @return フォロワーのコミュニティユーザーのリスト
	 */
	public SearchResult<CommunityUserDO> findFollowerCommunityUserByQuestionId(
			String questionId, int limit,
			int offset, boolean asc);

	/**
	 * 質問フォロー情報のインデックスを作成します。
	 * @param questionFollowId 質問フォローID
	 * @return 作成した場合、true
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.HIGH, frequencyComment="質問フォローの登録の頻度は高い")
	public boolean createQuestionFollowInIndex(
			String questionFollowId);

	/**
	 * 質問フォロー情報のインデックスを更新します。
	 * @param communityUserId フォロワーのコミュニティユーザーID
	 * @param followQuestionId フォローする質問ID
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="createQuestionFollowInIndexでやるのでテスト対象外")
	public void updateQuestionFollowInIndex(
			String communityUserId, String followQuestionId);

	/**
	 * 質問のフォロー情報マップを返します。
	 * @param communityUserId 対象となるコミュニティユーザーID
	 * @param questionIds 質問IDリスト
	 * @return 質問のフォロー情報マップ
	 */
	public Map<String, Boolean> loadQuestionFollowMap(
			String communityUserId, List<String> questionIds);

	/**
	 * フォローしている質問情報をフォロー日時順（降順）に返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return 検索結果
	 */
	public SearchResult<QuestionFollowDO> findFollowQuestion(
			String communityUserId, int limit, Date offsetTime, boolean previous);

	/**
	 * コミュニティユーザーIDからフォロー質問を検索して返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return フォロー質問のリスト
	 */
	public SearchResult<QuestionDO> findFollowQuestionByCommunityUserId(
			String communityUserId, int limit, int offset);
	
	
	public long countFollowQuestion(String communityUserId);

}
