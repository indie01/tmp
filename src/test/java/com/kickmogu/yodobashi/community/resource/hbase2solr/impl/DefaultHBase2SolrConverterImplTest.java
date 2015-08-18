package com.kickmogu.yodobashi.community.resource.hbase2solr.impl;

import static com.kickmogu.lib.core.BeanTestHelper.*;

import org.apache.solr.client.solrj.SolrQuery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.kickmogu.lib.core.BeanTestHelper;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.yodobashi.community.resource.domain.SlipDetailDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.SalesRegistDetailType;
import com.kickmogu.yodobashi.community.resource.domain.constants.SlipDetailCategory;
import com.kickmogu.yodobashi.community.resource.hbase2solr.HBase2SolrLoader;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/resourceContext.xml")
public class DefaultHBase2SolrConverterImplTest {
	
	@Autowired
	private HBase2SolrLoader loader;
	
	@Autowired  @Qualifier("MySite")
	protected HBaseOperations hBaseOperations;
	
	@Autowired  @Qualifier("MySite")
	protected SolrOperations solrOperations;
	
	@Before
	public void setup() {
		hBaseOperations.deleteAll(SlipDetailDO.class);
		solrOperations.deleteAll(SlipDetailDO.class);
	}

	@Test
	public void test01() {
		hBaseOperations.save(SlipDetailDO.class, BeanTestHelper.createList(SlipDetailDO.class,
			"id,outerCustomerId,slipNo,slipDetailNo,slipDetailCategory,janCode,effectiveNum,returnedNum,setCouponId,setParentDetailNo,salesRegistDetailType",
			"11,0000000001,1000000001,1,01,100000000011,11,21,2000000011,41,1",
			"12,0000000001,1000000001,2,02,100000000012,12,22,2000000012,42,2"
		));
		
		loader.loadByKey(SlipDetailDO.class, "11");
		
		assertListContents(solrOperations.findByQuery(new SolrQuery("*:*"), SlipDetailDO.class).getDocuments(), SlipDetailDO.class,
			"id,outerCustomerId,slipNo,slipDetailNo,slipDetailCategory,janCode,effectiveNum,returnedNum,setCouponId,setParentDetailNo,salesRegistDetailType",
			"11","0000000001","1000000001",1,SlipDetailCategory.NORMAL,"100000000011",11,21,"2000000011",41,SalesRegistDetailType.EFFECTIVE
		);
	}

	@Test
	public void test02() {
		hBaseOperations.save(SlipDetailDO.class, BeanTestHelper.createList(SlipDetailDO.class,
			"id,outerCustomerId,slipNo,slipDetailNo,slipDetailCategory,janCode,effectiveNum,returnedNum,setCouponId,setParentDetailNo,salesRegistDetailType",
			"11,0000000001,1000000001,1,01,100000000011,11,21,2000000011,41,1",
			"12,0000000001,1000000001,2,02,100000000012,12,22,2000000012,42,2"
		));
		
		loader.loadByKeyRange(SlipDetailDO.class, "11", "13");
		assertListContents(solrOperations.findByQuery(new SolrQuery("*:*"), SlipDetailDO.class).getDocuments(), SlipDetailDO.class,
			"id,outerCustomerId,slipNo,slipDetailNo,slipDetailCategory,janCode,effectiveNum,returnedNum,setCouponId,setParentDetailNo,salesRegistDetailType",
			"11","0000000001","1000000001",1,SlipDetailCategory.NORMAL,"100000000011",11,21,"2000000011",41,SalesRegistDetailType.EFFECTIVE,
			"12","0000000001","1000000001",2,SlipDetailCategory.MAKER_DIRECT,"100000000012",12,22,"2000000012",42,SalesRegistDetailType.INEFFECTIVE
		);
	}

	@Test
	public void test03() {

		loader.loadByHBaseObjects(SlipDetailDO.class, BeanTestHelper.createList(SlipDetailDO.class,
			"id,outerCustomerId,slipNo,slipDetailNo,slipDetailCategory,janCode,effectiveNum,returnedNum,setCouponId,setParentDetailNo,salesRegistDetailType",
			"11,0000000001,1000000001,1,01,100000000011,11,21,2000000011,41,1",
			"12,0000000001,1000000001,2,02,100000000012,12,22,2000000012,42,2"
		));
		assertListContents(solrOperations.findByQuery(new SolrQuery("*:*"), SlipDetailDO.class).getDocuments(), SlipDetailDO.class,
			"id,outerCustomerId,slipNo,slipDetailNo,slipDetailCategory,janCode,effectiveNum,returnedNum,setCouponId,setParentDetailNo,salesRegistDetailType",
			"11","0000000001","1000000001",1,SlipDetailCategory.NORMAL,"100000000011",11,21,"2000000011",41,SalesRegistDetailType.EFFECTIVE,
			"12","0000000001","1000000001",2,SlipDetailCategory.MAKER_DIRECT,"100000000012",12,22,"2000000012",42,SalesRegistDetailType.INEFFECTIVE
		);
	}
	
}
