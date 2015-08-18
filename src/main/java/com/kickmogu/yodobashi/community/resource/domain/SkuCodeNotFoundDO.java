package com.kickmogu.yodobashi.community.resource.domain;

import com.kickmogu.lib.core.id.annotation.IDParts;
import com.kickmogu.lib.core.resource.annotation.Label;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumn;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumnFamily;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseKey;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable.SizeGroup;
import com.kickmogu.lib.solr.annotation.SolrField;
import com.kickmogu.lib.solr.annotation.SolrSchema;
import com.kickmogu.lib.solr.annotation.SolrUniqKey;
import com.kickmogu.yodobashi.community.resource.domain.constants.SlipType;

/**
 * SKU未対応エラー情報です。
 * @author kamiike
 *
 */
@HBaseTable(columnFamilies={
		@HBaseColumnFamily(name="cf")
	}
,sizeGroup=SizeGroup.TINY)
@SolrSchema
public class SkuCodeNotFoundDO extends BaseWithTimestampDO{

	/**
	 *
	 */
	private static final long serialVersionUID = -8460956977751280640L;

	/**
	 * SKU未対応エラーID です。
	 */
	@HBaseKey (idGenerator="idPartsGenerator",createTableSplitKeys={"#", "5", "A", "G", "M", "S", "Y", "e", "k", "q", "w"})
	@SolrField @SolrUniqKey
	private String skuCodeNotFoundId;

	/**
	 * 伝票区分です。
	 */
	@HBaseColumn @IDParts(order=1)
	@SolrField
	@Label("伝票区分")
	private SlipType type;

	/**
	 * 受注伝票番号/POSレシート番号 です。
	 */
	@HBaseColumn @IDParts(order=2)
	@SolrField
	@Label("受注伝票番号/POSレシート番号")
	protected String dataId;

	/**
	 * 明細番号です。
	 */
	@HBaseColumn @IDParts(order=3)
	@SolrField
	@Label("明細番号")
	protected int detailNo;

	/**
	 * 外部顧客IDです。
	 */
	@HBaseColumn
	@SolrField
	@Label("外部顧客ID")
	protected String outerCustomerId;

	/**
	 * JANコード
	 */
	@HBaseColumn
	@SolrField
	@Label("JANコード")
	protected String janCode;

	/**
	 * @return skuCodeNotFoundId
	 */
	public String getSkuCodeNotFoundId() {
		return skuCodeNotFoundId;
	}

	/**
	 * @param skuCodeNotFoundId セットする skuCodeNotFoundId
	 */
	public void setSkuCodeNotFoundId(String skuCodeNotFoundId) {
		this.skuCodeNotFoundId = skuCodeNotFoundId;
	}

	/**
	 * @return type
	 */
	public SlipType getType() {
		return type;
	}

	/**
	 * @param type セットする type
	 */
	public void setType(SlipType type) {
		this.type = type;
	}

	/**
	 * @return dataId
	 */
	public String getDataId() {
		return dataId;
	}

	/**
	 * @param dataId セットする dataId
	 */
	public void setDataId(String dataId) {
		this.dataId = dataId;
	}

	/**
	 * @return detailNo
	 */
	public int getDetailNo() {
		return detailNo;
	}

	/**
	 * @param detailNo セットする detailNo
	 */
	public void setDetailNo(int detailNo) {
		this.detailNo = detailNo;
	}

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
	 * @return janCode
	 */
	public String getJanCode() {
		return janCode;
	}

	/**
	 * @param janCode セットする janCode
	 */
	public void setJanCode(String janCode) {
		this.janCode = janCode;
	}
}
