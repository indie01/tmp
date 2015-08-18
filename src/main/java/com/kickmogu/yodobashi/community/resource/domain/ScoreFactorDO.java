/**
 *
 */
package com.kickmogu.yodobashi.community.resource.domain;

import java.math.BigDecimal;

import com.kickmogu.yodobashi.community.service.annotation.AdminConfigKey;

/**
 * スコア係数です。
 * @author kamiike
 *
 */
public class ScoreFactorDO extends BaseWithTimestampDO {

	@AdminConfigKey
	public static final String REVIEW_DAY = "scoreFactor.reviewDay";

	@AdminConfigKey
	public static final String REVIEW_COMMENT_COUNT = "scoreFactor.reviewCommentCount";

	@AdminConfigKey
	public static final String REVIEW_LIKE_COUNT = "scoreFactor.reviewLikeCount";

	@AdminConfigKey
	public static final String REVIEW_VIEW_COUNT = "scoreFactor.reviewViewCount";

	@AdminConfigKey
	public static final String REVIEW_FOLLOWER_COUNT = "scoreFactor.reviewFollowerCount";

	@AdminConfigKey
	public static final String REVIEW_HAS_IMAGES_COEFFICIENT = "scoreFactor.reviewHasImages.coefficient";

	@AdminConfigKey
	public static final String REVIEW_CONTENTS_COUNT_COEFFICIENT = "scoreFactor.reviewContentsCountCoefficient";

	@AdminConfigKey
	public static final String REVIEW_CONTENTS_COUNT_TERM_0TO99    = "scoreFactor.reviewContentsCountTerm0to99";

	@AdminConfigKey
	public static final String REVIEW_CONTENTS_COUNT_TERM_100TO199 = "scoreFactor.reviewContentsCountTerm100to199";

	@AdminConfigKey
	public static final String REVIEW_CONTENTS_COUNT_TERM_200TO299  = "scoreFactor.reviewContentsCountTerm200to299";

	@AdminConfigKey
	public static final String REVIEW_CONTENTS_COUNT_TERM_300TO399  = "scoreFactor.reviewContentsCountTerm300to399";

	@AdminConfigKey
	public static final String REVIEW_CONTENTS_COUNT_TERM_400TO449  = "scoreFactor.reviewContentsCountTerm400to449";

	@AdminConfigKey
	public static final String REVIEW_CONTENTS_COUNT_TERM_450TO499  = "scoreFactor.reviewContentsCountTerm450to499";

	@AdminConfigKey
	public static final String REVIEW_CONTENTS_COUNT_TERM_MORE_500 = "scoreFactor.reviewContentsCountTermMore500";

	@AdminConfigKey
	public static final String REVIEW_HAS_IMAGES = "scoreFactor.reviewHasImages";



	@AdminConfigKey
	public static final String QUESTION_DAY = "scoreFactor.questionDay";

	@AdminConfigKey
	public static final String QUESTION_FOLLOWER_COUNT = "scoreFactor.questionFollowerCount";

	@AdminConfigKey
	public static final String QUESTION_LIKE_COUNT = "scoreFactor.questionLikeCount";

	@AdminConfigKey
	public static final String QUESTION_ANSWER_COUNT = "scoreFactor.questionAnswerCount";

	@AdminConfigKey
	public static final String QUESTION_VIEW_COUNT = "scoreFactor.questionViewCount";

	@AdminConfigKey
	public static final String QUESTION_ANSWER_DAY = "scoreFactor.questionAnswerDay";

	@AdminConfigKey
	public static final String QUESTION_ANSWER_COMMENT_COUNT = "scoreFactor.questionAnswerCommentCount";

	@AdminConfigKey
	public static final String QUESTION_ANSWER_LIKE_COUNT = "scoreFactor.questionAnswerLikeCount";

	@AdminConfigKey
	public static final String QUESTION_ANSWER_FOLLOWER_COUNT = "scoreFactor.questionAnswerFollowerCount";

	@AdminConfigKey
	public static final String QUESTION_ANSWER_HAS_IMAGES_COEFFICIENT = "scoreFactor.questionAnswerHasImages.coefficient";

	@AdminConfigKey
	public static final String QUESTION_ANSWER_CONTENTS_COUNT_COEFFICIENT = "scoreFactor.questionAnswerContentsCountCoefficient";

