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
import com.kickmogu.lib.hadoop.hbase.backup.HBaseRestoreService;
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
public class CommunityHBaseRestoreJob_DirectLoadingTest {

	private static final String TEST_DATE_STRING = "20120417131415";
	private String baseDir = "/home/comm/backup/current/hbase/";
	private String localDir = "./test_data/";

	private String host = "kmcms04";
	private String ftpUser = "comm";
	private String ftpPassword = "comm00";
	
	private Class<?> testClass = ReviewDO.class;

	@Autowired
	@Qualifier("default")
	private HBaseOperations hbaseOperations;
	
	@Autowired
	@Qualifier("MySite")
	private HBaseContainer hbaseContainer;

	@Autowired @Qualifier("simple")
	private HBaseRestoreService restoreService;

	@Autowired
	private ApplicationContext applicationContext;

	private TimestampConfigurableProcessContextHolderImpl processContextHolderImpl = new TimestampConfigurableProcessContextHolderImpl();

	private String tableNamePrefix;

	@Before
	public void setup() {
		tableNamePrefix = hbaseContainer.getMeta().getTableNamePrefix();
	}

	private void clearTable() {
		HBaseTemplate template = (HBaseTemplate) ((ExternalEntityHBaseTemplate) AopUtil.getTargetObject(hbaseOperations, HBaseOperations.class)).getOrgOperations();
		template.setProcessContextHolder(processContextHolderImpl);
		hbaseContainer.getAdministrator().dropTable(testClass);
		hbaseContainer.getAdministrator().createTable(testClass);
	}

	public void createDumpFile(String dateString) throws Exception {
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
		CommunityHBaseBackupToDumpJob.execute(applicationContext, new String[] { "-s", Long.valueOf(backupTimestamp).toString(), "-t",
				hbaseContainer.getMeta().getTableMeta(ReviewDO.class).getTableName(), "-f", "cf", "-b", dateString, "-Dio.sort.mb=8" });
	}
	
	@Test
	public void test_backUpAndRectore() {
		try {
			// バックアップ(MapReduceJob)
			clearTable();
			createDumpFile(TEST_DATE_STRING);
			String pre = dispHBase();
			
			// dumpファイルをローカルへダウンロード(ftp)
			String jobDir = getJobDir(baseDir + TEST_DATE_STRING);
			String remoteDir = baseDir + TEST_DATE_STRING + "/" + jobDir + "/" + tableNamePrefix + testClass.getSimpleName() + "/";
			List<File> files = downloadDumpFiles(remoteDir);		
			List<File> dumpFiles = Lists.newArrayList();
			List<File> hdrFiles = Lists.newArrayList();
			for (File file : files) {
				(file.getName().endsWith("dump")? dumpFiles:hdrFiles).add(file); 
			}
			
			// リストア
			clearTable();
			for (int i=0; i<hdrFiles.size(); i++) {
				restoreService.restoreFromFile(testClass, hdrFiles.get(i), dumpFiles.get(i), 1000);
			}

			String post = dispHBase();
			assertEquals(pre, post);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
	}

	public String dispHBase() {
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
