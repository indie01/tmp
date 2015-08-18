/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.core.aop.TimestampHolder;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.yodobashi.community.resource.cache.MethodCache;
import com.kickmogu.yodobashi.community.resource.cache.CacheStrategyType;
import com.kickmogu.yodobashi.community.resource.cache.TargetSystemType;
import com.kickmogu.yodobashi.community.resource.dao.AdminConfigDao;
import com.kickmogu.yodobashi.community.resource.domain.AdminConfigDO;
import com.kickmogu.yodobashi.community.resource.domain.ScoreFactorDO;

/**
 * 管理者コンフィグ です。
 * @author kamiike
 *
 */
@Service
public class AdminConfigDaoImpl implements AdminConfigDao {

	/**
	 * HBaseアクセサです。
	 */
	@Autowired  @Qualifier("default")
	private HBaseOperations hBaseOperations;

	/**
	 * タイムスタンプホルダーです。
	 */
	@Autowired
	private TimestampHolder timestampHolder;

	/**
	 * コンフィグを返します。
	 * @param key キー
	 * @return コンフィグ
	 */
	@Override
	public AdminConfigDO loadAdminConfig(String key) {
		return hBaseOperations.load(AdminConfigDO.class, key);
	}

	/**
	 * コンフィグを保存します。
	 * @param adminConfig コンフィグ
	 */
	@Override
	public void saveAdminConfig(AdminConfigDO adminConfig) {
		adminConfig.setModifyDateTime(timestampHolder.getTimestamp());
		hBaseOperations.save(adminConfig);
	}

