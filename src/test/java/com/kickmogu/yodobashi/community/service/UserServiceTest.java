package com.kickmogu.yodobashi.community.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import ch.ethz.ssh2.SCPClient;

import com.kickmogu.lib.core.exception.UniqueConstraintException;
import com.kickmogu.lib.core.ssh.SshCommand;
import com.kickmogu.yodobashi.community.common.test.YcComJUnit4ClassRunner;
import com.kickmogu.yodobashi.community.resource.dao.AnnounceDao;
import com.kickmogu.yodobashi.community.resource.dao.MailSettingDao;
import com.kickmogu.yodobashi.community.resource.domain.AnnounceDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.DecisivePurchaseDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.InformationDO;
import com.kickmogu.yodobashi.community.resource.domain.MailSettingDO;
import com.kickmogu.yodobashi.community.resource.domain.MailSettingMasterDO;
import com.kickmogu.yodobashi.community.resource.domain.NormalizeCharDO;
import com.kickmogu.yodobashi.community.resource.domain.NormalizeCharGroupDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDecisivePurchaseDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.SocialMediaSettingDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.AnnounceType;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommunityUserStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.ContentsStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.InformationType;
import com.kickmogu.yodobashi.community.resource.domain.constants.MailSendTiming;
import com.kickmogu.yodobashi.community.resource.domain.constants.MailSettingType;
import com.kickmogu.yodobashi.community.resource.domain.constants.PostContentType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ReviewType;
import com.kickmogu.yodobashi.community.resource.domain.constants.SocialMediaType;
import com.kickmogu.yodobashi.community.resource.domain.constants.Verification;
import com.kickmogu.yodobashi.community.service.vo.MailSettingCategoryVO;
import com.kickmogu.yodobashi.community.service.vo.MailSettingVO;
import com.kickmogu.yodobashi.community.service.vo.NewsFeedVO;

/**
 * ユーザーサービスのテストクラスです。
 *
 * @author hirabayashi
 *
 */
@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration("/jcclientContext.xml")
public class UserServiceTest extends DataSetTest {

	/**
	 * アナウンス DAO です。
	 */
	@Autowired
	private AnnounceDao announceDao;

	/**
	 * メール設定 DAO です。
	 */
	@Autowired
	private MailSettingDao mailSettingDao;

	/**
	 * データを初期化します。
	 */
	protected void initialize() {
		// 親クラスのinitializeを呼び出す
		super.initialize();
	}

	/**
	 * 全ての情報を削除して、初期化します。
	 */
	@Before
	public void setup() {
		initialize();
		NormalizeCharGroupDO group = new NormalizeCharGroupDO();
		hBaseOperations.save(group);
		NormalizeCharDO charI = new NormalizeCharDO();
		charI.setGroup(group);
		charI.setCharacter("I");
		charI.setOrderNo(1);
		hBaseOperations.save(charI);
		NormalizeCharDO charl = new NormalizeCharDO();
		charl.setGroup(group);
		charl.setCharacter("l");
		charl.setOrderNo(2);
		hBaseOperations.save(charl);
	}

	/**
	 * コミュニティユーザー登録を検証します。
	 */
	@Test
	public void testCreateCommunityUser() {
		// コミュニティユーザー登録(画像なし)を検証します。
		testCreateCommunityUser("あいうえお", false);
		// コミュニティユーザー登録(画像あり)を検証します。
		testCreateCommunityUser("かきくけこ", true);
	}

	/**
	 * コミュニティユーザー更新を検証します。
	 */
	@Test
	public void testUpdateCommunityUser() {
		// コミュニティユーザー更新を検証します。 画像なし=>画像なし
		testUpdateCommunityUser("あいうえお", "かきくけこ", false, false, null, null);
		// コミュニティユーザー更新を検証します。 画像なし=>画像あり
		testUpdateCommunityUser("さしすせそ", "たちつてと", false, true, null, null);
		// コミュニティユーザー更新を検証します。 画像あり=>画像あり
		testUpdateCommunityUser("なにぬねの", "はひふへほ", true, true, null, null);
		// コミュニティユーザー更新を検証します。 アダルト変更=>承認
		testUpdateCommunityUser("まみむめも", "まみむめも", true, true, Verification.AUTHORIZED, null);
		// コミュニティユーザー更新を検証します。 セロ変更=>承認
		testUpdateCommunityUser("やゆよ", "やゆよ", true, true, null, Verification.AUTHORIZED);
	}

