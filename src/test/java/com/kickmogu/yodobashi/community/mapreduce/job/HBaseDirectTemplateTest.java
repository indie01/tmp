package com.kickmogu.yodobashi.community.mapreduce.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Map;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Row;
import org.apache.hadoop.hbase.client.tableindexed.IndexedTable;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

import com.google.common.collect.Lists;
import com.kickmogu.lib.hadoop.hbase.HBaseContainer;
import com.kickmogu.lib.hadoop.hbase.impl.HBaseDirectTemplate;
import com.kickmogu.lib.hadoop.hbase.meta.HBaseColumnFamilyMeta;
import com.kickmogu.lib.hadoop.hbase.meta.HBaseTableMeta;
import com.kickmogu.yodobashi.community.common.test.YcComJUnit4ClassRunner;
import com.kickmogu.yodobashi.community.resource.domain.CommentDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommentTargetType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ContentsStatus;


class HBaseDirectTemplateTestStub extends HBaseDirectTemplate{
	@Override
	public <T> void batchRows(Class<T> type, String tableName, List<Row> row) {
		System.out.println("[km] HTableInterface#batchRows (" + tableName + ") size[" + row.size() + "] - start");
		dispRows(row);
		System.out.println("[km] HTableInterface#batchRows (" + tableName + ") - end");
	}
	@Override
	protected void doBatch(HTableInterface table, List<Row> puts) {
		System.out.println("[km] HTableInterface#doBatch (" + Bytes.toString(table.getTableName()) + ") size[" + puts.size() + "] - start");
		dispRows(puts);
		System.out.println("[km] HTableInterface#doBatch (" + Bytes.toString(table.getTableName()) + ") - end");
	}
	public void dispRows(List<Row> puts) {
		for (Row row : puts) {
			Put put = (Put) row;
			System.out.println("row " + Bytes.toString(put.getRow()));
			for (Map.Entry<byte[], List<KeyValue>> entry : put.getFamilyMap().entrySet()) {
				List<KeyValue> list = entry.getValue();
				for (KeyValue kv : list) {
					System.out.println("\tcol " + Bytes.toString(kv.getFamily()) + ":" + Bytes.toString(kv.getQualifier()) + "=" + Bytes.toString(kv.getValue()));				
				}
			}
		}
	}
	public Map<String, Put> test_makeIndexRecord(HBaseTableMeta tableMeta, Put record) {
		return this.makeIndexRecord(tableMeta, record);
	}
	public <T> Map<String, Put> test_makeIndexRecord(HBaseTableMeta tableMeta, T o, long time) {
		return this.makeIndexRecord(tableMeta, o, time);
	}
}

@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration("/serviceContext.xml")
public class HBaseDirectTemplateTest {

	@Autowired
	@Qualifier("MySite")
	private HBaseContainer hbaseContainer;
	
	private HBaseDirectTemplate template;

	private String tableNamePrefix;
	
	@BeforeClass
	public static void before() throws InterruptedException {
	}
	
	public HBaseDirectTemplateTest() {
	}
	
	@Before
	public void setup() throws Throwable {
		template = new HBaseDirectTemplateTestStub();
		template.setContainer(hbaseContainer);
		tableNamePrefix = hbaseContainer.getMeta().getTableNamePrefix();
	}
	
	@After
	public void teardown() throws Throwable {
	}

