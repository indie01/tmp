package com.kickmogu.yodobashi.community.common.utils;

import org.springframework.context.ApplicationContext;

public class ProfileUtil {

	// 本番profile名
	public static final String PRODUCT = "product";
	// yodobashi検証環境profile名
	public static final String STAGING_NEW = "staging_new";

	private ProfileUtil() {
		
	}

	/**
	 * profile名の取得
	 * @param applicationContext
	 * @return
	 */
	public static String getProfile(ApplicationContext applicationContext) {
		if (applicationContext.getEnvironment().getActiveProfiles() == null
				|| applicationContext.getEnvironment().getActiveProfiles().length == 0)
			return "default";
		return applicationContext.getEnvironment().getActiveProfiles()[0];
	}
	
	/**
	 * コミュニティユーザ新規登録時のHTTP・HTTPSアクセス制御デフォルト値を取得
	 * @param applicationContext
	 * @return
	 */
	public static boolean isCommunityUserProfileSecureAccess(ApplicationContext applicationContext) {
		// ローカル環境でもssl=trueだとウザイので、profileによって変わるようにしている。
		final String profile = getProfile(applicationContext);
		return PRODUCT.equals(profile) || STAGING_NEW.equals(profile);
	}
	
	/**
	 * コミュニティユーザ新規登録時のHTTP・HTTPSアクセス制御デフォルト値を取得
	 * @param applicationContext
	 * @return
	 */
	public static boolean isKmDevelopeMode(ApplicationContext applicationContext) {
		final String profile = getProfile(applicationContext);
		return !PRODUCT.equals(profile) && !STAGING_NEW.equals(profile);
	}
}