	/**
	 * ニックネームの重複チェックを検証します。
	 */
	@Test
	public void testDuplicateCommunityName() {
		// ニックネームの重複チェックを検証します。
		testDuplicateCommunityName("てすと", "てすと");
	}

	/**
	 * アダルト表示確認ステータスの更新を検証します。
	 */
	@Test
	public void testUpdateAdultVerification() {
		//  登録直後(随時)=>随時
		CommunityUserDO communityUser = createCommunityUser("test1", false);
		testUpdateAdultVerification(communityUser, Verification.ATANYTIME);
		// 登録直後(随時)=>未承認
		CommunityUserDO communityUser2 = createCommunityUser("test2", false);
		testUpdateAdultVerification(communityUser2, Verification.UNAUTHORIZED);
		//  登録直後(随時)=>承認
		CommunityUserDO communityUser3 = createCommunityUser("test3", false);
		testUpdateAdultVerification(communityUser3, Verification.AUTHORIZED);
		// 未承認=>随時
		CommunityUserDO communityUser4 = createCommunityUser("test4", false);
		testUpdateAdultVerification(communityUser4, Verification.UNAUTHORIZED);
		testUpdateAdultVerification(communityUser4, Verification.ATANYTIME);
		// 未承認=>未承認
		CommunityUserDO communityUser5 = createCommunityUser("test5", false);
		testUpdateAdultVerification(communityUser5, Verification.UNAUTHORIZED);
		testUpdateAdultVerification(communityUser5, Verification.UNAUTHORIZED);
		// 未承認=>承認
		CommunityUserDO communityUser6 = createCommunityUser("test6", false);
		testUpdateAdultVerification(communityUser6, Verification.UNAUTHORIZED);
		testUpdateAdultVerification(communityUser6, Verification.AUTHORIZED);
		// 承認=>随時
		CommunityUserDO communityUser7 = createCommunityUser("test7", false);
		testUpdateAdultVerification(communityUser7, Verification.AUTHORIZED);
		testUpdateAdultVerification(communityUser7, Verification.ATANYTIME);
		// 承認=>未承認
		CommunityUserDO communityUser8 = createCommunityUser("test8", false);
		testUpdateAdultVerification(communityUser8, Verification.AUTHORIZED);
		testUpdateAdultVerification(communityUser8, Verification.UNAUTHORIZED);
		// 承認=>承認
		CommunityUserDO communityUser9 = createCommunityUser("test9", false);
		testUpdateAdultVerification(communityUser9, Verification.AUTHORIZED);
		testUpdateAdultVerification(communityUser9, Verification.AUTHORIZED);
	}

	/**
	 * セロ表示確認ステータスの更新を検証します。
	 */
	@Test
	public void testUpdateCeroVerification() {
		// 登録直後(随時)=>未承認
		CommunityUserDO communityUser = createCommunityUser("test1", false);
		testUpdateCeroVerification(communityUser, Verification.UNAUTHORIZED);
		// 登録直後(随時)=>承認
		CommunityUserDO communityUser2 = createCommunityUser("test2", false);
		testUpdateCeroVerification(communityUser2, Verification.AUTHORIZED);
		// 未承認=>未承認
		CommunityUserDO communityUser3 = createCommunityUser("test3", false);
		testUpdateCeroVerification(communityUser3, Verification.UNAUTHORIZED);
		testUpdateCeroVerification(communityUser3, Verification.UNAUTHORIZED);
		// 未承認=>承認
		CommunityUserDO communityUser4 = createCommunityUser("test4", false);
		testUpdateCeroVerification(communityUser4, Verification.UNAUTHORIZED);
		testUpdateCeroVerification(communityUser4, Verification.AUTHORIZED);
		// 承認=>未承認
		CommunityUserDO communityUser5 = createCommunityUser("test5", false);
		testUpdateCeroVerification(communityUser5, Verification.AUTHORIZED);
		testUpdateCeroVerification(communityUser5, Verification.UNAUTHORIZED);
		// 承認=>承認
		CommunityUserDO communityUser6 = createCommunityUser("test6", false);
		testUpdateCeroVerification(communityUser6, Verification.AUTHORIZED);
		testUpdateCeroVerification(communityUser6, Verification.AUTHORIZED);
	}