	@AdminConfigKey
	public static final String QUESTION_ANSWER_CONTENTS_COUNT_TERM_0TO99    = "scoreFactor.questionAnswerContentsCountTerm0to99";

	@AdminConfigKey
	public static final String QUESTION_ANSWER_CONTENTS_COUNT_TERM_100TO199 = "scoreFactor.questionAnswerContentsCountTerm100to199";

	@AdminConfigKey
	public static final String QUESTION_ANSWER_CONTENTS_COUNT_TERM_200TO299  = "scoreFactor.questionAnswerContentsCountTerm200to299";

	@AdminConfigKey
	public static final String QUESTION_ANSWER_CONTENTS_COUNT_TERM_300TO399  = "scoreFactor.questionAnswerContentsCountTerm300to399";

	@AdminConfigKey
	public static final String QUESTION_ANSWER_CONTENTS_COUNT_TERM_400TO449  = "scoreFactor.questionAnswerContentsCountTerm400to449";

	@AdminConfigKey
	public static final String QUESTION_ANSWER_CONTENTS_COUNT_TERM_450TO499  = "scoreFactor.questionAnswerContentsCountTerm450to499";

	@AdminConfigKey
	public static final String QUESTION_ANSWER_CONTENTS_COUNT_TERM_MORE_500 = "scoreFactor.questionAnswerContentsCountTermMore500";

	@AdminConfigKey
	public static final String QUESTION_ANSWER_HAS_IMAGES = "scoreFactor.questionAnswerHasImages";



	@AdminConfigKey
	public static final String IMAGE_DAY = "scoreFactor.imageDay";

	@AdminConfigKey
	public static final String IMAGE_COMMENT_COUNT = "scoreFactor.imageCommentCount";

	@AdminConfigKey
	public static final String IMAGE_LIKE_COUNT = "scoreFactor.imageLikeCount";

	@AdminConfigKey
	public static final String IMAGE_VIEW_COUNT = "scoreFactor.imageViewCount";



	@AdminConfigKey
	public static final String PRODUCT_MASTER_REVIEW_POST_COUNT = "scoreFactor.productMasterReviewPostCount";

	@AdminConfigKey
	public static final String PRODUCT_MASTER_REVIEW_POST_COUNT_LIMIT = "scoreFactor.productMasterReviewPostCountLimit";

	@AdminConfigKey
	public static final String PRODUCT_MASTER_IMAGE_POST_COUNT_LIMIT = "scoreFactor.productMasterImagePostCountLimit";

	@AdminConfigKey
	public static final String PRODUCT_MASTER_REVIEW_SHOW_COUNT = "scoreFactor.productMasterReviewShowCount";

	@AdminConfigKey
	public static final String PRODUCT_MASTER_REVIEW_LIKE_COUNT = "scoreFactor.productMasterReviewLikeCount";

	@AdminConfigKey
	public static final String PRODUCT_MASTER_ANSWER_POST_COUNT = "scoreFactor.productMasterAnswerPostCount";

	@AdminConfigKey
	public static final String PRODUCT_MASTER_ANSWER_LIKE_COUNT = "scoreFactor.productMasterAnswerLikeCount";

	@AdminConfigKey
	public static final String PRODUCT_MASTER_IMAGE_POST_COUNT = "scoreFactor.productMasterImagePostCount";

	@AdminConfigKey
	public static final String PRODUCT_MASTER_IMAGE_LIKE_COUNT = "scoreFactor.productMasterImageLikeCount";



	/**
	 *
	 */
	private static final long serialVersionUID = -2173719747150548980L;

	/**
	 * 商品マスターのレビュー投稿件数の係数です。
	 */
	private BigDecimal productMasterReviewPostCount;

	/**
	 * 商品マスターのレビュー投稿件数のカウント制限です。
	 */
	private long productMasterReviewPostCountLimit;

	/**
	 * 商品マスターの画像投稿件数のカウント制限です。
	 */
	private long productMasterImagePostCountLimit;

	/**
	 * 商品マスターのレビュー閲覧件数の係数です。
	 */
	private BigDecimal productMasterReviewShowCount;

	/**
	 * 商品マスターのレビューいいね獲得件数の係数です。
	 */
	private BigDecimal productMasterReviewLikeCount;

	/**
	 * 商品マスターの回答投稿件数の係数です。
	 */
	private BigDecimal productMasterAnswerPostCount;

	/**
	 * 商品マスターの回答いいね獲得件数の係数です。
	 */
	private BigDecimal productMasterAnswerLikeCount;

