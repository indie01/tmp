package com.kickmogu.yodobashi.community.mapreduce.job;

import static org.junit.Assert.assertEquals;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;

import com.google.common.collect.Lists;
import com.kickmogu.lib.core.exception.CommonSystemException;
import com.kickmogu.lib.core.utils.AopUtil;
import com.kickmogu.lib.hadoop.hbase.HBaseContainer;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.lib.hadoop.hbase.impl.ExternalEntityHBaseTemplate;
import com.kickmogu.lib.hadoop.hbase.impl.HBaseTemplate;
import com.kickmogu.lib.hadoop.hbase.impl.TimestampConfigurableProcessContextHolderImpl;
import com.kickmogu.yodobashi.community.common.test.YcComJUnit4ClassRunner;
import com.kickmogu.yodobashi.community.resource.domain.CommentDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommentTargetType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ContentsStatus;


@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration("/serviceContext.xml")
public class CommunityHBaseRestoreJob_MR_DirectLoadingTest {

	private static final String TEST_DATE_STRING = "20120417131415";
	@SuppressWarnings("unused")
	private String baseDir = "/home/comm/backup/current/hbase/";
	private String localDir = "./test_data/";

	private String host = "kmcms04";
	private String ftpUser = "comm";
	private String ftpPassword = "comm00";
	
	@Autowired
	@Qualifier("default")
	private HBaseOperations hbaseOperations;
	
	@Autowired
	@Qualifier("MySite")
	private HBaseContainer hbaseContainer;

	@Autowired
	private ApplicationContext applicationContext;

	private TimestampConfigurableProcessContextHolderImpl processContextHolderImpl = new TimestampConfigurableProcessContextHolderImpl();

	@SuppressWarnings("unused")
	private String tableNamePrefix;

	@Before
	public void setup() {
		tableNamePrefix = hbaseContainer.getMeta().getTableNamePrefix();
	}

	private void clearTable() {
		HBaseTemplate template = (HBaseTemplate) ((ExternalEntityHBaseTemplate) AopUtil.getTargetObject(hbaseOperations, HBaseOperations.class)).getOrgOperations();
		template.setProcessContextHolder(processContextHolderImpl);
		hbaseContainer.getAdministrator().dropTable(ReviewDO.class);
		hbaseContainer.getAdministrator().createTable(ReviewDO.class);
		hbaseContainer.getAdministrator().dropTable(CommentDO.class);
		hbaseContainer.getAdministrator().createTable(CommentDO.class);
	}

	public void createDumpFile(String dateString, boolean backup) throws Exception {
		long backupTimestamp = System.currentTimeMillis();

		// single link
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
		hbaseOperations.save(review);

		// multi link
		CommunityUserDO communityUserA = new CommunityUserDO();
		communityUserA.setCommunityUserId("id-communityUserIdA");
		communityUserA.setCommunityName("val-communityNameA");

		CommunityUserDO communityUserB = new CommunityUserDO();
		communityUserB.setCommunityUserId("id-communityUserIdB");
		communityUserB.setCommunityName("val-communityNameB");

		ProductDO productA = new ProductDO();
		productA.setSku("id-skuA");
		productA.setBrndNm("val-brndNmA");

		ProductDO productB = new ProductDO();
		productB.setSku("id-skuB");
		productB.setBrndNm("val-brndNmB");

		review = new ReviewDO();
		review.setReviewId("r-0011");
		review.setEffective(true);
		review.setStatus(ContentsStatus.SUBMITTED);
		review.setWithdrawKey("val-withdrawKeyA");
		review.setCommunityUser(communityUserA);
		review.setProduct(productA);
		hbaseOperations.save(review);

		review = new ReviewDO();
		review.setReviewId("r-0012");
		review.setEffective(true);
		review.setStatus(ContentsStatus.SUBMITTED);
		review.setWithdrawKey("val-withdrawKeyB");
		review.setCommunityUser(communityUserB);
		review.setProduct(productA);
		hbaseOperations.save(review);

		review = new ReviewDO();
		review.setReviewId("r-0013");
		review.setEffective(true);
		review.setStatus(ContentsStatus.SUBMITTED);
		review.setWithdrawKey("val-withdrawKeyA");
		review.setCommunityUser(communityUserB);
		review.setProduct(productB);
		hbaseOperations.save(review);

		System.out.println("reviewDO save ok!");
		if (backup) {
			CommunityHBaseBackupToDumpJob.execute(applicationContext, new String[] { "-s", Long.valueOf(backupTimestamp).toString(), "-t",
				hbaseContainer.getMeta().getTableMeta(ReviewDO.class).getTableName(), "-f", "cf", "-b", dateString, "-Dio.sort.mb=8" });
		}
	}
	
