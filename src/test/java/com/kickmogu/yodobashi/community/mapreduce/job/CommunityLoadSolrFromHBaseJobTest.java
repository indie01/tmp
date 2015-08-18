package com.kickmogu.yodobashi.community.mapreduce.job;

import static com.kickmogu.lib.core.BeanTestHelper.*;
import static org.junit.Assert.*;

import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;

import com.kickmogu.lib.core.BeanTestHelper;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.yodobashi.community.common.test.YcComJUnit4ClassRunner;
import com.kickmogu.yodobashi.community.mapreduce.job.CommunityLoadSolrFromHBaseJob;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserFollowDO;
import com.kickmogu.yodobashi.community.resource.domain.SlipDetailDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.SalesRegistDetailType;
import com.kickmogu.yodobashi.community.resource.domain.constants.SlipDetailCategory;

@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration("/serviceContext.xml")
public class CommunityLoadSolrFromHBaseJobTest {
	
	
	@Autowired  @Qualifier("MySite")
	protected HBaseOperations hBaseOperations;
	
	@Autowired  @Qualifier("MySite")
	protected SolrOperations solrOperations;
	
	@Autowired
	protected ApplicationContext applicationContext;
	
	@Before
	public void setup() {

	}
	
	@Test
	public void test() throws Exception {
		hBaseOperations.deleteAll(SlipDetailDO.class);
		solrOperations.deleteAll(SlipDetailDO.class);
		
		hBaseOperations.save(SlipDetailDO.class, BeanTestHelper.createList(SlipDetailDO.class,
			"id,outerCustomerId,slipNo,slipDetailNo,slipDetailCategory,janCode,effectiveNum,salesNum,setCouponId,setParentDetailNo,salesRegistDetailType",
			"11,0000000001,1000000001,1,01,100000000011,11,21,2000000011,41,1",
			"12,0000000001,1000000001,2,02,100000000012,12,22,2000000012,42,2"
		));
		
		CommunityLoadSolrFromHBaseJob.execute(applicationContext, new String[]{
			"-t", "nomuSlipDetailDO"
		});

		assertListContents(solrOperations.findByQuery(new SolrQuery("*:*"), SlipDetailDO.class).getDocuments(), SlipDetailDO.class,
			"id,outerCustomerId,slipNo,slipDetailNo,slipDetailCategory,janCode,effectiveNum,salesNum,setCouponId,setParentDetailNo,salesRegistDetailType",
			"11","0000000001","1000000001",1,SlipDetailCategory.NORMAL,"100000000011",11,21,"2000000011",41,SalesRegistDetailType.EFFECTIVE,
			"12","0000000001","1000000001",2,SlipDetailCategory.MAKER_DIRECT,"100000000012",12,22,"2000000012",42,SalesRegistDetailType.INEFFECTIVE
		);
	}

	
	@Test
	public void test02() throws Exception {
		hBaseOperations.deleteAll(CommunityUserFollowDO.class);
		solrOperations.deleteAll(CommunityUserFollowDO.class);
		
		List<CommunityUserFollowDO> list = BeanTestHelper.createList(CommunityUserFollowDO.class,
				"communityUserFollowId,stop,withdraw",
				"11,true,false",
				"12,true,true"
			);
			
			for (CommunityUserFollowDO follow:list) {
				CommunityUserDO communityUser = new CommunityUserDO();
				communityUser.setCommunityUserId("21");
				follow.setCommunityUser(communityUser);
			}
			
			hBaseOperations.save(CommunityUserFollowDO.class, list);
			CommunityLoadSolrFromHBaseJob.execute(applicationContext, new String[]{
					"-t", "nomuCommunityUserFollowDO"
				});
			
			assertListContents(solrOperations.findByQuery(new SolrQuery("*:*"), CommunityUserFollowDO.class).getDocuments(), CommunityUserFollowDO.class,
				"communityUserFollowId,stop,withdraw",
				"11",true,false
			);
			
			assertEquals("21", solrOperations.findByQuery(new SolrQuery("*:*"), CommunityUserFollowDO.class).getDocuments().get(0).getCommunityUser().getCommunityUserId());

	}
}
