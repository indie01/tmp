package com.kickmogu.yodobashi.community.resource.domain.constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.kickmogu.lib.core.constants.LabeledEnum;
import com.kickmogu.yodobashi.community.common.utils.VelocityHelper;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.MailInfoDO;

/**
 * メールタイプです。
 */
public enum MailType implements LabeledEnum<MailType, String>{

	USER_REGISTER ("006001", "コミュニティアカウント取得", "userRegister"),
	REVIEW_LIMIT_NOTIFY_FOR_FIVE_DAYS_AGO ("006002", "購入商品のレビュー期限（5日前）", "reviewLimitNotifyForFiveDaysAgo"),
	REVIEW_LIMIT_NOTIFY_FOR_TEN_DAYS_AGO ("006003", "購入商品のレビュー期限（10日前）", "reviewLimitNotifyForTenDaysAgo"),
	NEW_QUESTION_FOR_PURCHASE_PRODUCT ("006004", "購入商品の新着QA質問", "newQuestionForPurchaseProduct"),
	NEW_QUESTION_FOR_PURCHASE_PRODUCT_SUMMARY ("006005", "購入商品の新着QA質問（まとめ）", "newQuestionForPurchaseProductSummary"),
	REVIEW_COMMENT ("006006", "レビューへのコメント", "reviewComment"),
	REVIEW_COMMENT_SUMMARY ("006007", "レビューへのコメント（まとめ）", "reviewCommentSummary"),
	QUESTION_ANSWER_COMMENT ("006008", "QA回答へのコメント", "questionAnswerComment"),
	QUESTION_ANSWER_COMMENT_SUMMARY ("006009", "QA回答へのコメント（まとめ）", "questionAnswerCommentSummary"),
	IMAGE_COMMENT ("006033", "画像へのコメント", "imageComment"),
	IMAGE_COMMENT_SUMMARY ("006034", "画像へのコメント（まとめ）", "imageCommentSummary"),
	QUESTION_ANSWER ("006010", "QA質問への回答", "questionAnswer"),
	QUESTION_ANSWER_SUMMARY ("006011", "QA質問への回答（まとめ）", "questionAnswerSummary"),
	USER_FOLLOW ("006012", "他ユーザからのフォロー", "userFollow"),
	USER_FOLLOW_SUMMARY ("006013", "他ユーザからのフォロー（まとめ）", "userFollowSummary"),
	PRODUCT_MASTER_RANK_IN_SUMMARY ("006015", "商品マスターランクイン（まとめ）", "productMasterRankInSummary"),
	ANOTHER_QUESTION_ANSWER ("006016", "回答QAへの別回答", "anotherQuestionAnswer"),
	ANOTHER_QUESTION_ANSWER_SUMMARY ("006017", "回答QAへの別回答（まとめ）", "anotherQuestionAnswerSummary"),
	ANOTHER_REVIEW ("006018", "レビュー商品への別レビュー", "anotherReview"),
	ANOTHER_REVIEW_SUMMARY ("006019", "レビュー商品への別レビュー（まとめ）", "anotherReviewSummary"),
	FOLLOW_PRODUCT_NEW_REVIEW ("006020", "フォロー商品への新着レビュー", "followProductNewReview"),
	FOLLOW_PRODUCT_NEW_REVIEW_SUMMARY ("006021", "フォロー商品への新着レビュー（まとめ）", "followProductNewReviewSummary"),
	FOLLOW_PRODUCT_NEW_QUESTION ("006022", "フォロー商品への新着QA質問", "followProductNewQuestion"),
	FOLLOW_PRODUCT_NEW_QUESTION_SUMMARY ("006023", "フォロー商品への新着QA質問（まとめ）", "followProductNewQuestionSummary"),
	FOLLOW_PRODUCT_NEW_IMAGE ("006035", "フォローQAへの新着画像", "followProductNewImage"),
	FOLLOW_PRODUCT_NEW_IMAGE_SUMMARY ("006036", "フォローQAへの新着画像（まとめ）", "followProductNewImageSummary"),
	FOLLOW_QUESTION_NEW_ANSWER ("006024", "フォローQAへの新着回答", "followQuestionNewAnswer"),
	FOLLOW_QUESTION_NEW_ANSWER_SUMMARY ("006025", "フォローQAへの新着回答（まとめ）", "followQuestionNewAnswerSummary"),
	FOLLOW_USER_NEW_QUESTION ("006026", "フォローユーザの新着QA質問", "followUserNewQuestion"),
	FOLLOW_USER_NEW_QUESTION_SUMMARY ("006027", "フォローユーザの新着QA質問（まとめ）", "followUserNewQuestionSummary"),
	FOLLOW_USER_NEW_REVIEW ("006028", "フォローユーザの新着レビュー", "followUserNewReview"),
	FOLLOW_USER_NEW_REVIEW_SUMMARY ("006029", "フォローユーザの新着レビュー（まとめ）", "followUserNewReviewSummary"),
	FOLLOW_USER_NEW_ANSWER ("006030", "フォローユーザの新着QA回答", "followUserNewAnswer"),
	FOLLOW_USER_NEW_ANSWER_SUMMARY ("006031", "フォローユーザの新着QA回答（まとめ）", "followUserNewAnswerSummary"),
	FOLLOW_USER_NEW_IMAGE ("006037", "フォローユーザの新着画像", "followUserNewImage"),
	FOLLOW_USER_NEW_IMAGE_SUMMARY ("006038", "フォローユーザの新着画像（まとめ）", "followUserNewImageSummary"),
	STOP_COMMUNITY_USER ("006032", "コミュニティアカウント停止", "stopCommunityUser"),
	;

