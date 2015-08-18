/**
 *
 */
package com.kickmogu.yodobashi.community.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.kickmogu.yodobashi.community.performance.PerformanceTest;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Frequency;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Type;
import com.kickmogu.yodobashi.community.resource.domain.QuestionAnswerDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionDO;
import com.kickmogu.yodobashi.community.resource.domain.ScoreFactorDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.service.vo.QuestionAnswerSetVO;
import com.kickmogu.yodobashi.community.service.vo.QuestionSetVO;
import com.kickmogu.yodobashi.community.service.vo.SimpleQuestionSetVO;

/**
 * 質問サービスです。
 * @author kamiike
 *
 */
public interface QuestionService {

	/**
	 * 指定した商品に対するQA情報件数を返します。
	 * @param sku SKU
	 * @return QA情報件数
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.SUPER_HIGH,
		frequencyComment="商品系の各画面で共通的に呼ばれるので頻度は極高",
		refClassNames={"AbstractProductBaseController"}
	)
	public long countQuestionBySku(String sku);
	
	/**
	 * 指定した商品に対するQA情報件数を返します。
	 * @param sku SKU
	 * @return QA情報件数
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.SUPER_HIGH,
		frequencyComment="商品系の各画面で共通的に呼ばれるので頻度は極高",
		refClassNames={"AbstractProductBaseController"}
	)
	public long countQuestionBySkus(List<String> skus);

	/**
	 * 指定した商品に対するQA情報件数リストを返します。
	 * @param skus SKUリスト
	 * @return QA情報件数リスト
	 */
	@PerformanceTest(type=Type.SELECT,
			frequency=Frequency.SUPER_HIGH,
			frequencyComment="商品系の各画面で共通的に呼ばれるので頻度は極高",
			refClassNames={"AbstractProductBaseController"}
	)
	public Map<String, Long> countQuestionBySkus(
			String[] skus);
	