	@Test
	public void test_batchRows() {
		Map<String, Put> data = null;
		List<ReviewDO> list = createTestData();
		try {
			data = ((HBaseDirectTemplateTestStub)template).test_makeIndexRecord(hbaseContainer.getMeta().getTableMeta(ReviewDO.class), list.get(0), System.currentTimeMillis());
		} catch (Throwable th) {
			th.printStackTrace();
			fail(th.getMessage());
		}
		
		Put index1 = data.get(tableNamePrefix + "ReviewDO-communityUserId");
		assertNotNull(index1);
		assertNotSame(0, index1.get(IndexedTable.INDEX_COL_FAMILY, IndexedTable.INDEX_BASE_ROW));
		assertEquals("id-communityUserIdZr-0001", Bytes.toString(index1.getRow()));
		assertEquals("r-0001", Bytes.toString(index1.get(IndexedTable.INDEX_COL_FAMILY, IndexedTable.INDEX_BASE_ROW).get(0).getValue()));
		assertEquals(true, Bytes.toBoolean(index1.get(Bytes.toBytes("cf"), Bytes.toBytes("effective")).get(0).getValue()));
		assertEquals("id-skuZ", Bytes.toString(index1.get(Bytes.toBytes("cf"), Bytes.toBytes("productId")).get(0).getValue()));

		Put index2 = data.get(tableNamePrefix + "ReviewDO-withdrawKey");
		assertNotNull(index2);
		assertNotSame(0, index2.get(IndexedTable.INDEX_COL_FAMILY, IndexedTable.INDEX_BASE_ROW));
		assertEquals("val-withdrawKeyZr-0001", Bytes.toString(index2.getRow()));
		assertEquals("r-0001", Bytes.toString(index2.get(IndexedTable.INDEX_COL_FAMILY, IndexedTable.INDEX_BASE_ROW).get(0).getValue()));

		Put index3 = data.get(tableNamePrefix + "ReviewDO-productId");
		assertNotNull(index3);
		assertNotSame(0, index3.get(IndexedTable.INDEX_COL_FAMILY, IndexedTable.INDEX_BASE_ROW));
		assertEquals("id-skuZr-0001", Bytes.toString(index3.getRow()));
		assertEquals("r-0001", Bytes.toString(index3.get(IndexedTable.INDEX_COL_FAMILY, IndexedTable.INDEX_BASE_ROW).get(0).getValue()));
		assertEquals("2", Bytes.toString(index3.get(Bytes.toBytes("cf"), Bytes.toBytes("status")).get(0).getValue()));
	}
	
	@Test
	public void test_batchRows2() {
		Map<String, Put> data = null;
		List<CommentDO> list = createTestData2();
		try {
			data = ((HBaseDirectTemplateTestStub)template).test_makeIndexRecord(hbaseContainer.getMeta().getTableMeta(CommentDO.class), list.get(0), System.currentTimeMillis());
		} catch (Throwable th) {
			th.printStackTrace();
			fail(th.getMessage());
		}
		
		Put index1 = data.get(tableNamePrefix + "CommentDO-communityUserId");
		assertNotNull(index1);
		assertNotSame(0, index1.get(IndexedTable.INDEX_COL_FAMILY, IndexedTable.INDEX_BASE_ROW));
		assertEquals("id-communityUserId1c-1", Bytes.toString(index1.getRow()));
		assertEquals("c-1", Bytes.toString(index1.get(IndexedTable.INDEX_COL_FAMILY, IndexedTable.INDEX_BASE_ROW).get(0).getValue()));
		assertEquals("1", Bytes.toString(index1.get(Bytes.toBytes("cf"), Bytes.toBytes("targetType")).get(0).getValue()));
		
		Put index2 = data.get(tableNamePrefix + "CommentDO-withdrawKey");
		assertNotNull(index2);
		assertNotSame(0, index2.get(IndexedTable.INDEX_COL_FAMILY, IndexedTable.INDEX_BASE_ROW));
		assertEquals("val-withdrawKeyc-1", Bytes.toString(index2.getRow()));
		assertEquals("c-1", Bytes.toString(index2.get(IndexedTable.INDEX_COL_FAMILY, IndexedTable.INDEX_BASE_ROW).get(0).getValue()));

		Put index3 = data.get(tableNamePrefix + "CommentDO-relationReviewOwnerId");
		assertNotNull(index3);
		assertNotSame(0, index3.get(IndexedTable.INDEX_COL_FAMILY, IndexedTable.INDEX_BASE_ROW));
		assertEquals("id-relationReviewOwnerIdc-1", Bytes.toString(index3.getRow()));
		assertEquals("c-1", Bytes.toString(index3.get(IndexedTable.INDEX_COL_FAMILY, IndexedTable.INDEX_BASE_ROW).get(0).getValue()));
		assertEquals("1", Bytes.toString(index3.get(Bytes.toBytes("cf"), Bytes.toBytes("targetType")).get(0).getValue()));

		Put index4 = data.get(tableNamePrefix + "CommentDO-relationQuestionOwnerId");
		assertNotNull(index4);
		assertNotSame(0, index4.get(IndexedTable.INDEX_COL_FAMILY, IndexedTable.INDEX_BASE_ROW));
		assertEquals("id-relationQuestionOwnerIdc-1", Bytes.toString(index4.getRow()));
		assertEquals("c-1", Bytes.toString(index4.get(IndexedTable.INDEX_COL_FAMILY, IndexedTable.INDEX_BASE_ROW).get(0).getValue()));
		assertEquals("1", Bytes.toString(index4.get(Bytes.toBytes("cf"), Bytes.toBytes("targetType")).get(0).getValue()));

		Put index5 = data.get(tableNamePrefix + "CommentDO-relationImageOwnerId");
		assertNotNull(index5);
		assertNotSame(0, index5.get(IndexedTable.INDEX_COL_FAMILY, IndexedTable.INDEX_BASE_ROW));
		assertEquals("id-relationImageOwnerIdc-1", Bytes.toString(index5.getRow()));
		assertEquals("c-1", Bytes.toString(index5.get(IndexedTable.INDEX_COL_FAMILY, IndexedTable.INDEX_BASE_ROW).get(0).getValue()));
		assertEquals("1", Bytes.toString(index5.get(Bytes.toBytes("cf"), Bytes.toBytes("targetType")).get(0).getValue()));
	}
		
