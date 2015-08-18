package com.kickmogu.yodobashi.community.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * ReviewVotingCountServiceのテストクラスです。
 *
 * @author m.takahashi
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/serviceContext.xml") // /test-serviceContext.xml
public class ReviewVotingCountServiceTest {

	@Autowired
	private ReviewVotingCountService service;

	@Test
	public void testCountReviewVoting() throws Exception {
//		String targetDate = "2013-08-27";
		String targetDate = "2013-07-11";
		DateFormat foramt = new SimpleDateFormat("yyyy-MM-dd");
		Date date = foramt.parse(targetDate);

		Map<String, Map<String, Long>> actual = service.countReviewVoting(date);
//		int expected = 21;
//		assertEquals("処理件数", expected, actual);

		for (String key : actual.keySet()) {
			System.out.println("key:"+key);
			System.out.println("val:"+actual.get(key));
		}

	}

//	String outputFile = "c:/home/comm/stat/output/sas_accesslog_0202_2013-08-28.tsv";
//	String outputFile = "c:/home/comm/stat/output/sas_accesslog_0202_2013-07-12.tsv";

	@After
	public void teardown() {
	}

}
