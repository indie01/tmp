/**
 *
 */
package com.kickmogu.yodobashi.community.service;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.solr.client.solrj.SolrQuery;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.core.utils.StringUtil;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.yodobashi.community.common.exception.SpoofingNameException;
import com.kickmogu.yodobashi.community.common.test.YcComJUnit4ClassRunner;
import com.kickmogu.yodobashi.community.common.utils.BackendStubUtils;
import com.kickmogu.yodobashi.community.mapreduce.job.BaseJobTest;
import com.kickmogu.yodobashi.community.resource.dao.MigrationDao;
import com.kickmogu.yodobashi.community.resource.dao.NormalizeCharDao;
import com.kickmogu.yodobashi.community.resource.dao.OuterCustomerDao;
import com.kickmogu.yodobashi.community.resource.domain.AccountSharingDO;
import com.kickmogu.yodobashi.community.resource.domain.ActionHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.InformationDO;
import com.kickmogu.yodobashi.community.resource.domain.MigrationCommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductMasterDO;
import com.kickmogu.yodobashi.community.resource.domain.SpoofingNameDO;
import com.kickmogu.yodobashi.community.resource.domain.VersionDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.ActionHistoryType;
import com.kickmogu.yodobashi.community.resource.domain.constants.InformationType;
import com.kickmogu.yodobashi.community.resource.domain.constants.VersionType;
import com.yodobashi.esa.customer.getoutcustomeridshareinfo.GetOutCustomerIDShareInfoResponse;
import com.yodobashi.esa.customer.structure.COMMONRETURN;

/**
 * データ作成テストです。
 * @author kamiike
 *
 */
@Ignore
@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration("/mr-context.xml")
public class DataTest extends BaseJobTest {

	/**
	 * Solrアクセサです。
	 */
	@Autowired @Qualifier("MySite-RefMaster")
	private SolrOperations solrOperationsMaster;

	/**
	 * 全ての情報を削除して、初期化します。
	 */
	@Before
	public void setup() throws Exception {
	}