	public void createDumpFile2(String dateString, boolean backup) throws Exception {
		long backupTimestamp = System.currentTimeMillis();
		
		CommunityUserDO communityUser1 = new CommunityUserDO();
		communityUser1.setCommunityUserId("id-communityUserId1");
		communityUser1.setCommunityName("val-communityName1");
		
		CommunityUserDO communityUser2 = new CommunityUserDO();
		communityUser2.setCommunityUserId("id-communityUserId1");
		communityUser2.setCommunityName("val-communityName1");
		
		CommentDO c1 = new CommentDO();
		c1.setCommentId("c-1");
		c1.setCommunityUser(communityUser1);
		c1.setRelationReviewOwnerId("id-relationReviewOwnerId");
		c1.setTargetType(CommentTargetType.REVIEW);
		c1.setRelationQuestionOwnerId("id-relationQuestionOwnerId");
		c1.setRelationQuestionAnswerOwnerId("id-relationQuestionAnswerOwnerId");
		c1.setRelationImageOwnerId("id-relationImageOwnerId");
		c1.setWithdrawKey("val-withdrawKey");
		
		CommentDO c2 = new CommentDO();
		c2.setCommentId("c-2");
		c2.setCommunityUser(communityUser1);
		c2.setRelationReviewOwnerId("id-relationReviewOwnerId");
		c2.setTargetType(CommentTargetType.REVIEW);
		c2.setRelationQuestionOwnerId("id-relationQuestionOwnerId");
		c2.setRelationQuestionAnswerOwnerId("id-relationQuestionAnswerOwnerId");
		c2.setRelationImageOwnerId("id-relationImageOwnerId");
		c2.setWithdrawKey("val-withdrawKey");
		
		CommentDO c3 = new CommentDO();
		c3.setCommentId("c-3");
		c3.setCommunityUser(communityUser2);
		c3.setRelationReviewOwnerId("id-relationReviewOwnerId");
		c3.setTargetType(CommentTargetType.REVIEW);
		c3.setRelationQuestionOwnerId("id-relationQuestionOwnerId");
		c3.setRelationQuestionAnswerOwnerId("id-relationQuestionAnswerOwnerId");
		c3.setRelationImageOwnerId("id-relationImageOwnerId");
		c3.setWithdrawKey("val-withdrawKey");
		
		hbaseOperations.save(c1);
		hbaseOperations.save(c2);
		hbaseOperations.save(c3);
		
		System.out.println("commentDO save ok!");
		if (backup) {
			CommunityHBaseBackupToDumpJob.execute(applicationContext, new String[] { "-s", Long.valueOf(backupTimestamp).toString(), "-t",
				hbaseContainer.getMeta().getTableMeta(CommentDO.class).getTableName(), "-f", "cf", "-b", dateString, "-Dio.sort.mb=8" });
		}
	}
	
