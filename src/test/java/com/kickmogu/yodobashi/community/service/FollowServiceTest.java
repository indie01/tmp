package com.kickmogu.yodobashi.community.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.solr.client.solrj.SolrQuery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.core.resource.Path.Condition;
import com.kickmogu.yodobashi.community.common.test.YcComJUnit4ClassRunner;
import com.kickmogu.yodobashi.community.resource.domain.ActionHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.InformationDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.constants.ActionHistoryType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ContentsStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.InformationType;
import com.kickmogu.yodobashi.community.service.config.ServiceConfig;
/**
 * フォローサービスのテストクラスです。
 * @author hirabayashi
 *
 */
@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration("/jcclientContext.xml")
public class FollowServiceTest extends BaseTest {
	/**
	 * フォローサービスです。
	 */
	@Autowired
	protected FollowService followService;

	/**
	 * 質問サービスです。
	 */
	@Autowired
	protected QuestionService questionService;

	protected CommunityUserDO communityUser;
	protected CommunityUserDO questionCommunityUser;

	protected CommunityUserDO followUser;
	protected ProductDO actionProduct;
	protected QuestionDO question;

	/**
	 * データを初期化します。
	 */
	protected void initialize() {
		// 親クラスのinitializeを呼び出す
		super.initialize();
	}

	/**
	 * 全ての情報を削除して、初期化します。
	 */
	@Before
	public void setup() {
		initialize();
		communityUser = createCommunityUser("communityUser", false);
	}

	/**
	 * コミュニティユーザーのフォローを検証します。
	 */
	@Test
	public void testFollowCommunityUser() {
		followUser = createCommunityUser("followUser", false);
		// フォローを検証します。
		testFollowCommunityUser(followUser.getCommunityUserId(), false, true);
		// 重複フォローを検証します。
		testFollowCommunityUser(followUser.getCommunityUserId(), false, false);
		// フォローの解除を検証します。
		testFollowCommunityUser(followUser.getCommunityUserId(), true, true);
		// フォローの解除を検証します。(フォローしていない状態で)
		testFollowCommunityUser(followUser.getCommunityUserId(), true, false);
	}

	/**
	 * 商品のフォローを検証します。
	 */
	@Test
	public void testFollowProduct() {
		actionProduct = product;
		// フォローを検証します。
		testFollowProduct(false, true);
		// 重複フォローを検証します。
		testFollowProduct( false, false);
		// フォローの解除を検証します。
		testFollowProduct(true, true);
		// フォローの解除を検証します。(フォローしていない状態で)
		testFollowProduct(true, false);
	}

	/**
	 * アダルト商品のフォローを検証します。
	 */
	@Test
	public void testFollowProductAdult() {
		actionProduct = product3;
		// フォローを検証します。
		testFollowProduct(false, true);
		// 重複フォローを検証します。
		testFollowProduct(false, false);
		// フォローの解除を検証します。
		testFollowProduct(true, true);
		// フォローの解除を検証します。(フォローしていない状態で)
		testFollowProduct(true, false);
	}

	/**
	 * CERO商品のフォローを検証します。
	 */
	@Test
	public void testFollowProductCero() {
		actionProduct = product4;
		// フォローを検証します。
		testFollowProduct(false, true);
		// 重複フォローを検証します。
		testFollowProduct(false, false);
		// フォローの解除を検証します。
		testFollowProduct(true, true);
		// フォローの解除を検証します。(フォローしていない状態で)
		testFollowProduct(true, false);
	}

	/**
	 * 質問のフォローを検証します。
	 */
	@Test
	public void testFollowQuestion() {
		questionCommunityUser = createCommunityUser("questionCommunityUser", false);
		QuestionDO questionSet = new QuestionDO();
		questionSet.setProduct(product);
		questionSet.setCommunityUser(questionCommunityUser);
		questionSet.setQuestionBody("質問本文");
		questionSet.setStatus(ContentsStatus.SUBMITTED);
		question = questionService.saveQuestion(questionSet);
		// フォローを検証します。
		testFollowQuestion(false, true);
		// 重複フォローを検証します。
		testFollowQuestion(false, false);
		// フォローの解除を検証します。
		testFollowQuestion(true, true);
		// フォローの解除を検証します。(フォローしていない状態で)
		testFollowQuestion(true, false);
	}

	/**
	 * コミュニティユーザーのフォローを検証します。
	 */
	private void testFollowCommunityUser(
			String followCommunityUserId,
			boolean release,
			boolean expectedSuccess) {
		boolean successful = followService.followCommunityUser(communityUser.getCommunityUserId(), followCommunityUserId, release);
		assertEquals(expectedSuccess, successful);
		// アクションヒストリーが追加されているか検証する
		testActionHistory(ActionHistoryType.USER_FOLLOW_USER, release);
		// お知らせが追加されているか検証する
		testFollowCommunityUserInformation(followCommunityUserId, release);
	}

