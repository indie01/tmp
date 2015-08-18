/**
 *
 */
package com.kickmogu.yodobashi.community.service;

import java.util.Date;
import java.util.List;

import com.kickmogu.yodobashi.community.performance.PerformanceTest;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Frequency;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Type;
import com.kickmogu.yodobashi.community.resource.domain.CommentDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommentTargetType;
import com.kickmogu.yodobashi.community.service.vo.CommentSetVO;

/**
 * コメントサービスです。
 * @author kamiike
 */
public interface CommentService extends CommonService{

	/**
	 * 指定したコメントを削除します。
	 * @param commentId コメントID
	 */
	public void deleteComment(String commentId);
	
	public void deleteComment(String commentId, boolean mngToolOperation);

	/**
	 * コメントを登録/更新します。
	 * @param comment コメント
	 * @return 登録したコメント
	 */
	public CommentDO saveComment(CommentDO comment);
	
	/**
	 * 
	 * @param targetType
	 * @param communityUserId
	 * @param contentId
	 * @param commentText
	 * @return
	 */
	public CommentDO saveComment(CommentTargetType targetType, CommunityUserDO communityUser, String contentId, String commentText);

	/**
	 * 指定したコメントをインデックス情報から返します。
	 * @param commentId コメントID
	 * @return コメント
	 */
	public CommentDO getCommentFromIndex(String commentId, boolean includeDeleteContents);

	/**
	 * 指定した画像に対するコメントを返します。
	 * @param imageId 画像ID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return コメントリスト
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.MEDIUM,
		frequencyComment="画像一覧の画像をクリックしたときのAjax処理でしか呼ばれないので頻度は中",
		refClassNames={"AjaxJsonProductCommonController"}
	)
	public long moreCountImageCommentByImageId(
			String contentsId, List<String> excludeCommentIds, Date offsetTime, boolean previous);

	/**
	 * 指定したレビューに対するコメントを返します。
	 * @param reviewId レビューID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return コメントリスト
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.HIGH,
		frequencyComment="商品レビュー詳細画面の初期表示、Ajax処理で呼ばれるので頻度は高",
		refClassNames={"AjaxJsonProductCommonController"}
	)
	public long moreCountReviewCommentByReviewId(
			String contentsId, List<String> excludeCommentIds, Date offsetTime, boolean previous);

	/**
	 * 指定した質問回答に対するコメントを返します。
	 * @param questionAnswerId 質問回答ID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return コメントリスト
	 */
	@PerformanceTest(type=Type.SELECT,
			frequency=Frequency.MEDIUM,
			frequencyComment="商品質問画面で回答のコメントをクリックしたときのAjax処理で呼ばれるので頻度は中",
			refClassNames={"AjaxJsonProductCommonController"}
		)
	public long moreCountQuestionAnswerCommentByQuestionAnswerId(
			String contentsId, List<String> excludeCommentIds, Date offsetTime, boolean previous);
	/**
	 * コメント情報をHbaseから取得し、周辺情報を付与して返します。
	 * SearchResult形式ですが、1件のみ返します
	 * hasAdultはfalse
	 * @param reviewId
	 * @return
	 */
	public SearchResult<CommentSetVO> loadCommentSet(String commentId);
	
	public CommentDO loadComment(String commentId);
	/**
	 * 画像のコメント数の取得
	 * @param imageId 画像ID
	 * @return コメント数
	 */
	public long loadImageCommentCount(String imageId);
	
}