	/**
	 * 商品マスターの画像投稿件数の係数です。
	 */
	private BigDecimal productMasterImagePostCount;

	/**
	 * 商品マスターの画像いいね獲得件数の係数です。
	 */
	private BigDecimal productMasterImageLikeCount;

	/**
	 * レビュースコアの経過日数の係数です。
	 */
	private BigDecimal reviewDay;

	/**
	 * レビュースコアのコメント数の係数です。
	 */
	private BigDecimal reviewCommentCount;

	/**
	 * レビュースコアのいいね数の係数です。
	 */
	private BigDecimal reviewLikeCount;

	/**
	 * レビュースコアのUU閲覧数の係数です。
	 */
	private BigDecimal reviewViewCount;

	/**
	 * レビュースコアのレビュワーのフォロワー数の係数です。
	 */
	private BigDecimal reviewFollowerCount;

	/**
	 * レビュースコアの文字数スコアの係数です。
	 */
	private BigDecimal reviewContentsCountCoefficient;

	/**
	 * レビュースコアの画像スコアの係数です。
	 */
	private BigDecimal reviewHasImagesCoefficient;

	/**
	 * レビュースコアの文字数スコアです。
	 */
	private BigDecimal reviewContentsCountTerm0to99;
	/**
	 * レビュースコアの文字数スコアです。
	 */
	private BigDecimal reviewContentsCountTerm100to199;
	/**
	 * レビュースコアの文字数スコアです。
	 */
	private BigDecimal reviewContentsCountTerm200to299;
	/**
	 * レビュースコアの文字数スコアです。
	 */
	private BigDecimal reviewContentsCountTerm300to399;
	/**
	 * レビュースコアの文字数スコアです。
	 */
	private BigDecimal reviewContentsCountTerm400to449;
	/**
	 * レビュースコアの文字数スコアです。
	 */
	private BigDecimal reviewContentsCountTerm450to499;
	/**
	 * レビュースコアの文字数スコアです。
	 */
	private BigDecimal reviewContentsCountTermMore500;

	/**
	 * レビューの画像有無スコアです。
	 */
	private BigDecimal reviewHasImages;


	/**
	 * 質問スコアの経過日数の係数です。
	 */
	private BigDecimal questionDay;

	/**
	 * 質問スコアの質問のフォロワー数の係数です。
	 */
	private BigDecimal questionFollowerCount;

	/**
	 * 質問スコアのいいね数の係数です。
	 */
	private BigDecimal questionLikeCount;

	/**
	 * 質問スコアの回答数の係数です。
	 */
	private BigDecimal questionAnswerCount;

	/**
	 * 質問スコアのUU閲覧数の係数です。
	 */
	private BigDecimal questionViewCount;



	/**
	 * 質問回答スコアの経過日数の係数です。
	 */
	private BigDecimal questionAnswerDay;

	/**
	 * 質問回答スコアのコメント数の係数です。
	 */
	private BigDecimal questionAnswerCommentCount;

	/**
	 * 質問回答スコアのいいね数の係数です。
	 */
	private BigDecimal questionAnswerLikeCount;

	/**
	 * 質問回答スコアの回答者のフォロワー数の係数です。
	 */
	private BigDecimal questionAnswerFollowerCount;

	/**
	 * 質問回答スコアの文字数スコアの係数です。
	 */
	private BigDecimal questionAnswerContentsCountCoefficient;

	/**
	 * 質問回答スコアの画像スコアの係数です。
	 */
	private BigDecimal questionAnswerHasImagesCoefficient;

	/**
	 * 質問回答スコアの文字数スコアです。
	 */
	private BigDecimal questionAnswerContentsCountTerm0to99;
	/**
	 * 質問回答スコアの文字数スコアです。
	 */
	private BigDecimal questionAnswerContentsCountTerm100to199;
	/**
	 * 質問回答スコアの文字数スコアです。
	 */
	private BigDecimal questionAnswerContentsCountTerm200to299;
	/**
	 * 質問回答スコアの文字数スコアです。
	 */
	private BigDecimal questionAnswerContentsCountTerm300to399;
	/**
	 * 質問回答スコアの文字数スコアです。
	 */
	private BigDecimal questionAnswerContentsCountTerm400to449;
	/**
	 * 質問回答スコアの文字数スコアです。
	 */
	private BigDecimal questionAnswerContentsCountTerm450to499;
	/**
	 * 質問回答スコアの文字数スコアです。
	 */
	private BigDecimal questionAnswerContentsCountTermMore500;
	/**
	 * レビューの画像有無スコアです。
	 */
	private BigDecimal questionAnswerHasImages;