	@Test
	public void test_DirectRectoreJob() {
		try {
			// バックアップ(MapReduceJob)
			clearTable();
			createDumpFile(TEST_DATE_STRING, false);
			String pre1= dispHBaseReviewDO();
			createDumpFile2(TEST_DATE_STRING, false);
			String pre2= dispHBaseCommentDO();
			
			// dumpファイルをローカルへダウンロード(ftp)
//			String jobDir = getJobDir(baseDir + TEST_DATE_STRING);
//			String remoteDir = baseDir + TEST_DATE_STRING + "/" + jobDir + "/" + tableNamePrefix + "CommentDO" + "/";
//			List<File> files = downloadDumpFiles(remoteDir);		
//			List<File> dumpFiles = Lists.newArrayList();
//			List<File> hdrFiles = Lists.newArrayList();
//			for (File file : files) {
//				System.out.println(file);
//				(file.getName().endsWith("dump")? dumpFiles:hdrFiles).add(file);
//			}
			
			clearTable();
			CommunityHBaseDirectRestoreJob.execute(applicationContext, new String[] {
					"-d", "/tmp/saka/data/",
					"-w", "/tmp/saka/restore_test/",
					"-o", "64",
					"-s", "2",
					"-Dio.sort.mb=8" });
			
			String after1= dispHBaseReviewDO();
			String after2= dispHBaseCommentDO();
			System.out.println(after1);
			System.out.println(after2);
			
			assertEquals(pre1, after1);
			assertEquals(pre2, after2);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
	}

	public String dispHBaseCommentDO() {
		StringWriter strw = new StringWriter();
		PrintWriter out = new PrintWriter(strw); 
		out.flush();
		
		out.println("CommentDO record");
		dispComment(out, hbaseOperations.load(CommentDO.class, "c-1"));
		dispComment(out, hbaseOperations.load(CommentDO.class, "c-2"));
		dispComment(out, hbaseOperations.load(CommentDO.class, "c-3"));
		
		out.println("index-table [communityUserId] find key [id-communityUserId1]");
		dispCommentList(out, hbaseOperations.findWithIndex(CommentDO.class, "communityUserId", "id-communityUserId1"));

		out.println("index-table [relationReviewOwnerId] find key [id-relationReviewOwnerId]");
		dispCommentList(out, hbaseOperations.findWithIndex(CommentDO.class, "relationReviewOwnerId", "id-relationReviewOwnerId"));
		
		out.println("index-table [relationQuestionAnswerOwnerId] find key [id-relationQuestionAnswerOwnerId]");
		dispCommentList(out, hbaseOperations.findWithIndex(CommentDO.class, "relationQuestionAnswerOwnerId", "id-relationQuestionAnswerOwnerId"));
		
		out.println("index-table [relationImageOwnerId] find key [id-relationImageOwnerId]");
		dispCommentList(out, hbaseOperations.findWithIndex(CommentDO.class, "relationImageOwnerId", "id-relationImageOwnerId"));

		out.println("index-table [withdrawKey] find key [val-withdrawKey]");
		dispCommentList(out, hbaseOperations.findWithIndex(CommentDO.class, "withdrawKey", "val-withdrawKey"));
		
		return strw.toString();
	}
	private void dispCommentList(PrintWriter out, List<CommentDO> list) {
		for (CommentDO review : list) {
			dispComment(out, review);
		}
	}

	private void dispComment(PrintWriter out, CommentDO cmt) {
		if (cmt == null)
			return;
		out.println("\t{");
		out.println("\t\tcommentId:" + cmt.getCommentId());
		out.println("\t\ttargetType:" + cmt.getTargetType());
		out.println("\t\trelationReviewOwnerId:" + cmt.getRelationReviewOwnerId());
		out.println("\t\trelationQuestionAnswerOwnerId:" + cmt.getRelationQuestionAnswerOwnerId());
		out.println("\t\trelationImageOwnerId:" + cmt.getRelationImageOwnerId());
		out.println("\t\twithdrawKey:" + cmt.getWithdrawKey());
		if (cmt.getCommunityUser() != null)
			out.println("\t\tcommynityUserId:" + cmt.getCommunityUser().getCommunityUserId());
		out.println("\t}");
	}
	public String dispHBaseReviewDO() {
		StringWriter strw = new StringWriter();
		PrintWriter out = new PrintWriter(strw); 

		out.println("--- multi link ---");
		// リストア後にテーブルを参照
		out.println("ReviewDO record");
		dispReview(out, hbaseOperations.load(ReviewDO.class, "r-0011"));
		dispReview(out, hbaseOperations.load(ReviewDO.class, "r-0012"));
		dispReview(out, hbaseOperations.load(ReviewDO.class, "r-0013"));

		// Indexから参照
		out.println("index-table [communityUserId] find key [id-communityUserIdA]");
		dispReviewList(out, hbaseOperations.findWithIndex(ReviewDO.class, "communityUserId", "id-communityUserIdA")); // productId,effective
		out.println("index-table [communityUserId] find key [id-communityUserIdB]");
		dispReviewList(out, hbaseOperations.findWithIndex(ReviewDO.class, "communityUserId", "id-communityUserIdB")); // productId,effective
		out.println("index-table [productId] find key [id-skuA]");
		dispReviewList(out, hbaseOperations.findWithIndex(ReviewDO.class, "productId", "id-skuA")); // status
		out.println("index-table [productId] find key [id-skuB]");
		dispReviewList(out, hbaseOperations.findWithIndex(ReviewDO.class, "productId", "id-skuB")); // status
		out.println("index-table [withdrawKey] find key [val-withdrawKeyA]");
		dispReviewList(out, hbaseOperations.findWithIndex(ReviewDO.class, "withdrawKey", "val-withdrawKeyA"));
		out.println("index-table [withdrawKey] find key [val-withdrawKeyB]");
		dispReviewList(out, hbaseOperations.findWithIndex(ReviewDO.class, "withdrawKey", "val-withdrawKeyB"));

		out.println("--- single link ---");
		// リストア後にテーブルを参照
		out.println("ReviewDO record");
		dispReview(out, hbaseOperations.load(ReviewDO.class, "r-0001"));

		// Indexから参照
		out.println("index-table [communityUserId] find key [id-communityUserId]");
		dispReviewList(out, hbaseOperations.findWithIndex(ReviewDO.class, "communityUserId", "id-communityUserId")); // productId,effective
		out.println("index-table [productId] find key [id-sku]");
		dispReviewList(out, hbaseOperations.findWithIndex(ReviewDO.class, "productId", "id-sku")); // status
		out.println("index-table [withdrawKey] find key [val-withdrawKey]");
		dispReviewList(out, hbaseOperations.findWithIndex(ReviewDO.class, "withdrawKey", "val-withdrawKey"));
		
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
		if (review.getProduct() != null)
			out.println("\t\tsku:" + review.getProduct().getSku());
		if (review.getCommunityUser() != null)
			out.println("\t\tcommynityUserId:" + review.getCommunityUser().getCommunityUserId());
		out.println("\t}");
	}

	public String getJobDir(String dir) throws IOException {
		FTPClient ftp = readyFtp(dir);
		FTPFile[] files = ftp.listFiles();
		ftp.disconnect();
		return files[files.length - 1].getName();
	}

	@SuppressWarnings("unused")
	private List<File> downloadDumpFiles(String dir) {
		List<File> localFiles = Lists.newArrayList();
		try {
			FTPFile[] files = findRemoteFiles(dir);
			for (int i = 0; i < files.length; i++) {
				String remoteFileName = dir + files[i].getName();
				String outFileName = localDir + files[i].getName();
				
				System.out.println("loding... " + remoteFileName + " size:" + files[i].getSize());
				saveRemoteFile(remoteFileName, outFileName);
				localFiles.add(new File(outFileName));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
		return localFiles;
	}

	private void saveRemoteFile(String remoteFile, String outFileName) throws Exception {
		OutputStream os = null;
		try {
			os = new BufferedOutputStream(new FileOutputStream(new File(outFileName)), 4096);
			retrieveFile(host, ftpUser, ftpPassword, remoteFile, os);
			os.flush();
		} catch (IOException e) {
			System.out.println(e.getMessage() + " " + remoteFile);
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (Exception e) {
				}
			}
		}
	}

	// ファイルダウンロード
	public static void retrieveFile(String host, String user, String password, String remoteFilename, OutputStream os) throws Exception {
		FTPClient ftpclient = new FTPClient();
		ftpclient.setBufferSize(4096);

		try {
			// 指定するホスト、ポートに接続します
			ftpclient.connect(host);
			int reply = ftpclient.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				// 接続エラー時処理
				Exception ee = new Exception("Can't Connect to :" + host);
				throw ee;
			}

			// ログイン
			if (ftpclient.login(user, password) == false) {
				// invalid user/password
				Exception ee = new Exception("Invalid user/password");
				throw ee;
			}

			// ファイル転送モード設定
			ftpclient.setFileType(FTP.BINARY_FILE_TYPE);
			// ftpclient.cwd("filetype=pdf");

			// ファイル受信
			ftpclient.retrieveFile(remoteFilename, os);

		} catch (IOException e) {
			throw e;
		} finally {
			try {
				ftpclient.disconnect(); // 接続解除
			} catch (IOException e) {
			}
		}
	}

	private FTPFile[] findRemoteFiles(String dir) throws IOException {
		FTPClient ftpClient = readyFtp(dir);
		try {
			return ftpClient.listFiles();
		} finally {
			ftpClient.disconnect();
		}
	}

	private FTPClient readyFtp(String workDir) {
		try {
			FTPClient ftpClient = new FTPClient();
			ftpClient.connect(host);
			int reply = ftpClient.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply))
				throw new CommonSystemException("ftp connect failed. host=" + host + ". reply=" + reply);
			if (!ftpClient.login(ftpUser, ftpPassword))
				throw new CommonSystemException("ftp login failed. host=" + host + ". user=" + ftpUser);
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

			if (!ftpClient.changeWorkingDirectory(workDir)) {
				throw new RuntimeException(workDir);
			}
			return ftpClient;
		} catch (Throwable th) {
			throw new CommonSystemException(th);
		}
	}
}
