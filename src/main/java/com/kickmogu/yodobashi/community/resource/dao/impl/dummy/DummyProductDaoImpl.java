/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.impl.dummy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.core.resource.ExternalEntityOperations;
import com.kickmogu.lib.core.resource.Path.Condition;
import com.kickmogu.yodobashi.community.resource.dao.ProductDao;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO.ProductImage;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO.ReviewPoint;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO.ReviewPointDetail;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO.ReviewPointSpecial;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO.ReviewPointSpecialDetail;
import com.kickmogu.yodobashi.community.resource.domain.constants.FillType;
import com.kickmogu.yodobashi.community.resource.domain.constants.PointIncentiveType;


/**
 * 商品 DAO のダミー実装です。
 * @author kamiike
 *
 */
@Service @Qualifier("dummy")
public class DummyProductDaoImpl implements ProductDao, InitializingBean , ExternalEntityOperations<ProductDO, String> {

	/**
	 * 指定した商品の情報を返します。
	 * @param sku SKU
	 * @return 商品情報
	 */
	@Override
	public ProductDO loadProduct(String sku) {
		return loadProduct(sku, FillType.SMALL, null, null, null, false, null);
	}

	@Override
	public List<ProductDO> loadProducts(List<String> skus) {
		return loadProducts(skus, FillType.SMALL, null, null, null, false,  null);
	}

	@Override
	public List<ProductDO> loadProducts(
			List<String> skus, 
			FillType fillType,
			String autoId, 
			String cartId, 
			String yatpz, 
			boolean isWithCart,
			Map<String, Object> params) {
		List<ProductDO> results = new ArrayList<ProductDO>();
		ProductDO product = null;
		for(String sku : skus ){
			product = skuMap.get(sku);
			if( product != null )
				results.add(product);
		}
		return results;
	}
	
	/**
	 * 指定した商品の情報を返します。
	 * @param janCode JANコード
	 * @return 商品情報
	 */
	@Override
	public ProductDO loadProductByJanCode(String janCode) {
		ProductDO product = janCodeMap.get(janCode);
		return product;
	}

	/**
	 * 指定した商品情報リストを返します。
	 * @param janCodes JANコードリスト
	 * @return 商品情報リスト
	 */
	@Override
	public Map<String, ProductDO> findByJanCode(Collection<String> janCodes) {
		Map<String, ProductDO> result = new HashMap<String, ProductDO>();
		for (String janCode : janCodes) {
			ProductDO product = loadProductByJanCode(janCode);
			if (product != null) {
				result.put(janCode, product);
			}
		}
		return result;
	}

	/**
	 * 指定した商品情報リストを返します。
	 * @param skus SKUリスト
	 * @return 商品情報リスト
	 */
	@Override
	public Map<String, ProductDO> findBySku(Collection<String> skus) {
		return find(skus, null);
	}

	/**
	 * 指定されたキーワードで商品を検索して返します。
	 * @param keyword キーワード
	 * @param excludeSkus 除外する商品リスト
	 * @param includeCero CERO商品を含める場合、true
	 * @param includeAdult アダルト商品を含める場合、true
	 * @return 商品リスト
	 */
	@Override
	public List<ProductDO> findByKeyword(
			String keyword,
			List<String> excludeSkus,
			boolean includeCero,
			boolean includeAdult) {
		List<ProductDO> products = new ArrayList<ProductDO>();
		for (ProductDO product : skuMap.values()) {
			if (!includeCero && product.isCero()) {
				continue;
			}
			if (!includeAdult && product.isAdult()) {
				continue;
			}
			if (excludeSkus != null && excludeSkus.size() > 0) {
				if (excludeSkus.contains(product.getSku())) {
					continue;
				}
			}
			products.add(product);
		}
		return products;
	}

	private Map<String, ProductDO> skuMap = new HashMap<String, ProductDO>();

	private Map<String, ProductDO> janCodeMap = new HashMap<String, ProductDO>();

