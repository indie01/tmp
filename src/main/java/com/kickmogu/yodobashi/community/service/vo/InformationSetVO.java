/**
 *
 */
package com.kickmogu.yodobashi.community.service.vo;

import java.util.Date;
import java.util.List;

import com.google.common.collect.Lists;
import com.kickmogu.yodobashi.community.resource.domain.InformationDO;

/**
 * お知らせ情報の表示情報を集めたビューオブジェクトです。
 * @author kamiike
 *
 */
public class InformationSetVO {

	/**
	 * 対象日付です。
	 */
	private Date targetDate;

	/**
	 * お知らせ情報リストです。
	 */
	private List<InformationDO> informations = Lists.newArrayList();

	/**
	 * @return targetDate
	 */
	public Date getTargetDate() {
		return targetDate;
	}

	/**
	 * @param targetDate セットする targetDate
	 */
	public void setTargetDate(Date targetDate) {
		this.targetDate = targetDate;
	}

	/**
	 * @return informations
	 */
	public List<InformationDO> getInformations() {
		return informations;
	}

	/**
	 * @param informations セットする informations
	 */
	public void setInformations(List<InformationDO> informations) {
		this.informations = informations;
	}
}
