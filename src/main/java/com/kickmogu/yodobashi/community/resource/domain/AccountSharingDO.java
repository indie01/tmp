/**
 *
 */
package com.kickmogu.yodobashi.community.resource.domain;

import org.msgpack.annotation.Message;

import com.kickmogu.lib.core.resource.domain.BaseDO;


/**
 * @author kamiike
 *
 */
@Message
public class AccountSharingDO extends BaseDO {

	/**
	 *
	 */
	private static final long serialVersionUID = -7924678608831397559L;

	/**
	 * 外部IDです。
	 */
	public String outerCustomerId;

	/**
	 * 口座タイプです。
	 */
	public boolean ec;

	/**
	 * 有効です。
	 */
	public boolean active;

	/**
	 * @return outerCustomerId
	 */
	public String getOuterCustomerId() {
		return outerCustomerId;
	}

	/**
	 * @param outerCustomerId セットする outerCustomerId
	 */
	public void setOuterCustomerId(String outerCustomerId) {
		this.outerCustomerId = outerCustomerId;
	}

	/**
	 * @return active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * @param active セットする active
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * @return ec
	 */
	public boolean isEc() {
		return ec;
	}

	/**
	 * @param ec セットする ec
	 */
	public void setEc(boolean ec) {
		this.ec = ec;
	}

}
