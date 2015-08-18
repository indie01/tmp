package com.kickmogu.yodobashi.community.service.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.kickmogu.lib.core.cofig.BaseConfig;
import com.kickmogu.lib.solr.SolrConstants;
import com.kickmogu.yodobashi.community.resource.config.PathConfig;

@Configuration
public class ServiceConfig extends BaseConfig implements InitializingBean{
	
	@Autowired
	private PathConfig pathConfig;
	
	public static ServiceConfig INSTANCE;
	
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		this.mailCommunityImageUrl = pathConfig.mailCommunityImageUrl;
	}
	
	@Value("${enable.dummyDao}")
	public boolean enableDummyDao;

	/**
	 * メール一覧制限数です。
	 */
	@Value("${mail.list.limit}")
	public int mailListLimit = 10;

	/**
	 * 読み込み制限数です。
	 */
	public int readLimit = SolrConstants.QUERY_ROW_LIMIT;

	/**
	 * 購入日（請求日）からレビューのポイント付与実行開始日までの期間（日数）です。
	 */
	@Value("${pointGrantExecStartInterval}")
	public Integer pointGrantExecStartInterval;

	/**
	 * コミュニティサイトのドメインです。
	 */
	@Value("${community.domain}")
	public String communityDomain;
	/**
	 * コミュニティサイトのコンテキストパスです。
	 */
	@Value("${community.context.path}")
	public String communityContextPath;

	/**
	 * コミュニティサイトのコピーライトです。
	 */
	@Value("${community.copyright}")
	public String communityCopyright;
	
	/**
	 * コミュニティサイトのガイドラインURLです。
	 */
	@Value("${community.guideline}")
	public String communityGuideline;

	@Value("${follow.product.limit}")
	public long followProductLimit;
	@Value("${follow.question.limit}")
	public long followQuestionLimit;
	@Value("${follow.user.limit}")
	public long followCommunityUserLimit;

	@Value("${sync.communityUser.to.backend}")
	public boolean syncCommunityUserToBackend;

	@Value("${like.review.label}")
	public String likeReviewLabel;
	@Value("${like.answer.label}")
	public String likeAnswerLabel;
	@Value("${like.image.label}")
	public String likeImageLabel;

	@Value("${mail.review.limit.five}")
	public int mailReviewLimitFive;
	@Value("${mail.review.limit.ten}")
	public int mailReviewLimitTen;

	@Value("${community.productPage.path}")
	public String communityProductPage;

	/** 画像投稿 最大公開数 */
	@Value("${product.image.submit.fileupload.max.length}")
	public int productImageSubmitFileuploadMaxLength;

	/** コミュニティWS　認証サービス許容システムコード **/
	@Value("${auth.permission.external.system.Id}")
	public String authPermissionExternalSystemId;
	
	/** ソーシャルメディア通知の処理遅延時間 **/
	@Value("${notify.socialmedia.delaytime}")
	public long notifySocialmediaDelaytime;
	
	@Value("${like.threshold.limit}")
	public int likeThresholdLimit;
	
	/**
	 * コミュニティサイトの画像URLです。
	 */
	public String mailCommunityImageUrl;
}
