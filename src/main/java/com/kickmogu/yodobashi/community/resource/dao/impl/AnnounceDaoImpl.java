/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.core.aop.TimestampHolder;
import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.yodobashi.community.resource.dao.AnnounceDao;
import com.kickmogu.yodobashi.community.resource.dao.util.IdUtil;
import com.kickmogu.yodobashi.community.resource.domain.AnnounceDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.AnnounceType;

/**
 * アナウンス DAO の実装クラスです。
 * @author kamiike
 *
 */
@Service
public class AnnounceDaoImpl implements AnnounceDao {

	/**
	 * HBaseアクセサです。
	 */
	@Autowired  @Qualifier("default")
	private HBaseOperations hBaseOperations;

	/**
	 * タイムスタンプホルダーです。
	 */
	@Autowired
	private TimestampHolder timestampHolder;

	/**
	 * 指定したコミュニティユーザーID、タイプのアナウンスを取得します。
	 * @param communityUserId コミュニティユーザーID
	 * @param type タイプ
	 * @return アナウンス
	 */
	@Override
	public AnnounceDO load(String communityUserId, AnnounceType type) {
		AnnounceDO announce = hBaseOperations.load(AnnounceDO.class,
				createAccountId(communityUserId, type));
		if (announce != null && !announce.isDeleted()) {
			return announce;
		} else {
			return null;
		}
	}

	/**
	 * アナウンス情報を新規に登録します。
	 * @param announce アナウンス
	 */
	@Override
	public void create(AnnounceDO announce) {
		announce.setAnnounceId(createAccountId(announce.getCommunityUserId(), announce.getType()));
		announce.setRegisterDateTime(timestampHolder.getTimestamp());
		announce.setModifyDateTime(timestampHolder.getTimestamp());
		hBaseOperations.save(announce);
	}

	/**
	 * アナウンス情報を削除します。
	 * @param announceId アナウンスID
	 * @param type タイプ
	 */
	@Override
	public void delete(String communityUserId, AnnounceType type) {
		AnnounceDO announce = new AnnounceDO();
		announce.setAnnounceId(createAccountId(communityUserId, type));
		announce.setDeleteFlag(true);
		announce.setDeleteDate(timestampHolder.getTimestamp());
		announce.setModifyDateTime(timestampHolder.getTimestamp());
		hBaseOperations.save(announce, Path.includeProp("deleteFlag,deleteDate,modifyDateTime"));
	}

	/**
	 * アナウンスIDを生成して返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param type タイプ
	 * @return アナウンスID
	 */
	private String createAccountId(String communityUserId, AnnounceType type) {
		return IdUtil.createIdByConcatIds(communityUserId, type.getCode());
	}

}
