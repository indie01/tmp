/**
 *
 */
package com.kickmogu.yodobashi.community.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.FacebookLink;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.hadoop.hbase.aop.ArroundHBase;
import com.kickmogu.lib.solr.annotation.ArroundSolr;
import com.kickmogu.yodobashi.community.common.utils.StringUtil;
import com.kickmogu.yodobashi.community.resource.dao.ImageDao;
import com.kickmogu.yodobashi.community.resource.dao.ProductDao;
import com.kickmogu.yodobashi.community.resource.dao.ProductMasterDao;
import com.kickmogu.yodobashi.community.resource.dao.QuestionAnswerDao;
import com.kickmogu.yodobashi.community.resource.dao.QuestionDao;
import com.kickmogu.yodobashi.community.resource.dao.RequestScopeDao;
import com.kickmogu.yodobashi.community.resource.dao.ReviewDao;
import com.kickmogu.yodobashi.community.resource.dao.ShortUrlDao;
import com.kickmogu.yodobashi.community.resource.dao.SocialMediaSettingDao;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductMasterDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionAnswerDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.SocialMediaSettingDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.AsyncMessageType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ContentsStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.SocialMediaPublicSettingType;
import com.kickmogu.yodobashi.community.resource.domain.constants.SocialMediaType;
import com.kickmogu.yodobashi.community.service.SocialMediaService;
import com.kickmogu.yodobashi.community.service.annotation.SendMessage;
import com.kickmogu.yodobashi.community.service.annotation.SendMessage.Timing;
import com.kickmogu.yodobashi.community.service.config.ServiceConfig;
/**
 * ソーシャルメディア連携サービスの実装です。
 * @author kamiike
 *
 */
@Service
public class SocialMediaServiceImpl implements SocialMediaService, InitializingBean  {

	/**
	 * このクラスに関するログを出力するためのインスタンスです。
	 */
	private static final Logger LOG = LoggerFactory.getLogger(SocialMediaServiceImpl.class);
	
	/**
	 * ソーシャルメディア連携設定 DAO です。
	 */
	@Autowired
	private SocialMediaSettingDao socialMediaSettingDao;

	/**
	 * リクエストスコープ DAO です。
	 */
	@Autowired
	private RequestScopeDao requestScopeDao;

	/**
	 * レビュー DAO です。
	 */
	@Autowired
	private ReviewDao reviewDao;

	/**
	 * 質問 DAO です。
	 */
	@Autowired
	private QuestionDao questionDao;

	/**
	 * 質問回答 DAO です。
	 */
	@Autowired
	private QuestionAnswerDao questionAnswerDao;

	/**
	 * 商品 DAO です。
	 */
	@Autowired @Qualifier("catalog")
	private ProductDao productDao;

	/**
	 * 商品マスター DAO です。
	 */
	@Autowired
	private ProductMasterDao productMasterDao;
	
	/**
	 * 画像 DAO です。
	 */
	@Autowired
	private ImageDao imageDao;

	/**
	 * ショートURLを管理するDaoです。
	 */
	@Autowired
	private ShortUrlDao shortUrlDao;

	@Autowired
	private ServiceConfig serviceConfig;

	private String siteUrl;
	
	private String crlf = System.getProperty("line.separator");

	/**
	 * レビュー投稿直後にソーシャルメディアに通知します。
	 * @param reviewId レビューID
	 * @param communityUserId コミュニティユーザーID
	 */
	@Override
	@SendMessage(timing=Timing.ASYNC_AFTER_PROCESS,asyncMessageType=AsyncMessageType.DELAYSERVICE,delayTime=300000L)
	@ArroundSolr
	@ArroundHBase
	public void notifySocialMediaForReviewSubmit(String reviewId, String communityUserId) {
		CommunityUserDO communityUser = new CommunityUserDO();
		communityUser.setCommunityUserId(communityUserId);
		requestScopeDao.initialize(communityUser, null);
		List<SocialMediaType> sendMedias = new ArrayList<SocialMediaType>();
		for (SocialMediaSettingDO socialMediaSetting : socialMediaSettingDao.findBySocialMediaSettingByCommunityUserId(communityUserId)) {
			if (socialMediaSetting.isPublic(SocialMediaPublicSettingType.REVIEW)) {
				sendMedias.add(socialMediaSetting.getSocialMediaType());
			}
		}
		ReviewDO review = reviewDao.loadReview(reviewId);
		String reviewUrl = siteUrl + "/product/review/" + reviewId + "/detail.html";
		String twitterTxt = 
				"商品レビュー：" 
				+ getOmissionContent(StringUtil.stripTags(review.getReviewBody()), "38", "...") 
				+ " - ヨドバシ.com  ";
		String fbTxt = 
				"レビューを投稿しました。"+ crlf 
				+ getOmissionContent(StringUtil.stripTags(review.getReviewBody()), "90", "...");
		String fbName = "ヨドバシ.com - " + getOmissionContent(review.getProduct().getProductName(), "90", "...") + "の";
		if("1".equals(review.getReviewType().getCode())){
			fbName = fbName + "第一印象レビュー";
		}else{
			fbName = fbName + "満足度レビュー";
		};
		String fbDescription = " ";
		sendContents(sendMedias, twitterTxt, fbTxt, fbName, fbDescription, reviewUrl);
	}

