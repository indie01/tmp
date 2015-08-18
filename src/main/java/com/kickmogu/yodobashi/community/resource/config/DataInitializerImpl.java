package com.kickmogu.yodobashi.community.resource.config;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.core.aop.TimestampHolder;
import com.kickmogu.lib.hadoop.hbase.aop.ArroundHBase;
import com.kickmogu.yodobashi.community.resource.dao.MailSettingDao;
import com.kickmogu.yodobashi.community.resource.dao.AdminConfigDao;
import com.kickmogu.yodobashi.community.resource.domain.AdminConfigDO;
import com.kickmogu.yodobashi.community.resource.domain.MailSettingMasterDO;
import com.kickmogu.yodobashi.community.resource.domain.ScoreFactorDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.MailSendTiming;
import com.kickmogu.yodobashi.community.resource.domain.constants.MailSettingType;

@Service
public class DataInitializerImpl implements DataInitializer, InitializingBean {

	/**
	 * メール設定 DAO です。
	 */
	@Autowired
	private MailSettingDao mailSettingDao;

	/**
	 * 管理者コンフィグ DAO です。
	 */
	@Autowired
	private AdminConfigDao adminConfigDao;

	/**
	 * タイムスタンプホルダーです。
	 */
	@Autowired
	private TimestampHolder timestampHolder;

	@Override
	@ArroundHBase
	public void afterPropertiesSet() throws Exception {
		initializeMailSettingMaster();
		initializeScoreFactor();
		initializeMailSendLimit();
	}

	public void initializeScoreFactor() throws Exception {
		ScoreFactorDO scoreFactor = adminConfigDao.loadScoreFactor();
		if (scoreFactor == null) {
			scoreFactor = new ScoreFactorDO();

			// Review関連
			scoreFactor.setReviewDay(new BigDecimal("0.2"));
			scoreFactor.setReviewContentsCountCoefficient(new BigDecimal("0.5"));
			scoreFactor.setReviewHasImagesCoefficient(new BigDecimal("0.2"));
			scoreFactor.setReviewCommentCount(new BigDecimal("0.5"));
			scoreFactor.setReviewLikeCount(new BigDecimal("0.4"));
			scoreFactor.setReviewViewCount(new BigDecimal("0.1"));
			scoreFactor.setReviewFollowerCount(new BigDecimal("0.2"));

			scoreFactor.setReviewContentsCountTerm0to99(new BigDecimal("0"));
			scoreFactor.setReviewContentsCountTerm100to199(new BigDecimal("10.0"));
			scoreFactor.setReviewContentsCountTerm200to299(new BigDecimal("20.0"));
			scoreFactor.setReviewContentsCountTerm300to399(new BigDecimal("30.0"));
			scoreFactor.setReviewContentsCountTerm400to449(new BigDecimal("40.0"));
			scoreFactor.setReviewContentsCountTerm450to499(new BigDecimal("45.0"));
			scoreFactor.setReviewContentsCountTermMore500(new BigDecimal("50.0"));
			scoreFactor.setReviewHasImages(new BigDecimal("5.0"));

			// 質問関連
			scoreFactor.setQuestionDay(new BigDecimal("0.2"));
			scoreFactor.setQuestionFollowerCount(new BigDecimal("0.5"));
			scoreFactor.setQuestionLikeCount(new BigDecimal("0.4"));
			scoreFactor.setQuestionAnswerCount(new BigDecimal("0.5"));
			scoreFactor.setQuestionViewCount(new BigDecimal("0.1"));

			// 質問回答関連
			scoreFactor.setQuestionAnswerDay(new BigDecimal("0.2"));
			scoreFactor.setQuestionAnswerContentsCountCoefficient(new BigDecimal("0.5"));
			scoreFactor.setQuestionAnswerHasImagesCoefficient(new BigDecimal("0.2"));
			scoreFactor.setQuestionAnswerLikeCount(new BigDecimal("0.5"));
			scoreFactor.setQuestionAnswerFollowerCount(new BigDecimal("0.2"));
			scoreFactor.setQuestionAnswerCommentCount(new BigDecimal("0"));

			scoreFactor.setQuestionAnswerContentsCountTerm0to99(new BigDecimal("0"));
			scoreFactor.setQuestionAnswerContentsCountTerm100to199(new BigDecimal("10.0"));
			scoreFactor.setQuestionAnswerContentsCountTerm200to299(new BigDecimal("20.0"));
			scoreFactor.setQuestionAnswerContentsCountTerm300to399(new BigDecimal("30.0"));
			scoreFactor.setQuestionAnswerContentsCountTerm400to449(new BigDecimal("40.0"));
			scoreFactor.setQuestionAnswerContentsCountTerm450to499(new BigDecimal("45.0"));
			scoreFactor.setQuestionAnswerContentsCountTermMore500(new BigDecimal("50.0"));
			scoreFactor.setQuestionAnswerHasImages(new BigDecimal("5.0"));

			// 画像投稿用
			scoreFactor.setImageCommentCount(new BigDecimal("0"));
			scoreFactor.setImageDay(new BigDecimal("0"));
			scoreFactor.setImageLikeCount(new BigDecimal("0"));
			scoreFactor.setImageViewCount(new BigDecimal("0"));

			// 商品マスター用
			scoreFactor.setProductMasterReviewPostCount(new BigDecimal("0.3"));
			scoreFactor.setProductMasterReviewShowCount(new BigDecimal("0.1"));
			scoreFactor.setProductMasterReviewLikeCount(new BigDecimal("0.5"));
			scoreFactor.setProductMasterAnswerPostCount(new BigDecimal("0.3"));
			scoreFactor.setProductMasterAnswerLikeCount(new BigDecimal("0.5"));
			scoreFactor.setProductMasterImagePostCount(new BigDecimal("0.2"));
			scoreFactor.setProductMasterImageLikeCount(new BigDecimal("0.3"));
			scoreFactor.setProductMasterReviewPostCountLimit(7);
			scoreFactor.setProductMasterImagePostCountLimit(7);

			scoreFactor.setRegisterDateTime(timestampHolder.getTimestamp());
			scoreFactor.setModifyDateTime(timestampHolder.getTimestamp());
			adminConfigDao.saveScoreFactor(scoreFactor);
		}
	}

