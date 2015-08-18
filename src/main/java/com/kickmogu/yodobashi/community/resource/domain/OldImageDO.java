package com.kickmogu.yodobashi.community.resource.domain;

import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumn;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumnFamily;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseKey;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable.SizeGroup;
import com.kickmogu.yodobashi.community.resource.domain.constants.MigrationImageLayout;

@HBaseTable(columnFamilies={
		@HBaseColumnFamily(name="cf")
	},sizeGroup=SizeGroup.TINY,excludeBackup=true)
public class OldImageDO extends BaseWithTimestampDO{


	/**
	 *
	 */
	private static final long serialVersionUID = 3635968628214235722L;

	/**
	 * 移行前の画像ファイル名です。
	 */
	@HBaseKey
	private String oldFileName;

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
	 * 旧レビューIDです。
	 */
	@HBaseColumn
	private String oldReviewId;

	/**
	 * レイアウトです。
	 */
	@HBaseColumn
	private MigrationImageLayout layout;

	/**
	 * @return oldFileName
	 */
	public String getOldFileName() {
		return oldFileName;
	}

	/**
	 * @param oldFileName セットする oldFileName
	 */
	public void setOldFileName(String oldFileName) {
		this.oldFileName = oldFileName;
	}

	/**
	 * @return data
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * @param data セットする data
	 */
	public void setData(byte[] data) {
		this.data = data;
	}

	/**
	 * @return mimeType
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * @param mimeType セットする mimeType
	 */
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	/**
	 * @return oldReviewId
	 */
	public String getOldReviewId() {
		return oldReviewId;
	}

	/**
	 * @param oldReviewId セットする oldReviewId
	 */
	public void setOldReviewId(String oldReviewId) {
		this.oldReviewId = oldReviewId;
	}

	/**
	 * @return layout
	 */
	public MigrationImageLayout getLayout() {
		return layout;
	}

	/**
	 * @param layout セットする layout
	 */
	public void setLayout(MigrationImageLayout layout) {
		this.layout = layout;
	}
}