	public List<CommentDO> createTestData2() {
		List<CommentDO> list = Lists.newArrayList();
		CommunityUserDO communityUser1 = new CommunityUserDO();
		communityUser1.setCommunityUserId("id-communityUserId1");
		communityUser1.setCommunityName("val-communityName1");
		
		CommunityUserDO communityUser2 = new CommunityUserDO();
		communityUser2.setCommunityUserId("id-communityUserId1");
		communityUser2.setCommunityName("val-communityName1");
		
		CommentDO c1 = new CommentDO();
		c1.setCommentId("c-1");
		c1.setTargetType(CommentTargetType.REVIEW);
		c1.setCommunityUser(communityUser1);
		c1.setRelationReviewOwnerId("id-relationReviewOwnerId");
		c1.setRelationQuestionOwnerId("id-relationQuestionOwnerId");
		c1.setRelationQuestionAnswerOwnerId("id-relationQuestionAnswerOwnerId");
		c1.setRelationImageOwnerId("id-relationImageOwnerId");
		c1.setWithdrawKey("val-withdrawKey");
		
		CommentDO c2 = new CommentDO();
		c2.setCommentId("c-2");
		c2.setCommunityUser(communityUser1);
		c2.setRelationReviewOwnerId("id-relationReviewOwnerId");
		c2.setTargetType(CommentTargetType.REVIEW);
		c2.setRelationQuestionOwnerId("id-relationQuestionOwnerId");
		c2.setRelationQuestionAnswerOwnerId("id-relationQuestionAnswerOwnerId");
		c2.setRelationImageOwnerId("id-relationImageOwnerId");
		c2.setWithdrawKey("val-withdrawKey");
		
		CommentDO c3 = new CommentDO();
		c3.setCommentId("c-3");
		c3.setCommunityUser(communityUser2);
		c3.setRelationReviewOwnerId("id-relationReviewOwnerId");
		c3.setTargetType(CommentTargetType.REVIEW);
		c3.setRelationQuestionOwnerId("id-relationQuestionOwnerId");
		c3.setRelationQuestionAnswerOwnerId("id-relationQuestionAnswerOwnerId");
		c3.setRelationImageOwnerId("id-relationImageOwnerId");
		c3.setWithdrawKey("val-withdrawKey");

		list.add(c1);
		list.add(c2);
		list.add(c3);
		return list;
	}

	public List<ReviewDO> createTestData() {
		List<ReviewDO> list = Lists.newArrayList();
		// single link
		ReviewDO review = new ReviewDO();
		review.setReviewId("r-0001");
		review.setEffective(true);
		review.setStatus(ContentsStatus.SUBMITTED);
		review.setWithdrawKey("val-withdrawKeyZ");

		CommunityUserDO communityUser = new CommunityUserDO();
		communityUser.setCommunityUserId("id-communityUserIdZ");
		communityUser.setCommunityName("val-communityNameZ");

		ProductDO product = new ProductDO();
		product.setSku("id-skuZ");
		product.setBrndNm("val-brndNmZ");

		review.setCommunityUser(communityUser);
		review.setProduct(product);
		list.add(review);
		//hbaseOperations.save(review);

		// multi link
		CommunityUserDO communityUserA = new CommunityUserDO();
		communityUserA.setCommunityUserId("id-communityUserIdA");
		communityUserA.setCommunityName("val-communityNameA");

		CommunityUserDO communityUserB = new CommunityUserDO();
		communityUserB.setCommunityUserId("id-communityUserIdB");
		communityUserB.setCommunityName("val-communityNameB");

		ProductDO productA = new ProductDO();
		productA.setSku("id-skuA");
		productA.setBrndNm("val-brndNmA");

		ProductDO productB = new ProductDO();
		productB.setSku("id-skuB");
		productB.setBrndNm("val-brndNmB");

		review = new ReviewDO();
		review.setReviewId("r-0011");
		review.setEffective(true);
		review.setStatus(ContentsStatus.SUBMITTED);
		review.setWithdrawKey("val-withdrawKeyA");
		review.setCommunityUser(communityUserA);
		review.setProduct(productA);
		list.add(review);
		//hbaseOperations.save(review);

		review = new ReviewDO();
		review.setReviewId("r-0012");
		review.setEffective(true);
		review.setStatus(ContentsStatus.SUBMITTED);
		review.setWithdrawKey("val-withdrawKeyB");
		review.setCommunityUser(communityUserB);
		review.setProduct(productA);
		list.add(review);
		//hbaseOperations.save(review);

		review = new ReviewDO();
		review.setReviewId("r-0013");
		review.setEffective(true);
		review.setStatus(ContentsStatus.SUBMITTED);
		review.setWithdrawKey("val-withdrawKeyA");
		review.setCommunityUser(communityUserB);
		review.setProduct(productB);
		list.add(review);
		//hbaseOperations.save(review);
		
		return list;
	}
	
