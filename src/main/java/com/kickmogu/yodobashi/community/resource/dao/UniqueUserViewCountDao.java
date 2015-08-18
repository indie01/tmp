/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao;

import com.kickmogu.yodobashi.community.resource.domain.constants.UniqueUserViewCountType;

/**
 * ユニークユーザー閲覧数の DAO です。
 * @author kamiike
 *
 */
public interface UniqueUserViewCountDao {

	/**
	 * 指定したコンテンツのユニークユーザー閲覧数を返します。
	 * @param contentsId コンテンツID
	 * @param type コンテンツタイプ
	 * @param readLimit 一度に読み込む件数
	 * @return ユニークユーザー閲覧数
	 */
	public long loadViewCountByContentsId(String contentsId,
			UniqueUserViewCountType type, int readLimit);
}