	/**
	 * メール設定関連処理を検証します。
	 */
	@Test
	public void testMailSettingList() {
		CommunityUserDO communityUser = createCommunityUser("test16", false);
		List<MailSettingDO> mailSettings = new ArrayList<MailSettingDO>();
		MailSettingDO mailSetting = new MailSettingDO();
		mailSetting.setCommunityUserId(communityUser.getCommunityUserId());
		mailSetting.setMailSettingType(MailSettingType.REVIEW_LIMIT);
		mailSetting.setMailSettingValue(MailSendTiming.NOT_NOTIFY);
		mailSettings.add(mailSetting);
		userService.saveMailSettings(mailSettings);

		List<MailSettingCategoryVO> mailSettingCategoris = userService
				.findMailSettingList(communityUser.getCommunityUserId());
		List<MailSettingMasterDO> masterList = mailSettingDao
				.findMailSettingMaster();

		assertEquals(masterList.size(), mailSettingCategoris.get(0)
				.getMailSettings().size()
				+ mailSettingCategoris.get(1).getMailSettings().size()
				+ mailSettingCategoris.get(2).getMailSettings().size());

		for (int i = 0; i < mailSettingCategoris.size(); i++) {
			for (MailSettingVO vo : mailSettingCategoris.get(i)
					.getMailSettings()) {
				if (MailSettingType.REVIEW_LIMIT == vo.getMailSettingMaster()
						.getMailSettingType()) {
					assertEquals(MailSendTiming.NOT_NOTIFY,
							vo.getSelectedValue());
				} else {
					for (MailSettingMasterDO master : masterList) {
						if (master.getMailSettingType() == vo
								.getMailSettingMaster().getMailSettingType()) {
							assertEquals(master, vo.getMailSettingMaster());
						}
					}
				}
			}
		}
	}

	/**
	 * 指定したコミュニティユーザーのSNS連携情報を保存・検証します。
	 * TODO
	 */
	@Test
	public void testSaveSocialMediaSetting() {
		List<SocialMediaSettingDO> socialMediaSettings =
				userService.findSocialMediaSettingList(communityUser.getCommunityUserId());
		for(SocialMediaSettingDO socialMediaSetting : socialMediaSettings) {
			if(SocialMediaType.TWITTER.equals(socialMediaSetting.getSocialMediaType())) {
				socialMediaSetting.setSocialMediaAccountCode("test1");
				socialMediaSetting.setSocialMediaAccountName("test1");
			} else if(SocialMediaType.FACEBOOK.equals(socialMediaSetting.getSocialMediaType())) {

			}
		}
		userService.saveSocialMediaSetting(socialMediaSettings);
	}

	/**
	 * はじめてのヒント画面を表示させない設定を保存・検証します。
	 */
	@Test
	public void testHideWelcomeHint() {
		assertTrue(userService.isShowWelcomeHint(communityUser.getCommunityUserId()));
		userService.hideWelcomeHint(communityUser.getCommunityUserId());
		assertEquals(false, userService.isShowWelcomeHint(communityUser.getCommunityUserId()));
	}

