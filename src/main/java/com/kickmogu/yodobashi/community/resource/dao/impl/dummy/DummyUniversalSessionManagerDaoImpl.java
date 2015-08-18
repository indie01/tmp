package com.kickmogu.yodobashi.community.resource.dao.impl.dummy;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.kickmogu.yodobashi.community.resource.dao.UniversalSessionManagerDao;

@Service @Qualifier("dummy")
public class DummyUniversalSessionManagerDaoImpl implements UniversalSessionManagerDao {

	@Override
	public void deleteUniversalSession(String universalSessionID) {
		System.out.println("HOGE=========================");
	}

	/**
	 * 指定したユニバーサルセッションに保存した外部顧客IDを取得します。
	 * @param universalSessionID ユニバーサルセッションID
	 * @return 外部顧客ID
	 */
	@Override
	public String loadOuterCustomerId(String universalSessionID) {
		try {
			return URLDecoder.decode(universalSessionID, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return universalSessionID;
		}
	}
}