	/**
	 * 商品のフォローを検証します。
	 */
	private void testFollowProduct(boolean release, boolean expectedSuccess) {
		boolean successful = followService.followProduct(communityUser.getCommunityUserId(), actionProduct.getSku(), release);
		assertEquals(expectedSuccess, successful);
		// アクションヒストリーが追加されているか検証する
		testActionHistory(ActionHistoryType.USER_FOLLOW_PRODUCT, release);
	}

	/**
	 * 質問のフォローを検証します。
	 */
	private void testFollowQuestion(boolean release, boolean expectedSuccess) {
		boolean successful = followService.followQuestion(communityUser.getCommunityUserId(), question.getQuestionId(), release);
		assertEquals(expectedSuccess, successful);
		// アクションヒストリーが追加されているか検証する
		testActionHistory(ActionHistoryType.USER_FOLLOW_QUESTION, release);
	}

	/**
	 * アクションヒストリーを取得・検証します。
	 */
	private void testActionHistory(ActionHistoryType actionHistoryType, boolean release){
		StringBuilder buffer = new StringBuilder();
		buffer.append("communityUserId_s:");
		buffer.append(communityUser.getCommunityUserId());
		SolrQuery query = new SolrQuery(buffer.toString());
		Condition path = Path.includeProp("*").includePath(
				"communityUser.communityUserId,product.sku,question.product.sku").depth(2);
		// アクションヒストリーをsolrから取得
		SearchResult<ActionHistoryDO> solrActionHistoris =
				new SearchResult<ActionHistoryDO>(solrOperations.findByQuery(query, ActionHistoryDO.class, path));
		if(!release) {
			// フォローしたとき
			// アクションヒストリーが重複されて登録されていないことを確認する
			assertEquals(1, solrActionHistoris.getDocuments().size());
			ActionHistoryDO solrActionHistory = solrActionHistoris.getDocuments().get(0);
			ActionHistoryDO hBaseActionHistory =
					hBaseOperations.load(ActionHistoryDO.class, solrActionHistory.getActionHistoryId(), path);
			checkActionHistory(hBaseActionHistory, actionHistoryType);
			checkActionHistory(solrActionHistory, actionHistoryType);
		} else {
			// フォロー外したとき
			// アクションヒストリーをHBaseから取得します。
			List<ActionHistoryDO> hBaseActionHistoris = hBaseOperations
					.scanWithIndex(ActionHistoryDO.class, "communityUserId", communityUser.getCommunityUserId(),
							hBaseOperations
									.createFilterBuilder(ActionHistoryDO.class)
									.appendSingleColumnValueFilter(
											"actionHistoryType", CompareOp.EQUAL,
											actionHistoryType).toFilter(), path);
			// アクションヒストリーが登録されていないことを確認する
			assertEquals(0, hBaseActionHistoris.size());
			assertEquals(0, solrActionHistoris.getDocuments().size());
		}
	}

	/**
	 * アクションヒストリーを検証します。
	 * @param actionHistory
	 */
	private void checkActionHistory(ActionHistoryDO actionHistory, ActionHistoryType actionHistoryType) {
		assertNotNull(actionHistory);
		assertEquals(actionHistoryType, actionHistory.getActionHistoryType());
		if(ActionHistoryType.USER_FOLLOW_USER == actionHistoryType){
			// ユーザーをフォローしたときの検証。
			assertEquals(followUser.getCommunityUserId(), actionHistory.getFollowCommunityUser().getCommunityUserId());
		} else if(ActionHistoryType.USER_FOLLOW_PRODUCT == actionHistoryType) {
			// 商品をフォローしたときの検証。
			assertEquals(actionProduct.getSku(), actionHistory.getProduct().getSku());
			assertEquals(actionProduct.isAdult(), actionHistory.getProduct().isAdult());
			assertEquals(actionProduct.isCero(), actionHistory.getProduct().isCero());
		} else if(ActionHistoryType.USER_FOLLOW_QUESTION == actionHistoryType) {
			// 質問をフォローしたときの検証。
			assertEquals(question.getQuestionId(), actionHistory.getQuestion().getQuestionId());
			assertEquals(question.getProduct().isAdult(), actionHistory.getQuestion().getProduct().isAdult());
			assertEquals(question.getProduct().isCero(), actionHistory.getQuestion().getProduct().isCero());
		}
	}

	/**
	 * コミュニティユーザーに紐付くお知らせを検証します。
	 * @param communityUserId コミュニティユーザーID
	 */
	private void testFollowCommunityUserInformation(String communityUserId, boolean release){
		StringBuilder buffer = new StringBuilder();
		buffer.append("communityUserId_s:");
		buffer.append(communityUserId);
		SolrQuery query = new SolrQuery(buffer.toString());
		Condition path = Path.includeProp("*").includePath("communityUserId.communityUserId").depth(1);

		SearchResult<InformationDO> solrInformations =
				new SearchResult<InformationDO>(solrOperations.findByQuery(query, InformationDO.class, path));
		assertNotNull(solrInformations.getDocuments());
		boolean existenceFlg = false;
		for(InformationDO information : solrInformations.getDocuments()){
			if(InformationType.FOLLOW == information.getInformationType()){
				existenceFlg = true;
				assertEquals(false, information.isReadFlag());
			}
		}
		if(!release){
			assertTrue(existenceFlg);
		}else{
			assertEquals(false, existenceFlg);
		}
	}


	
	@Autowired
	private ServiceConfig serviceConfig; 
	
