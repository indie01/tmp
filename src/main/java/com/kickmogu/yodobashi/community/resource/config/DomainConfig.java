package com.kickmogu.yodobashi.community.resource.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.kickmogu.lib.core.cofig.BaseConfig;
import com.kickmogu.lib.core.utils.StringUtil;


@Configuration
public class DomainConfig extends BaseConfig {

	public static DomainConfig INSTANCE;

	/**
	 * ユーザのコミュニティIDをハッシュ化する際に使用する salt キーです。
	 */
	@Value("${createHashSaltKey}")
	public String createHashSaltKey;

	/**
	 * ハッシュコミュニティIDを生成します。
	 * @param communityId コミュニティID
	 * @return ハッシュコミュニティID
	 */
	public String createHashCommunityId(String communityId) {
		return StringUtil.toSHA256(createHashSaltKey + communityId);
	}

}
