/**
 *
 */
package com.kickmogu.yodobashi.community.resource.domain;

import com.kickmogu.yodobashi.community.resource.domain.constants.MailType;

/**
 * メール情報です。
 * @author kamiike
 *
 */
public class MailInfoDO extends BaseWithTimestampDO {

	/**
	 *
	 */
	private static final long serialVersionUID = 2919585754130072321L;

	/**
	 * メールタイプです。
	 */
	private MailType mailType;

	/**
	 * 件名です。
	 */
	private String title;

	/**
	 * テキスト本文です。
	 */
	private String textBody;

	/**
	 * HTML本文です。
	 */
	private String htmlBody;

	/**
	 * コミュニティユーザー情報です。
	 */
	private CommunityUserDO communityUser;

	/**
	 * @return title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title セットする title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return communityUser
	 */
	public CommunityUserDO getCommunityUser() {
		return communityUser;
	}

	/**
	 * @param communityUser セットする communityUser
	 */
	public void setCommunityUser(CommunityUserDO communityUser) {
		this.communityUser = communityUser;
	}

	/**
	 * @return mailType
	 */
	public MailType getMailType() {
		return mailType;
	}

	/**
	 * @param mailType セットする mailType
	 */
	public void setMailType(MailType mailType) {
		this.mailType = mailType;
	}

	/**
	 * @return textBody
	 */
	public String getTextBody() {
		return textBody;
	}

	/**
	 * @param textBody セットする textBody
	 */
	public void setTextBody(String textBody) {
		this.textBody = textBody;
	}

	/**
	 * @return htmlBody
	 */
	public String getHtmlBody() {
		return htmlBody;
	}

	/**
	 * @param htmlBody セットする htmlBody
	 */
	public void setHtmlBody(String htmlBody) {
		this.htmlBody = htmlBody;
	}

}
