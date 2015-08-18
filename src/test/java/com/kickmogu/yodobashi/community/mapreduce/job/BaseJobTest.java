/**
 *
 */
package com.kickmogu.yodobashi.community.mapreduce.job;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;

import com.kickmogu.lib.core.ssh.SshUtils;
import com.kickmogu.yodobashi.community.resource.dao.ProductMasterDao;
import com.kickmogu.yodobashi.community.resource.dao.RequestScopeDao;
import com.kickmogu.yodobashi.community.resource.domain.CommentDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.DailyScoreFactorDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductMasterDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionAnswerDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.UniqueUserViewCountDO;
import com.kickmogu.yodobashi.community.resource.domain.VersionDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.AccessUserType;
import com.kickmogu.yodobashi.community.resource.domain.constants.AlsoBuyProduct;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommentTargetType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ContentsStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.ProductSatisfaction;
import com.kickmogu.yodobashi.community.resource.domain.constants.ReviewType;
import com.kickmogu.yodobashi.community.service.BaseTest;
import com.kickmogu.yodobashi.community.service.CommentService;
import com.kickmogu.yodobashi.community.service.FollowService;
import com.kickmogu.yodobashi.community.service.LikeService;
import com.kickmogu.yodobashi.community.service.ProductService;
import com.kickmogu.yodobashi.community.service.QuestionService;
import com.kickmogu.yodobashi.community.service.ReviewService;
import com.kickmogu.yodobashi.community.service.log.AccessLogHelper;

/**
 * ジョブテストのベースクラスです。
 * @author kamiike
 *
 */
public class BaseJobTest extends BaseTest {

	/**
	 * レビューサービスです。
	 */
	@Autowired
	protected ReviewService reviewService;

	/**
	 * 質問サービスです。
	 */
	@Autowired
	protected QuestionService questionService;

	/**
	 * いいねサービスです。
	 */
	@Autowired
	protected LikeService likeService;

	/**
	 * コメントサービスです。
	 */
	@Autowired
	protected CommentService commentService;

	/**
	 * フォローサービスです。
	 */
	@Autowired
	protected FollowService followService;

	/**
	 * 商品マスター DAO です。
	 */
	@Autowired
	protected ProductMasterDao productMasterDao;

	/**
	 * リクエストスコープ DAO です。
	 */
	@Autowired
	protected RequestScopeDao requestScopeDao;

	/**
	 * 商品サービスです。
	 */
	@Autowired
	protected ProductService productService;

	/**
	 * アプリケーションコンテキストです。
	 */
	@Autowired
	protected ApplicationContext applicationContext;

	/**
	 * MRコンフィグです。
	 */
	@Autowired
	protected CommunityMapReduceConfig mapReduceConfig;

	/**
	 * コミュニティユーザーです。
	 */
	protected CommunityUserDO communityUser;

	/**
	 * コミュニティユーザーです。
	 */
	protected CommunityUserDO communityUser2;

	/**
	 * レビューです。
	 */
	protected ReviewDO review;

	/**
	 * 質問です。
	 */
	protected QuestionDO question;

	/**
	 * 質問回答です。
	 */
	protected QuestionAnswerDO questionAnswer;

	/**
	 * 画像リストです。
	 */
	protected List<ImageHeaderDO> imageHeaders;

	/**
	 * Hadoop の bin ディレクトリです。
	 */
	@Value("${server.hadoopBin}")
	public String hadoopBin;

	/**
	 * ランダムインスタンスです。
	 */
	protected static Random random;

	static {
		try {
			random = SecureRandom.getInstance("SHA1PRNG", "SUN");
		} catch (Exception e) {
			random = new Random();
		}
	}

	/**
	 * ログディレクトリフォーマットです。
	 */
	protected SimpleDateFormat dirFormatter = new SimpleDateFormat("yyyy-MM-dd");

	/**
	 * ログ日付フォーマットです。
	 */
	protected SimpleDateFormat logFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	/**
	 * 対象とする時間文字列です。
	 */
	protected String targetTimeString = null;