	/**
	 * 画像スコアの経過日数の係数です。
	 */
	private BigDecimal imageDay;

	/**
	 * 画像スコアのコメント数の係数です。
	 */
	private BigDecimal imageCommentCount;

	/**
	 * 画像スコアのいいね数の係数です。
	 */
	private BigDecimal imageLikeCount;

	/**
	 * 画像スコアのUU閲覧数の係数です。
	 */
	private BigDecimal imageViewCount;

	/**
	 * @return productMasterReviewShowCount
	 */
	public BigDecimal getProductMasterReviewShowCount() {
		return productMasterReviewShowCount;
	}

	/**
	 * @param productMasterReviewShowCount セットする productMasterReviewShowCount
	 */
	public void setProductMasterReviewShowCount(
			BigDecimal productMasterReviewShowCount) {
		this.productMasterReviewShowCount = productMasterReviewShowCount;
	}

	/**
	 * @return productMasterReviewLikeCount
	 */
	public BigDecimal getProductMasterReviewLikeCount() {
		return productMasterReviewLikeCount;
	}

	/**
	 * @param productMasterReviewLikeCount セットする productMasterReviewLikeCount
	 */
	public void setProductMasterReviewLikeCount(
			BigDecimal productMasterReviewLikeCount) {
		this.productMasterReviewLikeCount = productMasterReviewLikeCount;
	}

	/**
	 * @return productMasterAnswerPostCount
	 */
	public BigDecimal getProductMasterAnswerPostCount() {
		return productMasterAnswerPostCount;
	}

	/**
	 * @param productMasterAnswerPostCount セットする productMasterAnswerPostCount
	 */
	public void setProductMasterAnswerPostCount(
			BigDecimal productMasterAnswerPostCount) {
		this.productMasterAnswerPostCount = productMasterAnswerPostCount;
	}

	/**
	 * @return productMasterAnswerLikeCount
	 */
	public BigDecimal getProductMasterAnswerLikeCount() {
		return productMasterAnswerLikeCount;
	}

	/**
	 * @param productMasterAnswerLikeCount セットする productMasterAnswerLikeCount
	 */
	public void setProductMasterAnswerLikeCount(
			BigDecimal productMasterAnswerLikeCount) {
		this.productMasterAnswerLikeCount = productMasterAnswerLikeCount;
	}

	/**
	 * @return reviewDay
	 */
	public BigDecimal getReviewDay() {
		return reviewDay;
	}

	/**
	 * @param reviewDay セットする reviewDay
	 */
	public void setReviewDay(BigDecimal reviewDay) {
		this.reviewDay = reviewDay;
	}

	/**
	 * @return reviewCommentCount
	 */
	public BigDecimal getReviewCommentCount() {
		return reviewCommentCount;
	}

	/**
	 * @param reviewCommentCount セットする reviewCommentCount
	 */
	public void setReviewCommentCount(BigDecimal reviewCommentCount) {
		this.reviewCommentCount = reviewCommentCount;
	}

	/**
	 * @return reviewLikeCount
	 */
	public BigDecimal getReviewLikeCount() {
		return reviewLikeCount;
	}

	/**
	 * @param reviewLikeCount セットする reviewLikeCount
	 */
	public void setReviewLikeCount(BigDecimal reviewLikeCount) {
		this.reviewLikeCount = reviewLikeCount;
	}

	/**
	 * @return reviewViewCount
	 */
	public BigDecimal getReviewViewCount() {
		return reviewViewCount;
	}

	/**
	 * @param reviewViewCount セットする reviewViewCount
	 */
	public void setReviewViewCount(BigDecimal reviewViewCount) {
		this.reviewViewCount = reviewViewCount;
	}

	/**
	 * @return reviewFollowerCount
	 */
	public BigDecimal getReviewFollowerCount() {
		return reviewFollowerCount;
	}

	/**
	 * @param reviewFollowerCount セットする reviewFollowerCount
	 */
	public void setReviewFollowerCount(BigDecimal reviewFollowerCount) {
		this.reviewFollowerCount = reviewFollowerCount;
	}

