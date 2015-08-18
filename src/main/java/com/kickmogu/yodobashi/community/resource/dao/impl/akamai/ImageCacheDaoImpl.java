package com.kickmogu.yodobashi.community.resource.dao.impl.akamai;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.fest.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.core.ssh.SshUtils;
import com.kickmogu.lib.core.utils.DumpUtil;
import com.kickmogu.yodobashi.community.common.exception.AkamaiAccessException;
import com.kickmogu.yodobashi.community.resource.config.AppConfigurationDao;
import com.kickmogu.yodobashi.community.resource.config.ResourceConfig;
import com.kickmogu.yodobashi.community.resource.dao.ImageCacheDao;
import com.kickmogu.yodobashi.community.resource.domain.constants.ImageDeleteResult;
import com.kickmogu.yodobashi.community.resource.domain.constants.ImageUploadResult;

/**
 * 画像キャッシュの DAO の実装です。
 * @author kamiike
 *
 */
@Service
public class ImageCacheDaoImpl implements ImageCacheDao{
	
	/**
	 * このクラスに関するログを出力するためのインスタンスです。
	 */
	private static final Logger LOG = LoggerFactory.getLogger(ImageCacheDaoImpl.class);
	
	private static final String akamaiPasswordKey = "akamai.password";
	
	/**
	 * リソースコンフィグです。
	 */
	@Autowired
	private ResourceConfig resourceConfig;

	/**
	 * アカマイサービスです。
	 */
	@Autowired
	private JavaClasses akamaiClient;
	
	/**
	 * 
	 */
	@Autowired
	private AppConfigurationDao appConfigurationDao;

	/**
	 * 指定した画像をアップロードします。
	 * @param data 画像データ
	 * @param remoteTargetDirectory アップロードディレクトリ
	 * @param remoteFileName アップロード先ファイル名
	 * @return 実行結果
	 */
	@Override
	public ImageUploadResult upload(byte[] data, String remoteTargetDirectory, final String remoteFileName) {
		if (resourceConfig.akamaiSkip) {
			LOG.warn("skip akamai upload.");
			return ImageUploadResult.SUCCESS;
		}
		String uploadDir = resourceConfig.imageServerSaveDirPrimary + remoteTargetDirectory;
		if (LOG.isInfoEnabled()) {
			LOG.info("ssh copy for primary: dir=" + uploadDir
					+ " file=" + remoteFileName);
		}
		RuntimeException primaryError = null;
		try {
			if( resourceConfig.imageServerStopPrimary )
				throw new RuntimeException("Primary Image Server Planned Outages");
			
			SshUtils.command(
					resourceConfig.imageServerHostPrimary,
					resourceConfig.imageServerUserPrimary,
					resourceConfig.imageServerPasswordPrimary,
					"mkdir -p " + uploadDir, true);
			SshUtils.scpUpload(
					resourceConfig.imageServerHostPrimary,
					resourceConfig.imageServerUserPrimary,
					resourceConfig.imageServerPasswordPrimary,
					data,
					uploadDir,
					remoteFileName);
		} catch (RuntimeException t) {
			primaryError = t;
		}
		if (resourceConfig.imageServerMirroring) {
			RuntimeException secondaryError = null;
			try {
				uploadDir = resourceConfig.imageServerSaveDirSecondary + remoteTargetDirectory;
				if (LOG.isInfoEnabled()) {
					LOG.info("ssh copy for secondary: dir=" + uploadDir
							+ " file=" + remoteFileName);
				}
				
				if( resourceConfig.imageServerStopSecondary )
					throw new RuntimeException("Secondary Image Server Planned Outages");
				
				SshUtils.command(
						resourceConfig.imageServerHostSecondary,
						resourceConfig.imageServerUserSecondary,
						resourceConfig.imageServerPasswordSecondary,
						"mkdir -p " + uploadDir, true);
				SshUtils.scpUpload(
						resourceConfig.imageServerHostSecondary,
						resourceConfig.imageServerUserSecondary,
						resourceConfig.imageServerPasswordSecondary,
						data,
						uploadDir,
						remoteFileName);
			} catch (RuntimeException t) {
				secondaryError = t;
			}
			if (primaryError != null && secondaryError != null) {
				throw primaryError;
			}
			if (secondaryError != null) {
				if (LOG.isErrorEnabled()) {
					LOG.error(secondaryError.toString(), secondaryError);
				}
				return ImageUploadResult.PRIMARY_ONLY;
			} else if (primaryError != null) {
				if (LOG.isErrorEnabled()) {
					LOG.error(primaryError.toString(), primaryError);
				}
				return ImageUploadResult.SECONDARY_ONLY;
			} else {
				return ImageUploadResult.SUCCESS;
			}
		} else {
			if (primaryError != null) {
				throw primaryError;
			}
			return ImageUploadResult.SUCCESS;
		}
	}

