package com.kickmogu.yodobashi.community.mapreduce.job;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

import com.kickmogu.lib.core.utils.AopUtil;
import com.kickmogu.lib.hadoop.hbase.HBaseContainer;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.lib.hadoop.hbase.impl.ExternalEntityHBaseTemplate;
import com.kickmogu.lib.hadoop.hbase.impl.HBaseTemplate;
import com.kickmogu.lib.hadoop.hbase.impl.TimestampConfigurableProcessContextHolderImpl;
import com.kickmogu.yodobashi.community.common.test.YcComJUnit4ClassRunner;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.ContentsStatus;


@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration("/serviceContext.xml")
public class SampleAppendIndexTable {
	
	@Autowired
	@Qualifier("default")
	private HBaseOperations hbaseOperations;
	
	@Autowired
	@Qualifier("MySite")
	private HBaseContainer hbaseContainer;

	private TimestampConfigurableProcessContextHolderImpl processContextHolderImpl = new TimestampConfigurableProcessContextHolderImpl();

	@Before
	public void setup() {
	}
	
	@Test
	public void step0() {
		/**
		 * 事前データ作成
		 * RevieDO.java viewCountに@HBaseIndexがついていないこと！
		 */
		
		HBaseTemplate template = (HBaseTemplate) ((ExternalEntityHBaseTemplate) AopUtil.getTargetObject(hbaseOperations, HBaseOperations.class)).getOrgOperations();
		template.setProcessContextHolder(processContextHolderImpl);
		hbaseContainer.getAdministrator().dropTable(ReviewDO.class);
		hbaseContainer.getAdministrator().createTable(ReviewDO.class);
		
		makeTestData();
		System.out.println(dispHBaseReviewDO(false));
	}
	
	@Test
	public void step1() {
		/**
		 * セカンダリインデックスの追加
		 */
		try {
			hbaseContainer.getAdministrator().addIndex(ReviewDO.class, "viewCount");
			hbaseContainer.getAdministrator().addIndex(ReviewDO.class, "testStr");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void step2() {
		/**
		 * インデックス生成
		 */
		try {
			hbaseContainer.getAdministrator().reIndex(ReviewDO.class, "viewCount", 64);
			hbaseContainer.getAdministrator().reIndex(ReviewDO.class, "testStr", 64);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void step3() {
		/**
		 * インデックスより検索を実行 (findWithIndexの実行）
		 * RevieDO.java viewCountに@HBaseIndexがついていること!
		 */
		System.out.println(dispHBaseReviewDO(true));				
	}
	
	@Test
	public void step4() {
		/**
		 * インデックス削除
		 */
		try {
			//hbaseContainer.getAdministrator().removeIndex(ReviewDO.class, "viewCount");
			hbaseContainer.getAdministrator().removeIndex(ReviewDO.class, "testStr");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private void makeTestData() {
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
		review.setViewCount(55);
		hbaseOperations.save(review);
		
		ReviewDO review2 = new ReviewDO();
		review2.setReviewId("r-0002");
		review2.setEffective(true);
		review2.setStatus(ContentsStatus.SUBMITTED);
		review2.setWithdrawKey("val-withdrawKey2");

		CommunityUserDO communityUser2 = new CommunityUserDO();
		communityUser2.setCommunityUserId("id-communityUserId2");
		communityUser2.setCommunityName("val-communityName2");

		ProductDO product2 = new ProductDO();
		product2.setSku("id-sku2");
		product2.setBrndNm("val-brndNm2");

		review2.setCommunityUser(communityUser2);
		review2.setProduct(product2);
		hbaseOperations.save(review2);		
	}
	
	public String dispHBaseReviewDO(boolean flgNewIndexTable) {
		StringWriter strw = new StringWriter();
		PrintWriter out = new PrintWriter(strw); 

		// リストア後にテーブルを参照
		out.println("ReviewDO record");
		dispReview(out, hbaseOperations.load(ReviewDO.class, "r-0001"));

		// Indexから参照
		out.println("index-table [communityUserId] find key [id-communityUserId]");
		dispReviewList(out, hbaseOperations.findWithIndex(ReviewDO.class, "communityUserId", "id-communityUserIdZ")); // productId,effective
		out.println("index-table [productId] find key [id-sku]");
		dispReviewList(out, hbaseOperations.findWithIndex(ReviewDO.class, "productId", "id-skuZ")); // status
		out.println("index-table [withdrawKey] find key [val-withdrawKey]");
		dispReviewList(out, hbaseOperations.findWithIndex(ReviewDO.class, "withdrawKey", "val-withdrawKeyZ"));
		
		if (flgNewIndexTable) {
			out.println("index-table [viewCount] find key [55]");
			dispReviewList(out, hbaseOperations.findWithIndex(ReviewDO.class, "viewCount", new Long(55)));
		}
		
		out.flush();
		return strw.toString();
	}

	private void dispReviewList(PrintWriter out, List<ReviewDO> list) {
		for (ReviewDO review : list) {
			dispReview(out, review);
		}
	}

	private void dispReview(PrintWriter out, ReviewDO review) {
		if (review == null)
			return;
		out.println("\t{");
		out.println("\t\treviewID:" + review.getReviewId());
		out.println("\t\tstatus:" + review.getStatus());
		out.println("\t\tWithdrawKey:" + review.getWithdrawKey());
		out.println("\t\tEffective:" + review.isEffective());
		out.println("\t\tviewCount:" + review.getViewCount());
		if (review.getProduct() != null)
			out.println("\t\tsku:" + review.getProduct().getSku());
		if (review.getCommunityUser() != null)
			out.println("\t\tcommynityUserId:" + review.getCommunityUser().getCommunityUserId());
		out.println("\t}");
	}

}