	/**
	 * 指定した商品に対するQA情報を更新日時（質問・回答の最終更新日時）順（降順）に返します。
	 * @param sku SKU
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return QA情報一覧
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.HIGH,
		frequencyComment="商品質問一覧の初期表示ともっと見るAjaxで呼ばれるので頻度は高",
		refClassNames={
			"AjaxJsonProductQuestionController",
			"ProductQuestionListController"}
	)
	public SearchResult<QuestionSetVO> findUpdateQuestionBySku(
			String sku,
			String excludeQuestionId,
			int limit,
			Date offsetTime,
			boolean previous);

	/**
	 * 指定した商品に対する回答無しのQA情報を質問投稿日時順（降順）に返します。
	 * @param sku SKU
	 * @param excludeQuestionId 除外する質問ID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return QA情報一覧
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.HIGH,
		frequencyComment="商品質問一覧の初期表示で呼ばれるので頻度は高",
		refClassNames={
			"AjaxJsonProductQuestionController",
			"ProductQuestionListController"}
	)
	public SearchResult<QuestionSetVO> findNewQuestionBySku(
			String sku,
			String excludeQuestionId,
			int limit,
			Date offsetTime,
			boolean previous);
	
	/**
	 * 指定した商品に対する回答無しのQA情報を質問投稿日時順（降順）に返します。
	 * @param sku SKU
	 * @param excludeCommunityUserId 除外するコミュニティユーザーID
	 * @param excludeQuestionId 除外する質問ID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return QA情報一覧
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.HIGH,
		frequencyComment="商品質問一覧の初期表示で呼ばれるので頻度は高",
		refClassNames={
			"AjaxJsonProductQuestionController",
			"ProductQuestionListController"}
	)
	public SearchResult<QuestionSetVO> findNewQuestionBySku(
			String sku,
			String excludeCommunityUserId,
			String excludeQuestionId,
			int limit,
			Date offsetTime,
			boolean previous);

	/**
	 * 指定した商品に対する回答付のQA情報を盛り上がり順（降順）に返します。
	 * @param sku SKU
	 * @param limit 最大取得件数
	 * @param offsetQuestionScore 検索開始スコア
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return QA情報一覧
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.HIGH,
		frequencyComment="商品質問一覧の初期表示で呼ばれるので頻度は高",
		refClassNames={
			"AjaxJsonProductQuestionController",
			"ProductQuestionListController"}
	)
	public SearchResult<QuestionSetVO> findPopularQuestionBySku(
			String sku,
			String excludeQuestionId,
			int limit,
			Double offsetQuestionScore,
			Date offsetTime,
			boolean previous);

	
	
	/**
	 * 指定した商品に対するQA情報を更新日時（質問・回答の最終更新日時）順（降順）に返します。
	 * @param sku SKU
	 * @param skus バリエーション商品一覧
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return QA情報一覧
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.HIGH,
		frequencyComment="商品質問一覧の初期表示ともっと見るAjaxで呼ばれるので頻度は高",
		refClassNames={
			"AjaxJsonProductQuestionController",
			"ProductQuestionListController"}
	)
	public SearchResult<QuestionSetVO> findUpdateQuestionBySkus(
			String sku,
			List<String> skus,
			String excludeQuestionId,
			int limit,
			Date offsetTime,
			boolean previous);

	/**
	 * 指定した商品に対する回答無しのQA情報を質問投稿日時順（降順）に返します。
	 * @param sku SKU
	 * @param skus バリエーション商品一覧
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return QA情報一覧
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.HIGH,
		frequencyComment="商品質問一覧の初期表示で呼ばれるので頻度は高",
		refClassNames={
			"AjaxJsonProductQuestionController",
			"ProductQuestionListController"}
	)
	public SearchResult<QuestionSetVO> findNewQuestionBySkus(
			String sku,
			List<String> skus,
			String excludeQuestionId,
			int limit,
			Date offsetTime,
			boolean previous);
	
	/**
	 * 指定した商品に対する回答無しのQA情報を質問投稿日時順（降順）に返します。
	 * @param skus 商品SKU一覧
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return QA情報一覧
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.HIGH,
		frequencyComment="商品質問一覧の初期表示で呼ばれるので頻度は高",
		refClassNames={
			"AjaxJsonProductQuestionController",
			"ProductQuestionListController"}
	)
	public SearchResult<QuestionSetVO> findNewQuestionBySkus(
			List<String> skus,
			String excludeQuestionId,
			int limit,
			Date offsetTime,
			boolean previous);
	
	public SearchResult<QuestionSetVO> findNewQuestionBySkusInPurchaseProduct(
			String communityUserId, 
			String excludeQuestonId, 
			int limit,
			Date offsetTime);

	/**
	 * 指定した商品に対する回答付のQA情報を盛り上がり順（降順）に返します。
	 * @param sku SKU
	 * @param skus バリエーション商品一覧
	 * @param limit 最大取得件数
	 * @param offsetQuestionScore 検索開始スコア
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return QA情報一覧
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.HIGH,
		frequencyComment="商品質問一覧の初期表示で呼ばれるので頻度は高",
		refClassNames={
			"AjaxJsonProductQuestionController",
			"ProductQuestionListController"}
	)
	public SearchResult<QuestionSetVO> findPopularQuestionBySkus(
			String sku,
			List<String> skus,
			String excludeQuestionId,
			int limit,
			Double offsetQuestionScore,
			Date offsetTime,
			boolean previous);

	/**
	 * 指定した質問をインデックス情報から返します。<br />
	 * 回答情報は設定されません。
	 * @param questionId 質問ID
	 * @return 質問
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.SUPER_HIGH,
		frequencyComment="商品質問詳細,年齢認証,違反登録,フォローで呼ばれるので頻度は極高",
		refClassNames={
			"AjaxJsonFollowController",
			"SpamReportController",
			"AgeVerifyIntercepter"}
	)
	public QuestionSetVO getQuestionFromIndex(
			String questionId,boolean includeDeleteContents);

	/**
	 * 指定した質問をインデックス情報から返します。退会削除質問は取得しない<br />
	 * 回答情報は設定されません。
	 * @param questionId 質問ID
	 * @return 質問
	 */
	@PerformanceTest(type=Type.SELECT,
			frequency=Frequency.SUPER_HIGH,
			frequencyComment="商品質問詳細,年齢認証,違反登録,フォローで呼ばれるので頻度は極高",
			refClassNames={
				"ProductQuestionDetailController"
				}
		)
	QuestionSetVO getQuestionFromIndexExcludeWithdraw(String questionId,
			boolean includeDeleteContents);
	