	/**
	 * 会員登録のテストです。
	 */
	private void testCreateCommunityUser(String communityName, boolean withImage) {
		CommunityUserDO communityUser = createCommunityUser(communityName,
				withImage);
		CommunityUserDO hBaseCommunityUser = getCommunityUserByHbase(communityUser
				.getCommunityUserId());
		CommunityUserDO solrCommunityUser = getCommunityUserBySolr(communityUser
				.getCommunityUserId());

		// 登録したコミュニティユーザーの検証(hBase)
		testCommunityUser(communityUser, hBaseCommunityUser, false);
		assertEquals(communityUser.getCommunityId().toString(),
				hBaseCommunityUser.getCommunityId().toString());
		assertEquals(communityUser.getCommunityName().toString(),
				hBaseCommunityUser.getCommunityName().toString());
		testInformation(hBaseCommunityUser.getCommunityUserId());
		testAnnounce(hBaseCommunityUser.getCommunityUserId());
		assertTrue(null != hBaseCommunityUser.getNormalizeCommunityName());

		// 登録したコミュニティユーザーの検証(solr)
		testCommunityUser(communityUser, solrCommunityUser, false);
		assertEquals(communityUser.getCommunityId().toString(),
				solrCommunityUser.getCommunityId().toString());
		assertEquals(communityUser.getCommunityName().toString(),
				solrCommunityUser.getCommunityName().toString());
		testInformation(solrCommunityUser.getCommunityUserId());
		testAnnounce(solrCommunityUser.getCommunityUserId());

		// 登録した画像の検証
		if (withImage) {
			// 画像情報の検証
			testCommunityUserImage(communityUser, hBaseCommunityUser, false);
			testCommunityUserImage(communityUser, solrCommunityUser, false);
			// 画像の実態の検証
			final ImageDO savedImage = hBaseOperations.load(ImageDO.class,
					communityUser.getImageHeader().getImageId());
			assertEquals(false, savedImage.isTemporaryFlag());
			assertEquals(communityUser.getCommunityUserId(),
					savedImage.getCommunityUserId());
			assertTrue(null == savedImage.getDeleteDate());
			assertTrue(!savedImage.isDeleteFlag());
			assertTrue(null != savedImage.getRegisterDateTime());
			assertTrue(null != savedImage.getModifyDateTime());

			if(!resourceConfig.akamaiSkip){
				new SshCommand(resourceConfig.imageServerHostPrimary,
						resourceConfig.imageServerUserPrimary,
						resourceConfig.imageServerPasswordPrimary) {
					@Override
					public void start() throws IOException {
						SCPClient scp = getConnection().createSCPClient();
						String remoteFilePath = savedImage.getImageUrl().substring(
								savedImage.getImageUrl().lastIndexOf(
										resourceConfig.imageUploadPath));
						int index = remoteFilePath.lastIndexOf("/");
						String remoteDir = remoteFilePath.substring(0, index);
						String remoteFileName = remoteFilePath.substring(index + 1);
						ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
						scp.get((resourceConfig.imageServerSaveDirPrimary
								+ remoteDir + "/" + remoteFileName).replace("//",
									"/"), outputStream);
						assertEquals(new String(savedImage.getData()), new String(
								outputStream.toByteArray()));
					}
				};
			}
		}
	}