	/**
	 * スコア係数を返します。
	 * @return スコア係数
	 */
	@Override
	public ScoreFactorDO loadScoreFactor() {
		ScoreFactorDO factor = null;
		AdminConfigDO productMasterReviewPostCount = hBaseOperations.load(
				AdminConfigDO.class, ScoreFactorDO.PRODUCT_MASTER_REVIEW_POST_COUNT);
		if (productMasterReviewPostCount != null) {
			if (factor == null) {
				factor = new ScoreFactorDO();
			}
			factor.setProductMasterReviewPostCount(
					new BigDecimal(productMasterReviewPostCount.getValue()));
		}
		AdminConfigDO productMasterReviewPostCountLimit = hBaseOperations.load(
				AdminConfigDO.class, ScoreFactorDO.PRODUCT_MASTER_REVIEW_POST_COUNT_LIMIT);
		if (productMasterReviewPostCountLimit != null) {
			if (factor == null) {
				factor = new ScoreFactorDO();
			}
			factor.setProductMasterReviewPostCountLimit(
					Long.valueOf(productMasterReviewPostCountLimit.getValue()));
		}
		AdminConfigDO productMasterReviewShowCount = hBaseOperations.load(
				AdminConfigDO.class, ScoreFactorDO.PRODUCT_MASTER_REVIEW_SHOW_COUNT);
		if (productMasterReviewShowCount != null) {
			if (factor == null) {
				factor = new ScoreFactorDO();
			}
			factor.setProductMasterReviewShowCount(
					new BigDecimal(productMasterReviewShowCount.getValue()));
		}
		AdminConfigDO productMasterReviewLikeCount = hBaseOperations.load(
				AdminConfigDO.class, ScoreFactorDO.PRODUCT_MASTER_REVIEW_LIKE_COUNT);
		if (productMasterReviewLikeCount != null) {
			if (factor == null) {
				factor = new ScoreFactorDO();
			}
			factor.setProductMasterReviewLikeCount(
					new BigDecimal(productMasterReviewLikeCount.getValue()));
		}
		AdminConfigDO productMasterAnswerPostCount = hBaseOperations.load(
				AdminConfigDO.class, ScoreFactorDO.PRODUCT_MASTER_ANSWER_POST_COUNT);
		if (productMasterAnswerPostCount != null) {
			if (factor == null) {
				factor = new ScoreFactorDO();
			}
			factor.setProductMasterAnswerPostCount(
					new BigDecimal(productMasterAnswerPostCount.getValue()));
		}
		AdminConfigDO productMasterAnswerLikeCount = hBaseOperations.load(
				AdminConfigDO.class, ScoreFactorDO.PRODUCT_MASTER_ANSWER_LIKE_COUNT);
		if (productMasterAnswerLikeCount != null) {
			if (factor == null) {
				factor = new ScoreFactorDO();
			}
			factor.setProductMasterAnswerLikeCount(
					new BigDecimal(productMasterAnswerLikeCount.getValue()));
		}
		AdminConfigDO productMasterImagePostCount = hBaseOperations.load(
				AdminConfigDO.class, ScoreFactorDO.PRODUCT_MASTER_IMAGE_POST_COUNT);
		if (productMasterImagePostCount != null) {
			if (factor == null) {
				factor = new ScoreFactorDO();
			}
			factor.setProductMasterImagePostCount(
					new BigDecimal(productMasterImagePostCount.getValue()));
		}
		AdminConfigDO productMasterImageLikeCount = hBaseOperations.load(
				AdminConfigDO.class, ScoreFactorDO.PRODUCT_MASTER_IMAGE_LIKE_COUNT);
		if (productMasterImageLikeCount != null) {
			if (factor == null) {
				factor = new ScoreFactorDO();
			}
			factor.setProductMasterImageLikeCount(
					new BigDecimal(productMasterImageLikeCount.getValue()));
		}
		AdminConfigDO reviewDay = hBaseOperations.load(
				AdminConfigDO.class, ScoreFactorDO.REVIEW_DAY);
		if (reviewDay != null) {
			if (factor == null) {
				factor = new ScoreFactorDO();
			}
			factor.setReviewDay(
					new BigDecimal(reviewDay.getValue()));
		}
		AdminConfigDO reviewCommentCount = hBaseOperations.load(
				AdminConfigDO.class, ScoreFactorDO.REVIEW_COMMENT_COUNT);
		if (reviewCommentCount != null) {
			if (factor == null) {
				factor = new ScoreFactorDO();
			}
			factor.setReviewCommentCount(
					new BigDecimal(reviewCommentCount.getValue()));
		}
		AdminConfigDO reviewLikeCount = hBaseOperations.load(
				AdminConfigDO.class, ScoreFactorDO.REVIEW_LIKE_COUNT);
		if (reviewLikeCount != null) {
			if (factor == null) {
				factor = new ScoreFactorDO();
			}
			factor.setReviewLikeCount(
					new BigDecimal(reviewLikeCount.getValue()));
		}
		AdminConfigDO reviewViewCount = hBaseOperations.load(
				AdminConfigDO.class, ScoreFactorDO.REVIEW_VIEW_COUNT);
		if (reviewViewCount != null) {
			if (factor == null) {
				factor = new ScoreFactorDO();
			}
			factor.setReviewViewCount(
					new BigDecimal(reviewViewCount.getValue()));
		}
		AdminConfigDO reviewFollowerCount = hBaseOperations.load(
				AdminConfigDO.class, ScoreFactorDO.REVIEW_FOLLOWER_COUNT);
		if (reviewFollowerCount != null) {
			if (factor == null) {
				factor = new ScoreFactorDO();
			}
			factor.setReviewFollowerCount(
					new BigDecimal(reviewFollowerCount.getValue()));
		}
		AdminConfigDO questionDay = hBaseOperations.load(
				AdminConfigDO.class, ScoreFactorDO.QUESTION_DAY);
		if (questionDay != null) {
			if (factor == null) {
				factor = new ScoreFactorDO();
			}
			factor.setQuestionDay(
					new BigDecimal(questionDay.getValue()));
		}
		AdminConfigDO questionFollowerCount = hBaseOperations.load(
				AdminConfigDO.class, ScoreFactorDO.QUESTION_FOLLOWER_COUNT);
		if (questionFollowerCount != null) {
			if (factor == null) {
				factor = new ScoreFactorDO();
			}
			factor.setQuestionFollowerCount(
					new BigDecimal(questionFollowerCount.getValue()));
		}
		AdminConfigDO questionLikeCount = hBaseOperations.load(
				AdminConfigDO.class, ScoreFactorDO.QUESTION_LIKE_COUNT);
		if (questionLikeCount != null) {
			if (factor == null) {
				factor = new ScoreFactorDO();
			}
			factor.setQuestionLikeCount(
					new BigDecimal(questionLikeCount.getValue()));
		}
		AdminConfigDO questionAnswerCount = hBaseOperations.load(
				AdminConfigDO.class, ScoreFactorDO.QUESTION_ANSWER_COUNT);
		if (questionAnswerCount != null) {
			if (factor == null) {
				factor = new ScoreFactorDO();
			}
			factor.setQuestionAnswerCount(
					new BigDecimal(questionAnswerCount.getValue()));
		}
		AdminConfigDO questionViewCount = hBaseOperations.load(
				AdminConfigDO.class, ScoreFactorDO.QUESTION_VIEW_COUNT);
		if (questionViewCount != null) {
			if (factor == null) {
				factor = new ScoreFactorDO();
			}
			factor.setQuestionViewCount(
					new BigDecimal(questionViewCount.getValue()));
		}
		AdminConfigDO questionAnswerDay = hBaseOperations.load(
				AdminConfigDO.class, ScoreFactorDO.QUESTION_ANSWER_DAY);
		if (questionAnswerDay != null) {
			if (factor == null) {
				factor = new ScoreFactorDO();
			}
			factor.setQuestionAnswerDay(
					new BigDecimal(questionAnswerDay.getValue()));
		}
		AdminConfigDO questionAnswerCommentCount = hBaseOperations.load(
				AdminConfigDO.class, ScoreFactorDO.QUESTION_ANSWER_COMMENT_COUNT);
		if (questionAnswerCommentCount != null) {
			if (factor == null) {
				factor = new ScoreFactorDO();
			}
			factor.setQuestionAnswerCommentCount(
					new BigDecimal(questionAnswerCommentCount.getValue()));
		}
		AdminConfigDO questionAnswerLikeCount = hBaseOperations.load(
				AdminConfigDO.class, ScoreFactorDO.QUESTION_ANSWER_LIKE_COUNT);
		if (questionAnswerLikeCount != null) {
			if (factor == null) {
				factor = new ScoreFactorDO();
			}
			factor.setQuestionAnswerLikeCount(
					new BigDecimal(questionAnswerLikeCount.getValue()));
		}
		AdminConfigDO questionAnswerFollowerCount = hBaseOperations.load(
				AdminConfigDO.class, ScoreFactorDO.QUESTION_ANSWER_FOLLOWER_COUNT);
		if (questionAnswerFollowerCount != null) {
			if (factor == null) {
				factor = new ScoreFactorDO();
			}
			factor.setQuestionAnswerFollowerCount(
					new BigDecimal(questionAnswerFollowerCount.getValue()));
		}
		AdminConfigDO imageDay = hBaseOperations.load(
				AdminConfigDO.class, ScoreFactorDO.IMAGE_DAY);
		if (imageDay != null) {
			if (factor == null) {
				factor = new ScoreFactorDO();
			}
			factor.setImageDay(
					new BigDecimal(imageDay.getValue()));
		}
		AdminConfigDO imageCommentCount = hBaseOperations.load(
				AdminConfigDO.class, ScoreFactorDO.IMAGE_COMMENT_COUNT);
		if (imageCommentCount != null) {
			if (factor == null) {
				factor = new ScoreFactorDO();
			}
			factor.setImageCommentCount(
					new BigDecimal(imageCommentCount.getValue()));
		}
		AdminConfigDO imageLikeCount = hBaseOperations.load(
				AdminConfigDO.class, ScoreFactorDO.IMAGE_LIKE_COUNT);
		if (imageLikeCount != null) {
			if (factor == null) {
				factor = new ScoreFactorDO();
			}
			factor.setImageLikeCount(
					new BigDecimal(imageLikeCount.getValue()));
		}
		AdminConfigDO imageViewCount = hBaseOperations.load(
				AdminConfigDO.class, ScoreFactorDO.IMAGE_VIEW_COUNT);
		if (imageViewCount != null) {
			if (factor == null) {
				factor = new ScoreFactorDO();
			}
			factor.setImageViewCount(
					new BigDecimal(imageViewCount.getValue()));
		}

		AdminConfigDO reviewHasImagesCoefficient = hBaseOperations.load(
				AdminConfigDO.class, ScoreFactorDO.REVIEW_HAS_IMAGES_COEFFICIENT);
		if (reviewHasImagesCoefficient != null) {
			if (factor == null) {
				factor = new ScoreFactorDO();
			}
			factor.setReviewHasImagesCoefficient(
					new BigDecimal(reviewHasImagesCoefficient.getValue()));
		}

		AdminConfigDO reviewContentsCountCoefficient = hBaseOperations.load(
				AdminConfigDO.class, ScoreFactorDO.REVIEW_CONTENTS_COUNT_COEFFICIENT);
		if (reviewContentsCountCoefficient != null) {
			if (factor == null) {
				factor = new ScoreFactorDO();
			}
			factor.setReviewContentsCountCoefficient(
					new BigDecimal(reviewContentsCountCoefficient.getValue()));
		}

		AdminConfigDO reviewContentsCountTerm0to99 = hBaseOperations.load(
				AdminConfigDO.class, ScoreFactorDO.REVIEW_CONTENTS_COUNT_TERM_0TO99);
		if (reviewContentsCountTerm0to99 != null) {
			if (factor == null) {
				factor = new ScoreFactorDO();
			}
			factor.setReviewContentsCountTerm0to99(
					new BigDecimal(reviewContentsCountTerm0to99.getValue()));
		}

		AdminConfigDO reviewContentsCountTerm100to199 = hBaseOperations.load(
				AdminConfigDO.class, ScoreFactorDO.REVIEW_CONTENTS_COUNT_TERM_100TO199);
		if (reviewContentsCountTerm100to199 != null) {
			if (factor == null) {
				factor = new ScoreFactorDO();
			}
			factor.setReviewContentsCountTerm100to199(
					new BigDecimal(reviewContentsCountTerm100to199.getValue()));
		}

		AdminConfigDO reviewContentsCountTerm200to299 = hBaseOperations.load(
				AdminConfigDO.class, ScoreFactorDO.REVIEW_CONTENTS_COUNT_TERM_200TO299);
		if (reviewContentsCountTerm200to299 != null) {
			if (factor == null) {
				factor = new ScoreFactorDO();
			}
			factor.setReviewContentsCountTerm200to299(
					new BigDecimal(reviewContentsCountTerm200to299.getValue()));
		}

		AdminConfigDO reviewContentsCountTerm300to399 = hBaseOperations.load(
				AdminConfigDO.class, ScoreFactorDO.REVIEW_CONTENTS_COUNT_TERM_300TO399);
		if (reviewContentsCountTerm300to399 != null) {
			if (factor == null) {
				factor = new ScoreFactorDO();
			}
			factor.setReviewContentsCountTerm300to399(
					new BigDecimal(reviewContentsCountTerm300to399.getValue()));
		}

		AdminConfigDO reviewContentsCountTerm400to449 = hBaseOperations.load(
				AdminConfigDO.class, ScoreFactorDO.REVIEW_CONTENTS_COUNT_TERM_400TO449);
		if (reviewContentsCountTerm400to449 != null) {
			if (factor == null) {
				factor = new ScoreFactorDO();
			}
			factor.setReviewContentsCountTerm400to449(
					new BigDecimal(reviewContentsCountTerm400to449.getValue()));
		}
		AdminConfigDO reviewContentsCountTerm450to499 = hBaseOperations.load(
				AdminConfigDO.class, ScoreFactorDO.REVIEW_CONTENTS_COUNT_TERM_450TO499);
		if (reviewContentsCountTerm450to499 != null) {
			if (factor == null) {
				factor = new ScoreFactorDO();
			}
			factor.setReviewContentsCountTerm450to499(
					new BigDecimal(reviewContentsCountTerm450to499.getValue()));
		}

		AdminConfigDO reviewContentsCountTermMore500 = hBaseOperations.load(
				AdminConfigDO.class, ScoreFactorDO.REVIEW_CONTENTS_COUNT_TERM_MORE_500);
		if (reviewContentsCountTermMore500 != null) {
			if (factor == null) {
				factor = new ScoreFactorDO();
			}
			factor.setReviewContentsCountTermMore500(
					new BigDecimal(reviewContentsCountTermMore500.getValue()));
		}

		AdminConfigDO reviewHasImages = hBaseOperations.load(
				AdminConfigDO.class, ScoreFactorDO.REVIEW_HAS_IMAGES);
		if (reviewHasImages != null) {
			if (factor == null) {
				factor = new ScoreFactorDO();
			}
			factor.setReviewHasImages(
					new BigDecimal(reviewHasImages.getValue()));
		}

		AdminConfigDO questionAnswerHasImagesCoefficient = hBaseOperations.load(
				AdminConfigDO.class, ScoreFactorDO.QUESTION_ANSWER_HAS_IMAGES_COEFFICIENT);
		if (questionAnswerHasImagesCoefficient != null) {
			if (factor == null) {
				factor = new ScoreFactorDO();
			}
			factor.setQuestionAnswerHasImagesCoefficient(
					new BigDecimal(questionAnswerHasImagesCoefficient.getValue()));
		}

		AdminConfigDO questionAnswerContentsCountCoefficient = hBaseOperations.load(
				AdminConfigDO.class, ScoreFactorDO.QUESTION_ANSWER_CONTENTS_COUNT_COEFFICIENT);
		if (questionAnswerContentsCountCoefficient != null) {
			if (factor == null) {
				factor = new ScoreFactorDO();
			}
			factor.setQuestionAnswerContentsCountCoefficient(
					new BigDecimal(questionAnswerContentsCountCoefficient.getValue()));
		}

		AdminConfigDO questionAnswerContentsCountTerm0to99 = hBaseOperations.load(
				AdminConfigDO.class, ScoreFactorDO.QUESTION_ANSWER_CONTENTS_COUNT_TERM_0TO99);
		if (questionAnswerContentsCountTerm0to99 != null) {
			if (factor == null) {
				factor = new ScoreFactorDO();
			}
			factor.setQuestionAnswerContentsCountTerm0to99(
					new BigDecimal(questionAnswerContentsCountTerm0to99.getValue()));
		}

		AdminConfigDO questionAnswerContentsCountTerm100to199 = hBaseOperations.load(
				AdminConfigDO.class, ScoreFactorDO.QUESTION_ANSWER_CONTENTS_COUNT_TERM_100TO199);
		if (questionAnswerContentsCountTerm100to199 != null) {
			if (factor == null) {
				factor = new ScoreFactorDO();
			}
			factor.setQuestionAnswerContentsCountTerm100to199(
					new BigDecimal(questionAnswerContentsCountTerm100to199.getValue()));
		}

		AdminConfigDO questionAnswerContentsCountTerm200to299 = hBaseOperations.load(
				AdminConfigDO.class, ScoreFactorDO.QUESTION_ANSWER_CONTENTS_COUNT_TERM_200TO299);
		if (questionAnswerContentsCountTerm200to299 != null) {
			if (factor == null) {
				factor = new ScoreFactorDO();
			}
			factor.setQuestionAnswerContentsCountTerm200to299(
					new BigDecimal(questionAnswerContentsCountTerm200to299.getValue()));
		}

		AdminConfigDO questionAnswerContentsCountTerm300to399 = hBaseOperations.load(
				AdminConfigDO.class, ScoreFactorDO.QUESTION_ANSWER_CONTENTS_COUNT_TERM_300TO399);
		if (questionAnswerContentsCountTerm300to399 != null) {
			if (factor == null) {
				factor = new ScoreFactorDO();
			}
			factor.setQuestionAnswerContentsCountTerm300to399(
					new BigDecimal(questionAnswerContentsCountTerm300to399.getValue()));
		}

		AdminConfigDO questionAnswerContentsCountTerm400to449 = hBaseOperations.load(
				AdminConfigDO.class, ScoreFactorDO.QUESTION_ANSWER_CONTENTS_COUNT_TERM_400TO449);
		if (questionAnswerContentsCountTerm400to449 != null) {
			if (factor == null) {
				factor = new ScoreFactorDO();
			}
			factor.setQuestionAnswerContentsCountTerm400to449(
					new BigDecimal(questionAnswerContentsCountTerm400to449.getValue()));
		}

		AdminConfigDO questionAnswerContentsCountTerm450to499 = hBaseOperations.load(
				AdminConfigDO.class, ScoreFactorDO.QUESTION_ANSWER_CONTENTS_COUNT_TERM_450TO499);
		if (questionAnswerContentsCountTerm450to499 != null) {
			if (factor == null) {
				factor = new ScoreFactorDO();
			}
			factor.setQuestionAnswerContentsCountTerm450to499(
					new BigDecimal(questionAnswerContentsCountTerm450to499.getValue()));
		}

		AdminConfigDO questionAnswerContentsCountTermMore500 = hBaseOperations.load(
				AdminConfigDO.class, ScoreFactorDO.QUESTION_ANSWER_CONTENTS_COUNT_TERM_MORE_500);
		if (questionAnswerContentsCountTermMore500 != null) {
			if (factor == null) {
				factor = new ScoreFactorDO();
			}
			factor.setQuestionAnswerContentsCountTermMore500(
					new BigDecimal(questionAnswerContentsCountTermMore500.getValue()));
		}

		AdminConfigDO questionAnswerHasImages = hBaseOperations.load(
				AdminConfigDO.class, ScoreFactorDO.QUESTION_ANSWER_HAS_IMAGES);
		if (questionAnswerHasImages != null) {
			if (factor == null) {
				factor = new ScoreFactorDO();
			}
			factor.setQuestionAnswerHasImages(
					new BigDecimal(questionAnswerHasImages.getValue()));
		}

		AdminConfigDO productMasterImagePostCountLimit = hBaseOperations.load(
				AdminConfigDO.class, ScoreFactorDO.PRODUCT_MASTER_IMAGE_POST_COUNT_LIMIT);
		if (productMasterImagePostCountLimit != null) {
			if (factor == null) {
				factor = new ScoreFactorDO();
			}
			factor.setProductMasterImagePostCountLimit(
					Long.valueOf(productMasterImagePostCountLimit.getValue()));
		}
		return factor;
	}