	/**
	 * @return questionDay
	 */
	public BigDecimal getQuestionDay() {
		return questionDay;
	}

	/**
	 * @param questionDay セットする questionDay
	 */
	public void setQuestionDay(BigDecimal questionDay) {
		this.questionDay = questionDay;
	}

	/**
	 * @return questionFollowerCount
	 */
	public BigDecimal getQuestionFollowerCount() {
		return questionFollowerCount;
	}

	/**
	 * @param questionFollowerCount セットする questionFollowerCount
	 */
	public void setQuestionFollowerCount(BigDecimal questionFollowerCount) {
		this.questionFollowerCount = questionFollowerCount;
	}

	/**
	 * @return questionLikeCount
	 */
	public BigDecimal getQuestionLikeCount() {
		return questionLikeCount;
	}

	/**
	 * @param questionLikeCount セットする questionLikeCount
	 */
	public void setQuestionLikeCount(BigDecimal questionLikeCount) {
		this.questionLikeCount = questionLikeCount;
	}

	/**
	 * @return questionAnswerCount
	 */
	public BigDecimal getQuestionAnswerCount() {
		return questionAnswerCount;
	}

	/**
	 * @param questionAnswerCount セットする questionAnswerCount
	 */
	public void setQuestionAnswerCount(BigDecimal questionAnswerCount) {
		this.questionAnswerCount = questionAnswerCount;
	}

	/**
	 * @return questionViewCount
	 */
	public BigDecimal getQuestionViewCount() {
		return questionViewCount;
	}

	/**
	 * @param questionViewCount セットする questionViewCount
	 */
	public void setQuestionViewCount(BigDecimal questionViewCount) {
		this.questionViewCount = questionViewCount;
	}

	/**
	 * @return questionAnswerDay
	 */
	public BigDecimal getQuestionAnswerDay() {
		return questionAnswerDay;
	}

	/**
	 * @param questionAnswerDay セットする questionAnswerDay
	 */
	public void setQuestionAnswerDay(BigDecimal questionAnswerDay) {
		this.questionAnswerDay = questionAnswerDay;
	}

	/**
	 * @return questionAnswerCommentCount
	 */
	public BigDecimal getQuestionAnswerCommentCount() {
		return questionAnswerCommentCount;
	}

	/**
	 * @param questionAnswerCommentCount セットする questionAnswerCommentCount
	 */
	public void setQuestionAnswerCommentCount(BigDecimal questionAnswerCommentCount) {
		this.questionAnswerCommentCount = questionAnswerCommentCount;
	}

	/**
	 * @return questionAnswerLikeCount
	 */
	public BigDecimal getQuestionAnswerLikeCount() {
		return questionAnswerLikeCount;
	}

	/**
	 * @param questionAnswerLikeCount セットする questionAnswerLikeCount
	 */
	public void setQuestionAnswerLikeCount(BigDecimal questionAnswerLikeCount) {
		this.questionAnswerLikeCount = questionAnswerLikeCount;
	}

	/**
	 * @return questionAnswerFollowerCount
	 */
	public BigDecimal getQuestionAnswerFollowerCount() {
		return questionAnswerFollowerCount;
	}

	/**
	 * @param questionAnswerFollowerCount セットする questionAnswerFollowerCount
	 */
	public void setQuestionAnswerFollowerCount(
			BigDecimal questionAnswerFollowerCount) {
		this.questionAnswerFollowerCount = questionAnswerFollowerCount;
	}

	/**
	 * @return imageDay
	 */
	public BigDecimal getImageDay() {
		return imageDay;
	}

	/**
	 * @param imageDay セットする imageDay
	 */
	public void setImageDay(BigDecimal imageDay) {
		this.imageDay = imageDay;
	}

	/**
	 * @return imageCommentCount
	 */
	public BigDecimal getImageCommentCount() {
		return imageCommentCount;
	}

	/**
	 * @param imageCommentCount セットする imageCommentCount
	 */
	public void setImageCommentCount(BigDecimal imageCommentCount) {
		this.imageCommentCount = imageCommentCount;
	}

	/**
	 * @return imageLikeCount
	 */
	public BigDecimal getImageLikeCount() {
		return imageLikeCount;
	}

	/**
	 * @param imageLikeCount セットする imageLikeCount
	 */
	public void setImageLikeCount(BigDecimal imageLikeCount) {
		this.imageLikeCount = imageLikeCount;
	}

