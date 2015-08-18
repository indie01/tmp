package com.kickmogu.yodobashi.community.resource;

import static org.junit.Assert.*;

import org.apache.solr.client.solrj.SolrQuery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.kickmogu.lib.core.domain.SearchResult;
import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/resourceContext.xml")
public class BugFixTest {
	
	
	@Autowired @Qualifier("default")
	private SolrOperations solrOperations;
	
	@Before
	public void setup() {
		solrOperations.deleteAll(PurchaseProductDO.class);
		solrOperations.deleteAll(CommunityUserDO.class);
		solrOperations.commit(PurchaseProductDO.class);
		solrOperations.commit(CommunityUserDO.class);
	}
	
	@Test
	public void test() {
		PurchaseProductDO purchaseProductDO = new PurchaseProductDO();
		purchaseProductDO.setJanCode("xxx");
		CommunityUserDO communityUserDO = new CommunityUserDO();
		communityUserDO.setCommunityUserId("c1");
		purchaseProductDO.setCommunityUser(communityUserDO);
		
		solrOperations.save(communityUserDO);
		solrOperations.save(purchaseProductDO);
		
		SearchResult<PurchaseProductDO> result = solrOperations.findByQuery(
				new SolrQuery("janCode:xxx"), PurchaseProductDO.class,
				Path.depth(1).includePath(
				"communityUser.communityUserId").depth(1));
		
		assertEquals("c1", result.getDocuments().get(0).getCommunityUser().getCommunityUserId());
	}

}
