package com.kickmogu.yodobashi.community.resource.config;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.kickmogu.lib.core.cofig.BaseConfig;

@Configuration
public class PathConfig extends BaseConfig implements InitializingBean {
	
	public static PathConfig INSTANCE;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		
		communityImageBaseUrl = null;
		if( StringUtils.isNotEmpty(imageBaseUrl) )
			communityImageBaseUrl = imageBaseUrl;
		
		if( StringUtils.isNotEmpty(imageVersionUrl) ){
			if( StringUtils.isNotEmpty(communityImageBaseUrl) )
				communityImageBaseUrl = communityImageBaseUrl + "/" + imageVersionUrl + "/";
			else
				communityImageBaseUrl = "/" + imageVersionUrl + "/";
		}else{
			if( StringUtils.isNotEmpty(communityImageBaseUrl) )
				communityImageBaseUrl = communityImageBaseUrl + "/";
			else
				communityImageBaseUrl = "/";
		}
		
		if( StringUtils.isEmpty(communityImageBaseUrl) )
			communityImageBaseUrl = "/";
		
		defaultProfileImageUrl = communityImageBaseUrl + defaultProfileImageUrl;
		defaultThumbnailImageUrl = communityImageBaseUrl + defaultThumbnailImageUrl;
		mailCommunityImageUrl = communityImageBaseUrl;
		unAuthorizedCeroThumbnailImageUrl = communityImageBaseUrl + unAuthorizedCeroThumbnailImageUrl;
		unAuthorizedCeroMiniThumbnailImageUrl = communityImageBaseUrl + unAuthorizedCeroMiniThumbnailImageUrl;
		dataNotfoundImageUrl = communityImageBaseUrl + dataNotfoundImageUrl;
		
	}
	/**
	 * 静的ファイルバージョン
	 */
	@Value("${image.version.url}")
	public String imageVersionUrl;
	
	/**
	 * イメージベースURL
	 */
	@Value("${image.base.url}")
	public String imageBaseUrl;
	
	public String communityImageBaseUrl;
	
	/**
	 * デフォルトプロフィール画像のURLです。
	 */
	@Value("${image.defaultProfile.url}")
	public String defaultProfileImageUrl;

	/**
	 * デフォルトプロフィールサムネイル画像のURLです。
	 */
	@Value("${image.defaultThumbnail.url}")
	public String defaultThumbnailImageUrl;
	
	/**
	 * CERO画像のパスです
	 */
	@Value("${unauthorized.cero.image.url}")
	public String unAuthorizedCeroImageUrl;

	/**
	 * CEROmiddle画像のパスです
	 */
	@Value("${unauthorized.cero.middle.image.url}")
	public String unAuthorizedCeroMiddleImageUrl;
	
	/**
	 * CEROサムネイル画像のパスです
	 */
	@Value("${unauthorized.cero.thumbnail.image.url}")
	public String unAuthorizedCeroThumbnailImageUrl;

	/**
	 * CEROミニサムネイル画像のパスです
	 */
	@Value("${unauthorized.cero.minithumbnail.image.url}")
	public String unAuthorizedCeroMiniThumbnailImageUrl;
	
	@Value("${data.notfound.image.url}")
	public String dataNotfoundImageUrl;
	
	/**
	 * コミュニティサイトの画像URLです。
	 */
	public String mailCommunityImageUrl;
	
	@Value("${image.common.url}")
	public String imageCommonUrl;
}