	@Test
	public void test_makeIndexRecordDelete() {
		HBaseTableMeta meta = hbaseContainer.getMeta().getTableMeta(ReviewDO.class);
		
		byte[] flg = new byte[1];
		flg[0] = 0x01;

		Put record = new Put(Bytes.toBytes("key001"));
		record.add(Bytes.toBytes("cf"), Bytes.toBytes("effective"), Bytes.toBytes("val-effective"));
		record.add(Bytes.toBytes("cf"), Bytes.toBytes("communityUserId"), Bytes.toBytes("val-communityUserId"));
		record.add(Bytes.toBytes("cf"), Bytes.toBytes("status"), Bytes.toBytes("val-status"));
		record.add(Bytes.toBytes("cf"), Bytes.toBytes("withdrawKey"), Bytes.toBytes("val-withdrawKey"));
		record.add(Bytes.toBytes("cf"), Bytes.toBytes("productId"), Bytes.toBytes("val-productId"));
		record.add(meta.getDeleteFlgColumnFamilyMeta().getColumnFamilyNameAsBytes(), 
				HBaseColumnFamilyMeta.DELETE_FLG_COLUMN_NAME_AS_BYTES, flg);

		Map<String, Put> data = null;
		try {
			data = ((HBaseDirectTemplateTestStub)template).test_makeIndexRecord(meta, record);
		} catch (Throwable th) {
			th.printStackTrace();
			fail(th.getMessage());
		}
		
		Put index1 = data.get(tableNamePrefix + "ReviewDO-communityUserId");
		assertNotNull(index1);
		assertNotSame(0, index1.get(IndexedTable.INDEX_COL_FAMILY, IndexedTable.INDEX_BASE_ROW));

		List<KeyValue> deleteFlgValues = index1.get(meta.getDeleteFlgColumnFamilyMeta().getColumnFamilyNameAsBytes(), HBaseColumnFamilyMeta.DELETE_FLG_COLUMN_NAME_AS_BYTES);
		KeyValue deleteFlgValue = deleteFlgValues.get(0);
		assertTrue(deleteFlgValue != null && deleteFlgValue.getBuffer()[deleteFlgValue.getValueOffset()] != 0);
	}
	
