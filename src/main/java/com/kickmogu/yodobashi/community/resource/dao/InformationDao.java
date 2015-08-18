/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao;


import java.util.Date;
import java.util.List;

import com.kickmogu.yodobashi.community.performance.PerformanceTest;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Frequency;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Type;
import com.kickmogu.yodobashi.community.resource.domain.InformationDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.constants.InformationType;

/**
 * お知らせ DAO です。
 * @author kamiike
 *
 */
public interface InformationDao {

	/**
	 * お知らせ情報を新規に登録します。
	 * @param information お知らせ情報
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.HIGH, frequencyComment="お知らせ登録の頻度は高い")
	public void createInformation(InformationDO information);

	/**
	 * お知らせ情報のインデックスを更新します。
	 * @param informationId お知らせ情報ID
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.HIGH, frequencyComment="お知らせ登録の頻度は高い")
	public void updateInformationInIndex(String informationId);

	/**
	 * お知らせ情報のインデックスを更新します。
	 * @param informationId お知らせ情報ID
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.HIGH, frequencyComment="お知らせ登録の頻度は高い")
	public void updateInformationInIndex(InformationDO information);

	/**
	 * 指定したコミュニティユーザーに対する未読お知らせ情報を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return 検索結果
	 */
	public SearchResult<InformationDO> findNoReadInformationByCommunityUserId(
			String communityUserId, int limit, int offset);

	/**
	 * 指定したコミュニティユーザーに対するお知らせ情報を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始日時
	 * @param previous より前を取得する場合、true
	 * @return 検索結果
	 */
	public SearchResult<InformationDO> findByCommunityUserId(
			String communityUserId, int limit, Date offsetTime, boolean previous, boolean excludeProduct);

	/**
	 * 指定したコミュニティユーザーに対するお知らせ情報で未読のカウントを返します。
	 * @param communityUserId コミュニティユーザーID
	 * @return 未読カウント
	 */
	public long countNoRead(String communityUserId);
	/**
	 * 指定したコミュニティユーザーに対するお知らせ情報のカウントを返します。ｓ
	 * @param communityUserId
	 * @return
	 */
	public long count(String communityUserId);

	/**
	 * 指定したコミュニティユーザーの全てのお知らせ情報を既読に更新します。
	 * @return informationIds お知らせ情報IDのリスト
	 */
	public List<String> updateInformationForRead(String communityUserId);
	/**
	 * 指定したお知らせIDを既読に更新します。
	 * @param informationIds
	 */
	public void updateInformationForRead(List<InformationDO> informations);
	
	public List<InformationDO> findInformationByType(String communityUserId, InformationType type);

	public void deleteInformation(String informationId);
}