	/**
	 * 質問投稿直後にソーシャルメディアに通知します。
	 * @param questionId 質問ID
	 * @param communityUserId コミュニティユーザーID
	 */
	@Override
	@SendMessage(timing=Timing.ASYNC_AFTER_PROCESS,asyncMessageType=AsyncMessageType.DELAYSERVICE,delayTime=300000L)
	@ArroundSolr
	@ArroundHBase
	public void notifySocialMediaForQuestionSubmit(String questionId, String communityUserId) {
		CommunityUserDO communityUser = new CommunityUserDO();
		communityUser.setCommunityUserId(communityUserId);
		requestScopeDao.initialize(communityUser, null);
		List<SocialMediaType> sendMedias = new ArrayList<SocialMediaType>();
		for (SocialMediaSettingDO socialMediaSetting : socialMediaSettingDao.findBySocialMediaSettingByCommunityUserId(
				communityUserId)) {
			if (socialMediaSetting.isPublic(SocialMediaPublicSettingType.QUESTION)) {
				sendMedias.add(socialMediaSetting.getSocialMediaType());
			}
		}

		QuestionDO question = questionDao.loadQuestion(questionId);
		String questionUrl = siteUrl + "/product/question/" + questionId + "/detail.html";
		String twitterTxt = "回答をお待ちしてます：" + getOmissionContent(
				StringUtil.stripTags(question.getQuestionBody()), "38", "...") + " - ヨドバシ.com ";
		String fbTxt = "質問を投稿しました。" + crlf 
				+ getOmissionContent(StringUtil.stripTags(question.getQuestionBody()), "90", "...");
		String fbName = "ヨドバシ.com - " + 
				getOmissionContent(question.getProduct().getProductName(), "90", "...") + "への質問";
		String fbDescription = " ";
		sendContents(sendMedias, twitterTxt, fbTxt, fbName, fbDescription, questionUrl);
	}

	/**
	 * 質問回答投稿直後にソーシャルメディアに通知します。
	 * @param questionAnswerId 質問回答ID
	 * @param communityUserId コミュニティユーザーID
	 */
	@Override
	@SendMessage(timing=Timing.ASYNC_AFTER_PROCESS,asyncMessageType=AsyncMessageType.DELAYSERVICE,delayTime=300000L)
	@ArroundSolr
	@ArroundHBase
	public void notifySocialMediaForQuestionAnswerSubmit(String questionAnswerId, String communityUserId) {
		CommunityUserDO communityUser = new CommunityUserDO();
		communityUser.setCommunityUserId(communityUserId);
		requestScopeDao.initialize(communityUser, null);
		List<SocialMediaType> sendMedias = new ArrayList<SocialMediaType>();
		for (SocialMediaSettingDO socialMediaSetting : socialMediaSettingDao.findBySocialMediaSettingByCommunityUserId(
				communityUserId)) {
			if (socialMediaSetting.isPublic(SocialMediaPublicSettingType.ANSWER)) {
				sendMedias.add(socialMediaSetting.getSocialMediaType());
			}
		}
		QuestionAnswerDO questionAnswer = questionAnswerDao.loadQuestionAnswer(questionAnswerId);
		String questionUrl = siteUrl + "/product/question/" + questionAnswer.getQuestion().getQuestionId() + "/detail.html";
		String twitterTxt = "回答しました：" + getOmissionContent(
				StringUtil.stripTags(questionAnswer.getQuestion().getQuestionBody()), "38", "...") + " - ヨドバシ.com  ";
		String fbTxt = "回答を投稿しました。" + crlf
				+ getOmissionContent(StringUtil.stripTags(questionAnswer.getAnswerBody()), "90", "...");
		String fbName = "ヨドバシ.com - " 
				+ getOmissionContent(questionAnswer.getProduct().getProductName(), "90", "...") + "への回答";
		String fbDescription = " ";
		sendContents(sendMedias,  twitterTxt, fbTxt, fbName, fbDescription, questionUrl);
	}