	@Test
	public void test_makeIndexRecordNullLink() {
		HBaseTableMeta meta = hbaseContainer.getMeta().getTableMeta(ReviewDO.class);

		Put record = new Put(Bytes.toBytes("key001"));
		record.add(Bytes.toBytes("cf"), Bytes.toBytes("effective"), Bytes.toBytes("val-effective"));
		record.add(Bytes.toBytes("cf"), Bytes.toBytes("communityUserId"), null);
		record.add(Bytes.toBytes("cf"), Bytes.toBytes("status"), Bytes.toBytes("val-status"));
		record.add(Bytes.toBytes("cf"), Bytes.toBytes("withdrawKey"), Bytes.toBytes("val-withdrawKey"));
		record.add(Bytes.toBytes("cf"), Bytes.toBytes("productId"), Bytes.toBytes("val-productId"));

		Map<String, Put> data = null;
		try {
			data = ((HBaseDirectTemplateTestStub)template).test_makeIndexRecord(meta, record);
		} catch (Throwable th) {
			th.printStackTrace();
			fail(th.getMessage());
		}
		assertFalse(data.containsKey(tableNamePrefix + "ReviewDO-communityUserId"));

		Put index2 = data.get(tableNamePrefix + "ReviewDO-withdrawKey");
		assertNotNull(index2);
		assertNotSame(0, index2.get(IndexedTable.INDEX_COL_FAMILY, IndexedTable.INDEX_BASE_ROW));
		assertEquals("val-withdrawKeykey001", Bytes.toString(index2.getRow()));
		assertEquals("key001", Bytes.toString(index2.get(IndexedTable.INDEX_COL_FAMILY, IndexedTable.INDEX_BASE_ROW).get(0).getValue()));

		Put index3 = data.get(tableNamePrefix + "ReviewDO-productId");
		assertNotNull(index3);
		assertNotSame(0, index3.get(IndexedTable.INDEX_COL_FAMILY, IndexedTable.INDEX_BASE_ROW));
		assertEquals("val-productIdkey001", Bytes.toString(index3.getRow()));
		assertEquals("key001", Bytes.toString(index3.get(IndexedTable.INDEX_COL_FAMILY, IndexedTable.INDEX_BASE_ROW).get(0).getValue()));
		assertEquals("val-status", Bytes.toString(index3.get(Bytes.toBytes("cf"), Bytes.toBytes("status")).get(0).getValue()));
	}

	@Test
	public void test_makeIndexRecord() {
		HBaseTableMeta meta = hbaseContainer.getMeta().getTableMeta(ReviewDO.class);
		Put record = new Put(Bytes.toBytes("key001"));
		record.add(Bytes.toBytes("cf"), Bytes.toBytes("effective"), Bytes.toBytes("val-effective"));
		record.add(Bytes.toBytes("cf"), Bytes.toBytes("communityUserId"), Bytes.toBytes("val-communityUserId"));
		record.add(Bytes.toBytes("cf"), Bytes.toBytes("status"), Bytes.toBytes("val-status"));
		record.add(Bytes.toBytes("cf"), Bytes.toBytes("withdrawKey"), Bytes.toBytes("val-withdrawKey"));
		record.add(Bytes.toBytes("cf"), Bytes.toBytes("productId"), Bytes.toBytes("val-productId"));

		Map<String, Put> data = null;
		try {
			data = ((HBaseDirectTemplateTestStub)template).test_makeIndexRecord(meta, record);
		} catch (Throwable th) {
			th.printStackTrace();
			fail(th.getMessage());
		}

		Put index1 = data.get(tableNamePrefix + "ReviewDO-communityUserId");
		assertNotNull(index1);
		assertNotSame(0, index1.get(IndexedTable.INDEX_COL_FAMILY, IndexedTable.INDEX_BASE_ROW));
		assertEquals("val-communityUserIdkey001", Bytes.toString(index1.getRow()));
		assertEquals("key001", Bytes.toString(index1.get(IndexedTable.INDEX_COL_FAMILY, IndexedTable.INDEX_BASE_ROW).get(0).getValue()));
		assertEquals("val-effective", Bytes.toString(index1.get(Bytes.toBytes("cf"), Bytes.toBytes("effective")).get(0).getValue()));
		assertEquals("val-productId", Bytes.toString(index1.get(Bytes.toBytes("cf"), Bytes.toBytes("productId")).get(0).getValue()));

		Put index2 = data.get(tableNamePrefix + "ReviewDO-withdrawKey");
		assertNotNull(index2);
		assertNotSame(0, index2.get(IndexedTable.INDEX_COL_FAMILY, IndexedTable.INDEX_BASE_ROW));
		assertEquals("val-withdrawKeykey001", Bytes.toString(index2.getRow()));
		assertEquals("key001", Bytes.toString(index2.get(IndexedTable.INDEX_COL_FAMILY, IndexedTable.INDEX_BASE_ROW).get(0).getValue()));

		Put index3 = data.get(tableNamePrefix + "ReviewDO-productId");
		assertNotNull(index3);
		assertNotSame(0, index3.get(IndexedTable.INDEX_COL_FAMILY, IndexedTable.INDEX_BASE_ROW));
		assertEquals("val-productIdkey001", Bytes.toString(index3.getRow()));
		assertEquals("key001", Bytes.toString(index3.get(IndexedTable.INDEX_COL_FAMILY, IndexedTable.INDEX_BASE_ROW).get(0).getValue()));
		assertEquals("val-status", Bytes.toString(index3.get(Bytes.toBytes("cf"), Bytes.toBytes("status")).get(0).getValue()));

	}
}