	private AdminConfigDO createInstance(
			String key,
			String value,
			ScoreFactorDO scoreFactor) {
		AdminConfigDO config = new AdminConfigDO();
		config.setKey(key);
		config.setValue(value);
		config.setRegisterDateTime(scoreFactor.getRegisterDateTime());
		config.setModifyDateTime(timestampHolder.getTimestamp());
		return config;
	}

	/**
	 * スコア係数を保存します。
	 * @param scoreFactor スコア係数
	 */
	@Override
	public void saveScoreFactor(ScoreFactorDO scoreFactor) {
		List<AdminConfigDO> configs = new ArrayList<AdminConfigDO>();
		configs.add(createInstance(
				ScoreFactorDO.PRODUCT_MASTER_REVIEW_POST_COUNT,
				scoreFactor.getProductMasterReviewPostCount().toString(),
				scoreFactor));
		configs.add(createInstance(
				ScoreFactorDO.PRODUCT_MASTER_REVIEW_POST_COUNT_LIMIT,
				String.valueOf(scoreFactor.getProductMasterReviewPostCountLimit()),
				scoreFactor));
		configs.add(createInstance(
				ScoreFactorDO.PRODUCT_MASTER_REVIEW_SHOW_COUNT,
				String.valueOf(scoreFactor.getProductMasterReviewShowCount()),
				scoreFactor));
		configs.add(createInstance(
				ScoreFactorDO.PRODUCT_MASTER_REVIEW_LIKE_COUNT,
				String.valueOf(scoreFactor.getProductMasterReviewLikeCount()),
				scoreFactor));
		configs.add(createInstance(
				ScoreFactorDO.PRODUCT_MASTER_ANSWER_POST_COUNT,
				String.valueOf(scoreFactor.getProductMasterAnswerPostCount()),
				scoreFactor));
		configs.add(createInstance(
				ScoreFactorDO.PRODUCT_MASTER_ANSWER_LIKE_COUNT,
				String.valueOf(scoreFactor.getProductMasterAnswerLikeCount()),
				scoreFactor));
		configs.add(createInstance(
				ScoreFactorDO.PRODUCT_MASTER_IMAGE_POST_COUNT,
				String.valueOf(scoreFactor.getProductMasterImagePostCount()),
				scoreFactor));
		configs.add(createInstance(
				ScoreFactorDO.PRODUCT_MASTER_IMAGE_LIKE_COUNT,
				String.valueOf(scoreFactor.getProductMasterImageLikeCount()),
				scoreFactor));
		configs.add(createInstance(
				ScoreFactorDO.REVIEW_DAY,
				String.valueOf(scoreFactor.getReviewDay()),
				scoreFactor));
		configs.add(createInstance(
				ScoreFactorDO.REVIEW_COMMENT_COUNT,
				String.valueOf(scoreFactor.getReviewCommentCount()),
				scoreFactor));
		configs.add(createInstance(
				ScoreFactorDO.REVIEW_LIKE_COUNT,
				String.valueOf(scoreFactor.getReviewLikeCount()),
				scoreFactor));
		configs.add(createInstance(
				ScoreFactorDO.REVIEW_VIEW_COUNT,
				String.valueOf(scoreFactor.getReviewViewCount()),
				scoreFactor));
		configs.add(createInstance(
				ScoreFactorDO.REVIEW_FOLLOWER_COUNT,
				String.valueOf(scoreFactor.getReviewFollowerCount()),
				scoreFactor));
		configs.add(createInstance(
				ScoreFactorDO.QUESTION_DAY,
				String.valueOf(scoreFactor.getQuestionDay()),
				scoreFactor));
		configs.add(createInstance(
				ScoreFactorDO.QUESTION_FOLLOWER_COUNT,
				String.valueOf(scoreFactor.getQuestionFollowerCount()),
				scoreFactor));
		configs.add(createInstance(
				ScoreFactorDO.QUESTION_LIKE_COUNT,
				String.valueOf(scoreFactor.getQuestionLikeCount()),
				scoreFactor));
		configs.add(createInstance(
				ScoreFactorDO.QUESTION_ANSWER_COUNT,
				String.valueOf(scoreFactor.getQuestionAnswerCount()),
				scoreFactor));
		configs.add(createInstance(
				ScoreFactorDO.QUESTION_VIEW_COUNT,
				String.valueOf(scoreFactor.getQuestionViewCount()),
				scoreFactor));
		configs.add(createInstance(
				ScoreFactorDO.QUESTION_ANSWER_DAY,
				String.valueOf(scoreFactor.getQuestionAnswerDay()),
				scoreFactor));
		configs.add(createInstance(
				ScoreFactorDO.QUESTION_ANSWER_COMMENT_COUNT,
				String.valueOf(scoreFactor.getQuestionAnswerCommentCount()),
				scoreFactor));
		configs.add(createInstance(
				ScoreFactorDO.QUESTION_ANSWER_LIKE_COUNT,
				String.valueOf(scoreFactor.getQuestionAnswerLikeCount()),
				scoreFactor));
		configs.add(createInstance(
				ScoreFactorDO.QUESTION_ANSWER_FOLLOWER_COUNT,
				String.valueOf(scoreFactor.getQuestionAnswerFollowerCount()),
				scoreFactor));
		configs.add(createInstance(
				ScoreFactorDO.IMAGE_DAY,
				String.valueOf(scoreFactor.getImageDay()),
				scoreFactor));
		configs.add(createInstance(
				ScoreFactorDO.IMAGE_COMMENT_COUNT,
				String.valueOf(scoreFactor.getImageCommentCount()),
				scoreFactor));
		configs.add(createInstance(
				ScoreFactorDO.IMAGE_LIKE_COUNT,
				String.valueOf(scoreFactor.getImageLikeCount()),
				scoreFactor));
		configs.add(createInstance(
				ScoreFactorDO.IMAGE_VIEW_COUNT,
				String.valueOf(scoreFactor.getImageViewCount()),
				scoreFactor));


		configs.add(createInstance(
				ScoreFactorDO.REVIEW_HAS_IMAGES_COEFFICIENT,
				String.valueOf(scoreFactor.getReviewHasImagesCoefficient()),
				scoreFactor));

		configs.add(createInstance(
				ScoreFactorDO.REVIEW_CONTENTS_COUNT_COEFFICIENT,
				String.valueOf(scoreFactor.getReviewContentsCountCoefficient()),
				scoreFactor));

		configs.add(createInstance(
				ScoreFactorDO.REVIEW_CONTENTS_COUNT_COEFFICIENT,
				String.valueOf(scoreFactor.getReviewContentsCountCoefficient()),
				scoreFactor));

		configs.add(createInstance(
				ScoreFactorDO.REVIEW_CONTENTS_COUNT_TERM_0TO99,
				String.valueOf(scoreFactor.getReviewContentsCountTerm0to99()),
				scoreFactor));

		configs.add(createInstance(
				ScoreFactorDO.REVIEW_CONTENTS_COUNT_TERM_100TO199,
				String.valueOf(scoreFactor.getReviewContentsCountTerm100to199()),
				scoreFactor));

		configs.add(createInstance(
				ScoreFactorDO.REVIEW_CONTENTS_COUNT_TERM_200TO299,
				String.valueOf(scoreFactor.getReviewContentsCountTerm200to299()),
				scoreFactor));

		configs.add(createInstance(
				ScoreFactorDO.REVIEW_CONTENTS_COUNT_TERM_300TO399,
				String.valueOf(scoreFactor.getReviewContentsCountTerm300to399()),
				scoreFactor));

		configs.add(createInstance(
				ScoreFactorDO.REVIEW_CONTENTS_COUNT_TERM_400TO449,
				String.valueOf(scoreFactor.getReviewContentsCountTerm400to449()),
				scoreFactor));

		configs.add(createInstance(
				ScoreFactorDO.REVIEW_CONTENTS_COUNT_TERM_450TO499,
				String.valueOf(scoreFactor.getReviewContentsCountTerm450to499()),
				scoreFactor));

		configs.add(createInstance(
				ScoreFactorDO.REVIEW_CONTENTS_COUNT_TERM_MORE_500,
				String.valueOf(scoreFactor.getReviewContentsCountTermMore500()),
				scoreFactor));

		configs.add(createInstance(
				ScoreFactorDO.REVIEW_HAS_IMAGES,
				String.valueOf(scoreFactor.getReviewHasImages()),
				scoreFactor));

		configs.add(createInstance(
				ScoreFactorDO.QUESTION_ANSWER_HAS_IMAGES_COEFFICIENT,
				String.valueOf(scoreFactor.getQuestionAnswerHasImagesCoefficient()),
				scoreFactor));

		configs.add(createInstance(
				ScoreFactorDO.QUESTION_ANSWER_CONTENTS_COUNT_COEFFICIENT,
				String.valueOf(scoreFactor.getQuestionAnswerContentsCountCoefficient()),
				scoreFactor));

		configs.add(createInstance(
				ScoreFactorDO.QUESTION_ANSWER_CONTENTS_COUNT_TERM_0TO99,
				String.valueOf(scoreFactor.getQuestionAnswerContentsCountTerm0to99()),
				scoreFactor));

		configs.add(createInstance(
				ScoreFactorDO.QUESTION_ANSWER_CONTENTS_COUNT_TERM_100TO199,
				String.valueOf(scoreFactor.getQuestionAnswerContentsCountTerm100to199()),
				scoreFactor));

		configs.add(createInstance(
				ScoreFactorDO.QUESTION_ANSWER_CONTENTS_COUNT_TERM_200TO299,
				String.valueOf(scoreFactor.getQuestionAnswerContentsCountTerm200to299()),
				scoreFactor));

		configs.add(createInstance(
				ScoreFactorDO.QUESTION_ANSWER_CONTENTS_COUNT_TERM_300TO399,
				String.valueOf(scoreFactor.getQuestionAnswerContentsCountTerm300to399()),
				scoreFactor));

		configs.add(createInstance(
				ScoreFactorDO.QUESTION_ANSWER_CONTENTS_COUNT_TERM_400TO449,
				String.valueOf(scoreFactor.getQuestionAnswerContentsCountTerm400to449()),
				scoreFactor));

		configs.add(createInstance(
				ScoreFactorDO.QUESTION_ANSWER_CONTENTS_COUNT_TERM_450TO499,
				String.valueOf(scoreFactor.getQuestionAnswerContentsCountTerm450to499()),
				scoreFactor));

		configs.add(createInstance(
				ScoreFactorDO.QUESTION_ANSWER_CONTENTS_COUNT_TERM_MORE_500,
				String.valueOf(scoreFactor.getQuestionAnswerContentsCountTermMore500()),
				scoreFactor));

		configs.add(createInstance(
				ScoreFactorDO.QUESTION_ANSWER_HAS_IMAGES,
				String.valueOf(scoreFactor.getQuestionAnswerHasImages()),
				scoreFactor));

		configs.add(createInstance(
				ScoreFactorDO.PRODUCT_MASTER_IMAGE_POST_COUNT_LIMIT,
				String.valueOf(scoreFactor.getProductMasterImagePostCountLimit()),
				scoreFactor));

		hBaseOperations.save(AdminConfigDO.class, configs);
	}

	@Override
	@MethodCache(
			cacheStrategy=CacheStrategyType.JavaVMGlobal,
			limitTime=30,
			limitTimeUnit=TimeUnit.SECONDS,
			targetSystems={TargetSystemType.CommunityWeb, TargetSystemType.CommunityWs, TargetSystemType.CommunityJc, TargetSystemType.CommunityBatch, TargetSystemType.CommunityDataSyncWeb}
			)
	public AdminConfigDO loadAdminConfigWithCache(String key) {
		return hBaseOperations.load(AdminConfigDO.class, key);
	}


}
