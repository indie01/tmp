/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.impl.xi;

import java.text.DecimalFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.kickmogu.yodobashi.community.common.utils.StringUtil;
import com.kickmogu.yodobashi.community.common.utils.XiUtil;
import com.kickmogu.yodobashi.community.resource.annotation.BackendWebServiceClient;
import com.kickmogu.yodobashi.community.resource.annotation.BackendWebServiceClientAware;
import com.kickmogu.yodobashi.community.resource.config.ResourceConfig;
import com.kickmogu.yodobashi.community.resource.dao.SendMailDao;
import com.kickmogu.yodobashi.community.resource.domain.MailInfoDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.AsyncMessageType;
import com.kickmogu.yodobashi.community.service.annotation.SendMessage;
import com.kickmogu.yodobashi.community.service.annotation.SendMessage.Timing;
import com.yodobashi.esa.customer.sendhtmlmail.SendHTMLMail;
import com.yodobashi.esa.customer.sendhtmlmail.SendHTMLMailReq;
import com.yodobashi.esa.customer.sendhtmlmail.SendHTMLMailReq.HtmlBodyList;
import com.yodobashi.esa.customer.sendhtmlmail.SendHTMLMailReq.SubjectList;
import com.yodobashi.esa.customer.sendhtmlmail.SendHTMLMailReq.TextBodyList;
import com.yodobashi.esa.customer.sendhtmlmail.SendHTMLMailResponse;
import com.yodobashi.esa.customer.structure.COMMONINPUT;


/**
 * メール送信用 DAO の XI 実装です。
 * @author kamiike
 *
 */
@Service @Qualifier("xi") @BackendWebServiceClientAware
public class XiSendMailDaoImpl implements SendMailDao {

	/**
	 * このクラスに関するログを出力するためのインスタンスです。
	 */
	private static final Logger LOG = LoggerFactory.getLogger(XiSendMailDaoImpl.class);

	/**
	 * メールフォーマットです。
	 * HTMLメール=50ですが、CC側でモバイルアドレスしかメールアドレスを保持していない
	 * 顧客には「70」に設定し直し、テキストメールを送信します。
	 */
	public static final String MAIL_FORMAT = "50";

	/**
	 * リソースコンフィグです。
	 */
	@Autowired
	private ResourceConfig resourceConfig;

	/**
	 * メール送信です。
	 */
	@BackendWebServiceClient(
		endPointUrlPropertyKey="endpoint.sendHTMLMail",
		usernamePropertyKey="xi.auth.user",
		passwordPropertyKey="xi.auth.passwd"
	)
	private SendHTMLMail sendHtmlMailClient;

	/**
	 * 送信メールを登録します。
	 * @param mailInfo メール情報
	 */
	@Override
	@SendMessage(timing=Timing.ASYNC_AFTER_PROCESS,asyncMessageType=AsyncMessageType.MAIL)
	public void sendMail(MailInfoDO mailInfo) {
		if (LOG.isInfoEnabled()) {
			LOG.info("start sendMail. communityUserId=" + mailInfo.getCommunityUser().getCommunityUserId()
					+ ", communityId=" +  mailInfo.getCommunityUser().getCommunityId()
					+ ", mailType=" + mailInfo.getMailType());
		}
		SendHTMLMailReq request = new SendHTMLMailReq();
		request.setCOMMONINPUT(new COMMONINPUT());
		request.setMailFormat(MAIL_FORMAT);
		request.setMailType(mailInfo.getMailType().getCode());
		request.setOuterCustomerId(mailInfo.getCommunityUser().getCommunityId());
		request.setOuterCustomerType(resourceConfig.communityOuterCustomerType);
		XiUtil.fillCommonInput(SendHTMLMailReq.class,
				request.getCOMMONINPUT());
		String[] titles = StringUtil.storageStringArray(mailInfo.getTitle());
		DecimalFormat formatter = new DecimalFormat("0000");
		for (int i = 0; i < titles.length; i++) {
			SubjectList value = new SubjectList();
			value.setNo(formatter.format(i));
			value.setText(titles[i]);
			request.getSubjectList().add(value);
		}
		String[] texts = StringUtil.storageStringArray(mailInfo.getTextBody());
		for (int i = 0; i < texts.length; i++) {
			TextBodyList value = new TextBodyList();
			value.setNo(formatter.format(i));
			value.setText(texts[i]);
			request.getTextBodyList().add(value);
		}
		String[] htmls = StringUtil.storageStringArray(mailInfo.getHtmlBody());
		for (int i = 0; i < htmls.length; i++) {
			HtmlBodyList value = new HtmlBodyList();
			value.setNo(formatter.format(i));
			value.setText(htmls[i]);
			request.getHtmlBodyList().add(value);
		}
		SendHTMLMailResponse response = sendHtmlMailClient.sendHTMLMail(request);

		XiUtil.checkResponse(response.getCOMMONRETURN());
	}
}