	/**
	 * データを初期化します。
	 */
	protected void initialize() {
		super.initialize();
		hBaseOperations.physicalDeleteAll(ImageHeaderDO.class);
		hBaseOperations.physicalDeleteAll(ImageDO.class);
		hBaseOperations.physicalDeleteAll(VersionDO.class);
		hBaseOperations.physicalDeleteAll(ProductMasterDO.class);
		hBaseOperations.physicalDeleteAll(QuestionAnswerDO.class);
		hBaseOperations.physicalDeleteAll(QuestionDO.class);
		hBaseOperations.physicalDeleteAll(UniqueUserViewCountDO.class);
		hBaseOperations.physicalDeleteAll(DailyScoreFactorDO.class);

		solrOperations.deleteAll(ImageHeaderDO.class);
		solrOperations.deleteAll(ProductMasterDO.class);
		solrOperations.deleteAll(QuestionAnswerDO.class);
		solrOperations.deleteAll(QuestionDO.class);
		solrOperations.deleteAll(UniqueUserViewCountDO.class);
	}
	
	/**
	 * ユーザーデータを作成します。
	 */
	protected void createUser() {
		communityUser = createCommunityUser("actionUser", false);
		communityUser2 = createCommunityUser("actionUser2", false);
	}

	/**
	 * 注文データを作成します。
	 */
	protected void createOrder() {
		product = productService.getProductBySku("200000002000012355").getProduct();
		Date salesDate = getDate("2011/10/01");
		// 注文履歴を登録します。
		createReceipt(communityUser, product.getJan(), salesDate);
		createReceipt(communityUser2, product.getJan(), salesDate);
	}

	/**
	 * レビューデータを作成します。
	 */
	protected void createReview() {
		review = new ReviewDO();
		review.setReviewType(ReviewType.REVIEW_AFTER_FEW_DAYS);
		review.setStatus(ContentsStatus.SUBMITTED);
		review.setCommunityUser(communityUser);
		review.setProduct(product);
		
		createImage(communityUser);
		createImageTwo(communityUser);
		String tempImageUrl = resourceConfig.temporaryImageUrl + image.getImageId();
		String tempImageUrl2 = resourceConfig.temporaryImageUrl + image2.getImageId();
		StringBuilder html = new StringBuilder();
		html.append("<img src=\"" + tempImageUrl + "\">");
		html.append("<script type=\"text/javascript\">alert('01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789本文" + "')</script>");
		html.append("<img src=\"" + tempImageUrl2 + "\">");
		review.setReviewBody(html.toString());
		review.setAlsoBuyProduct(AlsoBuyProduct.MAYNOTBUY);
		review.setProductSatisfaction(ProductSatisfaction.FOUR);
		reviewService.saveReview(review);
	}

	/**
	 * 質問データを作成します。
	 */
	protected void createQuestion() {
		question = new QuestionDO();
		question.setCommunityUser(communityUser);
		question.setProduct(product);
		question.setStatus(ContentsStatus.SUBMITTED);
		
		createImage(communityUser);
		createImageTwo(communityUser);
		String tempImageUrl = resourceConfig.temporaryImageUrl + image.getImageId();
		String tempImageUrl2 = resourceConfig.temporaryImageUrl + image2.getImageId();
		StringBuilder html = new StringBuilder();
		html.append("<img src=\"" + tempImageUrl + "\">");
		html.append("<script type=\"text/javascript\">alert('01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789本文" + "')</script>");
		html.append("<img src=\"" + tempImageUrl2 + "\">");
		question.setQuestionBody(html.toString());

		//質問を投稿で登録します。
		questionService.saveQuestion(question);
	}

	/**
	 * 質問回答データを作成します。
	 */
	protected void createQuestionAnswer() {
		questionAnswer = new QuestionAnswerDO();
		createImage(communityUser);
		createImageTwo(communityUser);
		String tempImageUrl = resourceConfig.temporaryImageUrl + image.getImageId();
		String tempImageUrl2 = resourceConfig.temporaryImageUrl + image2.getImageId();
		StringBuilder html = new StringBuilder();
		html.append("<img src=\"" + tempImageUrl + "\">");
		html.append("<script type=\"text/javascript\">alert('01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789本文" + "')</script>");
		html.append("<img src=\"" + tempImageUrl2 + "\">");

		questionAnswer.setAnswerBody(html.toString());
		questionAnswer.setCommunityUser(communityUser);
		questionAnswer.setQuestion(question);
		questionAnswer.setStatus(ContentsStatus.SUBMITTED);
		questionService.saveQuestionAnswer(questionAnswer);
	}