	/**
	 * @return imageViewCount
	 */
	public BigDecimal getImageViewCount() {
		return imageViewCount;
	}

	/**
	 * @param imageViewCount セットする imageViewCount
	 */
	public void setImageViewCount(BigDecimal imageViewCount) {
		this.imageViewCount = imageViewCount;
	}

	/**
	 * @return productMasterImagePostCount
	 */
	public BigDecimal getProductMasterImagePostCount() {
		return productMasterImagePostCount;
	}

	/**
	 * @param productMasterImagePostCount セットする productMasterImagePostCount
	 */
	public void setProductMasterImagePostCount(
			BigDecimal productMasterImagePostCount) {
		this.productMasterImagePostCount = productMasterImagePostCount;
	}

	/**
	 * @return productMasterImageLikeCount
	 */
	public BigDecimal getProductMasterImageLikeCount() {
		return productMasterImageLikeCount;
	}

	/**
	 * @param productMasterImageLikeCount セットする productMasterImageLikeCount
	 */
	public void setProductMasterImageLikeCount(
			BigDecimal productMasterImageLikeCount) {
		this.productMasterImageLikeCount = productMasterImageLikeCount;
	}

	/**
	 * @return productMasterReviewPostCount
	 */
	public BigDecimal getProductMasterReviewPostCount() {
		return productMasterReviewPostCount;
	}

	/**
	 * @param productMasterReviewPostCount セットする productMasterReviewPostCount
	 */
	public void setProductMasterReviewPostCount(
			BigDecimal productMasterReviewPostCount) {
		this.productMasterReviewPostCount = productMasterReviewPostCount;
	}

	/**
	 * @return productMasterReviewPostCountLimit
	 */
	public long getProductMasterReviewPostCountLimit() {
		return productMasterReviewPostCountLimit;
	}

	/**
	 * @param productMasterReviewPostCountLimit セットする productMasterReviewPostCountLimit
	 */
	public void setProductMasterReviewPostCountLimit(
			long productMasterReviewPostCountLimit) {
		this.productMasterReviewPostCountLimit = productMasterReviewPostCountLimit;
	}

	/**
	 * @return the reviewContentsCountTerm0to99
	 */
	public BigDecimal getReviewContentsCountTerm0to99() {
		return reviewContentsCountTerm0to99;
	}

	/**
	 * @param reviewContentsCountTerm0to99 the reviewContentsCountTerm0to99 to set
	 */
	public void setReviewContentsCountTerm0to99(
			BigDecimal reviewContentsCountTerm0to99) {
		this.reviewContentsCountTerm0to99 = reviewContentsCountTerm0to99;
	}

	/**
	 * @return the reviewContentsCountTerm100to199
	 */
	public BigDecimal getReviewContentsCountTerm100to199() {
		return reviewContentsCountTerm100to199;
	}

	/**
	 * @param reviewContentsCountTerm100to199 the reviewContentsCountTerm100to199 to set
	 */
	public void setReviewContentsCountTerm100to199(
			BigDecimal reviewContentsCountTerm100to199) {
		this.reviewContentsCountTerm100to199 = reviewContentsCountTerm100to199;
	}

	/**
	 * @return the reviewContentsCountTerm200to299
	 */
	public BigDecimal getReviewContentsCountTerm200to299() {
		return reviewContentsCountTerm200to299;
	}

	/**
	 * @param reviewContentsCountTerm200to299 the reviewContentsCountTerm200to299 to set
	 */
	public void setReviewContentsCountTerm200to299(
			BigDecimal reviewContentsCountTerm200to299) {
		this.reviewContentsCountTerm200to299 = reviewContentsCountTerm200to299;
	}

	/**
	 * @return the reviewContentsCountTerm300to399
	 */
	public BigDecimal getReviewContentsCountTerm300to399() {
		return reviewContentsCountTerm300to399;
	}

	/**
	 * @param reviewContentsCountTerm300to399 the reviewContentsCountTerm300to399 to set
	 */
	public void setReviewContentsCountTerm300to399(
			BigDecimal reviewContentsCountTerm300to399) {
		this.reviewContentsCountTerm300to399 = reviewContentsCountTerm300to399;
	}

	/**
	 * @return the reviewContentsCountTermMore500
	 */
	public BigDecimal getReviewContentsCountTermMore500() {
		return reviewContentsCountTermMore500;
	}