	private void testUpdateCommunityUser(String beforeCommunityName, String communityName,
			boolean createWithImage, boolean updateWithImage,
			Verification adultVerification, Verification ceroVerification) {
		CommunityUserDO communityUser = createCommunityUser(beforeCommunityName, createWithImage);
		CommunityUserDO oldCommunityUser = hBaseOperations.load(
				CommunityUserDO.class, communityUser.getCommunityUserId(),
				communityUserPath);
		testCommunityUser(oldCommunityUser, communityUser, false);

		communityUser.setCommunityName(communityName);
		if (adultVerification != null) {
			communityUser.setAdultVerification(adultVerification);
		}
		if (ceroVerification != null) {
			communityUser.setCeroVerification(ceroVerification);
		}

		if (updateWithImage) {
			image = new ImageDO();
			image.setData(testImageData);
			image.setMimeType("images/jpeg");
			image.setWidth(400);
			image.setHeigth(500);
			imageService.createTemporaryImage(image);

			ImageDO thumbnailImage = new ImageDO();
			thumbnailImage.setData(testImageData);
			thumbnailImage.setMimeType("images/jpeg");
			imageService.createTemporaryImage(thumbnailImage);

			ImageHeaderDO imageHeader = new ImageHeaderDO();
			imageHeader.setImageId(image.getImageId());
			imageHeader.setPostContentType(PostContentType.PROFILE);
			communityUser.setImageHeader(imageHeader);

			ImageHeaderDO thumbnailImageHeader = new ImageHeaderDO();
			thumbnailImageHeader.setImageId(thumbnailImage.getImageId());
			thumbnailImageHeader.setPostContentType(PostContentType.PROFILE_THUMBNAIL);
			communityUser.setThumbnail(thumbnailImageHeader);
		}
		CommunityUserDO updateCommunityUser = userService
				.updateCommunityUser(communityUser);
		CommunityUserDO hBaseCommunityUser = getCommunityUserByHbase(communityUser
				.getCommunityUserId());
		CommunityUserDO solrCommunityUser = getCommunityUserBySolr(communityUser
				.getCommunityUserId());

		// 更新したコミュニティユーザーの検証(hBase)
		testCommunityUser(updateCommunityUser, hBaseCommunityUser, true);
		assertEquals(updateCommunityUser.getCommunityId().toString(),
				hBaseCommunityUser.getCommunityId().toString());
		assertEquals(communityName.toString(), hBaseCommunityUser
				.getCommunityName().toString());
		assertTrue(null != hBaseCommunityUser.getNormalizeCommunityName());

		// 更新したコミュニティユーザーの検証(solr)
		testCommunityUser(updateCommunityUser, solrCommunityUser, true);
		assertEquals(updateCommunityUser.getCommunityId().toString(),
				solrCommunityUser.getCommunityId().toString());
		assertEquals(communityName.toString(), solrCommunityUser
				.getCommunityName().toString());

		if (adultVerification != null) {
			assertEquals(adultVerification,
					solrCommunityUser.getAdultVerification());
			assertEquals(adultVerification,
					hBaseCommunityUser.getAdultVerification());
		}
		if (ceroVerification != null) {
			assertEquals(ceroVerification,
					hBaseCommunityUser.getCeroVerification());
			assertEquals(ceroVerification,
					solrCommunityUser.getCeroVerification());
		}

		if (updateWithImage) {
			// 登録した画像の検証(登録した画像が正しく保存されているか)
			// 画像情報の検証
			testCommunityUserImage(updateCommunityUser, hBaseCommunityUser, true);
			assertEquals(updateCommunityUser.getImageHeader().getImageId(),
					hBaseCommunityUser.getImageHeader().getImageId());
			testCommunityUserImage(updateCommunityUser, solrCommunityUser, true);
			// 画像の実態の検証
			final ImageDO savedImage = hBaseOperations.load(ImageDO.class,
					updateCommunityUser.getImageHeader().getImageId());
			assertEquals(false, savedImage.isTemporaryFlag());
			assertEquals(updateCommunityUser.getCommunityUserId(),
					savedImage.getCommunityUserId());
			assertTrue(null == savedImage.getDeleteDate());
			assertTrue(!savedImage.isDeleteFlag());
			assertTrue(null != savedImage.getRegisterDateTime());
			assertTrue(null != savedImage.getModifyDateTime());

			if(!resourceConfig.akamaiSkip) {
				new SshCommand(resourceConfig.imageServerHostPrimary,
						resourceConfig.imageServerUserPrimary,
						resourceConfig.imageServerPasswordPrimary) {
					@Override
					public void start() throws IOException {
						SCPClient scp = getConnection().createSCPClient();
						String remoteFilePath = savedImage.getImageUrl().substring(
								savedImage.getImageUrl().lastIndexOf(
										resourceConfig.imageUploadPath));
						int index = remoteFilePath.lastIndexOf("/");
						String remoteDir = remoteFilePath.substring(0, index);
						String remoteFileName = remoteFilePath.substring(index + 1);
						ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
						scp.get((resourceConfig.imageServerSaveDirPrimary
								+ remoteDir + "/" + remoteFileName).replace("//",
										"/"), outputStream);
						assertEquals(new String(savedImage.getData()), new String(
								outputStream.toByteArray()));
					}
				};
			}

			if (createWithImage) {
				// 更新に伴い、変更前の画像の確認
				// 古い画像が正常に削除されているかどうか検証します。
				ImageHeaderDO oldImageHeader = hBaseOperations.load(
						ImageHeaderDO.class,
						oldCommunityUser.getCommunityUserId());
				ImageDO oldImage = hBaseOperations.load(ImageDO.class,
						oldCommunityUser.getCommunityUserId());
				assertTrue(null == oldImageHeader);
				assertTrue(null == oldImage);
			}
		}
	}