	/**
	 * 画像投稿直後にソーシャルメディアに通知します。
	 * @param imageId 画像ID
	 * @param imageSetId 画像セットID
	 * @param communityUserId コミュニティユーザーID
	 */
	@Override
	@SendMessage(timing=Timing.ASYNC_AFTER_PROCESS,asyncMessageType=AsyncMessageType.DELAYSERVICE,delayTime=300000L)
	@ArroundSolr
	@ArroundHBase
	public void notifySocialMediaForImageSubmit(
			String imageSetId,
			String sku, String communityUserId) {
		CommunityUserDO communityUser = new CommunityUserDO();
		communityUser.setCommunityUserId(communityUserId);
		requestScopeDao.initialize(communityUser, null);
		List<SocialMediaType> sendMedias = new ArrayList<SocialMediaType>();
		
		String mediaType = "";
		
		for (SocialMediaSettingDO socialMediaSetting : socialMediaSettingDao.findBySocialMediaSettingByCommunityUserId(
				communityUserId)) {
			if (socialMediaSetting.isPublic(SocialMediaPublicSettingType.IMAGE)) {
				sendMedias.add(socialMediaSetting.getSocialMediaType());
				mediaType += socialMediaSetting.getSocialMediaType().getLabel() + ":";
			}
		}
		List<ImageHeaderDO> imageHeaders 
		        = imageDao.findImageByImageSetId(imageSetId, null, new ContentsStatus[]{ContentsStatus.SUBMITTED, ContentsStatus.CONTENTS_STOP});
		String imageComment = "";
		for(ImageHeaderDO imageHeader : imageHeaders){
			if(null != imageHeader.getComment()){
				if(!imageComment.equals("")){
					imageComment = imageComment + "、";
				}
				imageComment = imageComment + imageHeader.getComment();
			}
		}
		ProductDO product = productDao.loadProduct(sku);
		String imageUrl = siteUrl + "/product/image/" + sku + "/" + imageSetId + "/detail.html";
		String twitterTxt = "「" + getOmissionContent(product.getProductName(),
				"35", "...") + "」に関する画像をヨドバシ.comに投稿！ ";
		String fbTxt = "画像を投稿しました。";
		System.out.println("#2");
		System.out.println(imageComment);
		if(!StringUtil.stripTags(imageComment).equals("")){ 
			fbTxt = fbTxt + crlf + getOmissionContent(StringUtil.stripTags(imageComment), "90", "...");
		}
		String fbName = "ヨドバシ.com - " + getOmissionContent(product.getProductName(), "90", "...") + "の画像";
		String fbDescription = " ";
		LOG.info(
				"communityUserId:" + communityUserId + 
				"\n" + mediaType +
				"\n「" + getOmissionContent(product.getProductName(),"35", "...") + "」に関する画像をヨドバシ.comに投稿！ " +
				"\n" + imageUrl);
		sendContents(sendMedias,  twitterTxt, fbTxt, fbName, fbDescription, imageUrl);
	}