	private List<ReviewPoint> getReviewPoints(int reviewPointSpecial) {
		try {
			List<ReviewPoint> list = new ArrayList<ReviewPoint>();
			ReviewPoint reviewPoint1 = new ReviewPoint();
			reviewPoint1.setRvwQstCd(
					PointIncentiveType.IMMEDIATELY_AFTER_DECISIVE_PURCHASE.getCode());
			reviewPoint1.setRvwQstSttTm("2001-12-01 01:00:00.000");
			reviewPoint1.setRvwQstEdTm("2021-12-01 01:00:00.000");
			ReviewPointDetail[] rvwQstDtls1 = new ReviewPointDetail[1];
			ReviewPointDetail rvwQstDtl1 = new ReviewPointDetail();
			rvwQstDtl1.setRvwQstDtlSttTm("2001-12-01 01:00:00.000");
			rvwQstDtl1.setRvwQstDtlEdTm("2021-12-01 01:00:00.000");
			rvwQstDtl1.setRvwQstDtlSttThd(1);
			rvwQstDtl1.setRvwQstDtlEdThd(14);
			rvwQstDtl1.setRvwBasePt(10L);
			rvwQstDtls1[0] = rvwQstDtl1;
			reviewPoint1.setRvwQstDtls(rvwQstDtls1);
			ReviewPointSpecial[] rvwSps1 = new ReviewPointSpecial[3];
			ReviewPointSpecial rvwSps11 = new ReviewPointSpecial();
			rvwSps1[0] = rvwSps11;
			rvwSps11.setPryNo(1);
			rvwSps11.setPtTyp(reviewPointSpecial);
			rvwSps11.setRvwSpCd("10001");
			rvwSps11.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_ORDER);
			rvwSps11.setRvwSpSttTm("2012-01-01 00:00:00.000");
			rvwSps11.setRvwSpEdTm("2012-02-01 00:00:00.000");
			rvwSps11.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_TERM);
			ReviewPointSpecialDetail rvwSp11Detail1 = new ReviewPointSpecialDetail();
			rvwSp11Detail1.setRvwSpDtlSttThdDt("2012-01-01 00:00:00.000");
			rvwSp11Detail1.setRvwSpDtlEdThdDt("2012-01-14 23:59:59.000");
			rvwSp11Detail1.setRvwSpDtlPt(100L);
			rvwSp11Detail1.setRvwSpDtlTyp("D");
			ReviewPointSpecialDetail rvwSp11Detail2 = new ReviewPointSpecialDetail();
			rvwSp11Detail2.setRvwSpDtlSttThdDt("2012-01-15 00:00:00.000");
			rvwSp11Detail2.setRvwSpDtlEdThdDt("2012-02-01 00:00:00.000");
			rvwSp11Detail2.setRvwSpDtlPt(80L);
			rvwSp11Detail2.setRvwSpDtlTyp("D");
			rvwSps11.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSp11Detail1, rvwSp11Detail2});

			ReviewPointSpecial rvwSps12 = new ReviewPointSpecial();
			rvwSps1[1] = rvwSps12;
			rvwSps12.setPryNo(2);
			rvwSps12.setPtTyp(reviewPointSpecial);
			rvwSps12.setRvwSpCd("10002");
			rvwSps12.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_PURCHASE);
			rvwSps12.setRvwSpSttTm("2001-02-02 00:00:00.000");
			rvwSps12.setRvwSpEdTm("2001-02-14 00:00:00.000");
			rvwSps12.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_FIRST);
			ReviewPointSpecialDetail rvwSp12Detail1 = new ReviewPointSpecialDetail();
			rvwSp12Detail1.setRvwSpDtlSttThdNum(1);
			rvwSp12Detail1.setRvwSpDtlEdThdNum(10);
			rvwSp12Detail1.setRvwSpDtlPt(60L);
			rvwSp12Detail1.setRvwSpDtlTyp("N");
			ReviewPointSpecialDetail rvwSp12Detail2 = new ReviewPointSpecialDetail();
			rvwSp12Detail2.setRvwSpDtlSttThdNum(11);
			rvwSp12Detail2.setRvwSpDtlEdThdNum(20);
			rvwSp12Detail2.setRvwSpDtlPt(40L);
			rvwSp12Detail2.setRvwSpDtlTyp("N");
			rvwSps12.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSp12Detail1, rvwSp12Detail2});

			ReviewPointSpecial rvwSps13 = new ReviewPointSpecial();
			rvwSps1[2] = rvwSps13;
			rvwSps13.setPryNo(3);
			rvwSps13.setPtTyp(reviewPointSpecial);
			rvwSps13.setRvwSpCd("10003");
			rvwSps13.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_POST);
			rvwSps13.setRvwSpSttTm("2001-02-15 00:00:00.000");
			rvwSps13.setRvwSpEdTm("2001-02-27 00:00:00.000");
			rvwSps13.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_TERM);
			ReviewPointSpecialDetail rvwSp13Detail1 = new ReviewPointSpecialDetail();
			rvwSp13Detail1.setRvwSpDtlSttThdDt("2012-02-15 00:00:00.000");
			rvwSp13Detail1.setRvwSpDtlEdThdDt("2012-02-19 23:59:59.000");
			rvwSp13Detail1.setRvwSpDtlPt(20L);
			rvwSp13Detail1.setRvwSpDtlTyp("D");
			ReviewPointSpecialDetail rvwSp13Detail2 = new ReviewPointSpecialDetail();
			rvwSp13Detail1.setRvwSpDtlSttThdDt("2012-02-20 00:00:00.000");
			rvwSp13Detail1.setRvwSpDtlEdThdDt("2012-02-26 23:59:59.000");
			rvwSp13Detail2.setRvwSpDtlPt(10L);
			rvwSp13Detail2.setRvwSpDtlTyp("D");
			rvwSps13.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSp13Detail1, rvwSp13Detail2});

			reviewPoint1.setRvwSps(rvwSps1);
			list.add(reviewPoint1);

			ReviewPoint reviewPoint2 = new ReviewPoint();
			reviewPoint2.setRvwQstCd(
					PointIncentiveType.IMMEDIATELY_AFTER_PURCHASE_LOST_PRODUCT.getCode());
			reviewPoint2.setRvwQstSttTm("2001-12-01 01:00:00.000");
			reviewPoint2.setRvwQstEdTm("2021-12-01 01:00:00.000");
			ReviewPointDetail[] rvwQstDtls2 = new ReviewPointDetail[1];
			ReviewPointDetail rvwQstDtl2 = new ReviewPointDetail();
			rvwQstDtl2.setRvwQstDtlSttTm("2001-12-01 01:00:00.000");
			rvwQstDtl2.setRvwQstDtlEdTm("2021-12-01 01:00:00.000");
			rvwQstDtl2.setRvwQstDtlSttThd(1);
			rvwQstDtl2.setRvwQstDtlEdThd(14);
			rvwQstDtl2.setRvwBasePt(9L);
			rvwQstDtls2[0] = rvwQstDtl2;
			reviewPoint2.setRvwQstDtls(rvwQstDtls2);
			ReviewPointSpecial[] rvwSps2 = new ReviewPointSpecial[3];
			ReviewPointSpecial rvwSps21 = new ReviewPointSpecial();
			rvwSps2[0] = rvwSps21;
			rvwSps21.setPryNo(1);
			rvwSps21.setPtTyp(reviewPointSpecial);
			rvwSps21.setRvwSpCd("20001");
			rvwSps21.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_ORDER);
			rvwSps21.setRvwSpSttTm("2001-01-01 00:00:00.000");
			rvwSps21.setRvwSpEdTm("2001-02-01 00:00:00.000");
			rvwSps21.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_TERM);
			ReviewPointSpecialDetail rvwSp21Detail1 = new ReviewPointSpecialDetail();
			rvwSp21Detail1.setRvwSpDtlSttThdDt("2012-01-01 00:00:00.000");
			rvwSp21Detail1.setRvwSpDtlEdThdDt("2012-01-14 23:59:59.000");
			rvwSp21Detail1.setRvwSpDtlPt(100L);
			rvwSp21Detail1.setRvwSpDtlTyp("D");
			ReviewPointSpecialDetail rvwSp21Detail2 = new ReviewPointSpecialDetail();
			rvwSp21Detail2.setRvwSpDtlSttThdDt("2012-01-15 00:00:00.000");
			rvwSp21Detail2.setRvwSpDtlEdThdDt("2012-02-01 00:00:00.000");
			rvwSp21Detail2.setRvwSpDtlPt(80L);
			rvwSp21Detail2.setRvwSpDtlTyp("D");
			rvwSps21.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSp21Detail1, rvwSp21Detail2});

			ReviewPointSpecial rvwSps22 = new ReviewPointSpecial();
			rvwSps2[1] = rvwSps22;
			rvwSps22.setPryNo(2);
			rvwSps22.setPtTyp(reviewPointSpecial);
			rvwSps22.setRvwSpCd("20002");
			rvwSps22.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_PURCHASE);
			rvwSps22.setRvwSpSttTm("2012-02-02 00:00:00.000");
			rvwSps22.setRvwSpEdTm("2012-02-14 00:00:00.000");
			rvwSps22.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_FIRST);
			ReviewPointSpecialDetail rvwSp22Detail1 = new ReviewPointSpecialDetail();
			rvwSp22Detail1.setRvwSpDtlSttThdNum(1);
			rvwSp22Detail1.setRvwSpDtlEdThdNum(10);
			rvwSp22Detail1.setRvwSpDtlPt(60L);
			rvwSp22Detail1.setRvwSpDtlTyp("N");
			ReviewPointSpecialDetail rvwSp22Detail2 = new ReviewPointSpecialDetail();
			rvwSp22Detail2.setRvwSpDtlSttThdNum(11);
			rvwSp22Detail2.setRvwSpDtlEdThdNum(20);
			rvwSp22Detail2.setRvwSpDtlPt(40L);
			rvwSp22Detail2.setRvwSpDtlTyp("N");
			rvwSps22.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSp22Detail1, rvwSp22Detail2});

			ReviewPointSpecial rvwSps23 = new ReviewPointSpecial();
			rvwSps2[2] = rvwSps23;
			rvwSps23.setPryNo(3);
			rvwSps23.setPtTyp(reviewPointSpecial);
			rvwSps23.setRvwSpCd("20003");
			rvwSps23.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_POST);
			rvwSps23.setRvwSpSttTm("2001-02-15 00:00:00.000");
			rvwSps23.setRvwSpEdTm("2001-02-27 00:00:00.000");
			rvwSps23.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_TERM);
			ReviewPointSpecialDetail rvwSp23Detail1 = new ReviewPointSpecialDetail();
			rvwSp23Detail1.setRvwSpDtlSttThdDt("2012-02-15 00:00:00.000");
			rvwSp23Detail1.setRvwSpDtlEdThdDt("2012-02-19 23:59:59.000");
			rvwSp23Detail1.setRvwSpDtlPt(20L);
			rvwSp23Detail1.setRvwSpDtlTyp("D");
			ReviewPointSpecialDetail rvwSp23Detail2 = new ReviewPointSpecialDetail();
			rvwSp23Detail2.setRvwSpDtlSttThdDt("2012-02-20 00:00:00.000");
			rvwSp23Detail2.setRvwSpDtlEdThdDt("2012-02-26 23:59:59.000");
			rvwSp23Detail2.setRvwSpDtlPt(10L);
			rvwSp23Detail2.setRvwSpDtlTyp("D");
			rvwSps23.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSp23Detail1, rvwSp23Detail2});

			reviewPoint2.setRvwSps(rvwSps2);
			list.add(reviewPoint2);

			ReviewPoint reviewPoint3 = new ReviewPoint();
			reviewPoint3.setRvwQstCd(
					PointIncentiveType.IMMEDIATELY_AFTER_USED_PRODUCT.getCode());
			reviewPoint3.setRvwQstSttTm("2001-12-01 01:00:00.000");
			reviewPoint3.setRvwQstEdTm("2021-12-01 01:00:00.000");
			ReviewPointDetail[] rvwQstDtls3 = new ReviewPointDetail[1];
			ReviewPointDetail rvwQstDtl3 = new ReviewPointDetail();
			rvwQstDtl3.setRvwQstDtlSttTm("2001-12-01 01:00:00.000");
			rvwQstDtl3.setRvwQstDtlEdTm("2021-12-01 01:00:00.000");
			rvwQstDtl3.setRvwQstDtlSttThd(1);
			rvwQstDtl3.setRvwQstDtlEdThd(14);
			rvwQstDtl3.setRvwBasePt(8L);
			rvwQstDtls3[0] = rvwQstDtl3;
			reviewPoint3.setRvwQstDtls(rvwQstDtls3);
			ReviewPointSpecial[] rvwSps3 = new ReviewPointSpecial[3];
			ReviewPointSpecial rvwSps31 = new ReviewPointSpecial();
			rvwSps3[0] = rvwSps31;
			rvwSps31.setPryNo(1);
			rvwSps31.setPtTyp(reviewPointSpecial);
			rvwSps31.setRvwSpCd("30001");
			rvwSps31.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_ORDER);
			rvwSps31.setRvwSpSttTm("2001-01-01 00:00:00.000");
			rvwSps31.setRvwSpEdTm("2001-02-01 00:00:00.000");
			rvwSps31.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_TERM);
			ReviewPointSpecialDetail rvwSp31Detail1 = new ReviewPointSpecialDetail();
			rvwSp31Detail1.setRvwSpDtlSttThdDt("2012-01-01 00:00:00.000");
			rvwSp31Detail1.setRvwSpDtlEdThdDt("2012-01-14 23:59:59.000");
			rvwSp31Detail1.setRvwSpDtlPt(100L);
			rvwSp31Detail1.setRvwSpDtlTyp("D");
			ReviewPointSpecialDetail rvwSp31Detail2 = new ReviewPointSpecialDetail();
			rvwSp31Detail2.setRvwSpDtlSttThdDt("2012-01-15 00:00:00.000");
			rvwSp31Detail2.setRvwSpDtlEdThdDt("2012-02-01 00:00:00.000");
			rvwSp31Detail2.setRvwSpDtlPt(80L);
			rvwSp31Detail2.setRvwSpDtlTyp("D");
			rvwSps31.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSp31Detail1, rvwSp31Detail2});

			ReviewPointSpecial rvwSps32 = new ReviewPointSpecial();
			rvwSps3[1] = rvwSps32;
			rvwSps32.setPryNo(2);
			rvwSps32.setPtTyp(reviewPointSpecial);
			rvwSps32.setRvwSpCd("30002");
			rvwSps32.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_PURCHASE);
			rvwSps32.setRvwSpSttTm("2001-02-02 00:00:00.000");
			rvwSps32.setRvwSpEdTm("2001-02-14 00:00:00.000");
			rvwSps32.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_FIRST);
			ReviewPointSpecialDetail rvwSp32Detail1 = new ReviewPointSpecialDetail();
			rvwSp32Detail1.setRvwSpDtlSttThdNum(1);
			rvwSp32Detail1.setRvwSpDtlEdThdNum(10);
			rvwSp32Detail1.setRvwSpDtlPt(60L);
			rvwSp32Detail1.setRvwSpDtlTyp("N");
			ReviewPointSpecialDetail rvwSp32Detail2 = new ReviewPointSpecialDetail();
			rvwSp32Detail2.setRvwSpDtlSttThdNum(11);
			rvwSp32Detail2.setRvwSpDtlEdThdNum(20);
			rvwSp32Detail2.setRvwSpDtlPt(40L);
			rvwSp32Detail2.setRvwSpDtlTyp("N");
			rvwSps32.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSp32Detail1, rvwSp32Detail2});

			ReviewPointSpecial rvwSps33 = new ReviewPointSpecial();
			rvwSps3[2] = rvwSps33;
			rvwSps33.setPryNo(3);
			rvwSps33.setPtTyp(reviewPointSpecial);
			rvwSps33.setRvwSpCd("30003");
			rvwSps33.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_POST);
			rvwSps33.setRvwSpSttTm("2012-02-15 00:00:00.000");
			rvwSps33.setRvwSpEdTm("2012-02-27 00:00:00.000");
			rvwSps33.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_TERM);
			ReviewPointSpecialDetail rvwSp33Detail1 = new ReviewPointSpecialDetail();
			rvwSp33Detail1.setRvwSpDtlSttThdDt("2012-02-15 00:00:00.000");
			rvwSp33Detail1.setRvwSpDtlEdThdDt("2012-02-19 23:59:59.000");
			rvwSp33Detail1.setRvwSpDtlPt(20L);
			rvwSp33Detail1.setRvwSpDtlTyp("D");
			ReviewPointSpecialDetail rvwSp33Detail2 = new ReviewPointSpecialDetail();
			rvwSp33Detail2.setRvwSpDtlSttThdDt("2012-02-20 00:00:00.000");
			rvwSp33Detail2.setRvwSpDtlEdThdDt("2012-02-26 23:59:59.000");
			rvwSp33Detail2.setRvwSpDtlPt(10L);
			rvwSp33Detail2.setRvwSpDtlTyp("D");
			rvwSps33.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSp33Detail1, rvwSp33Detail2});

			reviewPoint3.setRvwSps(rvwSps3);
			list.add(reviewPoint3);

			ReviewPoint reviewPoint4 = new ReviewPoint();
			reviewPoint4.setRvwQstCd(
					PointIncentiveType.IMMEDIATELY_AFTER_REVIEW.getCode());
			reviewPoint4.setRvwQstSttTm("2001-12-01 01:00:00.000");
			reviewPoint4.setRvwQstEdTm("2021-12-01 01:00:00.000");
			ReviewPointDetail[] rvwQstDtls4 = new ReviewPointDetail[1];
			ReviewPointDetail rvwQstDtl4 = new ReviewPointDetail();
			rvwQstDtl4.setRvwQstDtlSttTm("2001-12-01 01:00:00.000");
			rvwQstDtl4.setRvwQstDtlEdTm("2021-12-01 01:00:00.000");
			rvwQstDtl4.setRvwQstDtlSttThd(1);
			rvwQstDtl4.setRvwQstDtlEdThd(14);
			rvwQstDtl4.setRvwBasePt(7L);
			rvwQstDtls4[0] = rvwQstDtl4;
			reviewPoint4.setRvwQstDtls(rvwQstDtls4);
			list.add(reviewPoint4);

			ReviewPoint reviewPoint5 = new ReviewPoint();
			reviewPoint5.setRvwQstCd(
					PointIncentiveType.AFTER_FEW_DAYS_SATISFACTION.getCode());
			reviewPoint5.setRvwQstSttTm("2001-12-01 01:00:00.000");
			reviewPoint5.setRvwQstEdTm("2021-12-01 01:00:00.000");
			ReviewPointDetail[] rvwQstDtls5 = new ReviewPointDetail[5];
			ReviewPointDetail rvwQstDtl51 = new ReviewPointDetail();
			rvwQstDtl51.setRvwQstDtlSttTm("2001-12-01 01:00:00.000");
			rvwQstDtl51.setRvwQstDtlEdTm("2021-12-01 01:00:00.000");
			rvwQstDtl51.setRvwQstDtlSttThd(15);
			rvwQstDtl51.setRvwQstDtlEdThd(44);
			rvwQstDtl51.setRvwBasePt(6L);
			rvwQstDtls5[0] = rvwQstDtl51;
			ReviewPointDetail rvwQstDtl52 = new ReviewPointDetail();
			rvwQstDtl52.setRvwQstDtlSttTm("2001-12-01 01:00:00.000");
			rvwQstDtl52.setRvwQstDtlEdTm("2021-12-01 01:00:00.000");
			rvwQstDtl52.setRvwQstDtlSttThd(45);
			rvwQstDtl52.setRvwQstDtlEdThd(74);
			rvwQstDtl52.setRvwBasePt(5L);
			rvwQstDtls5[1] = rvwQstDtl52;
			ReviewPointDetail rvwQstDtl53 = new ReviewPointDetail();
			rvwQstDtl53.setRvwQstDtlSttTm("2001-12-01 01:00:00.000");
			rvwQstDtl53.setRvwQstDtlEdTm("2021-12-01 01:00:00.000");
			rvwQstDtl53.setRvwQstDtlSttThd(75);
			rvwQstDtl53.setRvwQstDtlEdThd(104);
			rvwQstDtl53.setRvwBasePt(4L);
			rvwQstDtls5[2] = rvwQstDtl53;
			ReviewPointDetail rvwQstDtl54 = new ReviewPointDetail();
			rvwQstDtl54.setRvwQstDtlSttTm("2001-12-01 01:00:00.000");
			rvwQstDtl54.setRvwQstDtlEdTm("2021-12-01 01:00:00.000");
			rvwQstDtl54.setRvwQstDtlSttThd(105);
			rvwQstDtl54.setRvwQstDtlEdThd(134);
			rvwQstDtl54.setRvwBasePt(3L);
			rvwQstDtls5[3] = rvwQstDtl54;
			ReviewPointDetail rvwQstDtl55 = new ReviewPointDetail();
			rvwQstDtl55.setRvwQstDtlSttTm("2001-12-01 01:00:00.000");
			rvwQstDtl55.setRvwQstDtlEdTm("2021-12-01 01:00:00.000");
			rvwQstDtl55.setRvwQstDtlSttThd(15);
			rvwQstDtl55.setRvwQstDtlEdThd(164);
			rvwQstDtl55.setRvwBasePt(2L);
			rvwQstDtls5[4] = rvwQstDtl55;
			reviewPoint5.setRvwQstDtls(rvwQstDtls5);
			list.add(reviewPoint5);

			ReviewPoint reviewPoint6 = new ReviewPoint();
			reviewPoint6.setRvwQstCd(
					PointIncentiveType.AFTER_FEW_DAYS_ALSO_BUY.getCode());
			reviewPoint6.setRvwQstSttTm("2001-12-01 01:00:00.000");
			reviewPoint6.setRvwQstEdTm("2021-12-01 01:00:00.000");
			reviewPoint6.setRvwQstDtls(rvwQstDtls5);
			list.add(reviewPoint6);

			ReviewPoint reviewPoint7 = new ReviewPoint();
			reviewPoint7.setRvwQstCd(
					PointIncentiveType.AFTER_FEW_DAYS_REVIEW.getCode());
			reviewPoint7.setRvwQstSttTm("2001-12-01 01:00:00.000");
			reviewPoint7.setRvwQstEdTm("2021-12-01 01:00:00.000");
			reviewPoint7.setRvwQstDtls(rvwQstDtls5);
			list.add(reviewPoint7);

			return list;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public ReviewPointSpecial[] getReplaceSpecialCondition() {
		ReviewPointSpecial[] rvwSps = new ReviewPointSpecial[1];
		ReviewPointSpecial rvwSp = new ReviewPointSpecial();
		rvwSps[0] = rvwSp;
		rvwSp.setPryNo(1);
		rvwSp.setPtTyp(ReviewPointSpecial.POINT_TYPE_REPLACE);
		rvwSp.setRvwSpCd("40001");
		rvwSp.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_PURCHASE);
		rvwSp.setRvwSpSttTm("2012-02-02 00:00:00.000");
		rvwSp.setRvwSpEdTm("2012-02-14 00:00:00.000");
		rvwSp.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_TERM);
		ReviewPointSpecialDetail rvwSp31Detail1 = new ReviewPointSpecialDetail();
		rvwSp31Detail1.setRvwSpDtlSttThdDt("2012-02-02 00:00:00.000");
		rvwSp31Detail1.setRvwSpDtlEdThdDt("2012-02-04 23:59:59.000");
		rvwSp31Detail1.setRvwSpDtlPt(1000L);
		rvwSp31Detail1.setRvwSpDtlTyp("D");
		ReviewPointSpecialDetail rvwSp31Detail2 = new ReviewPointSpecialDetail();
		rvwSp31Detail2.setRvwSpDtlSttThdDt("2012-02-05 00:00:00.000");
		rvwSp31Detail2.setRvwSpDtlEdThdDt("2012-02-14 00:00:00.000");
		rvwSp31Detail2.setRvwSpDtlPt(800L);
		rvwSp31Detail2.setRvwSpDtlTyp("D");
		rvwSp.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSp31Detail1, rvwSp31Detail2});
		return rvwSps;
	}

	public ReviewPointSpecial[] getAddSpecialCondition() {
		ReviewPointSpecial[] rvwSps = new ReviewPointSpecial[1];
		ReviewPointSpecial rvwSp = new ReviewPointSpecial();
		rvwSps[0] = rvwSp;
		rvwSp.setPryNo(1);
		rvwSp.setPtTyp(ReviewPointSpecial.POINT_TYPE_ADD);
		rvwSp.setRvwSpCd("50001");
		rvwSp.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_PURCHASE);
		rvwSp.setRvwSpSttTm("2012-02-02 00:00:00.000");
		rvwSp.setRvwSpEdTm("2012-02-14 00:00:00.000");
		rvwSp.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_TERM);
		ReviewPointSpecialDetail rvwSp31Detail1 = new ReviewPointSpecialDetail();
		rvwSp31Detail1.setRvwSpDtlSttThdDt("2012-02-02 00:00:00.000");
		rvwSp31Detail1.setRvwSpDtlEdThdDt("2012-02-04 23:59:59.000");
		rvwSp31Detail1.setRvwSpDtlPt(600L);
		rvwSp31Detail1.setRvwSpDtlTyp("D");
		ReviewPointSpecialDetail rvwSp31Detail2 = new ReviewPointSpecialDetail();
		rvwSp31Detail2.setRvwSpDtlSttThdDt("2012-02-05 00:00:00.000");
		rvwSp31Detail2.setRvwSpDtlEdThdDt("2012-02-14 00:00:00.000");
		rvwSp31Detail2.setRvwSpDtlPt(400L);
		rvwSp31Detail2.setRvwSpDtlTyp("D");
		rvwSp.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSp31Detail1, rvwSp31Detail2});
		return rvwSps;
	}


	@Override
	public void afterPropertiesSet() throws Exception {
		List<ReviewPoint> addList = new ArrayList<ReviewPoint>();
		addList.addAll(getReviewPoints(ReviewPointSpecial.POINT_TYPE_ADD));
		List<ReviewPoint> repList = new ArrayList<ReviewPoint>();
		repList.addAll(getReviewPoints(ReviewPointSpecial.POINT_TYPE_REPLACE));

		//通常商品
		ProductDO product1 = new ProductDO();
		product1.setSku("100000001000624829");
		product1.setJan("4905524312737");
		product1.setProductName("HVR-A1J [HDVカムコーダー]");
		product1.setMainImgs(new ProductImage[1]);
		product1.getMainImgs()[0] = new ProductImage();
		product1.getMainImgs()[0].setUrl("http://image.yodobashi.com/product/a/6526/100000001000624829/M000013591710002.jpg");
		product1.setProductDescription("<br><br><B>【365日ガッチリ補償！ヨドバシ・プレミアム】</b><br><a href=\"/ec/support/member/pointservice/gold/about/plus/premium/index.html\"target=_blank>持ち運ぶことの多いカメラ、万が一の破損や盗難も365日ワイドに補償！この機会にぜひご入会ください。</a>");
		product1.setRvwQsts(addList.toArray(new ReviewPoint[addList.size()]));
		product1.setRvwPtFlg(true);
		product1.setRvwInitPostTerm(14);
		product1.setRvwCntnPostCnt(5);
		product1.setRvwCntnPostTerm(30);
		product1.setRvwPtSttTm("2001-12-01 01:00:00.000");
		product1.setRvwPtEdTm("2021-12-01 01:00:00.000");
//		product1.setSlsPriceNum(10000L);
		product1.setRvwSps(getAddSpecialCondition());
		skuMap.put(product1.getSku(), product1);

		//エアコンなどのセット商品
		ProductDO product2 = new ProductDO();
		product2.setSku("200000002000012355");
		product2.setJan("4902530916232");
		product2.setProductName("RAS-M40A2-W [お掃除エアコン（14畳・200V対応） クリアホワイト 白くまくん Mシリーズ]");
		product2.setMainImgs(new ProductImage[1]);
		product2.getMainImgs()[0] = new ProductImage();
		product2.getMainImgs()[0].setUrl("http://image.yodobashi.com/product/a/1469/200000002000012355/M000073622710203.jpg");
		product2.setProductDescription("日立エアコン「M」シリーズは、キレイと快適が続く自動お掃除機能「クリーンシステム」を搭載。室内機高さ26cmとコンパクトで、カーテンレール上などでもスッキリと取り付けできます。");
		product2.setRvwQsts(addList.toArray(new ReviewPoint[addList.size()]));
		product2.setRvwPtFlg(true);
		product2.setRvwInitPostTerm(14);
		product2.setRvwCntnPostCnt(5);
		product2.setRvwCntnPostTerm(30);
		product2.setRvwPtSttTm("2001-12-01 01:00:00.000");
		product2.setRvwPtEdTm("2021-12-01 01:00:00.000");
//		product2.setSlsPriceNum(10000L);
		product2.setRvwSps(getReplaceSpecialCondition());
		skuMap.put(product2.getSku(), product2);

		//アダルトテスト用
		ProductDO product3 = new ProductDO();
		product3.setSku("100000009000738581");
		product3.setJan("4562215332070");
		product3.setProductName("アダルト商品");
		product3.setMainImgs(new ProductImage[1]);
		product3.getMainImgs()[0] = new ProductImage();
		product3.getMainImgs()[0].setUrl("http://image.yodobashi.com/product/a/5434/100000001001370503/M000075717110203.jpg");
//		product3.getMainImgs()[0].setUrl("http://image.yodobashi.com/product/a/5590/100000009000738581/M000075946310203.jpg");
		product3.setProductDescription("投稿はありません");
		product3.setAdult(true);
		product3.setRvwQsts(addList.toArray(new ReviewPoint[addList.size()]));
		product3.setRvwPtFlg(true);
		product3.setRvwInitPostTerm(14);
		product3.setRvwCntnPostCnt(5);
		product3.setRvwCntnPostTerm(30);
		product3.setRvwPtSttTm("2001-12-01 01:00:00.000");
		product3.setRvwPtEdTm("2021-12-01 01:00:00.000");
//		product3.setSlsPriceNum(10000L);
		skuMap.put(product3.getSku(), product3);

		//CEROテスト用
		ProductDO product4 = new ProductDO();
		product4.setSku("100000001001391026");
		product4.setJan("4988601007122");
		product4.setProductName("CERO商品");
		product4.setMainImgs(new ProductImage[1]);
		product4.getMainImgs()[0] = new ProductImage();
		product4.getMainImgs()[0].setUrl("http://image.yodobashi.com/product/a/5434/100000001001370503/M000075717110203.jpg");
		product4.setProductDescription("投稿はありません");
		product4.setCero(true);
		product4.setRvwQsts(addList.toArray(new ReviewPoint[addList.size()]));
		product4.setRvwPtFlg(true);
		product4.setRvwInitPostTerm(14);
		product4.setRvwCntnPostCnt(5);
		product4.setRvwCntnPostTerm(30);
		product4.setRvwPtSttTm("2001-12-01 01:00:00.000");
		product4.setRvwPtEdTm("2021-12-01 01:00:00.000");
//		product4.setSlsPriceNum(10000L);
		skuMap.put(product4.getSku(), product4);







		// ポイント付与テスト商品
		ProductDO product5 = new ProductDO();
		product5.setSku("100000001000827415");
		product5.setJan("4905524372564");
		product5.setProductName("ポイント付与フラグOFF");
		product5.setMainImgs(new ProductImage[1]);
		product5.getMainImgs()[0] = new ProductImage();
		product5.getMainImgs()[0].setUrl("http://image.yodobashi.com/product/a/5434/100000001001370503/M000075717110203.jpg");
		product5.setProductDescription("");
		product5.setCero(false);
		product5.setRvwQsts(addList.toArray(new ReviewPoint[addList.size()]));
		product5.setRvwPtFlg(false);
		product5.setRvwInitPostTerm(14);
		product5.setRvwCntnPostCnt(5);
		product5.setRvwCntnPostTerm(30);
		product5.setRvwPtSttTm("2001-12-01 01:00:00.000");
		product5.setRvwPtEdTm("2021-12-01 01:00:00.000");
//		product5.setSlsPriceNum(10000L);
		skuMap.put(product5.getSku(), product5);

		ProductDO product6 = new ProductDO();
		product6.setSku("100000001000937211");
		product6.setJan("4905524474978");
		product6.setProductName("ポイント付与フラグON マスタ不整合");
		product6.setMainImgs(new ProductImage[1]);
		product6.getMainImgs()[0] = new ProductImage();
		product6.getMainImgs()[0].setUrl("http://image.yodobashi.com/product/a/5434/100000001001370503/M000075717110203.jpg");
		product6.setProductDescription("");
		product6.setCero(false);
		product6.setRvwQsts(repList.toArray(new ReviewPoint[repList.size()]));
		product6.setRvwPtFlg(false);
		product6.setRvwInitPostTerm(14);
		product6.setRvwCntnPostCnt(5);
		product6.setRvwCntnPostTerm(30);
		product6.setRvwPtSttTm("2001-12-01 01:00:00.000");
		product6.setRvwPtEdTm("2021-12-01 01:00:00.000");
//		product6.setSlsPriceNum(10000L);
		skuMap.put(product6.getSku(), product6);

		ProductDO product7 = new ProductDO();
		product7.setSku("100000001000965304");
		product7.setJan("4905524485141");
		product7.setProductName("ポイント付与フラグON 期間切れ");
		product7.setMainImgs(new ProductImage[1]);
		product7.getMainImgs()[0] = new ProductImage();
		product7.getMainImgs()[0].setUrl("http://image.yodobashi.com/product/a/5434/100000001001370503/M000075717110203.jpg");
		product7.setProductDescription("");
		product7.setCero(false);
		product7.setRvwQsts(addList.toArray(new ReviewPoint[addList.size()]));
		product7.setRvwPtFlg(false);
		product7.setRvwInitPostTerm(14);
		product7.setRvwCntnPostCnt(5);
		product7.setRvwCntnPostTerm(30);
		product7.setRvwPtSttTm("2001-12-01 01:00:00.000");
		product7.setRvwPtEdTm("2021-12-01 01:00:00.000");
//		product7.setSlsPriceNum(10000L);
		skuMap.put(product7.getSku(), product7);

		ProductDO product8 = new ProductDO();
		product8.setSku("100000001001033690");
		product8.setJan("4905524545548");
		product8.setProductName("設問のみ設定");
		product8.setMainImgs(new ProductImage[1]);
		product8.getMainImgs()[0] = new ProductImage();
		product8.getMainImgs()[0].setUrl("http://image.yodobashi.com/product/a/5434/100000001001370503/M000075717110203.jpg");
		product8.setProductDescription("");
		product8.setCero(false);
		product8.setRvwQsts(getGrantBasicReviewPoints().toArray(new ReviewPoint[getGrantBasicReviewPoints().size()]));
		product8.setRvwPtFlg(true);
		product8.setRvwInitPostTerm(14);
		product8.setRvwCntnPostCnt(5);
		product8.setRvwCntnPostTerm(30);
		product8.setRvwPtSttTm("2001-12-01 01:00:00.000");
		product8.setRvwPtEdTm("2021-12-01 01:00:00.000");
//		product8.setSlsPriceNum(10000L);
		skuMap.put(product8.getSku(), product8);


		ProductDO product9 = new ProductDO();
		product9.setSku("100000001001074944");
		product9.setJan("4905524475029");
		product9.setProductName("設問 特別条件設定");
		product9.setMainImgs(new ProductImage[1]);
		product9.getMainImgs()[0] = new ProductImage();
		product9.getMainImgs()[0].setUrl("http://image.yodobashi.com/product/a/5434/100000001001370503/M000075717110203.jpg");
		product9.setProductDescription("");
		product9.setCero(false);
		product9.setRvwQsts(getGrantQestSpReviewPoints().toArray(new ReviewPoint[getGrantQestSpReviewPoints().size()]));
		product9.setRvwPtFlg(true);
		product9.setRvwInitPostTerm(14);
		product9.setRvwCntnPostCnt(5);
		product9.setRvwCntnPostTerm(30);
		product9.setRvwPtSttTm("2001-12-01 01:00:00.000");
		product9.setRvwPtEdTm("2021-12-01 01:00:00.000");
//		product9.setSlsPriceNum(10000L);
		skuMap.put(product9.getSku(), product9);

		ProductDO product10 = new ProductDO();
		product10.setSku("100000001001079012");
		product10.setJan("4905524551730");
		product10.setProductName("設問 特別条件設定 詳細条件");
		product10.setMainImgs(new ProductImage[1]);
		product10.getMainImgs()[0] = new ProductImage();
		product10.getMainImgs()[0].setUrl("http://image.yodobashi.com/product/a/5434/100000001001370503/M000075717110203.jpg");
		product10.setProductDescription("");
		product10.setCero(false);
		product10.setRvwQsts(getGrantQestSpReviewPoints2().toArray(new ReviewPoint[getGrantQestSpReviewPoints2().size()]));
		product10.setRvwPtFlg(true);
		product10.setRvwInitPostTerm(14);
		product10.setRvwCntnPostCnt(5);
		product10.setRvwCntnPostTerm(30);
		product10.setRvwPtSttTm("2001-12-01 01:00:00.000");
		product10.setRvwPtEdTm("2021-12-01 01:00:00.000");
//		product10.setSlsPriceNum(10000L);
		skuMap.put(product10.getSku(), product10);


		ProductDO product11 = new ProductDO();
		product11.setSku("100000001001188153");
		product11.setJan("4905524660579");
		product11.setProductName("設問 特別条件設定 詳細条件");
		product11.setMainImgs(new ProductImage[1]);
		product11.getMainImgs()[0] = new ProductImage();
		product11.getMainImgs()[0].setUrl("http://image.yodobashi.com/product/a/5434/100000001001370503/M000075717110203.jpg");
		product11.setProductDescription("");
		product11.setCero(false);
		product11.setRvwQsts(getGrantQestSpReviewPoints3().toArray(new ReviewPoint[getGrantQestSpReviewPoints3().size()]));
		product11.setRvwPtFlg(true);
		product11.setRvwInitPostTerm(14);
		product11.setRvwCntnPostCnt(5);
		product11.setRvwCntnPostTerm(30);
		product11.setRvwPtSttTm("2001-12-01 01:00:00.000");
		product11.setRvwPtEdTm("2021-12-01 01:00:00.000");
//		product11.setSlsPriceNum(10000L);
		skuMap.put(product11.getSku(), product11);


		ProductDO product12 = new ProductDO();
		product12.setSku("100000001001220335");
		product12.setJan("4960999665610");
		product12.setProductName("品目特別条件設定");
		product12.setMainImgs(new ProductImage[1]);
		product12.getMainImgs()[0] = new ProductImage();
		product12.getMainImgs()[0].setUrl("http://image.yodobashi.com/product/a/5434/100000001001370503/M000075717110203.jpg");
		product12.setProductDescription("");
		product12.setCero(false);
		product12.setRvwQsts(getGrantBasicReviewPoints().toArray(new ReviewPoint[getGrantBasicReviewPoints().size()]));
		product12.setRvwPtFlg(true);
		product12.setRvwInitPostTerm(14);
		product12.setRvwCntnPostCnt(5);
		product12.setRvwCntnPostTerm(30);
		product12.setRvwPtSttTm("2001-12-01 01:00:00.000");
		product12.setRvwPtEdTm("2021-12-01 01:00:00.000");
//		product12.setSlsPriceNum(10000L);
		product12.setRvwSps(getGrantItemSpReviewPoints());

		skuMap.put(product12.getSku(), product12);


		ProductDO product13 = new ProductDO();
		product13.setSku("100000001001325327");
		product13.setJan("4960999783123");
		product13.setProductName("品目特別条件設定");
		product13.setMainImgs(new ProductImage[1]);
		product13.getMainImgs()[0] = new ProductImage();
		product13.getMainImgs()[0].setUrl("http://image.yodobashi.com/product/a/5434/100000001001370503/M000075717110203.jpg");
		product13.setProductDescription("");
		product13.setCero(false);
		product13.setRvwQsts(getGrantBasicReviewPoints().toArray(new ReviewPoint[getGrantBasicReviewPoints().size()]));
		product13.setRvwPtFlg(true);
		product13.setRvwInitPostTerm(14);
		product13.setRvwCntnPostCnt(5);
		product13.setRvwCntnPostTerm(30);
		product13.setRvwPtSttTm("2001-12-01 01:00:00.000");
		product13.setRvwPtEdTm("2021-12-01 01:00:00.000");
//		product13.setSlsPriceNum(10000L);
		product13.setRvwSps(getGrantItemSpReviewPoints2());

		skuMap.put(product13.getSku(), product13);


		ProductDO product14 = new ProductDO();
		product14.setSku("100000001001363481");
		product14.setJan("4961607431665");
		product14.setProductName("品目特別条件設定");
		product14.setMainImgs(new ProductImage[1]);
		product14.getMainImgs()[0] = new ProductImage();
		product14.getMainImgs()[0].setUrl("http://image.yodobashi.com/product/a/5434/100000001001370503/M000075717110203.jpg");
		product14.setProductDescription("");
		product14.setCero(false);
		product14.setRvwQsts(getGrantBasicReviewPoints().toArray(new ReviewPoint[getGrantBasicReviewPoints().size()]));
		product14.setRvwPtFlg(true);
		product14.setRvwInitPostTerm(14);
		product14.setRvwCntnPostCnt(5);
		product14.setRvwCntnPostTerm(30);
		product14.setRvwPtSttTm("2001-12-01 01:00:00.000");
		product14.setRvwPtEdTm("2021-12-01 01:00:00.000");
//		product14.setSlsPriceNum(10000L);
		product14.setRvwSps(getGrantItemSpReviewPoints3());

		skuMap.put(product14.getSku(), product14);


		ProductDO product15 = new ProductDO();
		product15.setSku("100000001001237299");
		product15.setJan("4902530896275");
		product15.setProductName("品目特別条件設定");
		product15.setMainImgs(new ProductImage[1]);
		product15.getMainImgs()[0] = new ProductImage();
		product15.getMainImgs()[0].setUrl("http://image.yodobashi.com/product/a/5434/100000001001370503/M000075717110203.jpg");
		product15.setProductDescription("");
		product15.setCero(false);
		product15.setRvwQsts(getGrantBasicReviewPoints().toArray(new ReviewPoint[getGrantBasicReviewPoints().size()]));
		product15.setRvwPtFlg(true);
		product15.setRvwInitPostTerm(14);
		product15.setRvwCntnPostCnt(5);
		product15.setRvwCntnPostTerm(30);
		product15.setRvwPtSttTm("2001-12-01 01:00:00.000");
		product15.setRvwPtEdTm("2021-12-01 01:00:00.000");
//		product15.setSlsPriceNum(10000L);
		product15.setRvwSps(getGrantItemSpReviewPoints4());

		skuMap.put(product15.getSku(), product15);


		ProductDO product16 = new ProductDO();
		product16.setSku("100000001001377916");
		product16.setJan("4902530926705");
		product16.setProductName("品目特別条件設定");
		product16.setMainImgs(new ProductImage[1]);
		product16.getMainImgs()[0] = new ProductImage();
		product16.getMainImgs()[0].setUrl("http://image.yodobashi.com/product/a/5434/100000001001370503/M000075717110203.jpg");
		product16.setProductDescription("");
		product16.setCero(false);
		product16.setRvwQsts(getGrantBasicReviewPoints().toArray(new ReviewPoint[getGrantBasicReviewPoints().size()]));
		product16.setRvwPtFlg(true);
		product16.setRvwInitPostTerm(14);
		product16.setRvwCntnPostCnt(5);
		product16.setRvwCntnPostTerm(30);
		product16.setRvwPtSttTm("2001-12-01 01:00:00.000");
		product16.setRvwPtEdTm("2021-12-01 01:00:00.000");
//		product16.setSlsPriceNum(10000L);
		product16.setRvwSps(getGrantItemSpReviewPoints5());

		skuMap.put(product16.getSku(), product16);


		ProductDO product17 = new ProductDO();
		product17.setSku("100000001001377918");
		product17.setJan("4902530926729");
		product17.setProductName("品目特別条件設定");
		product17.setMainImgs(new ProductImage[1]);
		product17.getMainImgs()[0] = new ProductImage();
		product17.getMainImgs()[0].setUrl("http://image.yodobashi.com/product/a/5434/100000001001370503/M000075717110203.jpg");
		product17.setProductDescription("");
		product17.setCero(false);
		product17.setRvwQsts(getGrantBasicReviewPoints().toArray(new ReviewPoint[getGrantBasicReviewPoints().size()]));
		product17.setRvwPtFlg(true);
		product17.setRvwInitPostTerm(14);
		product17.setRvwCntnPostCnt(5);
		product17.setRvwCntnPostTerm(30);
		product17.setRvwPtSttTm("2001-12-01 01:00:00.000");
		product17.setRvwPtEdTm("2021-12-01 01:00:00.000");
//		product17.setSlsPriceNum(10000L);
		product17.setRvwSps(getGrantItemSpReviewPoints6());

		skuMap.put(product17.getSku(), product17);

		ProductDO product18 = new ProductDO();
		product18.setSku("100000001001274334");
		product18.setJan("4904550922736");
		product18.setProductName("品目特別条件設定");
		product18.setMainImgs(new ProductImage[1]);
		product18.getMainImgs()[0] = new ProductImage();
		product18.getMainImgs()[0].setUrl("http://image.yodobashi.com/product/a/5434/100000001001370503/M000075717110203.jpg");
		product18.setProductDescription("");
		product18.setCero(false);
		product18.setRvwQsts(getGrantBasicReviewPoints().toArray(new ReviewPoint[getGrantBasicReviewPoints().size()]));
		product18.setRvwPtFlg(true);
		product18.setRvwInitPostTerm(14);
		product18.setRvwCntnPostCnt(5);
		product18.setRvwCntnPostTerm(30);
		product18.setRvwPtSttTm("2001-12-01 01:00:00.000");
		product18.setRvwPtEdTm("2021-12-01 01:00:00.000");
//		product18.setSlsPriceNum(10000L);
		product18.setRvwSps(getGrantItemSpReviewPoints7());

		skuMap.put(product18.getSku(), product18);

	}

	@Override
	public void save(Collection<ProductDO> objects, Condition path) {
	}

	@Override
	public ProductDO load(String key, Condition path) {
		return null;
	}

	/**
	 * 指定した商品情報リストを返します。
	 * @param skus SKUリスト
	 * @param path パス
	 * @return 商品情報リスト
	 */
	@Override
	public Map<String, ProductDO> find(Collection<String> skus, Condition path) {
		Map<String, ProductDO> result = new HashMap<String, ProductDO>();
		for (String sku : skus) {
			ProductDO product = loadProduct(sku);
			if (product != null) {
				result.put(sku, product);
			}
		}
		return result;
	}

	@Override
	public void deleteByKeys(Collection<String> keys, Condition path) {
	}

	@Override
	public List<ProductDO> findByFk(String fkName, Object fkValue) {
		return null;
	}

	@Override
	public void deleteByFk(String fkName, Object fkValue) {
	}



	private List<ReviewPoint> getGrantBasicReviewPoints() {
		List<ReviewPoint> list = new ArrayList<ReviewPoint>();
		ReviewPoint reviewPointA001 = new ReviewPoint();
		ReviewPoint reviewPointA002 = new ReviewPoint();
		ReviewPoint reviewPointA003 = new ReviewPoint();
		ReviewPoint reviewPointA004 = new ReviewPoint();
		ReviewPoint reviewPointB001 = new ReviewPoint();
		ReviewPoint reviewPointB002 = new ReviewPoint();
		ReviewPoint reviewPointB003 = new ReviewPoint();

		reviewPointA001.setRvwQstCd(PointIncentiveType.IMMEDIATELY_AFTER_DECISIVE_PURCHASE.getCode());
		reviewPointA001.setRvwQstSttTm("2001-12-01 01:00:00.000");
		reviewPointA001.setRvwQstEdTm("2021-12-01 01:00:00.000");
		ReviewPointDetail[] rvwQstDtlsA001 = new ReviewPointDetail[1];
		ReviewPointDetail rvwQstDtlA001 = new ReviewPointDetail();
		rvwQstDtlA001.setRvwQstDtlSttTm("2001-12-01 01:00:00.000");
		rvwQstDtlA001.setRvwQstDtlEdTm("2021-12-01 01:00:00.000");
		rvwQstDtlA001.setRvwQstDtlSttThd(1);
		rvwQstDtlA001.setRvwQstDtlEdThd(165);
		rvwQstDtlA001.setRvwBasePt(1L);
		rvwQstDtlsA001[0] = rvwQstDtlA001;
		reviewPointA001.setRvwQstDtls(rvwQstDtlsA001);

		reviewPointA002.setRvwQstCd(PointIncentiveType.IMMEDIATELY_AFTER_PURCHASE_LOST_PRODUCT.getCode());
		reviewPointA002.setRvwQstSttTm("2001-12-01 01:00:00.000");
		reviewPointA002.setRvwQstEdTm("2021-12-01 01:00:00.000");

		ReviewPointDetail[] rvwQstDtlsA002 = new ReviewPointDetail[3];
		ReviewPointDetail rvwQstDtlA0021 = new ReviewPointDetail();
		rvwQstDtlA0021.setRvwQstDtlSttTm("2001-12-01 01:00:00.000");
		rvwQstDtlA0021.setRvwQstDtlEdTm("2021-12-01 01:00:00.000");
		rvwQstDtlA0021.setRvwQstDtlSttThd(1);
		rvwQstDtlA0021.setRvwQstDtlEdThd(165);
		rvwQstDtlA0021.setRvwBasePt(2L);
		rvwQstDtlsA002[0] = rvwQstDtlA0021;
		ReviewPointDetail rvwQstDtlA0022 = new ReviewPointDetail();
		rvwQstDtlA0022.setRvwQstDtlSttTm("2001-12-01 01:00:00.000");
		rvwQstDtlA0022.setRvwQstDtlEdTm("2021-12-01 01:00:00.000");
		rvwQstDtlA0022.setRvwQstDtlSttThd(1);
		rvwQstDtlA0022.setRvwQstDtlEdThd(165);
		rvwQstDtlA0022.setRvwBasePt(4L);
		rvwQstDtlsA002[1] = rvwQstDtlA0022;
		ReviewPointDetail rvwQstDtlA0023 = new ReviewPointDetail();
		rvwQstDtlA0023.setRvwQstDtlSttTm("2001-12-01 01:00:00.000");
		rvwQstDtlA0023.setRvwQstDtlEdTm("2021-12-01 01:00:00.000");
		rvwQstDtlA0023.setRvwQstDtlSttThd(1);
		rvwQstDtlA0023.setRvwQstDtlEdThd(165);
		rvwQstDtlA0023.setRvwBasePt(8L);
		rvwQstDtlsA002[2] = rvwQstDtlA0023;
		reviewPointA002.setRvwQstDtls(rvwQstDtlsA002);

		reviewPointA003.setRvwQstCd(PointIncentiveType.IMMEDIATELY_AFTER_USED_PRODUCT.getCode());
		reviewPointA003.setRvwQstSttTm("2001-12-01 01:00:00.000");
		reviewPointA003.setRvwQstEdTm("2021-12-01 01:00:00.000");
		ReviewPointDetail[] rvwQstDtlsA003 = new ReviewPointDetail[3];
		ReviewPointDetail rvwQstDtlA0031 = new ReviewPointDetail();
		rvwQstDtlA0031.setRvwQstDtlSttTm("2001-12-01 01:00:00.000");
		rvwQstDtlA0031.setRvwQstDtlEdTm("2001-12-01 01:00:00.000");
		rvwQstDtlA0031.setRvwQstDtlSttThd(1);
		rvwQstDtlA0031.setRvwQstDtlEdThd(165);
		rvwQstDtlA0031.setRvwBasePt(16L);
		rvwQstDtlsA003[0] = rvwQstDtlA0031;
		ReviewPointDetail rvwQstDtlA0032 = new ReviewPointDetail();
		rvwQstDtlA0032.setRvwQstDtlSttTm("2001-12-01 01:00:00.000");
		rvwQstDtlA0032.setRvwQstDtlEdTm("2012-12-01 01:00:00.000");
		rvwQstDtlA0032.setRvwQstDtlSttThd(1);
		rvwQstDtlA0032.setRvwQstDtlEdThd(165);
		rvwQstDtlA0032.setRvwBasePt(32L);
		rvwQstDtlsA003[1] = rvwQstDtlA0032;
		ReviewPointDetail rvwQstDtlA0033 = new ReviewPointDetail();
		rvwQstDtlA0033.setRvwQstDtlSttTm("2001-12-01 01:00:00.000");
		rvwQstDtlA0033.setRvwQstDtlEdTm("2012-12-01 01:00:00.000");
		rvwQstDtlA0033.setRvwQstDtlSttThd(1);
		rvwQstDtlA0033.setRvwQstDtlEdThd(165);
		rvwQstDtlA0033.setRvwBasePt(64L);
		rvwQstDtlsA003[2] = rvwQstDtlA0033;
		reviewPointA003.setRvwQstDtls(rvwQstDtlsA003);

		reviewPointA004.setRvwQstCd(PointIncentiveType.IMMEDIATELY_AFTER_REVIEW.getCode());
		reviewPointA004.setRvwQstSttTm("2001-12-01 01:00:00.000");
		reviewPointA004.setRvwQstEdTm("2021-12-01 01:00:00.000");
		ReviewPointDetail[] rvwQstDtlsA004 = new ReviewPointDetail[3];
		ReviewPointDetail rvwQstDtlA0041 = new ReviewPointDetail();
		rvwQstDtlA0041.setRvwQstDtlSttTm("2001-12-01 01:00:00.000");
		rvwQstDtlA0041.setRvwQstDtlEdTm("2012-12-01 01:00:00.000");
		rvwQstDtlA0041.setRvwQstDtlSttThd(1);
		rvwQstDtlA0041.setRvwQstDtlEdThd(165);
		rvwQstDtlA0041.setRvwBasePt(128L);
		rvwQstDtlsA004[0] = rvwQstDtlA0041;
		ReviewPointDetail rvwQstDtlA0042 = new ReviewPointDetail();
		rvwQstDtlA0042.setRvwQstDtlSttTm("2001-12-01 01:00:00.000");
		rvwQstDtlA0042.setRvwQstDtlEdTm("2001-12-01 01:00:00.000");
		rvwQstDtlA0042.setRvwQstDtlSttThd(1);
		rvwQstDtlA0042.setRvwQstDtlEdThd(165);
		rvwQstDtlA0042.setRvwBasePt(256L);
		rvwQstDtlsA004[1] = rvwQstDtlA0042;
		ReviewPointDetail rvwQstDtlA0043 = new ReviewPointDetail();
		rvwQstDtlA0043.setRvwQstDtlSttTm("2001-12-01 01:00:00.000");
		rvwQstDtlA0043.setRvwQstDtlEdTm("2012-12-01 01:00:00.000");
		rvwQstDtlA0043.setRvwQstDtlSttThd(1);
		rvwQstDtlA0043.setRvwQstDtlEdThd(165);
		rvwQstDtlA0043.setRvwBasePt(512L);
		rvwQstDtlsA004[2] = rvwQstDtlA0043;
		reviewPointA004.setRvwQstDtls(rvwQstDtlsA004);

		reviewPointB001.setRvwQstCd(PointIncentiveType.AFTER_FEW_DAYS_SATISFACTION.getCode());
		reviewPointB001.setRvwQstSttTm("2001-12-01 01:00:00.000");
		reviewPointB001.setRvwQstEdTm("2021-12-01 01:00:00.000");
		ReviewPointDetail[] rvwQstDtlsB001 = new ReviewPointDetail[3];
		ReviewPointDetail rvwQstDtlB0011 = new ReviewPointDetail();
		rvwQstDtlB0011.setRvwQstDtlSttTm("2001-12-01 01:00:00.000");
		rvwQstDtlB0011.setRvwQstDtlEdTm("2012-12-01 01:00:00.000");
		rvwQstDtlB0011.setRvwQstDtlSttThd(1);
		rvwQstDtlB0011.setRvwQstDtlEdThd(165);
		rvwQstDtlB0011.setRvwBasePt(1024L);
		rvwQstDtlsB001[0] = rvwQstDtlB0011;
		ReviewPointDetail rvwQstDtlB0012 = new ReviewPointDetail();
		rvwQstDtlB0012.setRvwQstDtlSttTm("2001-12-01 01:00:00.000");
		rvwQstDtlB0012.setRvwQstDtlEdTm("2012-12-01 01:00:00.000");
		rvwQstDtlB0012.setRvwQstDtlSttThd(1);
		rvwQstDtlB0012.setRvwQstDtlEdThd(165);
		rvwQstDtlB0012.setRvwBasePt(2048L);
		rvwQstDtlsB001[1] = rvwQstDtlB0012;
		ReviewPointDetail rvwQstDtlB0013 = new ReviewPointDetail();
		rvwQstDtlB0013.setRvwQstDtlSttTm("2001-12-01 01:00:00.000");
		rvwQstDtlB0013.setRvwQstDtlEdTm("2001-12-01 01:00:00.000");
		rvwQstDtlB0013.setRvwQstDtlSttThd(1);
		rvwQstDtlB0013.setRvwQstDtlEdThd(165);
		rvwQstDtlB0013.setRvwBasePt(4096L);
		rvwQstDtlsB001[2] = rvwQstDtlB0013;
		reviewPointB001.setRvwQstDtls(rvwQstDtlsB001);

		reviewPointB002.setRvwQstCd(PointIncentiveType.AFTER_FEW_DAYS_ALSO_BUY.getCode());
		reviewPointB002.setRvwQstSttTm("2001-12-01 01:00:00.000");
		reviewPointB002.setRvwQstEdTm("2021-12-01 01:00:00.000");
		ReviewPointDetail[] rvwQstDtlsB002 = new ReviewPointDetail[3];
		ReviewPointDetail rvwQstDtlB0021 = new ReviewPointDetail();
		rvwQstDtlB0021.setRvwQstDtlSttTm("2001-12-01 01:00:00.000");
		rvwQstDtlB0021.setRvwQstDtlEdTm("2001-12-01 01:00:00.000");
		rvwQstDtlB0021.setRvwQstDtlSttThd(1);
		rvwQstDtlB0021.setRvwQstDtlEdThd(165);
		rvwQstDtlB0021.setRvwBasePt(8192L);
		rvwQstDtlsB002[0] = rvwQstDtlB0021;
		ReviewPointDetail rvwQstDtlB0022 = new ReviewPointDetail();
		rvwQstDtlB0022.setRvwQstDtlSttTm("2001-12-01 01:00:00.000");
		rvwQstDtlB0022.setRvwQstDtlEdTm("2001-12-01 01:00:00.000");
		rvwQstDtlB0022.setRvwQstDtlSttThd(1);
		rvwQstDtlB0022.setRvwQstDtlEdThd(165);
		rvwQstDtlB0022.setRvwBasePt(16380L);
		rvwQstDtlsB002[1] = rvwQstDtlB0022;
		ReviewPointDetail rvwQstDtlB0023 = new ReviewPointDetail();
		rvwQstDtlB0023.setRvwQstDtlSttTm("2001-12-01 01:00:00.000");
		rvwQstDtlB0023.setRvwQstDtlEdTm("2001-12-01 01:00:00.000");
		rvwQstDtlB0023.setRvwQstDtlSttThd(1);
		rvwQstDtlB0023.setRvwQstDtlEdThd(165);
		rvwQstDtlB0023.setRvwBasePt(32768L);
		rvwQstDtlsB002[2] = rvwQstDtlB0023;
		reviewPointB002.setRvwQstDtls(rvwQstDtlsB002);

		reviewPointB003.setRvwQstCd(PointIncentiveType.AFTER_FEW_DAYS_REVIEW.getCode());
		reviewPointB003.setRvwQstSttTm("2001-12-01 01:00:00.000");
		reviewPointB003.setRvwQstEdTm("2021-12-01 01:00:00.000");

		ReviewPointDetail[] rvwQstDtlsB003 = new ReviewPointDetail[3];
		ReviewPointDetail rvwQstDtlB0031 = new ReviewPointDetail();
		rvwQstDtlB0031.setRvwQstDtlSttTm("2001-12-01 01:00:00.000");
		rvwQstDtlB0031.setRvwQstDtlEdTm("2021-12-01 01:00:00.000");
		rvwQstDtlB0031.setRvwQstDtlSttThd(1);
		rvwQstDtlB0031.setRvwQstDtlEdThd(14);
		rvwQstDtlB0031.setRvwBasePt(65536L);
		rvwQstDtlsB003[0] = rvwQstDtlB0031;
		ReviewPointDetail rvwQstDtlB0032 = new ReviewPointDetail();
		rvwQstDtlB0032.setRvwQstDtlSttTm("2001-12-01 01:00:00.000");
		rvwQstDtlB0032.setRvwQstDtlEdTm("2021-12-01 01:00:00.000");
		rvwQstDtlB0032.setRvwQstDtlSttThd(1);
		rvwQstDtlB0032.setRvwQstDtlEdThd(14);
		rvwQstDtlB0032.setRvwBasePt(131072L);
		rvwQstDtlsB003[1] = rvwQstDtlB0022;
		ReviewPointDetail rvwQstDtlB0033 = new ReviewPointDetail();
		rvwQstDtlB0033.setRvwQstDtlSttTm("2001-12-01 01:00:00.000");
		rvwQstDtlB0033.setRvwQstDtlEdTm("2021-12-01 01:00:00.000");
		rvwQstDtlB0033.setRvwQstDtlSttThd(1);
		rvwQstDtlB0033.setRvwQstDtlEdThd(165);
		rvwQstDtlB0033.setRvwBasePt(262144L);
		rvwQstDtlsB003[2] = rvwQstDtlB0033;
		reviewPointB003.setRvwQstDtls(rvwQstDtlsB003);

		list.add(reviewPointA001);
		list.add(reviewPointA002);
		list.add(reviewPointA003);
		list.add(reviewPointA004);
		list.add(reviewPointB001);
		list.add(reviewPointB002);
		list.add(reviewPointB003);

		return list;
	}



	private List<ReviewPoint> getGrantQestSpReviewPoints() {
		List<ReviewPoint> list = getGrantBasicReviewPoints();
		ReviewPoint reviewPointA001 = list.get(0);
		ReviewPoint reviewPointA002 = list.get(1);
		ReviewPoint reviewPointA003 = list.get(2);
		ReviewPoint reviewPointA004 = list.get(3);
		ReviewPoint reviewPointB001 = list.get(4);
		ReviewPoint reviewPointB002 = list.get(5);
		ReviewPoint reviewPointB003 = list.get(6);

		// A001-----------------------------
		ReviewPointSpecial[] rvwSpsA001 = new ReviewPointSpecial[1];
		ReviewPointSpecial rvwSpsA0011 = new ReviewPointSpecial();
		rvwSpsA001[0] = rvwSpsA0011;
		rvwSpsA0011.setPryNo(1);
		rvwSpsA0011.setPtTyp(ReviewPointSpecial.POINT_TYPE_ADD);
		rvwSpsA0011.setRvwSpCd("1000000001");
		rvwSpsA0011.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_PURCHASE);
		rvwSpsA0011.setRvwSpSttTm("2001-02-01 00:00:00.000");
		rvwSpsA0011.setRvwSpEdTm("2021-12-31 00:00:00.000");
		rvwSpsA0011.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_TERM);
		ReviewPointSpecialDetail rvwSpA0011Detail = new ReviewPointSpecialDetail();
		rvwSpA0011Detail.setRvwSpDtlSttThdDt("2012-01-01 00:00:00.000");
		rvwSpA0011Detail.setRvwSpDtlEdThdDt("2012-12-31 23:59:59.000");
		rvwSpA0011Detail.setRvwSpDtlPt(1L);
		rvwSpA0011Detail.setRvwSpDtlTyp("D");
		rvwSpsA0011.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSpA0011Detail});
		reviewPointA001.setRvwSps(rvwSpsA001);

		// A002-----------------------------
		ReviewPointSpecial[] rvwSpsA002 = new ReviewPointSpecial[3];
		ReviewPointSpecial rvwSpsA0021 = new ReviewPointSpecial();
		rvwSpsA002[0] = rvwSpsA0021;
		rvwSpsA0021.setPryNo(1);
		rvwSpsA0021.setPtTyp(ReviewPointSpecial.POINT_TYPE_REPLACE);
		rvwSpsA0021.setRvwSpCd("1000000002");
		rvwSpsA0021.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_ORDER);
		rvwSpsA0021.setRvwSpSttTm("2001-02-01 00:00:00.000");
		rvwSpsA0021.setRvwSpEdTm("2021-12-31 00:00:00.000");
		rvwSpsA0021.setRvwSpTyp(ReviewPointSpecial.POINT_TYPE_ADD);
		ReviewPointSpecialDetail rvwSpA0021Detail = new ReviewPointSpecialDetail();
		rvwSpA0021Detail.setRvwSpDtlSttThdDt("2012-01-01 00:00:00.000");
		rvwSpA0021Detail.setRvwSpDtlEdThdDt("2012-12-31 23:59:59.000");
		rvwSpA0021Detail.setRvwSpDtlPt(16777216L);
		rvwSpA0021Detail.setRvwSpDtlTyp("D");
		rvwSpsA0021.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSpA0021Detail});

		ReviewPointSpecial rvwSpsA0022 = new ReviewPointSpecial();
		rvwSpsA002[1] = rvwSpsA0022;
		rvwSpsA0022.setPryNo(2);
		rvwSpsA0022.setPtTyp(ReviewPointSpecial.POINT_TYPE_REPLACE);
		rvwSpsA0022.setRvwSpCd("1000000003");
		rvwSpsA0022.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_ORDER);
		rvwSpsA0022.setRvwSpSttTm("2001-02-01 00:00:00.000");
		rvwSpsA0022.setRvwSpEdTm("2021-12-31 00:00:00.000");
		rvwSpsA0022.setRvwSpTyp(ReviewPointSpecial.POINT_TYPE_ADD);
		ReviewPointSpecialDetail rvwSpA002Detail = new ReviewPointSpecialDetail();
		rvwSpA002Detail.setRvwSpDtlSttThdDt("2012-01-01 00:00:00.000");
		rvwSpA002Detail.setRvwSpDtlEdThdDt("2012-12-31 23:59:59.000");
		rvwSpA002Detail.setRvwSpDtlPt(33554432L);
		rvwSpA002Detail.setRvwSpDtlTyp("D");
		rvwSpsA0022.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSpA002Detail});

		ReviewPointSpecial rvwSpsA0023 = new ReviewPointSpecial();
		rvwSpsA002[2] = rvwSpsA0023;
		rvwSpsA0023.setPryNo(3);
		rvwSpsA0023.setPtTyp(ReviewPointSpecial.POINT_TYPE_REPLACE);
		rvwSpsA0023.setRvwSpCd("1000000004");
		rvwSpsA0023.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_ORDER);
		rvwSpsA0023.setRvwSpSttTm("2001-02-01 00:00:00.000");
		rvwSpsA0023.setRvwSpEdTm("2021-12-31 00:00:00.000");
		rvwSpsA0023.setRvwSpTyp(ReviewPointSpecial.POINT_TYPE_ADD);
		ReviewPointSpecialDetail rvwSpA0023Detail = new ReviewPointSpecialDetail();
		rvwSpA0023Detail.setRvwSpDtlSttThdDt("2012-01-01 00:00:00.000");
		rvwSpA0023Detail.setRvwSpDtlEdThdDt("2012-12-31 23:59:59.000");
		rvwSpA0023Detail.setRvwSpDtlPt(67108864L);
		rvwSpA0023Detail.setRvwSpDtlTyp("D");
		rvwSpsA0023.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSpA0023Detail});
		reviewPointA002.setRvwSps(rvwSpsA002);

		// A003-----------------------------
		ReviewPointSpecial[] rvwSpsA003 = new ReviewPointSpecial[3];
		ReviewPointSpecial rvwSpsA0031 = new ReviewPointSpecial();
		rvwSpsA003[0] = rvwSpsA0031;
		rvwSpsA0031.setPryNo(1);
		rvwSpsA0031.setPtTyp(ReviewPointSpecial.POINT_TYPE_ADD);
		rvwSpsA0031.setRvwSpCd("1000000005");
		rvwSpsA0031.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_POST);
		rvwSpsA0031.setRvwSpSttTm("2001-02-01 00:00:00.000");
		rvwSpsA0031.setRvwSpEdTm("2001-12-31 00:00:00.000");
		rvwSpsA0031.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_TERM);
		ReviewPointSpecialDetail rvwSpA0031Detail = new ReviewPointSpecialDetail();
		rvwSpA0031Detail.setRvwSpDtlSttThdDt("2012-01-01 00:00:00.000");
		rvwSpA0031Detail.setRvwSpDtlEdThdDt("2012-01-01 23:59:59.000");
		rvwSpA0031Detail.setRvwSpDtlPt(16L);
		rvwSpA0031Detail.setRvwSpDtlTyp("D");
		rvwSpsA0031.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSpA0031Detail});

		ReviewPointSpecial rvwSpsA0032 = new ReviewPointSpecial();
		rvwSpsA003[1] = rvwSpsA0032;
		rvwSpsA0032.setPryNo(2);
		rvwSpsA0032.setPtTyp(ReviewPointSpecial.POINT_TYPE_ADD);
		rvwSpsA0032.setRvwSpCd("1000000006");
		rvwSpsA0032.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_POST);
		rvwSpsA0032.setRvwSpSttTm("2001-02-01 00:00:00.000");
		rvwSpsA0032.setRvwSpEdTm("2021-12-31 00:00:00.000");
		rvwSpsA0032.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_TERM);
		ReviewPointSpecialDetail rvwSpA0032Detail = new ReviewPointSpecialDetail();
		rvwSpA0032Detail.setRvwSpDtlSttThdDt("2012-01-01 00:00:00.000");
		rvwSpA0032Detail.setRvwSpDtlEdThdDt("2012-12-31 23:59:59.000");
		rvwSpA0032Detail.setRvwSpDtlPt(32L);
		rvwSpA0032Detail.setRvwSpDtlTyp("D");
		rvwSpsA0032.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSpA0032Detail});

		ReviewPointSpecial rvwSpsA0033 = new ReviewPointSpecial();
		rvwSpsA003[2] = rvwSpsA0033;
		rvwSpsA0033.setPryNo(3);
		rvwSpsA0033.setPtTyp(ReviewPointSpecial.POINT_TYPE_ADD);
		rvwSpsA0033.setRvwSpCd("1000000007");
		rvwSpsA0033.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_POST);
		rvwSpsA0033.setRvwSpSttTm("2001-02-01 00:00:00.000");
		rvwSpsA0033.setRvwSpEdTm("2021-12-31 00:00:00.000");
		rvwSpsA0033.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_TERM);
		ReviewPointSpecialDetail rvwSpA0033Detail = new ReviewPointSpecialDetail();
		rvwSpA0033Detail.setRvwSpDtlSttThdDt("2012-01-01 00:00:00.000");
		rvwSpA0033Detail.setRvwSpDtlEdThdDt("2012-12-31 23:59:59.000");
		rvwSpA0033Detail.setRvwSpDtlPt(64L);
		rvwSpA0033Detail.setRvwSpDtlTyp("D");
		rvwSpsA0033.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSpA0033Detail});
		reviewPointA003.setRvwSps(rvwSpsA003);

		// A004-----------------------------
		ReviewPointSpecial[] rvwSpsA004 = new ReviewPointSpecial[3];
		ReviewPointSpecial rvwSpsA0041 = new ReviewPointSpecial();
		rvwSpsA004[0] = rvwSpsA0041;
		rvwSpsA0041.setPryNo(1);
		rvwSpsA0041.setPtTyp(ReviewPointSpecial.POINT_TYPE_REPLACE);
		rvwSpsA0041.setRvwSpCd("1000000008");
		rvwSpsA0041.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_ORDER);
		rvwSpsA0041.setRvwSpSttTm("2001-02-01 00:00:00.000");
		rvwSpsA0041.setRvwSpEdTm("2021-12-31 00:00:00.000");
		rvwSpsA0041.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_FIRST);
		ReviewPointSpecialDetail rvwSpA0041Detail = new ReviewPointSpecialDetail();
		rvwSpA0041Detail.setRvwSpDtlSttThdNum(1);
		rvwSpA0041Detail.setRvwSpDtlEdThdNum(10000);
		rvwSpA0041Detail.setRvwSpDtlPt(134217728L);
		rvwSpA0041Detail.setRvwSpDtlTyp("N");
		rvwSpsA0041.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSpA0041Detail});

		ReviewPointSpecial rvwSpsA0042 = new ReviewPointSpecial();
		rvwSpsA004[1] = rvwSpsA0042;
		rvwSpsA0042.setPryNo(2);
		rvwSpsA0042.setPtTyp(ReviewPointSpecial.POINT_TYPE_REPLACE);
		rvwSpsA0042.setRvwSpCd("1000000009");
		rvwSpsA0042.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_ORDER);
		rvwSpsA0042.setRvwSpSttTm("2001-02-01 00:00:00.000");
		rvwSpsA0042.setRvwSpEdTm("2001-02-01 00:00:00.000");
		rvwSpsA0042.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_FIRST);
		ReviewPointSpecialDetail rvwSpA0042Detail = new ReviewPointSpecialDetail();
		rvwSpA0042Detail.setRvwSpDtlSttThdNum(1);
		rvwSpA0042Detail.setRvwSpDtlEdThdNum(10000);
		rvwSpA0042Detail.setRvwSpDtlPt(268435456L);
		rvwSpA0042Detail.setRvwSpDtlTyp("N");
		rvwSpsA0042.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSpA0042Detail});

		ReviewPointSpecial rvwSpsA0043 = new ReviewPointSpecial();
		rvwSpsA004[2] = rvwSpsA0043;
		rvwSpsA0043.setPryNo(3);
		rvwSpsA0043.setPtTyp(ReviewPointSpecial.POINT_TYPE_REPLACE);
		rvwSpsA0043.setRvwSpCd("1000000010");
		rvwSpsA0043.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_ORDER);
		rvwSpsA0043.setRvwSpSttTm("2001-02-01 00:00:00.000");
		rvwSpsA0043.setRvwSpEdTm("2021-12-31 00:00:00.000");
		rvwSpsA0043.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_FIRST);
		ReviewPointSpecialDetail rvwSpA0043Detail = new ReviewPointSpecialDetail();
		rvwSpA0043Detail.setRvwSpDtlSttThdNum(1);
		rvwSpA0043Detail.setRvwSpDtlEdThdNum(10000);
		rvwSpA0043Detail.setRvwSpDtlPt(536870912L);
		rvwSpA0043Detail.setRvwSpDtlTyp("N");
		rvwSpsA0043.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSpA0043Detail});
		reviewPointA004.setRvwSps(rvwSpsA004);


		// B001-----------------------------
		ReviewPointSpecial[] rvwSpsB001 = new ReviewPointSpecial[3];
		ReviewPointSpecial rvwSpsB0011 = new ReviewPointSpecial();
		rvwSpsB001[0] = rvwSpsB0011;
		rvwSpsB0011.setPryNo(1);
		rvwSpsB0011.setPtTyp(ReviewPointSpecial.POINT_TYPE_ADD);
		rvwSpsB0011.setRvwSpCd("1000000011");
		rvwSpsB0011.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_ORDER);
		rvwSpsB0011.setRvwSpSttTm("2001-02-01 00:00:00.000");
		rvwSpsB0011.setRvwSpEdTm("2021-12-31 00:00:00.000");
		rvwSpsB0011.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_FIRST);
		ReviewPointSpecialDetail rvwSpB0011Detail = new ReviewPointSpecialDetail();
		rvwSpB0011Detail.setRvwSpDtlSttThdNum(1);
		rvwSpB0011Detail.setRvwSpDtlEdThdNum(10000);
		rvwSpB0011Detail.setRvwSpDtlPt(1024L);
		rvwSpB0011Detail.setRvwSpDtlTyp("N");
		rvwSpsB0011.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSpB0011Detail});

		ReviewPointSpecial rvwSpsB0012 = new ReviewPointSpecial();
		rvwSpsB001[1] = rvwSpsB0012;
		rvwSpsB0012.setPryNo(2);
		rvwSpsB0012.setPtTyp(ReviewPointSpecial.POINT_TYPE_ADD);
		rvwSpsB0012.setRvwSpCd("1000000012");
		rvwSpsB0012.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_ORDER);
		rvwSpsB0012.setRvwSpSttTm("2001-02-01 00:00:00.000");
		rvwSpsB0012.setRvwSpEdTm("2021-12-31 00:00:00.000");
		rvwSpsB0012.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_FIRST);
		ReviewPointSpecialDetail rvwSpB0012Detail = new ReviewPointSpecialDetail();
		rvwSpB0012Detail.setRvwSpDtlSttThdNum(1);
		rvwSpB0012Detail.setRvwSpDtlEdThdNum(10000);
		rvwSpB0012Detail.setRvwSpDtlPt(2048L);
		rvwSpB0012Detail.setRvwSpDtlTyp("N");
		rvwSpsB0012.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSpB0012Detail});

		ReviewPointSpecial rvwSpsB0013 = new ReviewPointSpecial();
		rvwSpsB001[2] = rvwSpsB0013;
		rvwSpsB0013.setPryNo(3);
		rvwSpsB0013.setPtTyp(ReviewPointSpecial.POINT_TYPE_ADD);
		rvwSpsB0013.setRvwSpCd("1000000013");
		rvwSpsB0013.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_ORDER);
		rvwSpsB0013.setRvwSpSttTm("2001-02-01 00:00:00.000");
		rvwSpsB0013.setRvwSpEdTm("2001-01-31 00:00:00.000");
		rvwSpsB0013.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_FIRST);
		ReviewPointSpecialDetail rvwSpB0013Detail = new ReviewPointSpecialDetail();
		rvwSpB0013Detail.setRvwSpDtlSttThdNum(1);
		rvwSpB0013Detail.setRvwSpDtlEdThdNum(10000);
		rvwSpB0013Detail.setRvwSpDtlPt(4096L);
		rvwSpB0013Detail.setRvwSpDtlTyp("N");
		rvwSpsB0013.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSpB0013Detail});
		reviewPointB001.setRvwSps(rvwSpsB001);

		// B002-----------------------------
		ReviewPointSpecial[] rvwSpsB002 = new ReviewPointSpecial[3];
		ReviewPointSpecial rvwSpsB0021 = new ReviewPointSpecial();
		rvwSpsB002[0] = rvwSpsB0021;
		rvwSpsB0021.setPryNo(1);
		rvwSpsB0021.setPtTyp(ReviewPointSpecial.POINT_TYPE_REPLACE);
		rvwSpsB0021.setRvwSpCd("1000000014");
		rvwSpsB0021.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_POST);
		rvwSpsB0021.setRvwSpSttTm("2001-02-01 00:00:00.000");
		rvwSpsB0021.setRvwSpEdTm("2001-12-31 00:00:00.000");
		rvwSpsB0021.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_FIRST);
		ReviewPointSpecialDetail rvwSpB0021Detail = new ReviewPointSpecialDetail();
		rvwSpB0021Detail.setRvwSpDtlSttThdNum(1);
		rvwSpB0021Detail.setRvwSpDtlEdThdNum(10000);
		rvwSpB0021Detail.setRvwSpDtlPt(1073741824L);
		rvwSpB0021Detail.setRvwSpDtlTyp("N");
		rvwSpsB0021.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSpB0021Detail});

		ReviewPointSpecial rvwSpsB0022 = new ReviewPointSpecial();
		rvwSpsB002[1] = rvwSpsB0022;
		rvwSpsB0022.setPryNo(2);
		rvwSpsB0022.setPtTyp(ReviewPointSpecial.POINT_TYPE_REPLACE);
		rvwSpsB0022.setRvwSpCd("1000000015");
		rvwSpsB0022.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_POST);
		rvwSpsB0022.setRvwSpSttTm("2001-02-01 00:00:00.000");
		rvwSpsB0022.setRvwSpEdTm("2001-12-31 00:00:00.000");
		rvwSpsB0022.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_FIRST);
		ReviewPointSpecialDetail rvwSpB0022Detail = new ReviewPointSpecialDetail();
		rvwSpB0022Detail.setRvwSpDtlSttThdNum(1);
		rvwSpB0022Detail.setRvwSpDtlEdThdNum(10000);
		rvwSpB0022Detail.setRvwSpDtlPt(2147483648L);
		rvwSpB0022Detail.setRvwSpDtlTyp("N");
		rvwSpsB0022.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSpB0022Detail});

		ReviewPointSpecial rvwSpsB0023 = new ReviewPointSpecial();
		rvwSpsB002[2] = rvwSpsB0023;
		rvwSpsB0023.setPryNo(3);
		rvwSpsB0023.setPtTyp(ReviewPointSpecial.POINT_TYPE_REPLACE);
		rvwSpsB0023.setRvwSpCd("1000000016");
		rvwSpsB0023.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_POST);
		rvwSpsB0023.setRvwSpSttTm("2001-02-01 00:00:00.000");
		rvwSpsB0023.setRvwSpEdTm("2001-01-31 00:00:00.000");
		rvwSpsB0023.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_FIRST);
		ReviewPointSpecialDetail rvwSpB0023Detail = new ReviewPointSpecialDetail();
		rvwSpB0023Detail.setRvwSpDtlSttThdNum(1);
		rvwSpB0023Detail.setRvwSpDtlEdThdNum(10000);
		rvwSpB0023Detail.setRvwSpDtlPt(4294967296L);
		rvwSpB0023Detail.setRvwSpDtlTyp("N");
		rvwSpsB0023.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSpB0023Detail});
		reviewPointB002.setRvwSps(rvwSpsB002);

		// B003-----------------------------
		ReviewPointSpecial[] rvwSpsB003 = new ReviewPointSpecial[3];
		ReviewPointSpecial rvwSpsB0031 = new ReviewPointSpecial();
		rvwSpsB003[0] = rvwSpsB0031;
		rvwSpsB0031.setPryNo(1);
		rvwSpsB0031.setPtTyp(ReviewPointSpecial.POINT_TYPE_ADD);
		rvwSpsB0031.setRvwSpCd("1000000017");
		rvwSpsB0031.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_PURCHASE);
		rvwSpsB0031.setRvwSpSttTm("2001-02-01 00:00:00.000");
		rvwSpsB0031.setRvwSpEdTm("2001-12-31 00:00:00.000");
		rvwSpsB0031.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_FIRST);
		ReviewPointSpecialDetail rvwSpB0031Detail = new ReviewPointSpecialDetail();
		rvwSpB0031Detail.setRvwSpDtlSttThdNum(1);
		rvwSpB0031Detail.setRvwSpDtlEdThdNum(10000);
		rvwSpB0031Detail.setRvwSpDtlPt(65536L);
		rvwSpB0031Detail.setRvwSpDtlTyp("N");
		rvwSpsB0031.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSpB0031Detail});

		ReviewPointSpecial rvwSpsB0032 = new ReviewPointSpecial();
		rvwSpsB003[1] = rvwSpsB0032;
		rvwSpsB0032.setPryNo(2);
		rvwSpsB0032.setPtTyp(ReviewPointSpecial.POINT_TYPE_ADD);
		rvwSpsB0032.setRvwSpCd("1000000018");
		rvwSpsB0032.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_POST);
		rvwSpsB0032.setRvwSpSttTm("2001-02-01 00:00:00.000");
		rvwSpsB0032.setRvwSpEdTm("2001-12-31 00:00:00.000");
		rvwSpsB0032.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_FIRST);
		ReviewPointSpecialDetail rvwSpB0032Detail = new ReviewPointSpecialDetail();
		rvwSpB0032Detail.setRvwSpDtlSttThdNum(1);
		rvwSpB0032Detail.setRvwSpDtlEdThdNum(10000);
		rvwSpB0032Detail.setRvwSpDtlPt(131072L);
		rvwSpB0032Detail.setRvwSpDtlTyp("N");
		rvwSpsB0032.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSpB0032Detail});

		ReviewPointSpecial rvwSpsB0033 = new ReviewPointSpecial();
		rvwSpsB003[2] = rvwSpsB0033;
		rvwSpsB0033.setPryNo(3);
		rvwSpsB0033.setPtTyp(ReviewPointSpecial.POINT_TYPE_ADD);
		rvwSpsB0033.setRvwSpCd("1000000019");
		rvwSpsB0033.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_ORDER);
		rvwSpsB0033.setRvwSpSttTm("2001-02-01 00:00:00.000");
		rvwSpsB0033.setRvwSpEdTm("2012-01-31 00:00:00.000");
		rvwSpsB0033.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_FIRST);
		ReviewPointSpecialDetail rvwSpB0033Detail = new ReviewPointSpecialDetail();
		rvwSpB0033Detail.setRvwSpDtlSttThdNum(1);
		rvwSpB0033Detail.setRvwSpDtlEdThdNum(10000);
		rvwSpB0033Detail.setRvwSpDtlPt(262144L);
		rvwSpB0033Detail.setRvwSpDtlTyp("N");
		rvwSpsB0033.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSpB0033Detail});
		reviewPointB003.setRvwSps(rvwSpsB003);





		List<ReviewPoint> ret  = new ArrayList<ReviewPoint>();
		ret.add(reviewPointA001);
		ret.add(reviewPointA002);
		ret.add(reviewPointA003);
		ret.add(reviewPointA004);
		ret.add(reviewPointB001);
		ret.add(reviewPointB002);
		ret.add(reviewPointB003);
		return ret;
	}


	private List<ReviewPoint> getGrantQestSpReviewPoints2() {
		List<ReviewPoint> list = getGrantBasicReviewPoints();
		ReviewPoint reviewPointA001 = list.get(0);
		ReviewPoint reviewPointA002 = list.get(1);
		ReviewPoint reviewPointA003 = list.get(2);
		ReviewPoint reviewPointA004 = list.get(3);

		ReviewPoint reviewPointB001 = list.get(4);
		ReviewPoint reviewPointB002 = list.get(5);
		ReviewPoint reviewPointB003 = list.get(6);



		// A001-----------------------------
		ReviewPointSpecial[] rvwSpsA001 = new ReviewPointSpecial[1];
		ReviewPointSpecial rvwSpsA0011 = new ReviewPointSpecial();
		rvwSpsA001[0] = rvwSpsA0011;
		rvwSpsA0011.setPryNo(1);
		rvwSpsA0011.setPtTyp(ReviewPointSpecial.POINT_TYPE_ADD);
		rvwSpsA0011.setRvwSpCd("1000000020");
		rvwSpsA0011.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_PURCHASE);
		rvwSpsA0011.setRvwSpSttTm("2001-02-01 00:00:00.000");
		rvwSpsA0011.setRvwSpEdTm("2021-12-31 00:00:00.000");
		rvwSpsA0011.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_TERM);
		ReviewPointSpecialDetail rvwSpA0011Detail = new ReviewPointSpecialDetail();
		rvwSpA0011Detail.setRvwSpDtlSttThdDt("2012-01-01 00:00:00.000");
		rvwSpA0011Detail.setRvwSpDtlEdThdDt("2012-12-31 23:59:59.000");
		rvwSpA0011Detail.setRvwSpDtlPt(1L);
		rvwSpA0011Detail.setRvwSpDtlTyp("D");
		rvwSpsA0011.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSpA0011Detail});
		reviewPointA001.setRvwSps(rvwSpsA001);

		// A002-----------------------------
		ReviewPointSpecial[] rvwSpsA002 = new ReviewPointSpecial[1];
		ReviewPointSpecial rvwSpsA0021 = new ReviewPointSpecial();
		rvwSpsA002[0] = rvwSpsA0021;
		rvwSpsA0021.setPryNo(1);
		rvwSpsA0021.setPtTyp(ReviewPointSpecial.POINT_TYPE_REPLACE);
		rvwSpsA0021.setRvwSpCd("1000000021");
		rvwSpsA0021.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_ORDER);
		rvwSpsA0021.setRvwSpSttTm("2001-02-01 00:00:00.000");
		rvwSpsA0021.setRvwSpEdTm("2021-12-31 00:00:00.000");
		rvwSpsA0021.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_TERM);

		ReviewPointSpecialDetail rvwSpA0021Detail = new ReviewPointSpecialDetail();
		rvwSpA0021Detail.setRvwSpDtlSttThdDt("2012-01-01 00:00:00.000");
		rvwSpA0021Detail.setRvwSpDtlEdThdDt("2012-12-31 23:59:59.000");
		rvwSpA0021Detail.setRvwSpDtlPt(16777216L);
		rvwSpA0021Detail.setRvwSpDtlTyp("D");

		ReviewPointSpecialDetail rvwSpA0022Detail = new ReviewPointSpecialDetail();
		rvwSpA0022Detail.setRvwSpDtlSttThdDt("2012-01-01 00:00:00.000");
		rvwSpA0022Detail.setRvwSpDtlEdThdDt("2012-12-31 23:59:59.000");
		rvwSpA0022Detail.setRvwSpDtlPt(33554432L);
		rvwSpA0022Detail.setRvwSpDtlTyp("D");

		ReviewPointSpecialDetail rvwSpA0023Detail = new ReviewPointSpecialDetail();
		rvwSpA0023Detail.setRvwSpDtlSttThdDt("2012-01-01 00:00:00.000");
		rvwSpA0023Detail.setRvwSpDtlEdThdDt("2012-12-31 23:59:59.000");
		rvwSpA0023Detail.setRvwSpDtlPt(67108864L);
		rvwSpA0023Detail.setRvwSpDtlTyp("D");
		rvwSpsA0021.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSpA0021Detail, rvwSpA0022Detail, rvwSpA0023Detail});
		reviewPointA002.setRvwSps(rvwSpsA002);

		// A003-----------------------------
		ReviewPointSpecial[] rvwSpsA003 = new ReviewPointSpecial[1];
		ReviewPointSpecial rvwSpsA0031 = new ReviewPointSpecial();
		rvwSpsA003[0] = rvwSpsA0031;
		rvwSpsA0031.setPryNo(1);
		rvwSpsA0031.setPtTyp(ReviewPointSpecial.POINT_TYPE_ADD);
		rvwSpsA0031.setRvwSpCd("1000000022");
		rvwSpsA0031.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_POST);
		rvwSpsA0031.setRvwSpSttTm("2001-02-01 00:00:00.000");
		rvwSpsA0031.setRvwSpEdTm("2012-12-31 00:00:00.000");
		rvwSpsA0031.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_TERM);

		ReviewPointSpecialDetail rvwSpA0031Detail = new ReviewPointSpecialDetail();
		rvwSpA0031Detail.setRvwSpDtlSttThdDt("2012-01-01 00:00:00.000");
		rvwSpA0031Detail.setRvwSpDtlEdThdDt("2012-01-01 23:59:59.000");
		rvwSpA0031Detail.setRvwSpDtlPt(16L);
		rvwSpA0031Detail.setRvwSpDtlTyp("D");

		ReviewPointSpecialDetail rvwSpA0032Detail = new ReviewPointSpecialDetail();
		rvwSpA0032Detail.setRvwSpDtlSttThdDt("2012-01-01 00:00:00.000");
		rvwSpA0032Detail.setRvwSpDtlEdThdDt("2012-12-31 23:59:59.000");
		rvwSpA0032Detail.setRvwSpDtlPt(32L);
		rvwSpA0032Detail.setRvwSpDtlTyp("D");

		ReviewPointSpecialDetail rvwSpA0033Detail = new ReviewPointSpecialDetail();
		rvwSpA0033Detail.setRvwSpDtlSttThdDt("2012-01-01 00:00:00.000");
		rvwSpA0033Detail.setRvwSpDtlEdThdDt("2012-12-31 23:59:59.000");
		rvwSpA0033Detail.setRvwSpDtlPt(64L);
		rvwSpA0033Detail.setRvwSpDtlTyp("D");

		rvwSpsA0031.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSpA0031Detail, rvwSpA0032Detail, rvwSpA0033Detail});
		reviewPointA003.setRvwSps(rvwSpsA003);

		// A004-----------------------------
		ReviewPointSpecial[] rvwSpsA004 = new ReviewPointSpecial[1];
		ReviewPointSpecial rvwSpsA0041 = new ReviewPointSpecial();
		rvwSpsA004[0] = rvwSpsA0041;
		rvwSpsA0041.setPryNo(1);
		rvwSpsA0041.setPtTyp(ReviewPointSpecial.POINT_TYPE_REPLACE);
		rvwSpsA0041.setRvwSpCd("1000000023");
		rvwSpsA0041.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_PURCHASE);
		rvwSpsA0041.setRvwSpSttTm("2001-02-01 00:00:00.000");
		rvwSpsA0041.setRvwSpEdTm("2012-12-31 23:59:59.000");
		rvwSpsA0041.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_TERM);

		ReviewPointSpecialDetail rvwSpA0041Detail = new ReviewPointSpecialDetail();
		rvwSpA0041Detail.setRvwSpDtlSttThdDt("2012-01-01 00:00:00.000");
		rvwSpA0041Detail.setRvwSpDtlEdThdDt("2012-12-31 23:59:59.000");
		rvwSpA0041Detail.setRvwSpDtlPt(134217728L);
		rvwSpA0041Detail.setRvwSpDtlTyp("D");

		ReviewPointSpecialDetail rvwSpA0042Detail = new ReviewPointSpecialDetail();
		rvwSpA0042Detail.setRvwSpDtlSttThdDt("2012-01-01 00:00:00.000");
		rvwSpA0042Detail.setRvwSpDtlEdThdDt("2012-01-01 00:00:00.000");
		rvwSpA0042Detail.setRvwSpDtlPt(268435456L);
		rvwSpA0042Detail.setRvwSpDtlTyp("D");

		ReviewPointSpecialDetail rvwSpA0043Detail = new ReviewPointSpecialDetail();
		rvwSpA0043Detail.setRvwSpDtlSttThdDt("2012-01-01 00:00:00.000");
		rvwSpA0043Detail.setRvwSpDtlEdThdDt("2012-12-31 23:59:59.000");
		rvwSpA0043Detail.setRvwSpDtlPt(536870912L);
		rvwSpA0043Detail.setRvwSpDtlTyp("D");

		rvwSpsA0041.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSpA0041Detail, rvwSpA0042Detail, rvwSpA0043Detail});
		reviewPointA004.setRvwSps(rvwSpsA004);


		// B001-----------------------------
		ReviewPointSpecial[] rvwSpsB001 = new ReviewPointSpecial[1];
		ReviewPointSpecial rvwSpsB0011 = new ReviewPointSpecial();
		rvwSpsB001[0] = rvwSpsB0011;
		rvwSpsB0011.setPryNo(1);
		rvwSpsB0011.setPtTyp(ReviewPointSpecial.POINT_TYPE_REPLACE);
		rvwSpsB0011.setRvwSpCd("1000000024");
		rvwSpsB0011.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_ORDER);
		rvwSpsB0011.setRvwSpSttTm("2001-02-01 00:00:00.000");
		rvwSpsB0011.setRvwSpEdTm("2021-12-31 00:00:00.000");
		rvwSpsB0011.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_FIRST);
		ReviewPointSpecialDetail rvwSpB0011Detail = new ReviewPointSpecialDetail();
		rvwSpB0011Detail.setRvwSpDtlSttThdNum(1);
		rvwSpB0011Detail.setRvwSpDtlEdThdNum(100);
		rvwSpB0011Detail.setRvwSpDtlPt(1L);
		rvwSpB0011Detail.setRvwSpDtlTyp("N");

		ReviewPointSpecialDetail rvwSpB0012Detail = new ReviewPointSpecialDetail();
		rvwSpB0012Detail.setRvwSpDtlSttThdNum(101);
		rvwSpB0012Detail.setRvwSpDtlEdThdNum(1000);
		rvwSpB0012Detail.setRvwSpDtlPt(2L);
		rvwSpB0012Detail.setRvwSpDtlTyp("N");

		ReviewPointSpecialDetail rvwSpB0013Detail = new ReviewPointSpecialDetail();
		rvwSpB0013Detail.setRvwSpDtlSttThdNum(1001);
		rvwSpB0013Detail.setRvwSpDtlEdThdNum(10000);
		rvwSpB0013Detail.setRvwSpDtlPt(4L);
		rvwSpB0013Detail.setRvwSpDtlTyp("N");

		rvwSpsB0011.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSpB0011Detail, rvwSpB0012Detail, rvwSpB0013Detail});
		reviewPointB001.setRvwSps(rvwSpsB001);

		// B002-----------------------------
		ReviewPointSpecial[] rvwSpsB002 = new ReviewPointSpecial[1];
		ReviewPointSpecial rvwSpsB0021 = new ReviewPointSpecial();
		rvwSpsB002[0] = rvwSpsB0021;
		rvwSpsB0021.setPryNo(1);
		rvwSpsB0021.setPtTyp(ReviewPointSpecial.POINT_TYPE_ADD);
		rvwSpsB0021.setRvwSpCd("1000000025");
		rvwSpsB0021.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_POST);
		rvwSpsB0021.setRvwSpSttTm("2001-02-01 00:00:00.000");
		rvwSpsB0021.setRvwSpEdTm("2021-12-31 00:00:00.000");
		rvwSpsB0021.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_FIRST);
		rvwSpsB0021.setRvwSpDtls(null);
		reviewPointB002.setRvwSps(rvwSpsB002);


		// B003-----------------------------
		ReviewPointSpecial[] rvwSpsB003 = new ReviewPointSpecial[1];
		ReviewPointSpecial rvwSpsB0031 = new ReviewPointSpecial();
		rvwSpsB003[0] = rvwSpsB0031;
		rvwSpsB0031.setPryNo(1);
		rvwSpsB0031.setPtTyp(ReviewPointSpecial.POINT_TYPE_REPLACE);
		rvwSpsB0031.setRvwSpCd("1000000026");
		rvwSpsB0031.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_POST);
		rvwSpsB0031.setRvwSpSttTm("2001-02-01 00:00:00.000");
		rvwSpsB0031.setRvwSpEdTm("2021-12-31 00:00:00.000");
		rvwSpsB0031.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_FIRST);


		ReviewPointSpecialDetail rvwSpB0031Detail = new ReviewPointSpecialDetail();
		rvwSpB0031Detail.setRvwSpDtlSttThdNum(1);
		rvwSpB0031Detail.setRvwSpDtlEdThdNum(100);
		rvwSpB0031Detail.setRvwSpDtlPt(16L);
		rvwSpB0031Detail.setRvwSpDtlTyp("N");
		ReviewPointSpecialDetail rvwSpB0032Detail = new ReviewPointSpecialDetail();
		rvwSpB0032Detail.setRvwSpDtlSttThdNum(101);
		rvwSpB0032Detail.setRvwSpDtlEdThdNum(1000);
		rvwSpB0032Detail.setRvwSpDtlPt(32L);
		rvwSpB0032Detail.setRvwSpDtlTyp("N");
		ReviewPointSpecialDetail rvwSpB0033Detail = new ReviewPointSpecialDetail();
		rvwSpB0033Detail.setRvwSpDtlSttThdNum(1001);
		rvwSpB0033Detail.setRvwSpDtlEdThdNum(10000);
		rvwSpB0033Detail.setRvwSpDtlPt(64L);
		rvwSpB0033Detail.setRvwSpDtlTyp("N");
		rvwSpsB0031.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSpB0031Detail, rvwSpB0032Detail, rvwSpB0033Detail});
		reviewPointB003.setRvwSps(rvwSpsB003);

		List<ReviewPoint> ret  = new ArrayList<ReviewPoint>();
		ret.add(reviewPointA001);
		ret.add(reviewPointA002);
		ret.add(reviewPointA003);
		ret.add(reviewPointA004);

		ret.add(reviewPointB001);
		ret.add(reviewPointB002);
		ret.add(reviewPointB003);
		return ret;
	}


	private List<ReviewPoint> getGrantQestSpReviewPoints3() {
		List<ReviewPoint> list = getGrantBasicReviewPoints();
		ReviewPoint reviewPointA001 = list.get(0);
		ReviewPoint reviewPointA002 = list.get(1);


		// A001-----------------------------
		ReviewPointSpecial[] rvwSpsA001 = new ReviewPointSpecial[1];
		ReviewPointSpecial rvwSpsA0011 = new ReviewPointSpecial();
		rvwSpsA001[0] = rvwSpsA0011;
		rvwSpsA0011.setPryNo(1);
		rvwSpsA0011.setPtTyp(ReviewPointSpecial.POINT_TYPE_ADD);
		rvwSpsA0011.setRvwSpCd("1000000027");
		rvwSpsA0011.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_PURCHASE);
		rvwSpsA0011.setRvwSpSttTm("2001-02-01 00:00:00.000");
		rvwSpsA0011.setRvwSpEdTm("2021-12-31 00:00:00.000");
		rvwSpsA0011.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_FIRST);
		ReviewPointSpecialDetail rvwSpA0011Detail = new ReviewPointSpecialDetail();
		rvwSpA0011Detail.setRvwSpDtlSttThdNum(1);
		rvwSpA0011Detail.setRvwSpDtlEdThdNum(100);
		rvwSpA0011Detail.setRvwSpDtlPt(1L);
		rvwSpA0011Detail.setRvwSpDtlTyp("N");
		ReviewPointSpecialDetail rvwSpA0012Detail = new ReviewPointSpecialDetail();
		rvwSpA0012Detail.setRvwSpDtlSttThdNum(101);
		rvwSpA0012Detail.setRvwSpDtlEdThdNum(1000);
		rvwSpA0012Detail.setRvwSpDtlPt(2L);
		rvwSpA0012Detail.setRvwSpDtlTyp("N");
		ReviewPointSpecialDetail rvwSpA0013Detail = new ReviewPointSpecialDetail();
		rvwSpA0013Detail.setRvwSpDtlSttThdNum(1001);
		rvwSpA0013Detail.setRvwSpDtlEdThdNum(10000);
		rvwSpA0013Detail.setRvwSpDtlPt(4L);
		rvwSpA0013Detail.setRvwSpDtlTyp("N");
		rvwSpsA0011.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSpA0011Detail, rvwSpA0012Detail, rvwSpA0013Detail});
		reviewPointA001.setRvwSps(rvwSpsA001);

		// A002-----------------------------
		ReviewPointSpecial[] rvwSpsA002 = new ReviewPointSpecial[1];
		ReviewPointSpecial rvwSpsA0021 = new ReviewPointSpecial();
		rvwSpsA002[0] = rvwSpsA0021;
		rvwSpsA0021.setPryNo(1);
		rvwSpsA0021.setPtTyp(ReviewPointSpecial.POINT_TYPE_REPLACE);
		rvwSpsA0021.setRvwSpCd("1000000028");
		rvwSpsA0021.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_ORDER);
		rvwSpsA0021.setRvwSpSttTm("2001-02-01 00:00:00.000");
		rvwSpsA0021.setRvwSpEdTm("2021-12-31 00:00:00.000");
		rvwSpsA0021.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_FIRST);

		ReviewPointSpecialDetail rvwSpA0021Detail = new ReviewPointSpecialDetail();
		rvwSpA0021Detail.setRvwSpDtlSttThdNum(1);
		rvwSpA0021Detail.setRvwSpDtlEdThdNum(100);
		rvwSpA0021Detail.setRvwSpDtlPt(1L);
		rvwSpA0021Detail.setRvwSpDtlTyp("N");
		ReviewPointSpecialDetail rvwSpA0022Detail = new ReviewPointSpecialDetail();
		rvwSpA0022Detail.setRvwSpDtlSttThdNum(101);
		rvwSpA0022Detail.setRvwSpDtlEdThdNum(1000);
		rvwSpA0022Detail.setRvwSpDtlPt(2L);
		rvwSpA0022Detail.setRvwSpDtlTyp("N");
		ReviewPointSpecialDetail rvwSpA0023Detail = new ReviewPointSpecialDetail();
		rvwSpA0023Detail.setRvwSpDtlSttThdNum(1001);
		rvwSpA0023Detail.setRvwSpDtlEdThdNum(10000);
		rvwSpA0023Detail.setRvwSpDtlPt(4L);
		rvwSpA0023Detail.setRvwSpDtlTyp("N");
		rvwSpsA0021.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSpA0021Detail, rvwSpA0022Detail, rvwSpA0023Detail});
		reviewPointA002.setRvwSps(rvwSpsA002);


		List<ReviewPoint> ret  = new ArrayList<ReviewPoint>();
		ret.add(reviewPointA001);
		ret.add(reviewPointA002);

		return ret;
	}

	private ReviewPointSpecial[] getGrantItemSpReviewPoints() {
		ReviewPointSpecial[] rvwSps = new ReviewPointSpecial[1];
		ReviewPointSpecial rvwSp = new ReviewPointSpecial();
		rvwSps[0] = rvwSp;
		rvwSp.setPryNo(1);
		rvwSp.setPtTyp(ReviewPointSpecial.POINT_TYPE_ADD);
		rvwSp.setRvwSpCd("1000000029");
		rvwSp.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_ORDER);
		rvwSp.setRvwSpSttTm("2012-02-02 00:00:00.000");
		rvwSp.setRvwSpEdTm("2021-02-14 00:00:00.000");
		rvwSp.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_TERM);
		ReviewPointSpecialDetail rvwSp31Detail1 = new ReviewPointSpecialDetail();
		rvwSp31Detail1.setRvwSpDtlSttThdDt("2012-02-02 00:00:00.000");
		rvwSp31Detail1.setRvwSpDtlEdThdDt("2021-02-04 23:59:59.000");
		rvwSp31Detail1.setRvwSpDtlPt(1L);
		rvwSp31Detail1.setRvwSpDtlTyp("D");
		rvwSp.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSp31Detail1});
		return rvwSps;

	}

	private ReviewPointSpecial[] getGrantItemSpReviewPoints2() {
		ReviewPointSpecial[] rvwSps = new ReviewPointSpecial[3];
		ReviewPointSpecial rvwSp = new ReviewPointSpecial();
		rvwSps[0] = rvwSp;
		rvwSp.setPryNo(1);
		rvwSp.setPtTyp(ReviewPointSpecial.POINT_TYPE_REPLACE);
		rvwSp.setRvwSpCd("1000000030");
		rvwSp.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_ORDER);
		rvwSp.setRvwSpSttTm("2012-02-02 00:00:00.000");
		rvwSp.setRvwSpEdTm("2021-02-14 00:00:00.000");
		rvwSp.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_FIRST);
		ReviewPointSpecialDetail rvwSp31Detail1 = new ReviewPointSpecialDetail();
		rvwSp31Detail1.setRvwSpDtlSttThdNum(1);
		rvwSp31Detail1.setRvwSpDtlEdThdNum(10000);
		rvwSp31Detail1.setRvwSpDtlPt(1L);
		rvwSp31Detail1.setRvwSpDtlTyp("N");
		rvwSp.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSp31Detail1});


		ReviewPointSpecial rvwSp2 = new ReviewPointSpecial();
		rvwSps[1] = rvwSp2;
		rvwSp2.setPryNo(1);
		rvwSp2.setPtTyp(ReviewPointSpecial.POINT_TYPE_ADD);
		rvwSp2.setRvwSpCd("1000000031");
		rvwSp2.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_PURCHASE);
		rvwSp2.setRvwSpSttTm("2012-02-02 00:00:00.000");
		rvwSp2.setRvwSpEdTm("2021-02-14 00:00:00.000");
		rvwSp2.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_FIRST);
		ReviewPointSpecialDetail rvwSp21Detail1 = new ReviewPointSpecialDetail();
		rvwSp21Detail1.setRvwSpDtlSttThdNum(1);
		rvwSp21Detail1.setRvwSpDtlEdThdNum(10000);
		rvwSp21Detail1.setRvwSpDtlPt(2L);
		rvwSp21Detail1.setRvwSpDtlTyp("N");
		rvwSp2.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSp21Detail1});


		ReviewPointSpecial rvwSp3 = new ReviewPointSpecial();
		rvwSps[1] = rvwSp3;
		rvwSp3.setPryNo(1);
		rvwSp3.setPtTyp(ReviewPointSpecial.POINT_TYPE_ADD);
		rvwSp3.setRvwSpCd("1000000032");
		rvwSp3.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_POST);
		rvwSp3.setRvwSpSttTm("2012-02-02 00:00:00.000");
		rvwSp3.setRvwSpEdTm("2021-02-14 00:00:00.000");
		rvwSp3.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_FIRST);
		ReviewPointSpecialDetail rvwSp23Detail1 = new ReviewPointSpecialDetail();
		rvwSp23Detail1.setRvwSpDtlSttThdNum(1);
		rvwSp23Detail1.setRvwSpDtlEdThdNum(10000);
		rvwSp23Detail1.setRvwSpDtlPt(4L);
		rvwSp23Detail1.setRvwSpDtlTyp("N");
		rvwSp2.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSp23Detail1});


		return rvwSps;

	}


	private ReviewPointSpecial[] getGrantItemSpReviewPoints3() {
		ReviewPointSpecial[] rvwSps = new ReviewPointSpecial[3];
		ReviewPointSpecial rvwSp = new ReviewPointSpecial();
		rvwSps[0] = rvwSp;
		rvwSp.setPryNo(1);
		rvwSp.setPtTyp(ReviewPointSpecial.POINT_TYPE_ADD);
		rvwSp.setRvwSpCd("1000000033");
		rvwSp.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_ORDER);
		rvwSp.setRvwSpSttTm("2012-02-02 00:00:00.000");
		rvwSp.setRvwSpEdTm("2012-02-02 00:00:00.000");
		rvwSp.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_TERM);
		ReviewPointSpecialDetail rvwSp31Detail1 = new ReviewPointSpecialDetail();
		rvwSp31Detail1.setRvwSpDtlSttThdDt("2012-02-02 00:00:00.000");
		rvwSp31Detail1.setRvwSpDtlEdThdDt("2021-02-02 00:00:00.000");
		rvwSp31Detail1.setRvwSpDtlPt(1L);
		rvwSp31Detail1.setRvwSpDtlTyp("D");
		rvwSp.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSp31Detail1});


		ReviewPointSpecial rvwSp2 = new ReviewPointSpecial();
		rvwSps[1] = rvwSp2;
		rvwSp2.setPryNo(1);
		rvwSp2.setPtTyp(ReviewPointSpecial.POINT_TYPE_REPLACE);
		rvwSp2.setRvwSpCd("1000000034");
		rvwSp2.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_PURCHASE);
		rvwSp2.setRvwSpSttTm("2012-02-02 00:00:00.000");
		rvwSp2.setRvwSpEdTm("2021-02-14 00:00:00.000");
		rvwSp2.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_TERM);
		ReviewPointSpecialDetail rvwSp21Detail1 = new ReviewPointSpecialDetail();
		rvwSp21Detail1.setRvwSpDtlSttThdDt("2012-02-02 00:00:00.000");
		rvwSp21Detail1.setRvwSpDtlEdThdDt("2021-02-02 00:00:00.000");
		rvwSp21Detail1.setRvwSpDtlPt(2L);
		rvwSp21Detail1.setRvwSpDtlTyp("D");
		rvwSp2.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSp21Detail1});


		ReviewPointSpecial rvwSp3 = new ReviewPointSpecial();
		rvwSps[2] = rvwSp3;
		rvwSp3.setPryNo(1);
		rvwSp3.setPtTyp(ReviewPointSpecial.POINT_TYPE_REPLACE);
		rvwSp3.setRvwSpCd("1000000035");
		rvwSp3.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_POST);
		rvwSp3.setRvwSpSttTm("2012-02-02 00:00:00.000");
		rvwSp3.setRvwSpEdTm("2021-02-14 00:00:00.000");
		rvwSp3.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_TERM);
		ReviewPointSpecialDetail rvwSp23Detail1 = new ReviewPointSpecialDetail();
		rvwSp23Detail1.setRvwSpDtlSttThdDt("2012-02-02 00:00:00.000");
		rvwSp23Detail1.setRvwSpDtlEdThdDt("2021-02-02 00:00:00.000");
		rvwSp23Detail1.setRvwSpDtlPt(4L);
		rvwSp23Detail1.setRvwSpDtlTyp("D");
		rvwSp3.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSp23Detail1});


		return rvwSps;

	}


	private ReviewPointSpecial[] getGrantItemSpReviewPoints4() {
		ReviewPointSpecial[] rvwSps = new ReviewPointSpecial[3];
		ReviewPointSpecial rvwSp = new ReviewPointSpecial();
		rvwSps[0] = rvwSp;
		rvwSp.setPryNo(1);
		rvwSp.setPtTyp(ReviewPointSpecial.POINT_TYPE_ADD);
		rvwSp.setRvwSpCd("1000000036");
		rvwSp.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_ORDER);
		rvwSp.setRvwSpSttTm("2012-02-02 00:00:00.000");
		rvwSp.setRvwSpEdTm("2012-02-02 00:00:00.000");
		rvwSp.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_FIRST);
		ReviewPointSpecialDetail rvwSp31Detail1 = new ReviewPointSpecialDetail();
		rvwSp31Detail1.setRvwSpDtlSttThdNum(1);
		rvwSp31Detail1.setRvwSpDtlEdThdNum(10000);
		rvwSp31Detail1.setRvwSpDtlPt(1L);
		rvwSp31Detail1.setRvwSpDtlTyp("N");
		rvwSp.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSp31Detail1});


		ReviewPointSpecial rvwSp2 = new ReviewPointSpecial();
		rvwSps[1] = rvwSp2;
		rvwSp2.setPryNo(1);
		rvwSp2.setPtTyp(ReviewPointSpecial.POINT_TYPE_ADD);
		rvwSp2.setRvwSpCd("1000000037");
		rvwSp2.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_PURCHASE);
		rvwSp2.setRvwSpSttTm("2012-02-02 00:00:00.000");
		rvwSp2.setRvwSpEdTm("2012-02-02 00:00:00.000");
		rvwSp2.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_FIRST);
		ReviewPointSpecialDetail rvwSp21Detail1 = new ReviewPointSpecialDetail();
		rvwSp21Detail1.setRvwSpDtlSttThdNum(1);
		rvwSp21Detail1.setRvwSpDtlEdThdNum(10000);
		rvwSp21Detail1.setRvwSpDtlPt(2L);
		rvwSp21Detail1.setRvwSpDtlTyp("N");
		rvwSp2.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSp21Detail1});


		ReviewPointSpecial rvwSp3 = new ReviewPointSpecial();
		rvwSps[2] = rvwSp3;
		rvwSp3.setPryNo(1);
		rvwSp3.setPtTyp(ReviewPointSpecial.POINT_TYPE_ADD);
		rvwSp3.setRvwSpCd("1000000038");
		rvwSp3.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_POST);
		rvwSp3.setRvwSpSttTm("2012-02-02 00:00:00.000");
		rvwSp3.setRvwSpEdTm("2012-02-02 00:00:00.000");
		rvwSp3.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_FIRST);
		ReviewPointSpecialDetail rvwSp23Detail1 = new ReviewPointSpecialDetail();
		rvwSp23Detail1.setRvwSpDtlSttThdNum(1);
		rvwSp23Detail1.setRvwSpDtlEdThdNum(10000);
		rvwSp23Detail1.setRvwSpDtlPt(4L);
		rvwSp23Detail1.setRvwSpDtlTyp("N");
		rvwSp3.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSp23Detail1});


		return rvwSps;

	}

	private ReviewPointSpecial[] getGrantItemSpReviewPoints5() {
		ReviewPointSpecial[] rvwSps = new ReviewPointSpecial[3];
		ReviewPointSpecial rvwSp = new ReviewPointSpecial();
		rvwSps[0] = rvwSp;
		rvwSp.setPryNo(1);
		rvwSp.setPtTyp(ReviewPointSpecial.POINT_TYPE_ADD);
		rvwSp.setRvwSpCd("1000000039");
		rvwSp.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_ORDER);
		rvwSp.setRvwSpSttTm("2012-02-02 00:00:00.000");
		rvwSp.setRvwSpEdTm("2021-02-02 00:00:00.000");
		rvwSp.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_FIRST);
		ReviewPointSpecialDetail rvwSp31Detail1 = new ReviewPointSpecialDetail();
		rvwSp31Detail1.setRvwSpDtlSttThdNum(1);
		rvwSp31Detail1.setRvwSpDtlEdThdNum(10000);
		rvwSp31Detail1.setRvwSpDtlPt(1L);
		rvwSp31Detail1.setRvwSpDtlTyp("N");
		rvwSp.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSp31Detail1});


		ReviewPointSpecial rvwSp2 = new ReviewPointSpecial();
		rvwSps[1] = rvwSp2;
		rvwSp2.setPryNo(1);
		rvwSp2.setPtTyp(ReviewPointSpecial.POINT_TYPE_REPLACE);
		rvwSp2.setRvwSpCd("1000000040");
		rvwSp2.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_PURCHASE);
		rvwSp2.setRvwSpSttTm("2012-02-02 00:00:00.000");
		rvwSp2.setRvwSpEdTm("2021-02-02 00:00:00.000");
		rvwSp2.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_FIRST);
		ReviewPointSpecialDetail rvwSp21Detail1 = new ReviewPointSpecialDetail();
		rvwSp21Detail1.setRvwSpDtlSttThdNum(1);
		rvwSp21Detail1.setRvwSpDtlEdThdNum(10000);
		rvwSp21Detail1.setRvwSpDtlPt(2L);
		rvwSp21Detail1.setRvwSpDtlTyp("N");
		rvwSp2.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSp21Detail1});


		ReviewPointSpecial rvwSp3 = new ReviewPointSpecial();
		rvwSps[2] = rvwSp3;
		rvwSp3.setPryNo(1);
		rvwSp3.setPtTyp(ReviewPointSpecial.POINT_TYPE_ADD);
		rvwSp3.setRvwSpCd("1000000041");
		rvwSp3.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_POST);
		rvwSp3.setRvwSpSttTm("2012-02-02 00:00:00.000");
		rvwSp3.setRvwSpEdTm("2021-02-02 00:00:00.000");
		rvwSp3.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_FIRST);
		ReviewPointSpecialDetail rvwSp23Detail1 = new ReviewPointSpecialDetail();
		rvwSp23Detail1.setRvwSpDtlSttThdNum(1);
		rvwSp23Detail1.setRvwSpDtlEdThdNum(10000);
		rvwSp23Detail1.setRvwSpDtlPt(4L);
		rvwSp23Detail1.setRvwSpDtlTyp("N");
		rvwSp3.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSp23Detail1});


		return rvwSps;

	}

	private ReviewPointSpecial[] getGrantItemSpReviewPoints6() {
		ReviewPointSpecial[] rvwSps = new ReviewPointSpecial[3];
		ReviewPointSpecial rvwSp = new ReviewPointSpecial();
		rvwSps[0] = rvwSp;
		rvwSp.setPryNo(1);
		rvwSp.setPtTyp(ReviewPointSpecial.POINT_TYPE_REPLACE);
		rvwSp.setRvwSpCd("1000000042");
		rvwSp.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_ORDER);
		rvwSp.setRvwSpSttTm("2012-02-02 00:00:00.000");
		rvwSp.setRvwSpEdTm("2021-02-02 00:00:00.000");
		rvwSp.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_TERM);
		ReviewPointSpecialDetail rvwSp1Detail1 = new ReviewPointSpecialDetail();
		rvwSp1Detail1.setRvwSpDtlSttThdDt("2012-02-02 00:00:00.000");
		rvwSp1Detail1.setRvwSpDtlEdThdDt("2012-02-02 00:00:00.000");
		rvwSp1Detail1.setRvwSpDtlPt(1L);
		rvwSp1Detail1.setRvwSpDtlTyp("D");

		ReviewPointSpecialDetail rvwSp1Detail2 = new ReviewPointSpecialDetail();
		rvwSp1Detail2.setRvwSpDtlSttThdDt("2012-02-02 00:00:00.000");
		rvwSp1Detail2.setRvwSpDtlEdThdDt("2012-02-02 00:00:00.000");
		rvwSp1Detail2.setRvwSpDtlPt(2L);
		rvwSp1Detail2.setRvwSpDtlTyp("D");

		ReviewPointSpecialDetail rvwSp1Detail3 = new ReviewPointSpecialDetail();
		rvwSp1Detail3.setRvwSpDtlSttThdDt("2012-02-02 00:00:00.000");
		rvwSp1Detail3.setRvwSpDtlEdThdDt("2021-02-02 00:00:00.000");
		rvwSp1Detail3.setRvwSpDtlPt(4L);
		rvwSp1Detail3.setRvwSpDtlTyp("D");

		rvwSp.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSp1Detail1, rvwSp1Detail2, rvwSp1Detail3});

		return rvwSps;

	}

	private ReviewPointSpecial[] getGrantItemSpReviewPoints7() {
		ReviewPointSpecial[] rvwSps = new ReviewPointSpecial[1];
		ReviewPointSpecial rvwSp = new ReviewPointSpecial();
		rvwSps[0] = rvwSp;
		rvwSp.setPryNo(1);
		rvwSp.setPtTyp(ReviewPointSpecial.POINT_TYPE_ADD);
		rvwSp.setRvwSpCd("1000000043");
		rvwSp.setRvwSpJdgTyp(ReviewPointSpecial.SPECIAL_COND_JUDGEMENT_TYPE_ORDER);
		rvwSp.setRvwSpSttTm("2012-02-02 00:00:00.000");
		rvwSp.setRvwSpEdTm("2021-02-02 00:00:00.000");
		rvwSp.setRvwSpTyp(ReviewPointSpecial.SPECIAL_COND_TYPE_FIRST);
		ReviewPointSpecialDetail rvwSp31Detail1 = new ReviewPointSpecialDetail();
		rvwSp31Detail1.setRvwSpDtlSttThdNum(1);
		rvwSp31Detail1.setRvwSpDtlEdThdNum(100);
		rvwSp31Detail1.setRvwSpDtlPt(1L);
		rvwSp31Detail1.setRvwSpDtlTyp("N");
		ReviewPointSpecialDetail rvwSp32Detail1 = new ReviewPointSpecialDetail();
		rvwSp32Detail1.setRvwSpDtlSttThdNum(101);
		rvwSp32Detail1.setRvwSpDtlEdThdNum(1000);
		rvwSp32Detail1.setRvwSpDtlPt(2L);
		rvwSp32Detail1.setRvwSpDtlTyp("N");
		ReviewPointSpecialDetail rvwSp33Detail1 = new ReviewPointSpecialDetail();
		rvwSp33Detail1.setRvwSpDtlSttThdNum(1001);
		rvwSp33Detail1.setRvwSpDtlEdThdNum(10000);
		rvwSp33Detail1.setRvwSpDtlPt(4L);
		rvwSp33Detail1.setRvwSpDtlTyp("N");

		rvwSp.setRvwSpDtls(new ReviewPointSpecialDetail[]{rvwSp31Detail1, rvwSp32Detail1, rvwSp33Detail1});
		return rvwSps;

	}

	@Override
	public void disableAccessCatalog() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ProductDO loadProductForMR(String sku) {
		// TODO Auto-generated method stub
		return null;
	}
	
//	@Override
//	public ProductDO loadProductForMR(String sku, boolean isWithCart) {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public Map<String, ProductDO> findBySkuForMR(Collection<String> skus) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ProductDO> findVariationProductBySku(String sku) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * 指定した条件で商品情報を返します。
	 * @param sku SKU
	 * @param fillType フィルタイプ
	 * @param autoId autoId
	 * @param cartId cartId
	 * @param yatpz yatpz
	 * @param params 拡張パラメーター
	 * @return 商品情報
	 */
	@Override
	public ProductDO loadProduct(
			String sku, 
			FillType fillType, 
			String autoId,
			String cartId, 
			String yatpz, 
			boolean isWithCart,
			Map<String, Object> params) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
