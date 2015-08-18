/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao;

import java.util.Collection;
import java.util.List;

import com.kickmogu.yodobashi.community.performance.PerformanceTest;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Frequency;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Type;
import com.kickmogu.yodobashi.community.resource.domain.MailSettingDO;
import com.kickmogu.yodobashi.community.resource.domain.MailSettingMasterDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.MailSendTiming;
import com.kickmogu.yodobashi.community.resource.domain.constants.MailSettingType;

/**
 * メール設定 DAO です。
 * @author kamiike
 *
 */
public interface MailSettingDao {

	/**
	 * メール設定値を返します。設定が無い場合はデフォルト値を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param mailSettingType メール設定タイプ
	 * @return メール設定情報値
	 */
	public MailSendTiming loadMailSettingValueWithDefault(
			String communityUserId, MailSettingType mailSettingType);

	/**
	 * メール設定情報IDを生成して返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param mailSettingType メール設定タイプ
	 * @return メール設定情報ID
	 */	
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="ID生成なのでテスト対象外")
	public String createMailSettingId(
			String communityUserId, MailSettingType mailSettingType);

	/**
	 * メール設定情報マスタを登録します。
	 * @param mailSettingMaster メール設定情報マスタ
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="頻度は極稀なのでテスト対象外")
	public void createMailSettingMaster(MailSettingMasterDO mailSettingMaster);

	/**
	 * メール設定情報マスタを検索して返します。
	 * @return メール設定情報マスタ
	 */
	public List<MailSettingMasterDO> findMailSettingMaster();

	/**
	 * 古い設定を全て破棄します。
	 * @param communityUserId コミュニティユーザーID
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.RARE, frequencyComment="マイページでの更新処理なので頻度は稀")
	public void destroyOldSettings(String communityUserId);

	/**
	 * メール設定情報を保存します。
	 * @param mailSetting メール設定情報
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.RARE, frequencyComment="マイページでの更新処理なので頻度は稀")
	public void saveMailSetting(MailSettingDO mailSetting);

	/**
	 * メール設定情報を検索して返します。
	 * @param mailSettingIds メール設定情報IDリスト
	 * @return メール設定情報リスト
	 */
	public Collection<MailSettingDO> findMailSettingByIds(List<String> mailSettingIds);
}
