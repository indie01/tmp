/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.core.aop.TimestampHolder;
import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.yodobashi.community.resource.dao.MailSettingDao;
import com.kickmogu.yodobashi.community.resource.dao.util.IdUtil;
import com.kickmogu.yodobashi.community.resource.domain.MailSettingDO;
import com.kickmogu.yodobashi.community.resource.domain.MailSettingMasterDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.MailSendTiming;
import com.kickmogu.yodobashi.community.resource.domain.constants.MailSettingType;

/**
 * メール設定 DAO の実装クラスです。
 * @author kamiike
 *
 */
@Service
public class MailSettingDaoImpl implements MailSettingDao, InitializingBean {

	/**
	 * HBaseアクセサです。
	 */
	@Autowired @Qualifier("default")
	private HBaseOperations hBaseOperations;

	/**
	 * タイムスタンプホルダーです。
	 */
	@Autowired
	private TimestampHolder timestampHolder;

	/**
	 * メール設定マスタ情報のリストです。
	 */
	private List<MailSettingMasterDO> mailSettingMasters;

	/**
	 * メール設定値を返します。設定が無い場合はデフォルト値を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param mailSettingType メール設定タイプ
	 * @return メール設定情報値
	 */
	@Override
	public MailSendTiming loadMailSettingValueWithDefault(
			String communityUserId, MailSettingType mailSettingType) {
		String mailSettingId = createMailSettingId(
				communityUserId, mailSettingType);
		MailSettingDO mailSetting = hBaseOperations.load(
				MailSettingDO.class, mailSettingId);
		if (mailSetting != null) {
			return mailSetting.getMailSettingValue();
		} else {
			for (MailSettingMasterDO master : mailSettingMasters) {
				if (master.getMailSettingType().equals(mailSettingType)) {
					return master.getDefaultValue();
				}
			}
		}
		throw new IllegalStateException(
				"MailSettingType is not registerd. input = " + mailSettingType.name());
	}

	/**
	 * メール設定情報IDを生成して返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param mailSettingType メール設定タイプ
	 * @return メール設定情報ID
	 */
	@Override
	public String createMailSettingId(
			String communityUserId, MailSettingType mailSettingType) {
		return IdUtil.createIdByConcatIds(communityUserId, mailSettingType.getCode());
	}

	/**
	 * メール設定情報マスタを登録します。
	 * @param mailSettingMaster メール設定情報マスタ
	 */
	@Override
	public void createMailSettingMaster(MailSettingMasterDO mailSettingMaster) {
		mailSettingMaster.setRegisterDateTime(
				timestampHolder.getTimestamp());
		mailSettingMaster.setModifyDateTime(
				timestampHolder.getTimestamp());
		hBaseOperations.save(mailSettingMaster);
	}

	/**
	 * メール設定情報マスタを検索して返します。
	 * @return メール設定情報マスタ
	 */
	@Override
	public List<MailSettingMasterDO> findMailSettingMaster() {
		return mailSettingMasters;
	}

	/**
	 * 古い設定を全て破棄します。
	 * @param communityUserId コミュニティユーザーID
	 */
	@Override
	public void destroyOldSettings(String communityUserId) {
		hBaseOperations.scanDeleteWithIndex(
				MailSettingDO.class, "communityUserId", communityUserId);
	}

	/**
	 * 初期化処理を行います。
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		mailSettingMasters = hBaseOperations.scanAll(MailSettingMasterDO.class);
		Collections.sort(mailSettingMasters, new Comparator<MailSettingMasterDO>() {

			/**
			 * ソートします。
			 * @param o1 メール設定情報マスタ
			 * @param o2メール設定情報マスタ
			 * @return 大小比較結果
			 */
			@Override
			public int compare(MailSettingMasterDO o1, MailSettingMasterDO o2) {
				return o1.getOrderNo() - o2.getOrderNo();
			}

		});
	}

	/**
	 * メール設定情報を保存します。
	 * @param mailSetting メール設定情報
	 */
	@Override
	public void saveMailSetting(MailSettingDO mailSetting) {
		mailSetting.setMailSettingId(createMailSettingId(
				mailSetting.getCommunityUserId(),
				mailSetting.getMailSettingType()));
		MailSettingDO oldMailSetting = 
				hBaseOperations.load(MailSettingDO.class, mailSetting.getMailSettingId(), Path.includeProp("mailSettingId"));
		if (oldMailSetting == null) {
			mailSetting.setRegisterDateTime(timestampHolder.getTimestamp());
		} else {
			if (null == oldMailSetting.getRegisterDateTime()) {
				// 以前のバグで登録日時がnullになってしまっているユーザーは現在日付を登録
				mailSetting.setRegisterDateTime(timestampHolder.getTimestamp());
			} else {
				mailSetting.setRegisterDateTime(oldMailSetting.getRegisterDateTime());
			}
		}
		mailSetting.setModifyDateTime(timestampHolder.getTimestamp());
		hBaseOperations.save(mailSetting);
	}

	/**
	 * メール設定情報を検索して返します。
	 * @param mailSettingIds メール設定情報IDリスト
	 * @return メール設定情報リスト
	 */
	@Override
	public Collection<MailSettingDO> findMailSettingByIds(List<String> mailSettingIds) {
		return hBaseOperations.find(MailSettingDO.class, String.class, mailSettingIds).values();
	}
}
