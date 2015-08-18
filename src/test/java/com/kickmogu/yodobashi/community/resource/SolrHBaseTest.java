package com.kickmogu.yodobashi.community.resource;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.yodobashi.community.resource.domain.AsyncMessageDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.test.TestService;
import com.kickmogu.yodobashi.community.resource.test.TestService.Invoker;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/resourceContext.xml")
public class SolrHBaseTest {

	@Autowired @Qualifier("default")
	private HBaseOperations hBaseOperations;
	
	@Autowired @Qualifier("default")
	private SolrOperations solrOperations;
	
	@Autowired
	private TestService testService;
	
	@Before
	public void setup() {
		solrOperations.deleteAll(CommunityUserDO.class);
		solrOperations.commit(CommunityUserDO.class);
	}
	
	@After
	public void teardown() {
		solrOperations.commit(CommunityUserDO.class);		
	}
	
	@Test
	public void test() {
		AsyncMessageDO asyncMessage = new AsyncMessageDO();
		asyncMessage.setHandlerName("name1");
		asyncMessage.setData("hoge".getBytes());
		hBaseOperations.save(asyncMessage);
		assertEquals("name1", hBaseOperations.load(AsyncMessageDO.class, asyncMessage.getMessageId()).getHandlerName());
		assertEquals("hoge", new String(hBaseOperations.load(AsyncMessageDO.class, asyncMessage.getMessageId()).getData()));
	}
	
	@Test
	public void test2() {
		CommunityUserDO communityUser = new CommunityUserDO();
		communityUser.setCommunityName("commu1");
		solrOperations.save(communityUser);
		assertEquals("commu1", solrOperations.load(CommunityUserDO.class, communityUser.getCommunityUserId()).getCommunityName());
	}
	
	@Test
	public void test03() {
		testService.invoke(new Invoker(){
			@Override
			public void invoke() {
				System.out.println("start lock");
				hBaseOperations.lockRow(AsyncMessageDO.class, "hoge");
				System.out.println("end lock");
			}
		});
	}
	
	// ArroundSolrがcommitAfterProcessを指定している場合、処理終了後にコミットされる
	@Test
	public void test04() {
		final CommunityUserDO communityUser = new CommunityUserDO();
		testService.invokeSolrCommitAfterProcess(new Invoker(){
			@Override
			public void invoke() {
				communityUser.setCommunityName("commu1");
				solrOperations.save(communityUser);
				assertNull(solrOperations.load(CommunityUserDO.class, communityUser.getCommunityUserId()));
			}
		});
		assertNotNull(solrOperations.load(CommunityUserDO.class, communityUser.getCommunityUserId()));
	}

	// ArroundSolrがcommitAfterProcessを指定していない場合、コミットされない	
	@Test
	public void test05() {
		final CommunityUserDO communityUser = new CommunityUserDO();
		testService.invokeSolrNoCommit(new Invoker(){
			@Override
			public void invoke() {
				communityUser.setCommunityName("commu1");
				solrOperations.save(communityUser);
				assertNull(solrOperations.load(CommunityUserDO.class, communityUser.getCommunityUserId()));
			}
		});
		assertNull(solrOperations.load(CommunityUserDO.class, communityUser.getCommunityUserId()));
		solrOperations.commit(CommunityUserDO.class, true, true);
		assertNotNull(solrOperations.load(CommunityUserDO.class, communityUser.getCommunityUserId()));
	}
	
	// ArroundSolrがrollbackOnExceptionを指定していない場合、例外が投げられてもロールバックされない
	@Test
	public void test06() {
		final CommunityUserDO communityUser = new CommunityUserDO();
		try {
			testService.invokeSolrCommitAfterProcess(new Invoker(){
				@Override
				public void invoke() {
					communityUser.setCommunityName("commu1");
					solrOperations.save(communityUser);
					assertNull(solrOperations.load(CommunityUserDO.class, communityUser.getCommunityUserId()));
					throw new RuntimeException();
				}
			});
		} catch (Throwable th) {
		}
		assertNotNull(solrOperations.load(CommunityUserDO.class, communityUser.getCommunityUserId()));
	}

	// ArroundSolrがrollbackOnExceptionを指定している場合、例外が投げられたらロールバックする
	@Test
	public void test07() {
		final CommunityUserDO communityUser = new CommunityUserDO();	
		testService.invokeSolrRollback(new Invoker(){
			@Override
			public void invoke() {
				communityUser.setCommunityName("commu1");
				solrOperations.save(communityUser);
				assertNull(solrOperations.load(CommunityUserDO.class, communityUser.getCommunityUserId()));
			}
		});
		assertNotNull(solrOperations.load(CommunityUserDO.class, communityUser.getCommunityUserId()));
		
		final CommunityUserDO communityUser2 = new CommunityUserDO();
		try {
			testService.invokeSolrRollback(new Invoker(){
				@Override
				public void invoke() {
					communityUser2.setCommunityName("commu1");
					solrOperations.save(communityUser2);
					assertNull(solrOperations.load(CommunityUserDO.class, communityUser2.getCommunityUserId()));
					throw new RuntimeException();
				}
			});
		} catch (Throwable th) {
		}
		assertNull(solrOperations.load(CommunityUserDO.class, communityUser2.getCommunityUserId()));
		solrOperations.commit(CommunityUserDO.class, true, true);
		assertNull(solrOperations.load(CommunityUserDO.class, communityUser2.getCommunityUserId()));
	}

}
