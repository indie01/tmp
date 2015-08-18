/**
 *
 */
package com.kickmogu.yodobashi.community.service;

import java.util.Date;

import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;

/**
 * バッチメールサービスです。
 * @author kamiike
 */
public interface BatchMailService {

	/**
	 * 指定したコミュニティユーザーに対してメールを送信します。
	 * @param communityUser コミュニティユーザー
	 * @param targetDate 対象日付
	 */
	public void sendMail(CommunityUserDO communityUser, Date targetDate);

}