	/**
	 * 商品マスターランクイン直後にソーシャルメディアに通知します。
	 * @param productMasterId 商品マスターID
	 * @param communityUserId コミュニティユーザーID
	 */
	@Override
	@SendMessage(timing=Timing.ASYNC_AFTER_PROCESS,asyncMessageType=AsyncMessageType.DELAYSERVICE,delayTime=300000L)
	@ArroundSolr
	@ArroundHBase
	public void notifySocialMediaForProductMasterRankIn(
			ProductMasterDO productMaster,
			String communityUserId) {
		CommunityUserDO communityUser = new CommunityUserDO();
		communityUser.setCommunityUserId(communityUserId);
		requestScopeDao.initialize(communityUser, null);
		
		String sendMedia = "";
		List<SocialMediaType> sendMedias = new ArrayList<SocialMediaType>();
		for (SocialMediaSettingDO socialMediaSetting : socialMediaSettingDao.findBySocialMediaSettingByCommunityUserId(
				communityUserId)) {
			if (socialMediaSetting.isPublic(SocialMediaPublicSettingType.RANKING)) {
				sendMedias.add(socialMediaSetting.getSocialMediaType());
				sendMedia += socialMediaSetting.getSocialMediaType().getLabel() + ":";
			}
		}
		String productMasterUrl = siteUrl + "/product/" + productMaster.getProduct().getSku() + "/master.html";
		String twitterTxt = "ヨドバシ.comで「" + getOmissionContent(productMaster.getProduct().getProductName(),
				"35", "..") + "」の商品マスター" + productMaster.getRank() + "位にランクイン！ ";
		String fbTxt = "ヨドバシ.com で" + productMaster.getProduct().getProductName() + "の商品マスターにランクインしました。";
		String fbName = "ヨドバシ.com - " + productMaster.getProduct().getProductName() + "の商品マスター";
		String fbDescription = " ";
		LOG.info(sendMedia + "\n" + 
				"communityUserId:" + communityUserId+ "\n" + 
				"contents:" + "ヨドバシ.comで「" + getOmissionContent(productMaster.getProduct().getProductName(),
						"35", "...") + "」の商品マスター" + productMaster.getRank() + "位にランクイン！ " + "\n" +  
				"url:" + productMasterUrl);
		
		sendContents(sendMedias,  twitterTxt, fbTxt, fbName, fbDescription, productMasterUrl);
	}

	/**
	 * 指定したメディアタイプにコンテンツを送信します。
	 * @param sendMedias メディアリスト
	 * @param twitterTxt Twitter連携文言
	 * @param fbTxt Facebook連携文言
	 * @param　fbName　Facebook名前
	 * @param　fbDescription　Facebook説明
	 * @param　viewUrl　リンクURL
	 */
	private void sendContents(
			List<SocialMediaType> sendMedias, 
			String twitterTxt, 
			String fbTxt, 
			String fbName, 
			String fbDescription, 
			String viewUrl
			) {
		if (sendMedias.size() > 0) {
			for (SocialMediaType type : sendMedias) {
				if (SocialMediaType.TWITTER.equals(type)) {
					try{
						String printTxt = twitterTxt + shortUrlDao.convertShortUrl(viewUrl) + " #yodobashi";
						LOG.info(">>>>>" + printTxt);
						sendContentsForTwitter(printTxt);
					}catch (Exception e) {
						LOG.error("", e);
					}
				} else if (SocialMediaType.FACEBOOK.equals(type)) {
					try{
						FacebookLink facebookLink = 
								new FacebookLink(viewUrl, fbName, serviceConfig.communityDomain, fbDescription);
						String printTxt = fbTxt + shortUrlDao.convertShortUrl(viewUrl) + " #yodobashi";
						LOG.info(">>>>>" + printTxt);
						sendContentsForFacebook(fbTxt, facebookLink);
					}catch (Exception e) {
						LOG.error("", e);
					}
				}
			}
		}
	}

	/**
	 * Twitterにコンテンツを投稿します。
	 * @param contents コンテンツ
	 */
	private void sendContentsForTwitter(String contents) {
		Twitter twitter = requestScopeDao.loadTwitterClient();
		if (twitter == null) {
			return;
		}
		twitter.timelineOperations().updateStatus(contents);
	}

	/**
	 * Facebookにコンテンツを投稿します。
	 * @param contents コンテンツ
	 */
	private void sendContentsForFacebook(String contents, FacebookLink link) {
		Facebook facebook = requestScopeDao.loadFacebookClient();
		if (facebook == null) {
			return;
		}
		facebook.feedOperations().postLink(contents, link);
	}

	/**
	 * 指定の文字数で文字列を切り出し
	 * @param src
	 * @param length
	 * @param suffix
	 * @return
	 */
	private String getOmissionContent(String src, String length, String suffix ) {
		if(StringUtils.isEmpty(src)) return "";
		if(src.length() <= Integer.parseInt(length)) return src;
		return src.substring(0, Integer.parseInt(length)) + suffix;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		siteUrl = "http://" + serviceConfig.communityDomain + "/community";
	}
	
}