	/**
	 * 指定した画像を削除します。
	 * @param remoteTargetDirectory アップロードディレクトリ
	 * @param remoteFileName アップロード先ファイル名
	 * @return 実行結果
	 */
	@Override
	public ImageDeleteResult delete(String remoteTargetDirectory, final String remoteFileName) {
		if (resourceConfig.akamaiSkip) {
			LOG.warn("skip akamai delete.");
			return ImageDeleteResult.SUCCESS;
		}
		if (LOG.isInfoEnabled()) {
			LOG.info("ssh delete for primary: dir=" + remoteTargetDirectory
					+ " file=" + remoteFileName);
		}
		String remoteTargetDirectoryTmp = remoteTargetDirectory;
		if (!remoteTargetDirectoryTmp.endsWith("/")) {
			remoteTargetDirectoryTmp = remoteTargetDirectoryTmp + "/";
		}
		RuntimeException primaryError = null;
		try {
			if( resourceConfig.imageServerStopPrimary )
				throw new RuntimeException("Primary Image Server Planned Outages");
			
			SshUtils.command(
					resourceConfig.imageServerHostPrimary,
					resourceConfig.imageServerUserPrimary,
					resourceConfig.imageServerPasswordPrimary,
					"rm -fr " + resourceConfig.imageServerSaveDirPrimary
					+ remoteTargetDirectoryTmp + remoteFileName, true);
		} catch (RuntimeException t) {
			primaryError = t;
		}
		if (resourceConfig.imageServerMirroring) {
			RuntimeException secondaryError = null;
			try {
				if (LOG.isInfoEnabled()) {
					LOG.info("ssh delete for secondary: dir=" + remoteTargetDirectory
							+ " file=" + remoteFileName);
				}
				
				if( resourceConfig.imageServerStopSecondary )
					throw new RuntimeException("Secondary Image Server Planned Outages");
				
				SshUtils.command(
						resourceConfig.imageServerHostSecondary,
						resourceConfig.imageServerUserSecondary,
						resourceConfig.imageServerPasswordSecondary,
						"rm -fr " + resourceConfig.imageServerSaveDirSecondary
						+ remoteTargetDirectoryTmp + remoteFileName, true);
			} catch (RuntimeException t) {
				secondaryError = t;
			}
			if (primaryError != null && secondaryError != null) {
				throw primaryError;
			}
			if (secondaryError != null) {
				if (LOG.isErrorEnabled()) {
					LOG.error(secondaryError.toString(), secondaryError);
				}
				return ImageDeleteResult.PRIMARY_ONLY;
			} else if (primaryError != null) {
				if (LOG.isErrorEnabled()) {
					LOG.error(primaryError.toString(), primaryError);
				}
				return ImageDeleteResult.SECONDARY_ONLY;
			} else {
				return ImageDeleteResult.SUCCESS;
			}
		} else {
			if (primaryError != null) {
				throw primaryError;
			}
			return ImageDeleteResult.SUCCESS;
		}
	}

	/**
	 * 指定したパスのキャッシュをクリアします。
	 * @param path パス
	 */
	@Override
	public void clearCache(String path) {
		clearCaches(Arrays.array(path));
	}

	/**
	 * 指定したパスのキャッシュをクリアします。
	 * @param paths パスリスト
	 */
	@Override
	public void clearCaches(String[] paths) {
		if (resourceConfig.akamaiSkip) {
			LOG.warn("skip akamai clearCaches.");
			return;
		}
		
		invokeAkamaiApi("remove", paths);
	}

	
	

	/**
	 * アカマイAPIを呼び出します。
	 * @param action アクション
	 * @param paths パスリスト
	 */
	private void invokeAkamaiApi(String action, String paths[]) {

		String[] params = new String[] {
				"email-notification=" + resourceConfig.akamaiEMail,		// キャッシュ操作結果の通知先メールアドレス(複数指定の場合はカンマ区切り)
				"type=arl",										// 操作するファイルのタイプ URL(arl)/CP CODE
				"action=" + action ,								// 操作(remove/invalidate)
				"domain=production"								// ドメイン(本番を使用しているのでproduction)
		};

		String[] urls = new String[paths.length];
		for (int i = 0 ; i < paths.length; i++) {
			urls[i] = resourceConfig.akamaiBaseUrl + paths[i];
		}

		LOG.info("AkamaiStart:"+ ToStringBuilder.reflectionToString(this) + "\n"+ DumpUtil.dumpBean(params) + "\n" + DumpUtil.dumpBean(urls));

		try {
			PurgeApi api = akamaiClient.getPurgeApi();
			PurgeResult result = api.purgeRequest(resourceConfig.akamaiUser, appConfigurationDao.get(akamaiPasswordKey), "", params, urls);
			LOG.info("AkamaiFinished:" + ToStringBuilder.reflectionToString(result, ToStringStyle.MULTI_LINE_STYLE));

		} catch (Throwable e) {
			throw new AkamaiAccessException(e);
		}

	}

	
}