	/**
	 * 画像データを作成します。
	 */
	protected void createImages() {
		imageHeaders = new ArrayList<ImageHeaderDO>();

		for (int i = 0; i < 4; i++) {
			ImageHeaderDO imageHeader = new ImageHeaderDO();
			imageHeaders.add(imageHeader);
			ImageDO image = new ImageDO();
			image.setData(testImageData);
			image.setMimeType("images/jpeg");
			image.setTemporaryKey("test");
			image.setWidth(400);
			image.setHeigth(500);
			image.setCommunityUserId(communityUser.getCommunityUserId());
			imageService.createTemporaryImage(image);
			imageHeader.setImageId(image.getImageId());
		}

		requestScopeDao.initialize(communityUser, null);
		imageService.saveImageSet(product.getSku(), imageHeaders, null);

		List<ImageHeaderDO> imageHeaders2 = new ArrayList<ImageHeaderDO>();
		for (int i = 0; i < 1; i++) {
			ImageHeaderDO imageHeader = new ImageHeaderDO();
			imageHeaders2.add(imageHeader);
			ImageDO image = new ImageDO();
			image.setData(testImageData);
			image.setMimeType("images/jpeg");
			image.setTemporaryKey("test");
			image.setWidth(400);
			image.setHeigth(500);
			image.setCommunityUserId(communityUser2.getCommunityUserId());
			imageService.createTemporaryImage(image);
			imageHeader.setImageId(image.getImageId());
		}
		requestScopeDao.initialize(communityUser2, null);
		imageService.saveImageSet(product.getSku(), imageHeaders2, null);
	}

	/**
	 * いいねを作成します。
	 */
	protected void createLike() {
		if (review != null) {
			likeService.updateLikeReview(
					communityUser2.getCommunityUserId(),
					review.getReviewId(), false);
		}
		if (questionAnswer != null) {
			likeService.updateLikeQuestionAnswer(
					communityUser2.getCommunityUserId(),
					questionAnswer.getQuestionAnswerId(), false);
		}
		if (imageHeaders != null) {
			likeService.updateLikeImage(
					communityUser2.getCommunityUserId(),
					imageHeaders.get(0).getImageId(), false);
		}
	}

	/**
	 * コメントを作成します。
	 */
	protected void createComment() {
		if (review != null) {
			CommentDO comment = new CommentDO();
			comment.setTargetType(CommentTargetType.REVIEW);
			comment.setCommentBody("");
			comment.setReview(review);
			comment.setCommunityUser(communityUser2);
			commentService.saveComment(comment);
		}
		if (questionAnswer != null) {
			CommentDO comment = new CommentDO();
			comment.setTargetType(CommentTargetType.QUESTION_ANSWER);
			comment.setCommentBody("");
			comment.setQuestionAnswer(questionAnswer);
			comment.setCommunityUser(communityUser2);
			commentService.saveComment(comment);
		}
		if (imageHeaders != null) {
			CommentDO comment = new CommentDO();
			comment.setTargetType(CommentTargetType.IMAGE);
			comment.setCommentBody("");
			comment.setImageHeader(imageHeaders.get(0));
			comment.setCommunityUser(communityUser2);
			commentService.saveComment(comment);
		}
	}

	/**
	 * フォロー情報を作成します。
	 */
	protected void createFollow() {
		followService.followCommunityUser(
				communityUser2.getCommunityUserId(),
				communityUser.getCommunityUserId(), false);
		if (question != null) {
			followService.followQuestion(
					communityUser2.getCommunityUserId(),
					question.getQuestionId(), false);
		}
	}