	/**
	 * コミュニティユーザーを検証します。
	 *
	 * @param communityUser
	 *            コミュニティユーザー
	 * @param saveCommunityUser
	 *            保存されたコミュニティユーザー
	 */
	private void testCommunityUser(CommunityUserDO communityUser,
			CommunityUserDO saveCommunityUser, boolean updateFlg) {
		// 存在チェック
		assertNotNull(saveCommunityUser);
		assertNotNull(saveCommunityUser.getCommunityId());
		assertNotNull(saveCommunityUser.getCommunityUserId());
		assertNotNull(saveCommunityUser.getCommunityName());
		assertNotNull(saveCommunityUser.getHashCommunityId());
		assertNotNull(saveCommunityUser.getRegisterDateTime());
		assertNotNull(saveCommunityUser.getModifyDateTime());

		// ハッシュコミュニティIDが正しいハッシュか
		assertEquals(domainConfig.createHashCommunityId(saveCommunityUser
				.getCommunityId()), saveCommunityUser.getHashCommunityId());

		// 未編集のため、登録日時と編集日時が一致すること
		if (!updateFlg) {
			assertEquals(saveCommunityUser.getRegisterDateTime(),
					saveCommunityUser.getModifyDateTime());
		} else {
			assertTrue(saveCommunityUser.getRegisterDateTime() != saveCommunityUser
					.getModifyDateTime());
		}

		// 会員属性が有効か
		assertEquals(CommunityUserStatus.ACTIVE, saveCommunityUser.getStatus());
	}

	/**
	 * コミュニティユーザーに紐付くアナウンス登録情報を検証します。
	 *
	 * @param communityUserId
	 *            コミュニティユーザーID
	 */
	private void testAnnounce(String communityUserId) {
		AnnounceDO announce = announceDao.load(communityUserId,
				AnnounceType.WELCOME_HINT);
		assertNotNull(announce);
	}

	/**
	 * コミュニティユーザーに紐付くお知らせを検証します。
	 *
	 * @param communityUserId
	 *            コミュニティユーザーID
	 */
	private void testInformation(String communityUserId) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("communityUserId_s:");
		buffer.append(communityUserId);
		SolrQuery query = new SolrQuery(buffer.toString());
		SearchResult<InformationDO> hBaseInformation = new SearchResult<InformationDO>(
				solrOperations
						.findByQuery(query, InformationDO.class, informationPath));
		assertNotNull(hBaseInformation.getDocuments());
		assertEquals(InformationType.WELCOME, hBaseInformation.getDocuments()
				.get(0).getInformationType());