	/**
	 * 注文情報を作成します。
	 */
	@Test
	public void test2() {
//		for (AsyncMessageDO message : hBaseOperations.scanAll(AsyncMessageDO.class)) {
//			message.setStatus(AsyncMessageStatus.ERROR);
//			message.setType(AsyncMessageType.SERVICE);
//			hBaseOperations.save(message);
//		}
//		hBaseOperations.deleteAll(AsyncMessageDO.class);
		/*
		hBaseOperations.deleteAll(EventHistoryDO.class);
		hBaseOperations.deleteAll(ProductMasterDO.class);
		hBaseOperations.deleteAll(VersionDO.class);
		solrOperations.deleteAll(ProductMasterDO.class);
		*/
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		int i = 0;
		for (ActionHistoryDO history : hBaseOperations.scanAll(ActionHistoryDO.class)) {
			if (history.getActionHistoryType().equals(ActionHistoryType.USER_PRODUCT_MASTER_RANK_CHANGE)) {
//				hBaseOperations.deleteByKey(ActionHistoryDO.class, history.getActionHistoryId());
				i++;
				System.out.println(i + ":a-hBase\t" + history.getActionHistoryId() + "\t" + history.getProductMaster().getProductMasterId() + "\t" + format.format(history.getRegisterDateTime()));
			}
		}
		i = 0;
		for (ActionHistoryDO history : solrOperationsMaster.findByQuery(new SolrQuery("*:*"), ActionHistoryDO.class).getDocuments()) {
			if (history.getActionHistoryType().equals(ActionHistoryType.USER_PRODUCT_MASTER_RANK_CHANGE)) {
//				solrOperations.deleteByKey(ActionHistoryDO.class, history.getActionHistoryId());
				i++;
				System.out.println(i + ":a-solr\t" + history.getActionHistoryId() + "\t" + history.getProductMaster().getProductMasterId() + "\t" + format.format(history.getRegisterDateTime()));
			}
		}
		i = 0;
		for (InformationDO info : hBaseOperations.scanAll(InformationDO.class)) {
			if (info.getInformationType().equals(InformationType.PRODUCT_MASTER_RANK_CHANGE)) {
//				hBaseOperations.deleteByKey(InformationDO.class, info.getInformationId());
				i++;
				System.out.println(i + ":i-hBase\t" + info.getInformationId() + "\t" + info.getProductMaster().getProductMasterId() + "\t" + format.format(info.getRegisterDateTime()));
			}
		}
		i = 0;
		for (InformationDO info : solrOperationsMaster.findByQuery(new SolrQuery("*:*"), InformationDO.class).getDocuments()) {
			if (info.getInformationType().equals(InformationType.PRODUCT_MASTER_RANK_CHANGE)) {
//				solrOperations.deleteByKey(InformationDO.class, info.getInformationId());
				i++;
				System.out.println(i + ":i-solr\t" + info.getInformationId() + "\t" + info.getProductMaster().getProductMasterId() + "\t" + format.format(info.getRegisterDateTime()));
			}
		}
		i = 0;
		VersionDO version = hBaseOperations.load(VersionDO.class, VersionType.PRODUCT_MASTER);
//		version.setVersion(1);
//		hBaseOperations.save(version);
		for (ProductMasterDO productMaster : hBaseOperations.scanAll(ProductMasterDO.class)) {
			if (productMaster.getVersion().equals(version.getVersion())) {
//				hBaseOperations.deleteByKey(ProductMasterDO.class, productMaster.getProductMasterId());
//				solrOperations.save(productMaster);
				i++;
				System.out.println(i + ":p-hBase\t" + productMaster.isRequiredNotify() + "\t" + productMaster.isDeleteFlag() + "\t" + productMaster.getRankInVersion() + "\t" + productMaster.getProductMasterId() + "\t" + format.format(productMaster.getRegisterDateTime()));
			}
		}
		i = 0;
		for (ProductMasterDO productMaster : solrOperationsMaster.findByQuery(new SolrQuery("*:*"), ProductMasterDO.class).getDocuments()) {
			if (productMaster.getVersion().equals(version.getVersion())) {
//				solrOperations.deleteByKey(ProductMasterDO.class, productMaster.getProductMasterId());
				i++;
				System.out.println(i + ":p-solr\t" + productMaster.isRequiredNotify() + "\t" + productMaster.isDeleteFlag() + "\t" + productMaster.getRankInVersion() + "\t" + productMaster.getProductMasterId() + "\t" + format.format(productMaster.getRegisterDateTime()));
			}
		}
//		solrOperations.optimize(ProductMasterDO.class);
		i = 0;
	}

	/**
	 * 注文情報を作成します。
	 */
	@Test
	public void test() {
		String communityUserId = "xxxxxxxxxxxxxxxxxxx";
		String sku = "";
		Date entryDate = getDate("2011/12/21");
		Date billingDate = getDate("2011/12/22");

		ProductDO product = productDao.loadProduct(sku);
		CommunityUserDO communityUser = hBaseOperations.load(
				CommunityUserDO.class, communityUserId);
		createSlip(communityUser, product.getJan(),entryDate, billingDate);
	}

	/**
	 * 移行用 DAO です。
	 */
	@Autowired
	private MigrationDao migrationDao;

	/**
	 * 標準化文字 DAO です。
	 */
	@Autowired
	private NormalizeCharDao normalizeCharDao;

	/**
	 * 外部顧客情報 DAO です。
	 */
	@Autowired @Qualifier("xi")
	private OuterCustomerDao outerCustomerDao;