	public void initializeMailSendLimit() throws Exception {
		AdminConfigDO unitTime = adminConfigDao.loadAdminConfig(
				AdminConfigDO.MAIL_SEND_LIMIT_UNIT_TIME);
		if (unitTime == null) {
			unitTime = new AdminConfigDO();
			unitTime.setKey(AdminConfigDO.MAIL_SEND_LIMIT_UNIT_TIME);
			unitTime.setValue("1000");
			unitTime.setRegisterDateTime(timestampHolder.getTimestamp());
			adminConfigDao.saveAdminConfig(unitTime);
		}
		AdminConfigDO sendCount = adminConfigDao.loadAdminConfig(
				AdminConfigDO.MAIL_SEND_LIMIT_SEND_COUNT);
		if (sendCount == null) {
			sendCount = new AdminConfigDO();
			sendCount.setKey(AdminConfigDO.MAIL_SEND_LIMIT_SEND_COUNT);
			sendCount.setValue("5");
			sendCount.setRegisterDateTime(timestampHolder.getTimestamp());
			adminConfigDao.saveAdminConfig(sendCount);
		}
	}

	public void initializeMailSettingMaster() throws Exception {
		if (mailSettingDao.findMailSettingMaster().size() > 0) {
			return;
		}
		List<MailSendTiming> choices = new ArrayList<MailSendTiming>();
		choices.add(MailSendTiming.FIVE_DAYS_AGO);
		choices.add(MailSendTiming.TEN_DAYS_AGO);
		choices.add(MailSendTiming.NOT_NOTIFY);

		List<MailSettingMasterDO> masters = new ArrayList<MailSettingMasterDO>();
		MailSettingMasterDO master0 = new MailSettingMasterDO();
		master0.setMailSettingType(MailSettingType.REVIEW_LIMIT);
		master0.setMailSettingMasterLabel("購入した商品のレビュー期限の事前通知");
		master0.setMailSettingMasterDescription(null);
		master0.setDefaultValue(MailSendTiming.FIVE_DAYS_AGO);
		master0.setOrderNo(masters.size());
		master0.setChoices(choices);
		masters.add(master0);

		choices = new ArrayList<MailSendTiming>();
		choices.add(MailSendTiming.EVERYTIME_NOTIFY);
		choices.add(MailSendTiming.DAILY_NOTIFY);
		choices.add(MailSendTiming.NOT_NOTIFY);

		MailSettingMasterDO master8 = new MailSettingMasterDO();
		master8.setMailSettingType(MailSettingType.REVIEW_PRODUCT_ANOTHER_REVIEW);
		master8.setMailSettingMasterLabel("レビューを書いた商品に別のレビューが投稿されたとき");
		master8.setMailSettingMasterDescription(null);
		master8.setDefaultValue(MailSendTiming.DAILY_NOTIFY);
		master8.setOrderNo(masters.size());
		master8.setChoices(choices);
		masters.add(master8);
		
		MailSettingMasterDO master1 = new MailSettingMasterDO();
		master1.setMailSettingType(MailSettingType.PURCHASE_PRODUCT_QUESTION);
		master1.setMailSettingMasterLabel("購入商品に関するQ&Aの質問が投稿されたとき");
		master1.setMailSettingMasterDescription(null);
		master1.setDefaultValue(MailSendTiming.DAILY_NOTIFY);
		master1.setOrderNo(masters.size());
		master1.setChoices(choices);
		masters.add(master1);

		MailSettingMasterDO master2 = new MailSettingMasterDO();
		master2.setMailSettingType(MailSettingType.REVIEW_COMMENT);
		master2.setMailSettingMasterLabel("自分が投稿したレビューにコメントがついたとき");
		master2.setMailSettingMasterDescription(null);
		master2.setDefaultValue(MailSendTiming.EVERYTIME_NOTIFY);
		master2.setOrderNo(masters.size());
		master2.setChoices(choices);
		masters.add(master2);

		MailSettingMasterDO master3 = new MailSettingMasterDO();
		master3.setMailSettingType(MailSettingType.ANSWER_COMMENT);
		master3.setMailSettingMasterLabel("自分が投稿したQ&Aの回答にコメントがついたとき");
		master3.setMailSettingMasterDescription(null);
		master3.setDefaultValue(MailSendTiming.EVERYTIME_NOTIFY);
		master3.setOrderNo(masters.size());
		master3.setChoices(choices);
		masters.add(master3);

		MailSettingMasterDO master4 = new MailSettingMasterDO();
		master4.setMailSettingType(MailSettingType.IMAGE_COMMENT);
		master4.setMailSettingMasterLabel("自分が投稿した画像にコメントがついたとき");
		master4.setMailSettingMasterDescription(null);
		master4.setDefaultValue(MailSendTiming.EVERYTIME_NOTIFY);
		master4.setOrderNo(masters.size());
		master4.setChoices(choices);
		masters.add(master4);

		MailSettingMasterDO master5 = new MailSettingMasterDO();
		master5.setMailSettingType(MailSettingType.QUESTION_ANSWER);
		master5.setMailSettingMasterLabel("自分が投稿したQ&Aの質問に回答がついたとき");
		master5.setMailSettingMasterDescription(null);
		master5.setDefaultValue(MailSendTiming.EVERYTIME_NOTIFY);
		master5.setOrderNo(masters.size());
		master5.setChoices(choices);
		masters.add(master5);

		MailSettingMasterDO master6 = new MailSettingMasterDO();
		master6.setMailSettingType(MailSettingType.ANSWER_QUESTION_ANOTHER_ANSWER);
		master6.setMailSettingMasterLabel("回答したQ&Aに別の回答がついたとき");
		master6.setMailSettingMasterDescription(null);
		master6.setDefaultValue(MailSendTiming.DAILY_NOTIFY);
		master6.setOrderNo(masters.size());
		master6.setChoices(choices);
		masters.add(master6);

		MailSettingMasterDO master7 = new MailSettingMasterDO();
		master7.setMailSettingType(MailSettingType.USER_FOLLOW);
		master7.setMailSettingMasterLabel("ほかのユーザーにフォローされたとき");
		master7.setMailSettingMasterDescription(null);
		master7.setDefaultValue(MailSendTiming.EVERYTIME_NOTIFY);
		master7.setOrderNo(masters.size());
		master7.setChoices(choices);
		masters.add(master7);


		MailSettingMasterDO master9 = new MailSettingMasterDO();
		master9.setMailSettingType(MailSettingType.FOLLOW_PRODUCT_REVIEW);
		master9.setMailSettingMasterLabel("フォローした商品に新着レビューが投稿されたとき");
		master9.setMailSettingMasterDescription(null);
		master9.setDefaultValue(MailSendTiming.DAILY_NOTIFY);
		master9.setOrderNo(masters.size());
		master9.setChoices(choices);
		masters.add(master9);

		MailSettingMasterDO master10 = new MailSettingMasterDO();
		master10.setMailSettingType(MailSettingType.FOLLOW_PRODUCT_QUESTION);
		master10.setMailSettingMasterLabel("フォローした商品にQ&Aの質問が投稿されたとき");
		master10.setMailSettingMasterDescription(null);
		master10.setDefaultValue(MailSendTiming.DAILY_NOTIFY);
		master10.setOrderNo(masters.size());
		master10.setChoices(choices);
		masters.add(master10);

		MailSettingMasterDO master11 = new MailSettingMasterDO();
		master11.setMailSettingType(MailSettingType.FOLLOW_PRODUCT_IMAGE);
		master11.setMailSettingMasterLabel("フォローした商品に画像が投稿されたとき");
		master11.setMailSettingMasterDescription(null);
		master11.setDefaultValue(MailSendTiming.DAILY_NOTIFY);
		master11.setOrderNo(masters.size());
		master11.setChoices(choices);
		masters.add(master11);

		MailSettingMasterDO master12 = new MailSettingMasterDO();
		master12.setMailSettingType(MailSettingType.FOLLOW_QUESTION_ANSWER);
		master12.setMailSettingMasterLabel("フォローしたQ&Aに回答がついたとき");
		master12.setMailSettingMasterDescription(null);
		master12.setDefaultValue(MailSendTiming.DAILY_NOTIFY);
		master12.setOrderNo(masters.size());
		master12.setChoices(choices);
		masters.add(master12);

		MailSettingMasterDO master13 = new MailSettingMasterDO();
		master13.setMailSettingType(MailSettingType.FOLLOW_USER_QUESTION);
		master13.setMailSettingMasterLabel("フォローしたユーザーがQ&Aの質問を投稿したとき");
		master13.setMailSettingMasterDescription(null);
		master13.setDefaultValue(MailSendTiming.NOT_NOTIFY);
		master13.setOrderNo(masters.size());
		master13.setChoices(choices);
		masters.add(master13);

		MailSettingMasterDO master14 = new MailSettingMasterDO();
		master14.setMailSettingType(MailSettingType.FOLLOW_USER_REVIEW);
		master14.setMailSettingMasterLabel("フォローしたユーザーがレビューを投稿したとき");
		master14.setMailSettingMasterDescription(null);
		master14.setDefaultValue(MailSendTiming.NOT_NOTIFY);
		master14.setOrderNo(masters.size());
		master14.setChoices(choices);
		masters.add(master14);

		MailSettingMasterDO master15 = new MailSettingMasterDO();
		master15.setMailSettingType(MailSettingType.FOLLOW_USER_QUESTION_ANSWER);
		master15.setMailSettingMasterLabel("フォローしたユーザーがQ&Aの回答を投稿したとき");
		master15.setMailSettingMasterDescription(null);
		master15.setDefaultValue(MailSendTiming.NOT_NOTIFY);
		master15.setOrderNo(masters.size());
		master15.setChoices(choices);
		masters.add(master15);

		MailSettingMasterDO master16 = new MailSettingMasterDO();
		master16.setMailSettingType(MailSettingType.FOLLOW_USER_IMAGE);
		master16.setMailSettingMasterLabel("フォローしたユーザーが画像を投稿したとき");
		master16.setMailSettingMasterDescription(null);
		master16.setDefaultValue(MailSendTiming.NOT_NOTIFY);
		master16.setOrderNo(masters.size());
		master16.setChoices(choices);
		masters.add(master16);

		choices = new ArrayList<MailSendTiming>();
		choices.add(MailSendTiming.DAILY_NOTIFY);
		choices.add(MailSendTiming.NOT_NOTIFY);

		MailSettingMasterDO master17 = new MailSettingMasterDO();
		master17.setMailSettingType(MailSettingType.RANK_IN_PRODUCT_MASTER);
		master17.setMailSettingMasterLabel("Top50以内の商品マスターになったとき");
		master17.setMailSettingMasterDescription("ランクが上がるたびに通知されます");
		master17.setDefaultValue(MailSendTiming.DAILY_NOTIFY);
		master17.setOrderNo(masters.size());
		master17.setChoices(choices);
		masters.add(master17);

		for (MailSettingMasterDO master : masters) {
			mailSettingDao.createMailSettingMaster(master);
		}
	}

}