	/**
	 * @param reviewContentsCountTermMore500 the reviewContentsCountTermMore500 to set
	 */
	public void setReviewContentsCountTermMore500(
			BigDecimal reviewContentsCountTermMore500) {
		this.reviewContentsCountTermMore500 = reviewContentsCountTermMore500;
	}

	/**
	 * @return the reviewHasImages
	 */
	public BigDecimal getReviewHasImages() {
		return reviewHasImages;
	}

	/**
	 * @param reviewHasImages the reviewHasImages to set
	 */
	public void setReviewHasImages(BigDecimal reviewHasImages) {
		this.reviewHasImages = reviewHasImages;
	}

	/**
	 * @return the reviewContentsCountCoefficient
	 */
	public BigDecimal getReviewContentsCountCoefficient() {
		return reviewContentsCountCoefficient;
	}

	/**
	 * @param reviewContentsCountCoefficient the reviewContentsCountCoefficient to set
	 */
	public void setReviewContentsCountCoefficient(
			BigDecimal reviewContentsCountCoefficient) {
		this.reviewContentsCountCoefficient = reviewContentsCountCoefficient;
	}

	/**
	 * @return the reviewHasImagesCoefficient
	 */
	public BigDecimal getReviewHasImagesCoefficient() {
		return reviewHasImagesCoefficient;
	}

	/**
	 * @param reviewHasImagesCoefficient the reviewHasImagesCoefficient to set
	 */
	public void setReviewHasImagesCoefficient(BigDecimal reviewHasImagesCoefficient) {
		this.reviewHasImagesCoefficient = reviewHasImagesCoefficient;
	}

	/**
	 * @return the questionAnswerContentsCountCoefficient
	 */
	public BigDecimal getQuestionAnswerContentsCountCoefficient() {
		return questionAnswerContentsCountCoefficient;
	}

	/**
	 * @param questionAnswerContentsCountCoefficient the questionAnswerContentsCountCoefficient to set
	 */
	public void setQuestionAnswerContentsCountCoefficient(
			BigDecimal questionAnswerContentsCountCoefficient) {
		this.questionAnswerContentsCountCoefficient = questionAnswerContentsCountCoefficient;
	}

	/**
	 * @return the questionAnswerHasImagesCoefficient
	 */
	public BigDecimal getQuestionAnswerHasImagesCoefficient() {
		return questionAnswerHasImagesCoefficient;
	}

	/**
	 * @param questionAnswerHasImagesCoefficient the questionAnswerHasImagesCoefficient to set
	 */
	public void setQuestionAnswerHasImagesCoefficient(
			BigDecimal questionAnswerHasImagesCoefficient) {
		this.questionAnswerHasImagesCoefficient = questionAnswerHasImagesCoefficient;
	}

	/**
	 * @return the questionAnswerContentsCountTerm0to99
	 */
	public BigDecimal getQuestionAnswerContentsCountTerm0to99() {
		return questionAnswerContentsCountTerm0to99;
	}

	/**
	 * @param questionAnswerContentsCountTerm0to99 the questionAnswerContentsCountTerm0to99 to set
	 */
	public void setQuestionAnswerContentsCountTerm0to99(
			BigDecimal questionAnswerContentsCountTerm0to99) {
		this.questionAnswerContentsCountTerm0to99 = questionAnswerContentsCountTerm0to99;
	}

	/**
	 * @return the questionAnswerContentsCountTerm100to199
	 */
	public BigDecimal getQuestionAnswerContentsCountTerm100to199() {
		return questionAnswerContentsCountTerm100to199;
	}

	/**
	 * @param questionAnswerContentsCountTerm100to199 the questionAnswerContentsCountTerm100to199 to set
	 */
	public void setQuestionAnswerContentsCountTerm100to199(
			BigDecimal questionAnswerContentsCountTerm100to199) {
		this.questionAnswerContentsCountTerm100to199 = questionAnswerContentsCountTerm100to199;
	}

	/**
	 * @return the questionAnswerContentsCountTerm200to299
	 */
	public BigDecimal getQuestionAnswerContentsCountTerm200to299() {
		return questionAnswerContentsCountTerm200to299;
	}

	/**
	 * @param questionAnswerContentsCountTerm200to299 the questionAnswerContentsCountTerm200to299 to set
	 */
	public void setQuestionAnswerContentsCountTerm200to299(
			BigDecimal questionAnswerContentsCountTerm200to299) {
		this.questionAnswerContentsCountTerm200to299 = questionAnswerContentsCountTerm200to299;
	}