	/**
	 * コードです。
	 */
	private String code;

	/**
	 * ラベルです。
	 */
	private String label;

	/**
	 * テンプレート名です。
	 */
	private String templateName;

	/**
	 * テキストテンプレートです。
	 */
	private Template textTemplate;

	/**
	 * HTMLテンプレートです。
	 */
	private Template htmlTemplate;

	/**
	 * コンストラクタです。
	 * @param code コード
	 * @param label ラベル
	 */
	private MailType(String code, String label, String templateName) {
		this.code = code;
		this.label = label;
		this.templateName = templateName;
	}

	/**
	 * コードを返します。
	 * @return コード
	 */
	public String getCode() {
		return code;
	}

	/**
	 * 指定したコードの値を返します。
	 * @param code コード
	 * @return
	 */
	public static MailType codeOf(String code) {
		for (MailType element : values()) {
			if (code.equals(element.code)) {
				return element;
			}
		}
		return null;
	}

	/**
	 * 文字列表現を返します。
	 * @return 文字列表現
	 */
	@Override
	public String toString() {
		return name();
	}

	/**
	 * ラベルを返します。
	 * @return ラベル
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * テンプレート名を返します。
	 * @return テンプレート名
	 */
	public String getTemplateName() {
		return templateName;
	}

	/**
	 * 初期化します。
	 * @param engine テンプレートエンジン
	 * @throws IOException 入出力例外が発生した場合
	 */
	public static void initialize(
			VelocityEngine engine) throws Exception {
		for (MailType mailType : MailType.values()) {
			mailType.setTextTemplate(engine.getTemplate("text/" + mailType.getTemplateName() + ".vm"));
			mailType.setHtmlTemplate(engine.getTemplate("html/" + mailType.getTemplateName() + ".vm"));
		}
	}

	/**
	 * メール情報を生成して返します。
	 * @param communityUser コミュニティユーザー
	 * @param dataMap データマップ
	 * @return メール情報
	 */
	public MailInfoDO createMailInfo(
			CommunityUserDO communityUser,
			Map<?, ?> dataMap) {
		MailInfoDO mailInfo = new MailInfoDO();
		mailInfo.setMailType(this);
		mailInfo.setCommunityUser(communityUser);
		BufferedReader reader = null;
		VelocityContext vc = new VelocityContext(dataMap);
		vc.put("helper", new VelocityHelper());
		try {
			StringWriter writer = new StringWriter();
			textTemplate.merge(vc, writer);
			reader = new BufferedReader(new StringReader(writer.toString()));
			String title = reader.readLine();
			StringBuilder body = new StringBuilder();
			reader.readLine();
			for (String line = reader.readLine();
				line != null;
				line = reader.readLine()) {
				body.append(line).append("@@BR@@");
			}
			mailInfo.setTitle(title);
			mailInfo.setTextBody(body.toString());
		} catch (Exception e) {
			throw new IllegalStateException(
					"This template is invalid. format = text, name = " + templateName, e);
		}

		try {
			StringWriter writer = new StringWriter();
			htmlTemplate.merge(vc, writer);
			reader = new BufferedReader(new StringReader(writer.toString()));
			reader.readLine();
			StringBuilder body = new StringBuilder();
			reader.readLine();
			for (String line = reader.readLine();
				line != null;
				line = reader.readLine()) {
				body.append(line).append("@@BR@@");
			}
			mailInfo.setHtmlBody(body.toString());

		} catch (Exception e) {
			throw new IllegalStateException(
					"This template is invalid. format = html, name = " + templateName, e);
		}
		return mailInfo;
	}

	/**
	 * テキストテンプレートです。
	 * @param textTemplate テキストテンプレート
	 */
	private void setTextTemplate(Template textTemplate) {
		this.textTemplate = textTemplate;
	}

	/**
	 * HTMLテンプレートです。
	 * @param htmlTemplate HTMLテンプレート
	 */
	private void setHtmlTemplate(Template htmlTemplate) {
		this.htmlTemplate = htmlTemplate;
	}

}