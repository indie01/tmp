package com.kickmogu.yodobashi.community.resource.hbase2solr.impl;

import static com.kickmogu.lib.core.BeanTestHelper.*;
import static org.junit.Assert.*;

import java.util.List;

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
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserFollowDO;
import com.kickmogu.yodobashi.community.resource.hbase2solr.HBase2SolrLoader;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/resourceContext.xml")
public class DefaultVisibilityHBase2SolrConverterImplTest {
	
	@Autowired
	private HBase2SolrLoader loader;
	
	@Autowired  @Qualifier("MySite")
	protected HBaseOperations hBaseOperations;
	
	@Autowired  @Qualifier("MySite")
	protected SolrOperations solrOperations;
	
	@Before
	public void setup() {
		hBaseOperations.deleteAll(CommunityUserFollowDO.class);
		solrOperations.deleteAll(CommunityUserFollowDO.class);
	}
	
	@Test
	public void hoge() {
		loader.loadByKey(CommunityUserDO.class, "aaa");
	}

	@Test
	public void test() {
		
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
		
		loader.loadByHBaseObjects(CommunityUserFollowDO.class, list);
		assertListContents(solrOperations.findByQuery(new SolrQuery("*:*"), CommunityUserFollowDO.class).getDocuments(), CommunityUserFollowDO.class,
			"communityUserFollowId,stop,withdraw",
			"11",true,false
		);
		
		assertEquals("21", solrOperations.findByQuery(new SolrQuery("*:*"), CommunityUserFollowDO.class).getDocuments().get(0).getCommunityUser().getCommunityUserId());
	}
	
	@Test
	public void test02() {
		
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
		loader.loadByKeyRange(CommunityUserFollowDO.class, "11", "13");
		
		assertListContents(solrOperations.findByQuery(new SolrQuery("*:*"), CommunityUserFollowDO.class).getDocuments(), CommunityUserFollowDO.class,
			"communityUserFollowId,stop,withdraw",
			"11",true,false
		);
		
		assertEquals("21", solrOperations.findByQuery(new SolrQuery("*:*"), CommunityUserFollowDO.class).getDocuments().get(0).getCommunityUser().getCommunityUserId());
	}
	
	
}