		InformationDO solrInformation = solrOperations.load(
				InformationDO.class, hBaseInformation.getDocuments().get(0)
						.getInformationId(), informationPath);
		assertNotNull(solrInformation);
		assertEquals(InformationType.WELCOME,
				solrInformation.getInformationType());
	}

	/**
	 * コミュニティユーザーに紐付く画像を検証します。
	 *
	 * @param communityUser
	 *            コミュニティユーザー
	 * @param saveCommunityUser
	 *            保存されたコミュニティユーザー
	 */
	private void testCommunityUserImage(CommunityUserDO communityUser,
			CommunityUserDO saveCommunityUser, boolean updateFlg) {
		assertTrue(saveCommunityUser.getImageHeader().getImageUrl() != null);
		assertEquals(communityUser.getImageHeader().getImageId(),
				saveCommunityUser.getImageHeader().getImageId());
		assertEquals(communityUser.getCommunityUserId(), saveCommunityUser
				.getImageHeader().getOwnerCommunityUserId());
		assertEquals(PostContentType.PROFILE, saveCommunityUser
				.getImageHeader().getPostContentType());
		if (!updateFlg) {
			assertEquals(saveCommunityUser.getRegisterDateTime(),
					saveCommunityUser.getImageHeader().getPostDate());
		} else {
			assertTrue(saveCommunityUser.getRegisterDateTime() != saveCommunityUser
					.getImageHeader().getPostDate());
		}
		assertEquals(ContentsStatus.SUBMITTED, saveCommunityUser
				.getImageHeader().getStatus());
		assertEquals(image.getHeigth(), saveCommunityUser.getImageHeader().getHeigth());
		assertEquals(image.getWidth(), saveCommunityUser.getImageHeader().getWidth());
	}

	/**
	 * ニックネームの重複チェックを検証します。
	 */
	private void testDuplicateCommunityName(String communityNameOne,
			String communityNameTwo) {
		createCommunityUser(communityNameOne, false);
		try {
			createCommunityUser(communityNameTwo, false);
			fail();
		} catch (UniqueConstraintException e) {
			assertTrue(true);
		}
	}

	/**
	 * アダルト表示確認ステータスの更新を検証します。
	 */
	private void testUpdateAdultVerification(CommunityUserDO communityUser,
			Verification verification) {
		userService.updateAdultVerification(communityUser.getCommunityUserId(),
				verification);
		CommunityUserDO hBaseCommunityUser = getCommunityUserByHbase(communityUser
				.getCommunityUserId());
		CommunityUserDO solrCommunityUser = getCommunityUserBySolr(communityUser
				.getCommunityUserId());
		assertEquals(verification, hBaseCommunityUser.getAdultVerification());
		assertEquals(verification, solrCommunityUser.getAdultVerification());
	}

	/**
	 * セロ表示確認ステータスの更新を検証します。
	 */
	private void testUpdateCeroVerification(CommunityUserDO communityUser,
			Verification verification) {
		userService.updateCeroVerification(communityUser.getCommunityUserId(),
				verification);
		CommunityUserDO hBaseCommunityUser = getCommunityUserByHbase(communityUser
				.getCommunityUserId());
		CommunityUserDO solrCommunityUser = getCommunityUserBySolr(communityUser
				.getCommunityUserId());
		assertEquals(verification, hBaseCommunityUser.getCeroVerification());
		assertEquals(verification, solrCommunityUser.getCeroVerification());
	}

	
	
	@Test
	public void testFindNewsFeedByCommunityUserId(){
		
//		
//		　1. フォローユーザーのレビュー投稿(101:USER_REVIEW)
//		　2. 自身のレビュー投稿(101:USER_REVIEW)
//		　3. フォロー商品へのレビュー投稿(201:PRODUCT_REVIEW)
//		　4. 購入商品へのレビュー投稿(201:PRODUCT_REVIEW)
//
//		□質問投稿があった場合
//		　1. フォローユーザーの質問投稿(102:USER_QUESTION)
//		　2. 自身の質問投稿(102:USER_QUESTION)
//		　3. フォロー商品への質問投稿(202:PRODUCT_QUESTION)
//		　4. 購入商品への質問投稿(202:PRODUCT_QUESTION)
//
//		□回答投稿があった場合
//		　1. フォローユーザーの回答投稿(103:USER_ANSWER)
//		　2. 自身の回答投稿(103:USER_ANSWER)
//		　3. フォロー質問への回答投稿(301:QUESTION_ANSWER)
//		　4. フォロー商品への回答投稿(203:PRODUCT_ANSWER)
//		　5. 購入商品への回答投稿(203:PRODUCT_ANSWER)
//
//		□画像投稿があった場合
//		　1. フォローユーザーの画像投稿(104:USER_IMAGE)
//		　2. 自身の画像投稿(104:USER_IMAGE)
//		　3. フォロー商品への画像投稿(204:PRODUCT_IMAGE)
//		　4. 購入商品への画像投稿(204:PRODUCT_IMAGE)

		// フォローユーザー作成
		//　商品の取得
		ProductDO  product = hBaseOperations.load(ProductDO.class, "100000001001377918");
		// ユーザー作成
		CommunityUserDO followUser = createCommunityUser("フォローユーザー", false);
		CommunityUserDO comunityUser = createCommunityUser("ユーザー", false);
		//　ユーザーに購入履歴追加
		Date salesDate = getDate("2011/10/01");
		// 注文履歴を登録します。
		createReceipt(comunityUser, product.getJan(), salesDate);
		createReceipt(followUser, product.getJan(), salesDate);

		//　ユーザーに商品フォロー追加
		followService.followProduct(comunityUser.getCommunityUserId(), product.getSku(), false);

		// フォローユーザーのレビュー
		createReviewSet(followUser, product);
		//assert
		SearchResult<NewsFeedVO> results = userService.findNewsFeedByCommunityUserId(comunityUser.getCommunityUserId(), 100, null, false);

		assertTrue((2 == results.getNumFound()));
		assertEquals(2, results.getDocuments().size());
		
		// ユーザーのレビュー
		createReviewSet(comunityUser, product);
		//assert
		results = userService.findNewsFeedByCommunityUserId(comunityUser.getCommunityUserId(), 100, null, false);
		assertTrue((4 == results.getNumFound()));
		assertEquals(3, results.getDocuments().size());
		
		//　ユーザーにフォローユーザーフォロー追加
		followService.followCommunityUser(comunityUser.getCommunityUserId(), followUser.getCommunityUserId(), false);
		// フォローユーザーのレビュー
		createReviewSet(followUser, product);
		//assert
		results = userService.findNewsFeedByCommunityUserId(comunityUser.getCommunityUserId(), 100, null, false);
		assertTrue((8 == results.getNumFound()));
		assertEquals(5, results.getDocuments().size());
		
	}

	/**
	 * レビューを作成します。
	 * @param communityUser
	 */
	private ReviewDO createReviewSet(CommunityUserDO communityUser, ProductDO product) {
		ReviewDO review = new ReviewDO();
		review.setReviewType(ReviewType.REVIEW_AFTER_FEW_DAYS);
		review.setCommunityUser(communityUser);
		review.setProduct(product);
		ReviewDecisivePurchaseDO reviewDecisivePurchase1 = new ReviewDecisivePurchaseDO();
		reviewDecisivePurchase1.setDecisivePurchase(new DecisivePurchaseDO());
		reviewDecisivePurchase1.getDecisivePurchase().setDecisivePurchaseName("購入の決め手-デザイン");
		review.getReviewDecisivePurchases().add(reviewDecisivePurchase1);
		review.setStatus(ContentsStatus.SUBMITTED);
		StringBuilder html = new StringBuilder();
		createImage(communityUser);
		String tempImageUrl = resourceConfig.temporaryImageUrl + image.getImageId();
		html.append("<img src=\"" + tempImageUrl + "\" />");
		html.append("<script type=\"text/javascript\">alert('" + "レビュー本文" + "')</script>");
		review.setReviewBody(html.toString());
		review = reviewService.saveReview(review);
		return review;
	}
	
	
}
