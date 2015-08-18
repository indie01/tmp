package com.kickmogu.yodobashi.community.resource.domain;

import java.util.Date;

import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumn;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumnFamily;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseIndex;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseKey;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable.SizeGroup;

@HBaseTable(columnFamilies={
		@HBaseColumnFamily(name="cf")
	},sizeGroup=SizeGroup.LARGE)
public class ImageDO extends BaseWithTimestampDO implements LogicalDeletable {


	/**
	 *
	 */
	private static final long serialVersionUID = -716913496309971195L;

	/**
	 *  画像ID
	 */
	@HBaseKey(createTableSplitKeys={"1","5","9","D","H","L","P","T","X","b","f","j","n","r","v"})
	private String imageId;

	/**
	 *  画像URL
	 */
	@HBaseColumn
	private String imageUrl;

	/**
	 *  データ
	 */
	@HBaseColumn
	private byte[] data;

	/**
	 *  マイムタイプ
	 */
	@HBaseColumn
	private String mimeType;

	/**
	 * 横幅です。
	 */
	@HBaseColumn
	private int width;

	/**
	 * 縦幅です。
	 */
	@HBaseColumn
	private int heigth;

	/**
	 *  コミュニティユーザーID
	 */
	@HBaseColumn
	private String communityUserId;

	/**
	 *  テンポラリキー（コミュニティユーザーIDが指定できない場合に使用）
	 */
	@HBaseColumn
	private String temporaryKey;

	/**
	 *  削除日
	 */
	@HBaseColumn
	private Date deleteDate;

	/**
	 *  削除フラグ
	 */
	@HBaseColumn
	private boolean deleteFlag;

	/**
	 * 一時保存フラグです。
	 */
	@HBaseColumn
	private boolean temporaryFlag;

	/**
	 * 退会データかどうかです。
	 */
	@HBaseColumn
	private boolean withdraw;

	/**
	 * 退会キーです。
	 */
	@HBaseColumn
	@HBaseIndex
	private String withdrawKey;

	/**
	 * @return withdraw
	 */
	public boolean isWithdraw() {
		return withdraw;
	}

	/**
	 * @param withdraw セットする withdraw
	 */
	public void setWithdraw(boolean withdraw) {
		this.withdraw = withdraw;
	}

	/**
	 * 削除済かどうか返します。
	 * @return 削除済の場合、true
	 */
	public boolean isDeleted() {
		return withdraw || deleteFlag;
	}

	/**
	 * @return the imageId
	 */
	public String getImageId() {
		return imageId;
	}
	/**
	 * @param imageId the imageId to set
	 */
	public void setImageId(String imageId) {
		this.imageId = imageId;
	}
	/**
	 * @return the imageUrl
	 */
	public String getImageUrl() {
		return imageUrl;
	}
	/**
	 * @param imageUrl the imageUrl to set
	 */
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	/**
	 * @return the data
	 */
	public byte[] getData() {
		return data;
	}
	/**
	 * @param data the data to set
	 */
	public void setData(byte[] data) {
		this.data = data;
	}
	/**
	 * @return the deleteDate
	 */
	public Date getDeleteDate() {
		return deleteDate;
	}
	/**
	 * @param deleteDate the deleteDate to set
	 */
	public void setDeleteDate(Date deleteDate) {
		this.deleteDate = deleteDate;
	}
	/**
	 * @return the deleteFlag
	 */
	public boolean isDeleteFlag() {
		return deleteFlag;
	}
	/**
	 * @param deleteFlag the deleteFlag to set
	 */
	public void setDeleteFlag(boolean deleteFlag) {
		this.deleteFlag = deleteFlag;
	}
	/**
	 * @return the mimeType
	 */
	public String getMimeType() {
		return mimeType;
	}
	/**
	 * @param mimeType the mimeType to set
	 */
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	/**
	 * @return temporaryFlag
	 */
	public boolean isTemporaryFlag() {
		return temporaryFlag;
	}
	/**
	 * @param temporaryFlag セットする temporaryFlag
	 */
	public void setTemporaryFlag(boolean temporaryFlag) {
		this.temporaryFlag = temporaryFlag;
	}
	/**
	 * @return the temporaryKey
	 */
	public String getTemporaryKey() {
		return temporaryKey;
	}
	/**
	 * @param temporaryKey the temporaryKey to set
	 */
	public void setTemporaryKey(String temporaryKey) {
		this.temporaryKey = temporaryKey;
	}
	/**
	 * @return communityUserId
	 */
	public String getCommunityUserId() {
		return communityUserId;
	}
	/**
	 * @param communityUserId セットする communityUserId
	 */
	public void setCommunityUserId(String communityUserId) {
		this.communityUserId = communityUserId;
	}

	public String getWithdrawKey() {
		return withdrawKey;
	}

	public void setWithdrawKey(String withdrawKey) {
		this.withdrawKey = withdrawKey;
	}

	/**
	 * @return width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @param width セットする width
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @return heigth
	 */
	public int getHeigth() {
		return heigth;
	}

	/**
	 * @param heigth セットする heigth
	 */
	public void setHeigth(int heigth) {
		this.heigth = heigth;
	}

}