	@Test
	public void testCanFollowQuestion(){
		
		serviceConfig.followQuestionLimit = 10;
		CommunityUserDO submitCommunityUser = createCommunityUser("submitCommunityUser", false);
		CommunityUserDO followCommunityUser = createCommunityUser("FollowQuestionCommunityUser", false);
		ProductDO product = productDao.loadProduct("100000001000624829");
		assertEquals(true, followService.canFollowQuestion(followCommunityUser.getCommunityUserId()));
		for(int i=1;i<serviceConfig.followQuestionLimit;i++){
			QuestionDO question = new QuestionDO();
			question.setCommunityUser(submitCommunityUser);
			question.setProduct(product);
			question.setStatus(ContentsStatus.SUBMITTED);
			question.setQuestionBody("質問本文");
			question = questionService.saveQuestion(question);
			followService.followQuestion(followCommunityUser.getCommunityUserId(), question.getQuestionId(), false);

		}
		assertEquals(true, followService.canFollowQuestion(followCommunityUser.getCommunityUserId()));

		QuestionDO question = new QuestionDO();
		question.setCommunityUser(submitCommunityUser);
		question.setProduct(product);
		question.setStatus(ContentsStatus.SUBMITTED);
		question.setQuestionBody("質問本文");
		question = questionService.saveQuestion(question);
		followService.followQuestion(followCommunityUser.getCommunityUserId(), question.getQuestionId(), false);

		assertEquals(false, followService.canFollowQuestion(followCommunityUser.getCommunityUserId()));

		
	}
	@Test
	public void testCanFollowProduct(){
		
		String[] skus = 
				{"100000001000624829",
				"200000002000012355",
				"100000009000738581",
				"100000001001391026",
				"100000001000827415",
				"100000001000937211",
				"100000001000965304",
				"100000001001033690",
				"100000001001074944",
				"100000001001079012",
				"100000001001188153",
				"100000001001220335",
				"100000001001325327",
				"100000001001363481",
				"100000001001237299",
				"100000001001377916",
				"100000001001377918",
				"100000001001274334"};
		
		serviceConfig.followProductLimit = 12;
		CommunityUserDO followCommunityUser1 = createCommunityUser("FollowProductCommunityUser1", false);
		CommunityUserDO followCommunityUser2 = createCommunityUser("FollowProductCommunityUser2", false);
		CommunityUserDO followCommunityUser3 = createCommunityUser("FollowProductCommunityUser3", false);
		assertEquals(true, followService.canFollowProduct(followCommunityUser1.getCommunityUserId()));
		int productCount = 0;
		for(int i=1;i<serviceConfig.followProductLimit;i++){
			ProductDO product = productDao.loadProduct(skus[productCount]);
			followService.followProduct(followCommunityUser1.getCommunityUserId(), product.getSku(), false);
			followService.followProduct(followCommunityUser2.getCommunityUserId(), product.getSku(), false);
			followService.followProduct(followCommunityUser3.getCommunityUserId(), product.getSku(), false);
			productCount++;
		}
		assertEquals(true, followService.canFollowProduct(followCommunityUser1.getCommunityUserId()));

		ProductDO product = productDao.loadProduct(skus[productCount]);
		followService.followProduct(followCommunityUser1.getCommunityUserId(), product.getSku(), false);
		followService.followProduct(followCommunityUser2.getCommunityUserId(), product.getSku(), false);
		followService.followProduct(followCommunityUser3.getCommunityUserId(), product.getSku(), false);

		assertEquals(false, followService.canFollowProduct(followCommunityUser1.getCommunityUserId()));
	}

	@Test
	public void testCanFollowCommunityUser(){
		
		serviceConfig.followCommunityUserLimit= 15;
		CommunityUserDO followCommunityUser = createCommunityUser("FollowCommunityUserCommunityUser", false);
		assertEquals(true, followService.canFollowCommunityUser(followCommunityUser.getCommunityUserId()));
		int communityCountCount = 0;
		
		for(int i=1;i<serviceConfig.followCommunityUserLimit;i++){
			CommunityUserDO communityUser = createCommunityUser("FollowCommunityUserCommunityUser" + communityCountCount, false);
			followService.followCommunityUser(followCommunityUser.getCommunityUserId(), communityUser.getCommunityUserId(), false);
			communityCountCount++;
		}
		assertEquals(true, followService.canFollowCommunityUser(followCommunityUser.getCommunityUserId()));

		CommunityUserDO communityUser = createCommunityUser("FollowCommunityUserCommunityUser" + communityCountCount, false);
		followService.followCommunityUser(followCommunityUser.getCommunityUserId(), communityUser.getCommunityUserId(), false);

		assertEquals(false, followService.canFollowCommunityUser(followCommunityUser.getCommunityUserId()));
	}

}
