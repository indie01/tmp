/**
 *
 */
package com.kickmogu.yodobashi.community.resource.domain;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Date;

import org.apache.hadoop.io.Writable;

import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumn;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumnFamily;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseKey;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable.SizeGroup;
import com.kickmogu.lib.solr.annotation.SolrField;
import com.kickmogu.lib.solr.annotation.SolrSchema;
import com.kickmogu.lib.solr.annotation.SolrUniqKey;
import com.kickmogu.yodobashi.community.resource.domain.constants.UniqueUserViewCountType;

/**
 * ユニークユーザー閲覧数です。
 * @author kamiike
 *
 */
@HBaseTable(columnFamilies={
		@HBaseColumnFamily(name="cf")
	},sizeGroup=SizeGroup.HUGE,
	excludeBackup=true)
@SolrSchema
public class UniqueUserViewCountDO extends BaseWithTimestampDO implements Writable {

	/**
	 *
	 */
	private static final long serialVersionUID = 3953100936936949139L;

	/**
	 * ユニークユーザー閲覧数IDです。
	 */
	@HBaseKey(createTableSplitKeys={"1","5","9","D","H","L","P","T","X","b","f","j","n","r","v"})
	@SolrField @SolrUniqKey
	private String uniqueUserViewCountId;

	/**
	 * タイプです。
	 */
	@HBaseColumn
	@SolrField
	private UniqueUserViewCountType type;

	/**
	 * 対象日時です。
	 */
	@HBaseColumn
	@SolrField
	private Date targetTime;

	/**
	 * コンテンツIDです。
	 */
	@HBaseColumn
	@SolrField
	private String contentsId;

	/**
	 * SKU です。
	 */
	@HBaseColumn
	@SolrField
	private String sku;

	/**
	 * コミュニティユーザーIDです。
	 */
	@HBaseColumn
	@SolrField
	private String communityUserId;

	/**
	 * 閲覧数です。
	 */
	@HBaseColumn
	@SolrField
	private long viewCount;

	/**
	 * データを書き込みます。
	 * @param out データ
	 * @throws IOException 入出力例外が発生した場合
	 */
	@Override
	public void write(DataOutput out) throws IOException {
		out.writeUTF(uniqueUserViewCountId);
		out.writeUTF(type.getCode());
		if (targetTime == null) {
			out.writeLong(0);
		} else {
			out.writeLong(targetTime.getTime());
		}
		out.writeUTF(contentsId);
		out.writeUTF(sku);
		out.writeUTF(communityUserId);
		out.writeLong(viewCount);
	}

	/**
	 * データを読み込みます。
	 * @param in データ
	 * @throws IOException 入出力例外が発生した場合
	 */
	@Override
	public void readFields(DataInput in) throws IOException {
		uniqueUserViewCountId = in.readUTF();
		type = UniqueUserViewCountType.codeOf(in.readUTF());
		long time = in.readLong();
		if (time == 0) {
			targetTime = null;
		} else {
			targetTime = new Date(time);
		}
		contentsId = in.readUTF();
		sku = in.readUTF();
		communityUserId = in.readUTF();
		viewCount = in.readLong();
	}

	/**
	 * @return uniqueUserViewCountId
	 */
	public String getUniqueUserViewCountId() {
		return uniqueUserViewCountId;
	}

	/**
	 * @param uniqueUserViewCountId セットする uniqueUserViewCountId
	 */
	public void setUniqueUserViewCountId(String uniqueUserViewCountId) {
		this.uniqueUserViewCountId = uniqueUserViewCountId;
	}

	/**
	 * @return type
	 */
	public UniqueUserViewCountType getType() {
		return type;
	}

	/**
	 * @param type セットする type
	 */
	public void setType(UniqueUserViewCountType type) {
		this.type = type;
	}

	/**
	 * @return targetTime
	 */
	public Date getTargetTime() {
		return targetTime;
	}

	/**
	 * @param targetTime セットする targetTime
	 */
	public void setTargetTime(Date targetTime) {
		this.targetTime = targetTime;
	}

	/**
	 * @return sku
	 */
	public String getSku() {
		return sku;
	}

	/**
	 * @param sku セットする sku
	 */
	public void setSku(String sku) {
		this.sku = sku;
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

	/**
	 * @return viewCount
	 */
	public long getViewCount() {
		return viewCount;
	}

	/**
	 * @param viewCount セットする viewCount
	 */
	public void setViewCount(long viewCount) {
		this.viewCount = viewCount;
	}

	/**
	 * @return contentsId
	 */
	public String getContentsId() {
		return contentsId;
	}

	/**
	 * @param contentsId セットする contentsId
	 */
	public void setContentsId(String contentsId) {
		this.contentsId = contentsId;
	}

}
