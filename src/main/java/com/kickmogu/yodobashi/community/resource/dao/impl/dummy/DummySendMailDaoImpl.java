/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.impl.dummy;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.kickmogu.yodobashi.community.resource.dao.SendMailDao;
import com.kickmogu.yodobashi.community.resource.domain.MailInfoDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.AsyncMessageType;
import com.kickmogu.yodobashi.community.service.annotation.SendMessage;
import com.kickmogu.yodobashi.community.service.annotation.SendMessage.Timing;


/**
 * メール送信用 DAO のダミー実装です。
 * @author kamiike
 *
 */
@Service @Qualifier("dummy")
public class DummySendMailDaoImpl implements SendMailDao {

	
	private static final Logger LOG = LoggerFactory.getLogger(DummySendMailDaoImpl.class);
	/**
	 * メール情報です。
	 */
	private static List<MailInfoDO> mailInfos = new ArrayList<MailInfoDO>();

	/**
	 * 送信メールを登録します。
	 * @param mailInfo メール情報
	 */
	@Override
	@SendMessage(timing=Timing.ASYNC_AFTER_PROCESS,asyncMessageType=AsyncMessageType.MAIL)
	public void sendMail(MailInfoDO mailInfo) {
		
		LOG.info(mailInfo.toString());
		synchronized (DummySendMailDaoImpl.class) {
			mailInfos.add(mailInfo);
			System.out.println(">>>>>>>>>>>>>>>>>size:" + mailInfos.size());
		}
	}

	/**
	 * 溜まったメールを取り出して、削除します。
	 * @return 溜まったメールリスト
	 */
	public static List<MailInfoDO> popMailInfoAndDelete() {
		synchronized (DummySendMailDaoImpl.class) {
			return mailInfos;
		}
	}
}