	/**
	 * 指定した質問以外で、指定した商品の盛り上がっている質問を盛り上がり順
	 * （降順）に返します。
	 * @param sku SKU
	 * @param excudeQuestionId 除外する質問ID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return 盛り上がっている質問一覧
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.HIGH,
		frequencyComment="商品質問詳細の右サイドで呼ばれるので頻度は高",
		refClassNames={
			"AjaxJsonComponentController"}
	)
	public SearchResult<SimpleQuestionSetVO> findPopularQuestionExcudeQuestionId(
			String sku,
			String excudeQuestionId,
			int limit,
			int offset);

	/**
	 * 指定した質問以外で、指定した商品の質問を質問投稿日時順（降順）に返します。
	 * @param sku SKU
	 * @param excudeQuestionId 除外する質問ID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return 新着質問一覧
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.HIGH,
		frequencyComment="商品質問詳細の右サイドで呼ばれるので頻度は高",
		refClassNames={
			"AjaxJsonComponentController"}
	)
	public SearchResult<QuestionDO> findNewQuestionExcudeQuestionId(
			String sku,
			String excudeQuestionId,
			int limit,
			int offset);

	/**
	 * 指定したコミュニティユーザーが投稿した質問を質問投稿日時順（降順）に返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return 質問一覧
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.MEDIUM,
		frequencyComment="ユーザの質問一覧ともっとみるAjaxで呼ばれるので頻度は中",
		refClassNames={
			"AjaxJsonMypageActivityController",
			"AjaxJsonUserActivityController",
			"MypageActivityQuestionListController",
			"UserActivityQuestionListController"
			}
	)
	public SearchResult<QuestionSetVO> findQuestionByCommunityUserId(
			String communityUserId, int limit, Date offsetTime, boolean previous);

	/**
	 * 指定したコミュニティユーザーが投稿した一時保存質問を質問保存日時順（降順）に返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return 一時保存質問一覧
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.LOW,
		frequencyComment="マイページの一時保存質問一覧ともっとみるAjaxで呼ばれるので頻度は低",
		refClassNames={
			"AjaxJsonMypageSaveController",
			"MypageSaveQuestionListController"
			}
	)
	public SearchResult<QuestionSetVO> findTemporaryQuestionByCommunityUserId(
			String communityUserId, String excludeQuestionId, int limit, Date offsetTime, boolean previous);

	/**
	 * 指定したコミュニティユーザーが投稿した質問回答を回答投稿日時順（降順）に返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return 質問回答一覧
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.MEDIUM,
		frequencyComment="ユーザ,マイページの質問回答一覧ともっとみるAjaxで呼ばれるので頻度は中",
		refClassNames={
			"AjaxJsonMypageActivityController",
			"AjaxJsonUserActivityController",
			"MypageActivityAnswerListController",
			"UserActivityAnswerListController"
			}
	)
	public SearchResult<QuestionAnswerSetVO> findQuestionAnswerByCommunityUserId(
			String communityUserId, int limit, Date offsetTime, boolean previous);

	/**
	 * 指定したコミュニティユーザーが投稿した一時保存質問回答を
	 * 回答保存日時順（降順）に返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return 一時保存質問回答一覧
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.LOW,
		frequencyComment="マイページの一時保存質問回答一覧ともっとみるAjaxで呼ばれるので頻度は低",
		refClassNames={
			"AjaxJsonMypageSaveController",
			"MypageSaveAnswerListController"
			}
	)
	public SearchResult<QuestionAnswerSetVO> findTemporaryQuestionAnswerByCommunityUserId(
			String communityUserId, String excludeQuestionId, int limit, Date offsetTime, boolean previous);

	/**
	 * 指定したユーザーの購入商品の新着Q&A情報を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return 購入商品の新着Q&A情報
	 */
	//新着Q＆A
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.LOW,
		frequencyComment="マイページの新着Q＆AともっとみるAjaxで呼ばれるので頻度は低",
		refClassNames={
			"AjaxJsonMypageQuestionController",
			"MypageQuestionListController"
			}
	)
	public SearchResult<QuestionSetVO> findNewQuestionByPurchaseProduct(
			String communityUserId, int limit, Date offsetTime, boolean previous);

	/**
	 * 指定したコミュニティユーザーが、指定した商品に対して投稿した一時保存中の
	 * 質問を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param sku SKU
	 * @return 一時保存質問
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.RARE,
		frequencyComment="一時保存中の質問投稿で呼ばれるので頻度は稀",
		refClassNames={
			"AjaxJsonProductQuestionSubmitController"
			}
	)
	public QuestionDO getTemporaryQuestion(String communityUserId, String sku);


	/**
	 * 指定した質問を返します。
	 * @param questionId 質問ID
	 * @return 質問
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.RARE,
		frequencyComment="質問編集、質問回答一時保存,投稿で呼ばれるので頻度は稀",
		refClassNames={
			"AjaxJsonProductAnswerSubmitController",
			"AjaxJsonProductQuestionDetailController",
			"AjaxJsonProductQuestionSubmitController"
			}
	)
	public QuestionSetVO getQuestion(String questionId);

	/**
	 * 質問情報を登録します。
	 * @param question 質問情報
	 * @return 登録した質問情報
	 */
	public QuestionDO addQuestion(QuestionDO question);
	/**
	 * 質問情報を更新します。
	 * @param question 質問情報
	 * @return 登録した質問情報
	 */
	public QuestionDO modifyQuestion(QuestionDO question);
	/**
	 * 質問情報を登録します。
	 * @param question 質問情報
	 * @return 登録した質問情報
	 */
	public QuestionDO saveQuestion(QuestionDO question);

	/**
	 * 指定した質問を削除します。
	 * @param questionId 質問ID
	 */
	public void deleteQuestion(String questionId);

	public void deleteQuestion(String questionId, boolean mngToolOperation);

	/**
	 * 指定した質問回答をインデックス情報から返します。
	 * @param questionAnswerId 質問回答ID
	 * @return 質問回答
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.SUPER_HIGH,
		frequencyComment="商品質問詳細のAjax処理(コメントリスト表示,画像コメント投稿,コメントいいね件数表示部表示,コメント編集,いいね表示,いいね取り消し)違反報告で呼ばれるので頻度は極高",
		refClassNames={
			"AjaxJsonProductQuestionDetailController",
			"SpamReportController"
			}
	)
	public QuestionAnswerSetVO getQuestionAnswerFromIndex(String questionAnswerId, boolean includeDeleteContents);

	/**
	 * 指定した質問回答を返します。
	 * @param questionAnswerId 質問回答ID
	 * @return 質問回答
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.RARE,
		frequencyComment="商品質問詳細で回答投稿,いいね登録時に呼ばれるので頻度は稀",
		refClassNames={
			"AjaxJsonProductAnswerSubmitController",
			"AjaxJsonProductQuestionDetailController"
			}
	)
	public QuestionAnswerSetVO getQuestionAnswer(String questionAnswerId);

	/**
	 * 指定の質問に対して、指定したコミュニティユーザーが回答しているかどうか
	 * 返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param questionId 質問ID
	 * @return 回答している場合、true
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.HIGH,
		frequencyComment="商品質問詳細で初期表示時に呼ばれるので頻度は高",
		refClassNames={
			"ProductQuestionDetailController"
			}
	)
	public boolean hasQuestionAnswer(String communityUserId, String questionId);
	
	

	/**
	 * 指定した質問に対する、指定したコミュニティユーザーが一時保存した回答情報
	 * を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param questionId 質問ID
	 * @return 一時保存された質問回答情報
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.RARE,
		frequencyComment="商品質問詳細で回答エディター表示時に呼ばれるので頻度は稀",
		refClassNames={
			"AjaxJsonProductQuestionDetailController"
			}
	)
	public QuestionAnswerDO getTemporaryQuestionAnswer(String communityUserId, String questionId);
	/**
	 * 指定した商品、コミュニティユーザーに対する、一時保存した最新の回答情報を取得する。
	 * @param communityUserId コミュニティユーザーID
	 * @param sku 商品SKU
	 * @return 最新の一時保存回答情報
	 */
	public QuestionAnswerDO getNewTemporaryQuestionAnswerBySku(String communityUserId, String sku);
	/**
	 * 質問回答情報を登録します。
	 * @param questionAnswer 質問回答情報
	 * @return 登録された質問回答情報
	 */
	public QuestionAnswerDO addQuestionAnswer(QuestionAnswerDO questionAnswer);
	/**
	 * 質問回答情報を更新します。
	 * @param questionAnswer 質問回答情報
	 * @return 登録された質問回答情報
	 */
	public QuestionAnswerDO modifyQuestionAnswer(QuestionAnswerDO questionAnswer);
	/**
	 * 質問回答情報を保存します。
	 * @param questionAnswer 質問回答情報
	 * @return 登録された質問回答情報
	 */
	public QuestionAnswerDO saveQuestionAnswer(QuestionAnswerDO questionAnswer);

	/**
	 * 指定した質問回答を削除します。
	 * @param questionAnswerId 質問回答ID
	 */
	public void deleteQuestionAnswer(String questionAnswerId);

	public void deleteQuestionAnswer(String questionAnswerId, boolean mngToolOperation);

	/**
	 * 質問情報のスコア情報と閲覧数を更新します。
	 * @param targetDate 対象日付
	 * @param question 質問情報
	 * @param scoreFactor スコア係数
	 */
	public void updateQuestionScoreAndViewCountForBatch(
			Date targetDate,
			QuestionDO question,
			ScoreFactorDO scoreFactor);
	public void updateQuestionScoreAndViewCountForBatchBegin(int bulkSize);
	public void updateQuestionScoreAndViewCountForBatchEnd();

	/**
	 * 質問回答情報のスコア情報を更新します。
	 * @param targetDate 対象日付
	 * @param questionAnswer 質問回答情報
	 * @param scoreFactor スコア係数
	 */
	public void updateQuestionAnswerScoreForBatch(
			Date targetDate,
			QuestionAnswerDO questionAnswer,
			ScoreFactorDO scoreFactor);
	public void updateQuestionAnswerScoreForBatchBegin(int bulkSize);
	public void updateQuestionAnswerScoreForBatchEnd();

	/**
	 * 指定した質問に対して投稿した質問回答を回答投稿日時順（降順）に返します。
	 * @param questionId 質問ID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return 質問回答一覧
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.HIGH,
		frequencyComment="商品質問詳細の初期表示Ajaxで呼ばれるので頻度は高",
		refClassNames={
			"AjaxJsonProductQuestionDetailController",
			"ProductQuestionDetailController"
			}
	)
	public SearchResult<QuestionAnswerSetVO> findNewQuestionAnswerByQuestionId(
			String questionId, String excludeAnswerId, int limit, Date offsetTime, boolean previous);

	/**
	 * 指定した質問に対して投稿した質問回答を適合度順（降順）に返します。
	 * @param questionId 質問ID
	 * @param limit 最大取得件数
	 * @param offsetMatchScore 検索開始適合度
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return 質問回答一覧
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.HIGH,
		frequencyComment="商品質問詳細の初期表示Ajaxで呼ばれるので頻度は高",
		refClassNames={
			"AjaxJsonProductQuestionDetailController"
			}
	)
	public SearchResult<QuestionAnswerSetVO> findMatchQuestionAnswerByQuestionId(
			String questionId, String excludeAnswerId, int limit, Double offsetMatchScore,
			Date offsetTime, boolean previous);

	/**
	 * 質問情報をHbaseから取得し、周辺情報を付与して返します。
	 * SearchResult形式ですが、1件のみ返します
	 * hasAdultはfalse
	 * @param reviewId
	 * @return
	 */
	public SearchResult<QuestionSetVO> loadQuestionSet(String questionId);

	/**
	 * 質問情報をHbaseから取得し、周辺情報を付与して返します。
	 * SearchResult形式ですが、1件のみ返します
	 * hasAdultはfalse
	 * @param reviewId
	 * @return
	 */
	public SearchResult<QuestionAnswerSetVO> loadQuestionAnswerSet(String questionAnswerId);

	public boolean isShowQuestion(String questionId);

	public QuestionDO loadQuestion(String questionId);
	
	public QuestionAnswerDO loadQuestionAnswer(String questionAnswerId);
	
	public String findProductSku(String questionId);
	
	public String findProductSkuByAnswer(String questionAnswerId);

	/**
	 * 指定した商品に対するQA情報を回答なし・質問投稿日時順（降順）に返します。
	 * @param sku
	 * @param excludeQuestionId
	 * @param limit
	 * @param offsetTime
	 * @param previous
	 * @return
	 */
	public SearchResult<QuestionSetVO> findNewQuestionWithNotAnswerPriorityBySku(
			String sku,
			String excludeCommunityUserId,
			String excludeQuestionId,
			int limit,
			Date offsetTime,
			boolean previous);

}