	@Test
	public void createMargeCommunityNameData() {
		CommunityUserDO communityUser = hBaseOperations.load(
				CommunityUserDO.class, "300000Gyd270300Jx6270");
		String communityName = convertSafeCommunityName(
				communityUser.getCommunityName() + "IC移行");
		String normalizeCommunityName = normalizeCharDao.normalizeString(
				communityName);

		String icOuterCustomerId = null;
		for (AccountSharingDO sharing : outerCustomerDao.findAccountSharingByOuterCustomerId(
				communityUser.getCommunityId())) {
			if (!sharing.isEc()) {
				icOuterCustomerId = sharing.getOuterCustomerId();
				break;
			}
		}

		MigrationCommunityUserDO migrationCommunityUser = new MigrationCommunityUserDO();
		migrationCommunityUser.setOuterCustomerId(icOuterCustomerId);
		migrationCommunityUser.setCommunityName(communityName);
		// SpoofingPattern
		String spoofingNamePattern = normalizeCharDao.getSpoofingPattern(normalizeCommunityName);
		if(!normalizeCharDao.validateSpoofingPattern(spoofingNamePattern, false)){
			throw new SpoofingNameException("community name is spoofing communityId:" + icOuterCustomerId 
										+ " communityName:" + communityName 
										+ " normalizeName:" + normalizeCommunityName 
										+ " spoofingNamePattern:" + spoofingNamePattern);
		}

		migrationCommunityUser.setNormalizeCommunityName(normalizeCommunityName);
		migrationCommunityUser.setRegisterDateTime(communityUser.getRegisterDateTime());
		migrationCommunityUser.setModifyDateTime(communityUser.getModifyDateTime());

		SpoofingNameDO spoofingName = new SpoofingNameDO();
		spoofingName.setSpoofingNameId(StringUtil.toSHA256(spoofingNamePattern));
		spoofingName.setSpoofingPattern(spoofingNamePattern);
		spoofingName.setSpoofingName(normalizeCommunityName);

		migrationDao.createMigrationCommunityUser(migrationCommunityUser, spoofingName);

		communityUser.setCommunityNameMergeRequired(true);
		hBaseOperations.save(communityUser,
				Path.includeProp("communityNameMergeRequired"));
		solrOperations.save(communityUser);

		xiWillReturn(icOuterCustomerId, communityUser.getCommunityId());
	}

	public static void xiWillReturn(
			String icOuterCustomerId,
			String ecOuterCustomerId) {
		GetOutCustomerIDShareInfoResponse response = new GetOutCustomerIDShareInfoResponse();
		response.setCOMMONRETURN(COMMONRETURN.SUCCESS);
		GetOutCustomerIDShareInfoResponse.ShareInfoList shareInfoWrapper
				= new GetOutCustomerIDShareInfoResponse.ShareInfoList();
		shareInfoWrapper.setOuterCustomerId(icOuterCustomerId);
		GetOutCustomerIDShareInfoResponse.ShareInfoList.OuterCustomerIdShareInfoList shareInfo
				= new GetOutCustomerIDShareInfoResponse.ShareInfoList.OuterCustomerIdShareInfoList();
		shareInfo.setOuterCustomerId(icOuterCustomerId);
		shareInfo.setOuterCustomerStatus("01");
		shareInfo.setCustomerType("0003");
		shareInfoWrapper.getOuterCustomerIdShareInfoList().add(shareInfo);
		if (ecOuterCustomerId != null) {
			GetOutCustomerIDShareInfoResponse.ShareInfoList.OuterCustomerIdShareInfoList shareInfo2
					= new GetOutCustomerIDShareInfoResponse.ShareInfoList.OuterCustomerIdShareInfoList();
			shareInfo2.setOuterCustomerId(ecOuterCustomerId);
			shareInfo2.setOuterCustomerStatus("01");
			shareInfo2.setCustomerType("0002");
			shareInfoWrapper.getOuterCustomerIdShareInfoList().add(shareInfo2);
		}
		response.getShareInfoList().add(shareInfoWrapper);
		BackendStubUtils.prepareResponse("outerCustomerId", icOuterCustomerId, response);
		BackendStubUtils.prepareResponse("outerCustomerId", ecOuterCustomerId, response);
	}

	/**
	 * コミュニティ名をルールに従って寄せます。
	 * @param src 元の値
	 * @return 変換後の値
	 */
	private String convertSafeCommunityName(String src) {
		String dest = src;
		dest = StringUtil.trimAllSpace(dest);
		dest = StringUtil.hankakuKatakanaToZenkakuKatakana(dest);
		dest = StringUtil.hankakuSignToZenkakuSign(dest);
		return dest;
	}
}