	/**
	 * 閲覧ログをアップロードします。
	 */
	protected void uploadViewLog() throws Exception {

		targetTimeString = dirFormatter.format(getTargetTime());
		List<String> logs = new ArrayList<String>();

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(
					new OutputStreamWriter(outputStream, "UTF-8"));

			if (review != null) {
				addLog(logs,
						AccessLogHelper.getReviewShowLog(AccessUserType.MEMBER, "",
						communityUser.getCommunityUserId(),
						review.getReviewId(),
						review.getProduct().getSku(),
						review.getCommunityUser().getCommunityUserId()));

				addLog(logs,
						AccessLogHelper.getReviewShowLog(AccessUserType.GUEST, "",
						"test",
						review.getReviewId(),
						review.getProduct().getSku(),
						review.getCommunityUser().getCommunityUserId()));
			}

			if (question != null) {
				addLog(logs,
						AccessLogHelper.getQuestionShowLog(AccessUserType.MEMBER, "",
						communityUser.getCommunityUserId(),
						question.getQuestionId(),
						question.getProduct().getSku(),
						question.getCommunityUser().getCommunityUserId()));

				addLog(logs,
						AccessLogHelper.getQuestionShowLog(AccessUserType.GUEST, "",
						"test",
						question.getQuestionId(),
						question.getProduct().getSku(),
						question.getCommunityUser().getCommunityUserId()));
			}

			if (imageHeaders != null) {
				addLog(logs,
						AccessLogHelper.getImageShowLog(AccessUserType.MEMBER, "",
						communityUser.getCommunityUserId(),
						imageHeaders.get(0).getImageSetId(),
						imageHeaders.get(0).getSku(),
						imageHeaders.get(0).getOwnerCommunityUserId()));

				addLog(logs,
						AccessLogHelper.getImageShowLog(AccessUserType.GUEST, "",
						"test",
						imageHeaders.get(0).getImageSetId(),
						imageHeaders.get(0).getSku(),
						imageHeaders.get(0).getOwnerCommunityUserId()));
			}

			Collections.shuffle(logs, random);
			for (String log : logs) {
				printLog(writer, log);
			}

		} finally {
			if (writer != null) {
				writer.close();
			}
		}

		String saveDir = mapReduceConfig.accessLogServerSaveDir
				+ "/" + targetTimeString;
		saveDir = saveDir.replace("//", "/");
		SshUtils.command(
				mapReduceConfig.accessLogServerHost,
				mapReduceConfig.accessLogServerUser,
				mapReduceConfig.accessLogServerPassword,
				"rm -fR " + saveDir);
		SshUtils.command(
				mapReduceConfig.accessLogServerHost,
				mapReduceConfig.accessLogServerUser,
				mapReduceConfig.accessLogServerPassword,
				"mkdir -p " + saveDir);
		SshUtils.scpUpload(
				mapReduceConfig.accessLogServerHost,
				mapReduceConfig.accessLogServerUser,
				mapReduceConfig.accessLogServerPassword,
				outputStream.toByteArray(),
				saveDir,
				"test.log");

		String hadoopDir = (mapReduceConfig.uniqueUserViewLogCountJobInput
				+ "/" + targetTimeString).replace("//", "/");
		try {
			SshUtils.command(
					mapReduceConfig.accessLogServerHost,
					mapReduceConfig.accessLogServerUser,
					mapReduceConfig.accessLogServerPassword,
					"cd " + hadoopBin + " ; ./hadoop fs -rmr " + hadoopDir);
		} catch (Exception e) {
			//ignore
		}
		SshUtils.command(
				mapReduceConfig.accessLogServerHost,
				mapReduceConfig.accessLogServerUser,
				mapReduceConfig.accessLogServerPassword,
				"cd " + hadoopBin + " ; ./hadoop fs -mkdir " + hadoopDir);
		SshUtils.command(
				mapReduceConfig.accessLogServerHost,
				mapReduceConfig.accessLogServerUser,
				mapReduceConfig.accessLogServerPassword,
				"cd " + hadoopBin + " ; ./hadoop fs -copyFromLocal " + saveDir + "/test.log " + hadoopDir);
	}


	/**
	 * 対象時間を取得します。
	 * @return 対象時間
	 */
	private Date getTargetTime() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -1);
		return calendar.getTime();
	}

	/**
	 * ログを追加します。
	 * @param logs ログリスト
	 * @param log ログ
	 * @throws Exception 例外が発生した場合
	 */
	private void addLog(
			List<String> logs,
			String log) throws Exception {
		int count = random.nextInt(20);
		count++;
		for (int i = 0; i < count; i++) {
			StringBuilder buffer = new StringBuilder();
			buffer.append(logFormatter.format(getTargetTime()));
			buffer.append("\t");
			buffer.append(log);
			logs.add(buffer.toString());
		}
	}

	/**
	 * ログを書き込みます。
	 * @param writer ライター
	 * @param log ログ
	 * @throws Exception 例外が発生した場合
	 */
	private void printLog(
			BufferedWriter writer,
			String log) throws Exception {
		writer.write(log);
		writer.newLine();
		writer.flush();
	}

}