	/**
	 * @return the questionAnswerContentsCountTerm300to399
	 */
	public BigDecimal getQuestionAnswerContentsCountTerm300to399() {
		return questionAnswerContentsCountTerm300to399;
	}

	/**
	 * @param questionAnswerContentsCountTerm300to399 the questionAnswerContentsCountTerm300to399 to set
	 */
	public void setQuestionAnswerContentsCountTerm300to399(
			BigDecimal questionAnswerContentsCountTerm300to399) {
		this.questionAnswerContentsCountTerm300to399 = questionAnswerContentsCountTerm300to399;
	}

	/**
	 * @return the questionAnswerContentsCountTermMore500
	 */
	public BigDecimal getQuestionAnswerContentsCountTermMore500() {
		return questionAnswerContentsCountTermMore500;
	}

	/**
	 * @param questionAnswerContentsCountTermMore500 the questionAnswerContentsCountTermMore500 to set
	 */
	public void setQuestionAnswerContentsCountTermMore500(
			BigDecimal questionAnswerContentsCountTermMore500) {
		this.questionAnswerContentsCountTermMore500 = questionAnswerContentsCountTermMore500;
	}

	/**
	 * @return the questionAnswerHasImages
	 */
	public BigDecimal getQuestionAnswerHasImages() {
		return questionAnswerHasImages;
	}

	/**
	 * @param questionAnswerHasImages the questionAnswerHasImages to set
	 */
	public void setQuestionAnswerHasImages(BigDecimal questionAnswerHasImages) {
		this.questionAnswerHasImages = questionAnswerHasImages;
	}

	/**
	 * @return the productMasterImagePostCountLimit
	 */
	public long getProductMasterImagePostCountLimit() {
		return productMasterImagePostCountLimit;
	}

	/**
	 * @param productMasterImagePostCountLimit the productMasterImagePostCountLimit to set
	 */
	public void setProductMasterImagePostCountLimit(
			long productMasterImagePostCountLimit) {
		this.productMasterImagePostCountLimit = productMasterImagePostCountLimit;
	}

	/**
	 * @return questionAnswerContentsCountTerm400to449
	 */
	public BigDecimal getQuestionAnswerContentsCountTerm400to449() {
		return questionAnswerContentsCountTerm400to449;
	}

	/**
	 * @param questionAnswerContentsCountTerm400to449 セットする questionAnswerContentsCountTerm400to449
	 */
	public void setQuestionAnswerContentsCountTerm400to449(
			BigDecimal questionAnswerContentsCountTerm400to449) {
		this.questionAnswerContentsCountTerm400to449 = questionAnswerContentsCountTerm400to449;
	}

	/**
	 * @return questionAnswerContentsCountTerm450to499
	 */
	public BigDecimal getQuestionAnswerContentsCountTerm450to499() {
		return questionAnswerContentsCountTerm450to499;
	}

	/**
	 * @param questionAnswerContentsCountTerm450to499 セットする questionAnswerContentsCountTerm450to499
	 */
	public void setQuestionAnswerContentsCountTerm450to499(
			BigDecimal questionAnswerContentsCountTerm450to499) {
		this.questionAnswerContentsCountTerm450to499 = questionAnswerContentsCountTerm450to499;
	}

	/**
	 * @return reviewContentsCountTerm400to449
	 */
	public BigDecimal getReviewContentsCountTerm400to449() {
		return reviewContentsCountTerm400to449;
	}

	/**
	 * @param reviewContentsCountTerm400to449 セットする reviewContentsCountTerm400to449
	 */
	public void setReviewContentsCountTerm400to449(
			BigDecimal reviewContentsCountTerm400to449) {
		this.reviewContentsCountTerm400to449 = reviewContentsCountTerm400to449;
	}

	/**
	 * @return reviewContentsCountTerm450to499
	 */
	public BigDecimal getReviewContentsCountTerm450to499() {
		return reviewContentsCountTerm450to499;
	}

	/**
	 * @param reviewContentsCountTerm450to499 セットする reviewContentsCountTerm450to499
	 */
	public void setReviewContentsCountTerm450to499(
			BigDecimal reviewContentsCountTerm450to499) {
		this.reviewContentsCountTerm450to499 = reviewContentsCountTerm450to499;
	}

}
