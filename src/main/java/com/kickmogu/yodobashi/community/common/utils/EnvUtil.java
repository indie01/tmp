package com.kickmogu.yodobashi.community.common.utils;


import com.kickmogu.yodobashi.community.resource.config.ResourceConfig;

/**
 * ユーティリティクラスです。
 * @author kamiike
 */
public class EnvUtil {

	/**
	 * 環境ごとのクッキーSuffix名前を返します。
	 * @return 環境ごとの名前
	 */
	public static String getEnvName(String name) {
		String env = ResourceConfig.INSTANCE.siteDeploymentType;
		if ("dev".equals(env)){
			return name + "2";
		} else if ("st".equals(env)) {
			return name + "1";
		} else {
			return name;
		}
	}
}
