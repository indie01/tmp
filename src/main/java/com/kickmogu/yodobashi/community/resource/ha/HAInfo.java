package com.kickmogu.yodobashi.community.resource.ha;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.hadoop.io.WritableComparable;

import com.kickmogu.hadoop.io.utils.WritableUtil;
import com.kickmogu.lib.core.resource.Site;
import com.kickmogu.lib.core.utils.Asserts;

public class HAInfo implements WritableComparable<HAInfo> {

	private HAStatus status;
	
	private Site oneLungSite;
	
	private Date modifyTime;
	
	public HAInfo(){}
	
	public static HAInfo createNormalHAInfo() {
		return new HAInfo(HAStatus.NORMAL, null, new Date());
	}
	
	public static HAInfo fromBytes(byte[] bytes) {
		return WritableUtil.toObject(HAInfo.class, bytes);
	}
	
	public HAInfo(HAStatus status, Site oneLungSite, Date modifyTime) {
		Asserts.notNull(status);
		if (!status.equals(HAStatus.NORMAL)) {
			Asserts.notNull(oneLungSite);
		}
		this.status = status;
		this.oneLungSite = oneLungSite;
		this.modifyTime = modifyTime;
	}
	
	public boolean isNormal() {
		return HAStatus.NORMAL.equals(status);
	}
	
	public boolean isOneLungOnRef() {
		return HAStatus.ONE_LUNG_ON_REF.equals(status);
	}
	
	public HAStatus getStatus() {
		return status;
	}

	public Site getOneLungSite() {
		return oneLungSite;
	}

	public Date getModifyTime() {
		return modifyTime;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeUTF(status.getCode());
		out.writeUTF(oneLungSite == null ? "" : oneLungSite.name());
		out.writeLong(modifyTime == null ? -1L : modifyTime.getTime());
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		status = HAStatus.codeOf(in.readUTF());
		String oneLungSiteString = in.readUTF();
		oneLungSite = oneLungSiteString.equals("") ? null : Site.valueOf(oneLungSiteString);
		long modifyTimeLong = in.readLong();
		modifyTime = modifyTimeLong == -1L ? null : new Date(modifyTimeLong);
	}

	@Override
	public int compareTo(HAInfo other) {
		if (other == null) return -1;
		if (!status.equals(other.status)) return -1;
		if (ObjectUtils.equals(oneLungSite, other.oneLungSite)) return -1;
		return 0;
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.status).append(this.oneLungSite).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (!(obj instanceof HAInfo)) return false;
		HAInfo other = (HAInfo)obj;
		return new EqualsBuilder().append(this.status, other.status).append(this.oneLungSite, other.oneLungSite).isEquals();
	}

	public byte[] toBytes() {
		return WritableUtil.toByte(this);
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
